package org.flagz;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Flag}s annotated with {@link FlagProperty}.
 *
 * **Note:** All flags are declared in-object, and the `this` object is passed to Flagz to find these fields. We are not
 * testing the ability of Flagz to resolve fields, just value parsing.
 */
public class FlagPropertySyncerTest {

  public static final String[] EMPTY_ARGS = {};
  public static final List<String> EMPTY_PACKAGE_PREFIXES = ImmutableList.of();
  static final float PRECISION = 0.001f;
  static Properties systemProps;

  @FlagInfo(name = "paramflag_int", help = "some int")
  @FlagProperty(value = "org.flagz.testing.some_int", sync = false)
  final Flag<Integer> flagInt = Flagz.valueOf(400);

  @FlagInfo(name = "paramflag_map_int", help = "some map")
  @FlagProperty(value = "org.flagz.testing.some_map", sync = true)
  final Flag<Map<String, Integer>> flagMap = Flagz.valueOf(ImmutableMap.of("foo", 100, "boo", 200));

  final Set<Object> SET_OF_THIS_TEST = ImmutableSet.of(this);


  @Before
  public void setProperties() {
    systemProps = System.getProperties();
    System.setProperties(new Properties(systemProps));
  }

  @After
  public void resetProperties() {
    System.setProperties(systemProps);
  }

  @Test
  public void testOneWay_NoPropertySetWithoutExisting() {
    String[] args = {};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(flagMap.get(), is(flagMap.defaultValue()));
    assertThat(System.getProperty("org.flagz.testing.some_int"), nullValue());
  }

  @Test
  public void testOneWay_ValueFromProperty() {
    System.setProperty("org.flagz.testing.some_int", "777");
    String[] args = {};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(flagInt.get(), is(777));
  }

  @Test
  public void testOneWay_CmdLnLeavesProperty() {
    System.setProperty("org.flagz.testing.some_int", "777");
    String[] args = {"--paramflag_int=1337"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(flagInt.get(), is(1337));
    assertThat(System.getProperty("org.flagz.testing.some_int"), is("777"));
  }

  @Test
  public void testOneWay_DynamicLeavesProperty() {
    System.setProperty("org.flagz.testing.some_int", "777");
    String[] args = {};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(flagInt.get(), is(777));
    flagInt.accept(1337);
    assertThat(System.getProperty("org.flagz.testing.some_int"), is("777"));
    assertThat(flagInt.get(), is(1337));
  }

  @Test
  public void testBidirectional_DefaultUpdatesProperty() {
    String[] args = {};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(flagMap.get(), is(flagMap.defaultValue()));
    assertThat(System.getProperty("org.flagz.testing.some_map"), is("foo:100,boo:200"));
  }

  @Test
  public void testBidirectional_ValueFromProperty() {
    System.setProperty("org.flagz.testing.some_map", "car:234,tar:789");
    String[] args = {};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(flagMap.get(), equalTo(ImmutableMap.of("car", 234, "tar", 789)));
  }

  @Test
  public void testBidirectional_CmdlnUpdatesProperty() {
    System.setProperty("org.flagz.testing.some_map", "car:234,tar:789");
    String[] args = {"--paramflag_map_int=zoo:700,moo:800"};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    assertThat(flagMap.get(), equalTo(ImmutableMap.of("zoo", 700, "moo", 800)));
    assertThat(System.getProperty("org.flagz.testing.some_map"), is("zoo:700,moo:800"));
  }

  @Test
  public void testBidirectional_DynamicUpdatesProperty() {
    System.setProperty("org.flagz.testing.some_map", "car:234,tar:789");
    String[] args = {};
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    flagMap.accept(ImmutableMap.of("zoo", 700, "moo", 800));
    assertThat(System.getProperty("org.flagz.testing.some_map"), is("zoo:700,moo:800"));
  }
}
