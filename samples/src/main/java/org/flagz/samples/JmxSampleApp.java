package org.flagz.samples;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.flagz.Flag;
import org.flagz.FlagFieldRegistry;
import org.flagz.FlagInfo;
import org.flagz.Flagz;
import org.flagz.JmxFlagFieldRegistrar;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;

public class JmxSampleApp {

  @FlagInfo(name = "test_some_list", help = "Some list")
  private static final Flag<List<String>> someStrings = Flagz
      .valueOf(ImmutableList.of("foo", "boo", "zoo"));

  @FlagInfo(name = "test_some_int", help = "Some int")
  private static final Flag<Integer> someInt = Flagz.valueOf(1337);

  @FlagInfo(name = "test_some_map", help = "Some int")
  private static final Flag<Map<String, Integer>> someMap = Flagz
      .valueOf(ImmutableMap.of("car", 200, "bar", 300));

  @FlagInfo(name = "test_running", help = "Controls start and stop.")
  private static final Flag<Boolean> running = Flagz.valueOf(true);


  /** Example app that listens for JMX params. */
  public static void main(String[] args) throws Exception {
    FlagFieldRegistry registry = Flagz.parse(args);
    JmxFlagFieldRegistrar jmx = new JmxFlagFieldRegistrar(registry);
    jmx.register(ManagementFactory.getPlatformMBeanServer());
    while (running.get()) {
      System.out.println("test_some_int: " + someInt.get().toString());
      System.out.println("test_some_list: " + someStrings.get());
      System.out.println("test_some_map: " + someMap.get());
      System.out.println();
      Thread.sleep(1000);
    }
    System.out.println("Bye!");
  }
}
