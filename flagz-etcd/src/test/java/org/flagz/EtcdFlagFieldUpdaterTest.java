package org.flagz;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import mousio.client.retry.RetryOnce;
import mousio.client.retry.RetryPolicy;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.requests.EtcdKeyGetRequest;
import mousio.etcd4j.responses.EtcdKeyAction;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EtcdFlagFieldUpdaterTest {
  private static final Logger LOG = LoggerFactory.getLogger(EtcdFlagFieldUpdater.class);

  private static final String ETCD_SERVER = "http://localhost:2379";
  private static final String FLAGZ_PATH = "/FLAGZ_TESTING_DIR/";
  private static final RetryPolicy ETCD_RETRY = new RetryOnce(50);
  private static final ReentrantLock singleTestLock = new ReentrantLock();
  
  private static EtcdClient client;

  @SuppressWarnings("unchecked")
  private final Consumer<Integer> intListener = mock(Consumer.class);
  @SuppressWarnings("unchecked")
  private final Consumer<String> stringListener = mock(Consumer.class);

  @FlagInfo(name = "etcd_test_int", help = "some int")
  public final Flag<Integer> flagInt = Flagz.valueOf(400)
      .withValidator(Validators.greaterThan(100))
      .withListener(intListener);

  @FlagInfo(name = "etcd_test_string", help = "some string")
  public final Flag<String> flagString = Flagz.valueOf("unoverwritten")
      .withListener(stringListener);

  @FlagInfo(name = "etcd_test_map_int", help = "some map")
  public final Flag<Map<String, Integer>> flagMap = Flagz.valueOf(ImmutableMap.of("foo", 100, "boo", 200));

  private EtcdFlagFieldUpdater etcdUpdater;

  @BeforeClass
  public static void connectEtcd() {
    try {
      client = new EtcdClient(URI.create(ETCD_SERVER));
      client.setRetryHandler(ETCD_RETRY);
      Long etcdIndex = client.getAll().send().get().etcdIndex;
      LOG.info("Connected to Etcd version={} at EtcdIndex({}).", client.version(), etcdIndex);
    } catch (Exception e) {
      throw new RuntimeException("Etc.d must be running on localhost for these tests.", e);
    }
  }

  @AfterClass
  public static void disconnectEtcd() throws IOException {
    client.close();
  }

  @Before
  public void setUp() throws Exception {
//    singleTestLock.lock();
    client.putDir(FLAGZ_PATH).send().get();
    String[] argz = {
        "--etcd_test_string=cmdline_overwrite",
        String.format("--flagz_etcd_directory=%s", FLAGZ_PATH)
    };
    FlagFieldRegistry fieldRegistry = Flagz.parse(argz, ImmutableList.<String>of(), ImmutableSet.of(this));
    etcdUpdater = new EtcdFlagFieldUpdater(fieldRegistry,
                                           ImmutableList.of(ETCD_SERVER),
                                           ETCD_RETRY, Executors.newSingleThreadExecutor());

  }

  @After
  public void tearDown() throws Exception {
    etcdUpdater.stop();
    client.deleteDir(FLAGZ_PATH).recursive().send().get();
//    singleTestLock.unlock();
  }

  @Test
  public void testNothingInEtcd() throws Exception {
    etcdUpdater.init();
    assertThat(flagInt.get(), is(flagInt.defaultValue()));
    assertThat(flagString.get(), is("cmdline_overwrite"));
    assertThat(flagMap.get(), is(flagMap.defaultValue()));
  }

  @Test
  public void testInitFromEtcd() throws Exception {
    client.put(FLAGZ_PATH + "etcd_test_int", "101").send().get();
    client.put(FLAGZ_PATH + "etcd_test_string", "etcdinit_overwritten").send().get();
    etcdUpdater.init();
    assertThat(flagInt.get(), is(101));
    assertThat(flagString.get(), is("etcdinit_overwritten"));
    assertThat(flagMap.get(), is(flagMap.defaultValue()));
  }

  @Test
  public void testInitFromEtcd_CallsListener() throws Exception {
    client.put(FLAGZ_PATH + "etcd_test_int", "333").send().get();
    etcdUpdater.init();
    assertThat(flagInt.get(), is(333));
    verify(intListener).accept(eq(333));
  }

  @Test(expected = FlagException.UnknownFlag.class)
  public void testInitFromEtcd_UnknownFlag_ThrowsException() throws Exception {
    client.put(FLAGZ_PATH + "etcd_test_some_flag", "123").send().get();
    etcdUpdater.init();
  }

  @Test(expected = FlagException.IllegalFormat.class)
  public void testInitFromEtcd_IllegalFormat_ThrowsException() throws Exception {
    client.put(FLAGZ_PATH + "etcd_test_map_int", "random_stuff").send().get();
    etcdUpdater.init();
  }

  @Test(expected = FlagException.BadValue.class)
  public void testInitFromEtcd_BadValue_ThrowsException() throws Exception {
    // 99 is below the >100 validator.
    client.put(FLAGZ_PATH + "etcd_test_int", "99").send().get();
    etcdUpdater.init();
  }

  @Test
  public void testUpdateWatch_CallsListener() throws Exception {
    etcdUpdater.init();
    etcdUpdater.watchForUpdates();
    client.put(FLAGZ_PATH + "etcd_test_int", "333").send().get();
    verify(intListener, timeout(100)).accept(eq(333));
    assertThat(flagInt.get(), is(333));
  }

  @Test
  public void testUpdateWatch_MultipleValues() throws Exception {
    etcdUpdater.init();
    etcdUpdater.watchForUpdates();
    client.put(FLAGZ_PATH + "etcd_test_int", "999").send().get();
    client.put(FLAGZ_PATH + "etcd_test_string", "new_dynamic").send().get();

    verify(intListener, timeout(100)).accept(eq(999));
    verify(stringListener, timeout(100)).accept(eq("new_dynamic"));
    assertThat(flagInt.get(), is(999));
    assertThat(flagString.get(), is("new_dynamic"));
  }

  @Test
  public void testUpdateWatch_BadNewValue_IsRolledBack() throws Exception {
    etcdUpdater.init();
    etcdUpdater.watchForUpdates();
    EtcdKeysResponse put = client.put(FLAGZ_PATH + "etcd_test_int", "99").send().get();
    EtcdKeysResponse watch = client.get(FLAGZ_PATH + "etcd_test_int")
        .waitForChange(put.node.modifiedIndex + 1).send().get();
    // NOTE: This is crucial, because it means we're using an atomic delete, deleting only what we've read.
    assertThat(watch.action, is(EtcdKeyAction.compareAndDelete));
    assertThat(watch.node.value, nullValue());
    verify(intListener, never()).accept(eq(99));
  }

  @Test
  public void testUpdateWatch_BadExistingValue_IsRolledBack() throws Exception {
    etcdUpdater.init();
    etcdUpdater.watchForUpdates();
    EtcdKeysResponse firstPut = client.put(FLAGZ_PATH + "etcd_test_int", "1337").send().get();
    EtcdKeysResponse secondPut = client.put(FLAGZ_PATH + "etcd_test_int", "99").send().get();
    EtcdKeysResponse watch = client.get(FLAGZ_PATH + "etcd_test_int")
        .waitForChange(secondPut.node.modifiedIndex + 1).send().get();
    // NOTE: This is crucial, because it means we're using an atomic set, overwriting only what we've read.
    assertThat(watch.action, is(EtcdKeyAction.compareAndSwap));
    assertThat(watch.node.value, is("1337"));
    // We will have this value updates twice, because of the rollback.
    verify(intListener, atLeast(1)).accept(eq(1337));
    verify(intListener, never()).accept(eq(99));
  }
}
