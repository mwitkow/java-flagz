package org.flagz;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import mousio.client.retry.RetryPolicy;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdKeysResponse.EtcdNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Populates and updates {@link Flag}s from a given path in Etc.d.
 *
 * The class assumes that all flagz exist in the same Etc.d directory, and their keys are the flagz
 * full names (not alt names). The values of each separate key is treated as the value of the flag.
 *
 * For example:
 *
 * ```
 * /v2/keys/some/path/my_int_flag_1 -> "700"
 * /v2/keys/some/path/my_string_flag_2 -> "some random string"
 * ```
 */
public class EtcdFlagFieldUpdater {

  private static final Logger LOG = LoggerFactory.getLogger(EtcdFlagFieldUpdater.class);

  @FlagInfo(name = "flagz_etcd_enabled", help = "Whether to use EtcdFlagFieldUpdater at all.")
  private static final Flag<Boolean> enabledFlag = Flagz.valueOf(false);

  @FlagInfo(name = "flagz_etcd_directory",
            help = "Directory containing flags. E.g. for /v2/keys/foo/bar/flag the "
                + "value should be /foo/bar/.")
  private static final Flag<String> directoryFlag = Flagz.valueOf("");

  @FlagInfo(name = "flagz_etcd_server_uris",
            help = "List of comma-delimited URIs for etc.d servers.")
  private static final Flag<List<String>> urisFlag = Flagz
      .valueOf(ImmutableList.of("https://127.0.0.1:4001"));

  @FlagInfo(name = "flagz_etcd_retry_policy",
            help = "Name of EtcdRetryPolicy to use for all accesses")
  private static final Flag<EtcdRetryPolicy> retryPolicyFlag =
      Flagz.valueOf(EtcdRetryPolicy.EXPONENTIAL_BACKOFF_5MS_MAX_10_TIMES);

  @FlagInfo(name = "flagz_etcd_reelection_backoff",
            help = "Time to backoff for during Etcd reelection.")
  private static final Flag<Long> reelectionBackoffMs = Flagz.valueOf(200L);

  /**
   * Public method to check if the flag flagz_etcd_enabled is true.
   */
  public static boolean isEnabled() {
    return enabledFlag.get();
  }

  private static final int ETCD_PRECONDITION_FAILED_CODE = 101;
  private static final int ETCD_LEADER_ELECT_CODE = 301;
  private static final int ETCD_WATCHER_CLEARED_CODE = 400;
  private static final int ETCD_EVENT_INDEX_CLEARED_CODE = 401;


  private final List<URI> uris;
  private final RetryPolicy retryPolicy;
  private final FlagFieldRegistry registry;
  private final ExecutorService executorService;
  private String directoryPrefix;
  private EtcdClient client;
  private volatile boolean running = false;
  private long lastKnownFlagzModificationIndex = 0;

  public EtcdFlagFieldUpdater(FlagFieldRegistry registry) {
    this(
        registry, urisFlag.get(), retryPolicyFlag.get().get(), Executors.newSingleThreadExecutor());
  }

  EtcdFlagFieldUpdater(FlagFieldRegistry registry, List<String> uris, RetryPolicy retryPolicy,
                       ExecutorService executorService) {
    this.registry = Preconditions.checkNotNull(registry);
    this.uris = Preconditions.checkNotNull(uris).stream().map(URI::create)
        .collect(Collectors.toList());
    this.retryPolicy = Preconditions.checkNotNull(retryPolicy);
    this.executorService = Preconditions.checkNotNull(executorService);
  }

  public void init() {
    init(Preconditions.checkNotNull(Strings.emptyToNull(directoryFlag.get())));
  }

  /** Init performs the initial read of values from etcd. */
  public void init(String flagzDirectory) throws FlagException, EtcdFlagFieldUpdaterException {
    this.directoryPrefix = MoreObjects.firstNonNull(flagzDirectory, directoryFlag.get());
    client = new EtcdClient(uris.toArray(new URI[uris.size()]));
    client.setRetryHandler(retryPolicy);
    initialSetAllFlagz();
  }

  /** Kicks off a separate thread that will keep flag values in sync with etcd. */
  public void watchForUpdates() {
    Preconditions.checkState(client != null, "You need to call init() before watchForUpdates().");
    running = true;
    executorService.submit(
        () -> {
          while (running) {
            try {
              watchAndUpdateSingleFlagz();
            } catch (Exception exception) {
              LOG.warn("Unexpected exception. Continuing with Flagz watch thread.", exception);
            }
          }
        });
  }

  /** Stops the watching of etcd values. */
  public void stop() {
    running = false;
    // TODO(michal): Add handling of pending requests, as EtcdClient doesn't do that =(
    try {
      client.close();
    } catch (IOException exception) {
      LOG.error("Exception while closing EtcdFlagFieldUpdater.", exception);
    }
  }

  private void initialSetAllFlagz() {
    for (EtcdNode n : fetchAllFlagzNodes()) {
      setFlagFromFlagzNode(n);
    }
  }

  private void watchAndUpdateSingleFlagz() {
    try {
      EtcdKeysResponse response = watchForUpdatedFlagzNode();
      if (response.node.value == null) {
        // A 'delete' or some other non-continuous action.
        return;
      }
      String flagName = nodeKeyToFlagName(response.node.key);
      try {
        setFlagFromFlagzNode(response.node);
      } catch (FlagException.UnknownFlag exception) {
        LOG.warn(
            "Flag({}) is not known, but set at EtcdIndex({}). Consider manual deletion. Ignoring.",
            flagName, response.node.modifiedIndex);
      } catch (FlagException exception) {
        LOG.warn(
            "Flag({}) update to value='{}' at EtcdIndex({}) failed due to error='{}'. "
                + "Will try to roll back Etc.d value.",
            flagName, response.node.modifiedIndex, response.node.value, exception.getMessage());
        rollbackFlagzNode(response);
      }
    } catch (EtcdFlagFieldUpdaterException.EtcdFetchingFailed exception) {
      LOG.error("Couldn't fetch updates for Flagz due to connectivity issues..", exception);
    } catch (EtcdFlagFieldUpdaterException.EtcdRollbackFailed exception) {
      LOG.warn(
          String
              .format("Flag({}) rollback failed due to connectivity issues.", exception.flagName()),
          exception);
    }
  }

  private List<EtcdNode> fetchAllFlagzNodes() {
    try {
      EtcdResponsePromise<EtcdKeysResponse> promise = client
          .get(this.directoryPrefix)
          .dir()
          .send();
      EtcdKeysResponse response = promise.get();
      // NOTE: We use etcdIndex here, because we know we got latest data up to this point.
      lastKnownFlagzModificationIndex = response.etcdIndex;
      return MoreObjects.firstNonNull(response.node.nodes, ImmutableList.<EtcdNode>of());
    } catch (IOException | EtcdException
        | TimeoutException | EtcdAuthenticationException exception) {
      throw new EtcdFlagFieldUpdaterException.EtcdFetchingFailed(exception);
    }
  }

  private EtcdKeysResponse watchForUpdatedFlagzNode() {
    try {
      EtcdResponsePromise<EtcdKeysResponse> promise = client
          .get(this.directoryPrefix)
          .dir()
          .recursive()
          .waitForChange(lastKnownFlagzModificationIndex + 1)
          .send();
      EtcdKeysResponse response = promise.get();
      // NOTE: We are not using, because there might be more than one write that had happened.
      lastKnownFlagzModificationIndex = response.node.modifiedIndex;
      return response;
    } catch (EtcdException exception) {
      return handleEtcdWatchErrors(exception);
    } catch (IOException | TimeoutException | EtcdAuthenticationException exception) {
      throw new EtcdFlagFieldUpdaterException.EtcdFetchingFailed(exception);
    }
  }

  /**
   * Handles watch issues with watching. See Etcd docs:
   *
   * @see <a href="https://github.com/coreos/etcd/blob/master/Documentation/api.md">Docs</a>
   */
  private EtcdKeysResponse handleEtcdWatchErrors(EtcdException exception) {
    //
    if (exception.errorCode == ETCD_EVENT_INDEX_CLEARED_CODE) {
      // If our watching failed due to index, re-read everything because we might have missed
      // something. The lastKnownFlagzModificationIndex will be reset in fetchAllFlagzNodes.
      initialSetAllFlagz();
      return null;
    } else if (exception.errorCode == ETCD_WATCHER_CLEARED_CODE) {
      // This means that etcd is recovering from a problem.
      try {
        Thread.sleep(reelectionBackoffMs.get());
      } catch (InterruptedException e1) {
        // ignore
      }
      return null;
    } else {
      throw new EtcdFlagFieldUpdaterException.EtcdFetchingFailed(exception);
    }
  }

  private void setFlagFromFlagzNode(EtcdNode node) throws FlagException {
    String flagName = nodeKeyToFlagName(node.key);
    registry.setField(flagName, node.value);
    LOG.info(
        "Flag({}) updated to value='{}' from EtcdIndex({}).", flagName, node.value,
        node.modifiedIndex);
  }

  private void rollbackFlagzNode(EtcdKeysResponse response) {
    // NOTE: the prevIndex here is crucial, we only want to rollback once (from one server) and
    // don't want to overwrite by mistake something that was written since we read it.
    String flagName = nodeKeyToFlagName(response.node.key);
    try {
      if (response.prevNode == null) { // It didn't exist before.
        // TODO(michal): Remove cast once upstream correctly returns EtcdKeyDeleteRequest.
        (client
            .delete(response.node.key))
            .prevIndex(response.node.modifiedIndex)
            .send()
            .get();
        LOG.warn(
            "Flag({}) successfully removed due to rollback with EtcdIndex({}).",
            flagName, response.node.modifiedIndex);

      } else {
        client
            .put(response.node.key, response.prevNode.value)
            .prevIndex(response.node.modifiedIndex)
            .send()
            .get();
        LOG.warn(
            "Flag({}) successfully rolled back to value='{}' with EtcdIndex({}).",
            flagName, response.prevNode.value, response.node.modifiedIndex);
      }

    } catch (EtcdException exception) {
      if (exception.errorCode == ETCD_PRECONDITION_FAILED_CODE) {
        LOG.info(
            "Flag({}) rollback wouldn't be atomic. Probably done by another server.", flagName);
      } else {
        throw new EtcdFlagFieldUpdaterException.EtcdRollbackFailed(exception, flagName);
      }
    } catch (TimeoutException | IOException | EtcdAuthenticationException exception) {
      throw new EtcdFlagFieldUpdaterException.EtcdRollbackFailed(exception, flagName);
    }
  }

  private String nodeKeyToFlagName(String key) {
    Preconditions.checkArgument(
        key.startsWith(directoryPrefix),
        String.format("The key {} doesn't start with {}", key, directoryPrefix));
    int offset = directoryPrefix.length();
    if (!directoryPrefix.endsWith("/")) {
      offset += 1;
    }
    return key.substring(offset);
  }
}
