package org.flagz;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.flagz.testclasses.StaticFlags;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for scanning of {@link Flag} annotated {@link FlagField} through reflection.
 *
 * This uses {@link StaticFlags} class that defines `static` fields. These should be found through normal reflection.
 * The `Container*` objects in this class are used to find non-static fields in `Singleton` objects.
 */
public class FlagFieldScannerTest {

  public static final String[] EMPTY_ARGS = {};
  public static final List<String> EMPTY_PACKAGE_PREFIXES = ImmutableList.of();

  static class ContainerOne {
    @FlagInfo(name = "test_flag_obj_public", help = "")
    public final Flag<Long> testPublicFlag = Flagz.valueOf(1337L);

    @FlagInfo(name = "test_flag_obj_private", help = "")
    private final Flag<Long> testPrivateFlag = Flagz.valueOf(4000L);

    public Flag<Long> testPrivateFlagAccessor() {
      return testPrivateFlag;
    }
  }

  ;

  static class ContainerTwo {
    @FlagInfo(name = "test_flag_obj_public_two", help = "")
    public final Flag<Long> testPublicFlag = Flagz.valueOf(999L);
  }

  ;

  static class ContainerClashingWithStatic {
    @FlagInfo(name = "test_flag_public_static", help = "")
    public final Flag<Long> testStaticClash = Flagz.valueOf(999L);
  }

  ;

  private ContainerOne one = new ContainerOne();
  private ContainerTwo two = new ContainerTwo();
  private ContainerClashingWithStatic clashing = new ContainerClashingWithStatic();
  private Set<Object> goodContainers = ImmutableSet.of(one, two);
  private Set<Object> clashingContainers = ImmutableSet.of(one, two, clashing);


  @Test
  public void testGeneric_FieldNameResolution() {
    String[] args = {"--testFieldNameFlag=101010"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, goodContainers);
    assertThat(StaticFlags.testFieldNameFlag.get(), is(101010));
  }

  @Test
  public void testGeneric_AltNameResolution() {
    String[] args = {"-t_alt=scoobydoo"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, goodContainers);
    assertThat(StaticFlags.testAltNameFlag.get(), is("scoobydoo"));
  }

  @Test
  public void testGeneric_AltNameResolution_WithFullName() {
    String[] args = {"-test_alt_name=scoobydoo"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, goodContainers);
    assertThat(StaticFlags.testAltNameFlag.get(), is("scoobydoo"));
  }

  @Test
  public void testGeneric_FullNameResolution() {
    String[] args = {"--test_flag_full_name=1337"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, goodContainers);
    assertThat(StaticFlags.testFullNameFlag.get(), is(1337L));
  }

  @Test
  public void testStatic_PublicVisible() {
    String[] args = {"--test_flag_public_static=7777"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, goodContainers);
    assertThat(StaticFlags.testPublicStaticFlag.get(), is(7777L));
  }

  @Test
  public void testStatic_PrivateVisible() {
    String[] args = {"--test_flag_private_static=5555"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, goodContainers);
    assertThat(StaticFlags.testPrivateStaticFlagAccessor().get(), is(5555L));
  }

  @Test(expected = FlagException.UnknownFlag.class)
  public void testStatic_NonStaticInvisible() {
    String[] args = {"--test_flag_non_static=5555"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, goodContainers);
  }

  @Test
  public void testSingleton_PublicVisible() {
    String[] args = {"--test_flag_obj_public=7777"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, goodContainers);
    assertThat(one.testPublicFlag.get(), is(7777L));
  }

  @Test
  public void testSingleton_PrivateVisible() {
    String[] args = {"--test_flag_obj_private=44444"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, goodContainers);
    assertThat(one.testPrivateFlagAccessor().get(), is(44444L));
  }

  @Test
  public void testSingleton_ManySingletons() {
    String[] args = {"--test_flag_obj_public=7777", "--test_flag_obj_public_two=9999"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, goodContainers);
    assertThat(one.testPublicFlag.get(), is(7777L));
    assertThat(two.testPublicFlag.get(), is(9999L));
  }

  @Test(expected = FlagException.NameConflict.class)
  public void testNameClashThrowsException() {
    String[] args = {"--test_flag_obj_public=7777", "--test_flag_obj_public_two=9999"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, clashingContainers);
  }

}
