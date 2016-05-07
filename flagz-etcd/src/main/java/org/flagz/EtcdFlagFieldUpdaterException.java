package org.flagz;

/**
 * Exception thrown when Etcd cannot be contacted to parse Flagz.
 */
public class EtcdFlagFieldUpdaterException extends RuntimeException {
  EtcdFlagFieldUpdaterException(Throwable cause) {
    super(cause);
  }
  
  public static class EtcdFetchingFailed extends EtcdFlagFieldUpdaterException {
    EtcdFetchingFailed(Throwable cause) {
      super(cause);
    }
  }

  static class EtcdRollbackFailed extends EtcdFlagFieldUpdaterException {
    private String flagName;

    EtcdRollbackFailed(Throwable cause, String flagName) {
      super(cause);
      this.flagName = flagName;
    }

    public String flagName() {
      return flagName;
    }
  }
}
