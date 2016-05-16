package org.flagz;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for validator and listener functionality of {@link BaseFlag}.
 */
public class UnusedFlagTest {

  public static final List<String> EMPTY_PACKAGE_PREFIXES = ImmutableList.of();
  private UsedFlags usedFlags = new UsedFlags();
  private UnusedFlags unusedFlags = new UnusedFlags();
  private Set<Object> onlyUsed = ImmutableSet.of(usedFlags);
  private Set<Object> onlyUnused = ImmutableSet.of(unusedFlags);
  private Set<Object> mixed = ImmutableSet.of(usedFlags, unusedFlags);

  @Test
  public void testNoParsingAllFlagsAvailable() {
    String[] args = {};
    FlagFieldRegistry registry = Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, mixed);
    Set<FlagField<?>> unused = registry.unusedFlags(Utils.parseArgsToFieldMap(args));
    assertThat(unused.isEmpty(), is(true));
  }

  @Test
  public void testNoParsingUnusedFlagsAvailable() {
    String[] args = {};
    FlagFieldRegistry registry = Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, onlyUnused);
    Set<FlagField<?>> unused = registry.unusedFlags(Utils.parseArgsToFieldMap(args));
    assertThat(unused.isEmpty(), is(true));
  }

  @Test
  public void testNoParsingUsedFlagsAvailable() {
    String[] args = {};
    FlagFieldRegistry registry = Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, onlyUsed);
    Set<FlagField<?>> unused = registry.unusedFlags(Utils.parseArgsToFieldMap(args));
    assertThat(unused.isEmpty(), is(true));
  }

  @Test
  public void testParsingOnlyUsedFlags() {
    String[] args = {"--test_flag_int=101010", "--test_flag_string=hi", "--test_flag_float=200.0"};
    FlagFieldRegistry registry = Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, onlyUsed);
    Set<FlagField<?>> unused = registry.unusedFlags(Utils.parseArgsToFieldMap(args));
    assertThat(unused.isEmpty(), is(true));
  }

  @Test
  public void testParsingOnlyUnusedFlags() {
    String[] args = {"--test_flag_other_int=101010", "--test_flag_other_string=hi", "--test_flag_other_float=200.0"};
    FlagFieldRegistry registry = Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, onlyUnused);
    Set<FlagField<?>> unused = registry.unusedFlags(Utils.parseArgsToFieldMap(args));
    assertThat(unused.size(), is(3));
  }

  @Test
  public void testParsingMixedFlags() {
    String[] args = {"--test_flag_int=101010", "--test_flag_string=hi", "--test_flag_float=200.0",
        "--test_flag_other_int=101010", "--test_flag_other_string=hi", "--test_flag_other_float=200.0"};
    FlagFieldRegistry registry = Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, mixed);
    Set<FlagField<?>> unused = registry.unusedFlags(Utils.parseArgsToFieldMap(args));
    assertThat(unused.size(), is(3));
  }

  @Test
  public void testCorrectStringProduced() {
    String[] args = {"--test_flag_int=101010", "--test_flag_string=hi", "--test_flag_float=200.0",
        "--test_flag_other_int=101010", "--test_flag_other_string=hi", "--test_flag_other_float=200.0"};
    String[] expectedMessage =
        {"The following flag is unused - it will have no effect on the system: --test_flag_other_int [-tfoi]",
            "The following flag is unused - it will have no effect on the system: --test_flag_other_string",
            "The following flag is unused - it will have no effect on the system: --test_flag_other_float [-tfof]"};
    FlagFieldRegistry registry = Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, mixed);
    Set<FlagField<?>> unused = registry.unusedFlags(Utils.parseArgsToFieldMap(args));
    String[] generatedMessage = FlagFieldRegistry.unusedFlagsMessages(unused);
    Arrays.sort(expectedMessage);
    Arrays.sort(generatedMessage);
    assertThat(generatedMessage, is(expectedMessage));
  }

  static class UsedFlags {
    @FlagInfo(name = "test_flag_int", help = "some int")
    public final Flag<Integer> flagInt = Flagz.valueOf(200);

    @FlagInfo(name = "test_flag_string", help = "some string")
    public final Flag<String> flagStr = Flagz.valueOf("test");

    @FlagInfo(name = "test_flag_float", help = "some float")
    public final Flag<Float> flagFloat = Flagz.valueOf(200.0f);
  }

  static class UnusedFlags {
    @FlagzUnused
    @FlagInfo(name = "test_flag_other_int", altName = "tfoi", help = "some other int")
    public final Flag<Integer> flagOtherInt = Flagz.valueOf(100);

    @FlagzUnused
    @FlagInfo(name = "test_flag_other_string", help = "some other string")
    public final Flag<String> flagOtherStr = Flagz.valueOf("other test");

    @FlagzUnused
    @FlagInfo(name = "test_flag_other_float", altName = "tfof", help = "some other float")
    public final Flag<Float> flagOtherFloat = Flagz.valueOf(100.0f);
  }

}
