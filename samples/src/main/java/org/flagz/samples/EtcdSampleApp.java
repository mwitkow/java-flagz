package org.flagz.samples;

import com.google.common.collect.ImmutableMap;
import org.flagz.EtcdFlagFieldUpdater;
import org.flagz.Flag;
import org.flagz.FlagFieldRegistry;
import org.flagz.FlagInfo;
import org.flagz.Flagz;

import java.util.Map;

/**
 * Sample App that demonstrates use of {@link EtcdFlagFieldUpdater}.
 *
 * To use, start:
 * {@code
 * java -classpath some.jar org.flagz.samples.EtcdSampleApp \
 * --flagz_etcd_enabled=true \
 * --flagz_etcd_directory=/myapp/flagz
 * }
 */
public class EtcdSampleApp {

  @FlagInfo(name = "test_etcd_int", help = "Some Etcd int.")
  private static final Flag<Integer> someInt = Flagz.valueOf(1337);

  @FlagInfo(name = "test_etcd_map", help = "Some Etcd Map.")
  private static final Flag<Map<String, Integer>> someMap = Flagz
      .valueOf(ImmutableMap.of("car", 200, "bar", 300));

  /** Sample app that listens to etcd at port 4001. */
  public static void main(String[] args) throws Exception {
    FlagFieldRegistry registry = Flagz.parse(args);
    EtcdFlagFieldUpdater updater = new EtcdFlagFieldUpdater(registry);
    updater.init();
    updater.watchForUpdates();
    try {
      while (true) {
        System.out.println("test_etcd_int: " + someInt.get().toString());
        System.out.println("test_etcd_map: " + someMap.get());
        System.out.println();
        Thread.sleep(3000);
      }
    } finally {
      updater.stop();
    }
  }
}
