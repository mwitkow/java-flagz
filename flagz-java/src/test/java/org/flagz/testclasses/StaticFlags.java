package org.flagz.testclasses;

import com.google.common.collect.ImmutableList;
import org.flagz.Flag;
import org.flagz.FlagInfo;
import org.flagz.Flagz;

import java.util.List;

/**
 * Set of flags that are useful for testing of {@link Flagz}.
 */
public class StaticFlags {

  @FlagInfo(help = "for testing of resolution with no name argument")
  public static final Flag<Integer> testFieldNameFlag = Flagz.valueOf(0);

  @FlagInfo(name = "test_alt_name", help = "for testing of resolution with alt name", altName = "t_alt")
  public static final Flag<String> testAltNameFlag = Flagz.valueOf("my_alt");

  @FlagInfo(name = "test_flag_full_name", help = "for testing of resolution with full name")
  public static final Flag<Long> testFullNameFlag = Flagz.valueOf(0L);

  @FlagInfo(name = "test_flag_private_static", help = "for testing of resolution of private static fields")
  private static final Flag<Long> testPrivateStaticFlag = Flagz.valueOf(0L);

  @FlagInfo(name = "test_flag_public_static", help = "for testing of resolution of public static fields")
  public static final Flag<Long> testPublicStaticFlag = Flagz.valueOf(0L);

  @FlagInfo(name = "test_flag_non_static", help = "for validating that we won't see non static fields")
  private final Flag<Long> testInvisibleNonStaticFlag = Flagz.valueOf(0L);


  public static Flag<Long> testPrivateStaticFlagAccessor() {
    return testPrivateStaticFlag;
  }

  @SuppressWarnings("unchecked")
  public static void resetToDefaults() {
    List<Flag<?>> list = ImmutableList.of(
        testFieldNameFlag, testAltNameFlag, testFullNameFlag, testPrivateStaticFlag, testPublicStaticFlag);
    for (Flag f : list) {
      f.accept(f.defaultValue());
    }
  }
}
