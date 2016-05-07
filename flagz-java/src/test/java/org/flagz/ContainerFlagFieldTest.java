package org.flagz;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.flagz.testclasses.SomeEnum;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Tests of flags that contain other values (Sets, Lists, Maps).
 *
 * **Note:** All flags are declared in-object, and the `this` object is passed to Flagz to find these fields. We are not
 * testing the ability of Flagz to resolve fields, just value parsing.
 */
public class ContainerFlagFieldTest {

  public static final String[] EMPTY_ARGS = {};
  public static final List<String> EMPTY_PACKAGE_PREFIXES = ImmutableList.of();

  @FlagInfo(name = "test_list_int_flag", help = "")
  final Flag<List<Integer>> integerListFlag = Flagz.valueOf(ImmutableList.of(1337, 9999));

  @FlagInfo(name = "test_set_string_flag", help = "")
  final Flag<Set<String>> stringSetFlag = Flagz.valueOf(ImmutableSet.of("elite", "awesome"));

  @FlagInfo(name = "test_list_enum_flag", help = "")
  final Flag<List<SomeEnum>> enumListFlag = Flagz.valueOf(ImmutableList.of(SomeEnum.NORMAL, SomeEnum.CHILLED));

  @FlagInfo(name = "test_map_enum_flag", help = "")
  final Flag<Map<SomeEnum, Integer>> enumMapFlag = Flagz.valueOf(ImmutableMap.of(SomeEnum.NORMAL, 1337));

  @FlagInfo(name = "test_map_string_flag", help = "")
  final Flag<Map<String, String>> stringMapFlag = Flagz.valueOf(ImmutableMap.of("key1", "value1", "key2", "value2"));


  final Set<Object> SET_OF_THIS_TEST = ImmutableSet.of(this);


  @Test
  public void testSmokeNoCrosstalk() {
    String[] args = {"--test_list_int_flag=123,987", "--test_map_string_flag=key4:value4"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(integerListFlag.get(), equalTo(ImmutableList.of(123, 987)));
    assertThat(stringMapFlag.get(), equalTo(ImmutableMap.of("key4", "value4")));
  }

  @Test
  public void testCollections_Default() {
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(integerListFlag.get(), equalTo(ImmutableList.of(1337, 9999)));
    assertThat(enumListFlag.get(), equalTo(ImmutableList.of(SomeEnum.NORMAL, SomeEnum.CHILLED)));
    assertThat(stringSetFlag.get(), equalTo(ImmutableSet.of("elite", "awesome")));
  }

  @Test
  public void testCollections_Set() {
    String[] args = {"--test_list_int_flag=123,987", "--test_list_enum_flag=WACKY,CRAZY", 
        "--test_set_string_flag=foo,bar"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(integerListFlag.get(), equalTo(ImmutableList.of(123, 987)));
    assertThat(enumListFlag.get(), equalTo(ImmutableList.of(SomeEnum.WACKY, SomeEnum.CRAZY)));
    assertThat(stringSetFlag.get(), equalTo(ImmutableSet.of("bar", "foo")));
  }

  @Test(expected = FlagException.IllegalFormat.class)
  public void testCollections_Bad_Separator() {
    String[] args = {"--test_list_int_flag=123:987", "--test_list_enum_flag=WACKY:CRAZY",};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
  }

  @Test(expected = FlagException.IllegalFormat.class)
  public void testCollections_Bad_Integer() {
    String[] args = {"--test_list_int_flag=1e1,231"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
  }

  @Test(expected = FlagException.IllegalFormat.class)
  public void testCollections_Bad_Enum() {
    String[] args = {"--test_list_enum_flag=RUBBISH,BIN"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
  }

  @Test
  public void testCollections_StringValue() {
    // This is not a public interface, but is important that this representation is solid.
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    FlagField<List<Integer>> integerListReal = (FlagField<List<Integer>>) integerListFlag;
    FlagField<List<SomeEnum>> enumListReal = (FlagField<List<SomeEnum>>) enumListFlag;
    FlagField<Set<String>> stringSetReal = (FlagField<Set<String>>) stringSetFlag;
    
    assertThat(integerListReal.valueString(integerListReal.get()), equalTo("1337,9999"));
    assertThat(enumListReal.valueString(enumListReal.get()), equalTo("NORMAL,CHILLED"));
    assertThat(stringSetReal.valueString(stringSetReal.get()), equalTo("elite,awesome"));

  }

  @Test
  public void testMaps_Default() {
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(integerListFlag.get(), equalTo(ImmutableList.of(1337, 9999)));
    assertThat(enumMapFlag.get(), equalTo(ImmutableMap.of(SomeEnum.NORMAL, 1337)));
    assertThat(stringMapFlag.get(), equalTo(ImmutableMap.of("key1", "value1", "key2", "value2")));
  }

  @Test
  public void testMaps_Set() {
    String[] args = {"--test_map_enum_flag=WACKY:123,CRAZY:1000", "--test_map_string_flag=key2:value2,key3:value3"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(enumMapFlag.get(), equalTo(ImmutableMap.of(SomeEnum.WACKY, 123, SomeEnum.CRAZY, 1000)));
    assertThat(stringMapFlag.get(), equalTo(ImmutableMap.of("key3", "value3", "key2", "value2")));
  }

  @Test(expected = FlagException.IllegalFormat.class)
  public void testMaps_Bad_Separator() {
    String[] args = {"--test_map_enum_flag=WACKY=123,CRAZY=1000"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
  }

  @Test(expected = FlagException.IllegalFormat.class)
  public void testMaps_Bad_Item() {
    String[] args = {"--test_map_enum_flag=RUBBISH:123,CRAZY:1000"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
  }
  
  @Test
  public void testMaps_Set_Empty() {
    String[] args = {"--test_map_enum_flag=", "--test_map_string_flag="};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(enumMapFlag.get(), equalTo(ImmutableMap.<SomeEnum, Integer>of()));
    assertThat(stringMapFlag.get(), equalTo(ImmutableMap.<String, String>of()));
  }

  @Test
  public void testMaps_StringValue() {
    // This is not a public interface, but is important that this representation is solid.
    Flagz.parse(EMPTY_ARGS, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    FlagField<Map<SomeEnum, Integer>> enumMapReal = (FlagField<Map<SomeEnum,Integer>>) enumMapFlag;
    FlagField<Map<String, String>> stringMapReal = (FlagField<Map<String,String>>) stringMapFlag;

    assertThat(enumMapReal.valueString(enumMapReal.get()), equalTo("NORMAL:1337"));
    assertThat(stringMapReal.valueString(stringMapReal.get()), equalTo("key1:value1,key2:value2"));
  }
}
