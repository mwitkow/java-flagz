package org.flagz;

import mousio.client.retry.RetryNTimes;
import mousio.client.retry.RetryPolicy;
import mousio.client.retry.RetryWithExponentialBackOff;

import java.util.function.Supplier;

/**
 * Enum-factory for the {@link RetryPolicy} to use for {@link EtcdFlagFieldUpdater} flags.
 */
public enum EtcdRetryPolicy implements Supplier<RetryPolicy> {
  EXPONENTIAL_BACKOFF_5MS_MAX_10_TIMES {
    @Override
    public RetryPolicy get() {
      return new RetryWithExponentialBackOff(5, 10, 5 * 60 * 1000);  // max 5ms * 2**10 = 5sec.
    }
  },
  EXPONENTIAL_BACKOFF_10MS_MAX_20_TIMES {
    @Override
    public RetryPolicy get() {
      return new RetryWithExponentialBackOff(10, 13, 160 * 1000);  // max 10ms * 2*20 = 160 sec
    }
  },
  LINEAR_5MS_MAX_20_TIMES {
    @Override
    public RetryPolicy get() {
      return new RetryNTimes(5, 20);
    }
  }
}
