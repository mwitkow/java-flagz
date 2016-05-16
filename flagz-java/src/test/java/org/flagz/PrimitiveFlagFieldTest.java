package org.flagz;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.flagz.testclasses.SomeEnum;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests of flags of primitive types.
 *
 * **Note:** All flags are declared in-object, and the `this` object is passed to Flagz to find these fields. We are not
 * testing the ability of Flagz to resolve fields, just value parsing.
 */
public class PrimitiveFlagFieldTest {

  public static final String[] EMPTY_ARGS = {};
  public static final List<String> EMPTY_PACKAGE_PREFIXES = ImmutableList.of();

  @FlagInfo(name = "test_int_flag", help = "")
  final Flag<Integer> integerFlag = Flagz.valueOf(1337);

  @FlagInfo(name = "test_long_flag", help = "")
  final Flag<Long> longFlag = Flagz.valueOf(13371337L);

  @FlagInfo(name = "test_double_flag", help = "")
  final Flag<Double> doubleFlag = Flagz.valueOf(13.37);

  @FlagInfo(name = "test_float_flag", help = "")
  final Flag<Float> floatFlag = Flagz.valueOf((float) 13.37);

  @FlagInfo(name = "test_bool_flag", help = "")
  final Flag<Boolean> booleanFlag = Flagz.valueOf(false);

  @FlagInfo(name = "test_string_flag", help = "")
  final Flag<String> stringFlag = Flagz.valueOf("elite");

  @FlagInfo(name = "test_short_flag", help = "")
  final Flag<Short> shortFlag = Flagz.valueOf((short) 13);

  @FlagInfo(name = "test_byte_flag", help = "")
  final Flag<Byte> byteFlag = Flagz.valueOf((byte) 0xDD);

  @FlagInfo(name = "test_enum_flag", help = "")
  final Flag<SomeEnum> enumFlag = Flagz.valueOf(SomeEnum.NORMAL);


  final Set<Object> SET_OF_THIS_TEST = ImmutableSet.of(this);


  @Test
  public void testSmokeNoCrosstalk() {
    String[] args = {"--test_int_flag=100", "--test_double_flag=0.33", "--test_long_flag=13337"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(integerFlag.get(), is(100));
    assertThat(doubleFlag.get(), is(0.33d));
    assertThat(longFlag.get(), is(13337L));
  }

  @Test
  public void testInteger_Default() {
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(integerFlag.get(), is(1337));
  }

  @Test
  public void testInteger_Set() {
    String[] args = {"--test_int_flag=100"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(integerFlag.get(), is(100));
  }

  @Test
  public void testInteger_Hex() {
    String[] args = {"--test_int_flag=0x1000"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(integerFlag.get(), is(0x1000));
  }

  @Test
  public void testLong_Default() {
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(longFlag.get(), is(13371337L));
  }

  @Test(expected = FlagException.IllegalFormat.class)
  public void testInteger_Bad() {
    String[] args = {"--test_int_flag=99.9"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
  }

  @Test
  public void testLong_Set() {
    String[] args = {"--test_long_flag=99999999999"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(longFlag.get(), is(99999999999L));
  }

  @Test
  public void testDouble_Default() {
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(doubleFlag.get(), is(13.37));
  }

  @Test(expected = FlagException.IllegalFormat.class)
  public void testDouble_Bad() {
    String[] args = {"--test_double_flag=99,9"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
  }

  @Test
  public void testDouble_Set() {
    String[] args = {"--test_double_flag=9.999"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(doubleFlag.get(), is(9.999d));
  }

  @Test
  public void testFloat_Default() {
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(floatFlag.get(), is(13.37f));
  }

  @Test
  public void testFloat_Set() {
    String[] args = {"--test_float_flag=9.999"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(floatFlag.get(), is(9.999f));
  }

  @Test
  public void testBoolean_Default() {
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(booleanFlag.get(), is(false));
  }

  @Test
  public void testBoolean_Set() {
    String[] args = {"--test_bool_flag=true"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(booleanFlag.get(), is(true));
  }

  @Test
  public void testBoolean_Set_Short() {
    String[] args = {"--test_bool_flag"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(booleanFlag.get(), is(true));
  }

  @Test()
  public void testBoolean_Set_Insensitive() {
    String[] args = {"--test_bool_flag=False"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(booleanFlag.get(), is(false));
  }

  @Test(expected = FlagException.IllegalFormat.class)
  public void testBoolean_Bad() {
    String[] args = {"--test_bool_flag=rubbish"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
  }

  @Test
  public void testString_Default() {
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(stringFlag.get(), is("elite"));
  }

  @Test
  public void testString_Set() {
    String[] args = {"--test_string_flag=my_value"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(stringFlag.get(), is("my_value"));
  }

  @Test
  public void testString_Set_Empty() {
    String[] args = {"--test_string_flag="};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(stringFlag.get(), is(""));
  }

  @Test
  public void testString_Set_WithEquals() {
    String[] args = {"--test_string_flag=something=good"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(stringFlag.get(), is("something=good"));
  }


  @Test
  public void testShort_Default() {
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(shortFlag.get(), is((short) 13));
  }

  @Test
  public void testShort_Set() {
    String[] args = {"--test_short_flag=1337"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(shortFlag.get(), is((short) 1337));
  }

  @Test(expected = FlagException.IllegalFormat.class)
  public void testShort_Bad() {
    String[] args = {"--test_short_flag=13371337"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
  }

  @Test
  public void testByte_Default() {
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(byteFlag.get(), is((byte) 0xDD));
  }

  @Test
  public void testByte_Set() {
    String[] args = {"--test_byte_flag=0xAF"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(byteFlag.get(), is((byte) 0xAF));
  }

  @Test(expected = FlagException.IllegalFormat.class)
  public void testByte_Bad() {
    String[] args = {"--test_byte_flag=DEAD"}; // Bigger than range.
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
  }

  @Test
  public void testEnum_Default() {
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(enumFlag.get(), is(SomeEnum.NORMAL));
  }

  @Test
  public void testEnum_Set() {
    String[] args = {"--test_enum_flag=WACKY"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(enumFlag.get(), is(SomeEnum.WACKY));
  }

  @Test(expected = FlagException.IllegalFormat.class)
  public void testEnum_Bad() {
    String[] args = {"--test_enum_flag=RUBBISH"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
  }
}
