package org.flagz;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

/**
 * Tests for validator and listener functionality of {@link BaseFlag}.
 */
public class BaseFlagTest {

  public static final String[] EMPTY_ARGS = {};
  public static final List<String> EMPTY_PACKAGE_PREFIXES = ImmutableList.of();

  @SuppressWarnings("unchecked")
  Consumer<String> mockConsumer = mock(Consumer.class);

  @FlagInfo(name = "test_flag_int", help = "some int")
  public final Flag<Integer> flagInt = Flagz.valueOf(200).withValidator(Validators.inRange(0, 100));

  @FlagInfo(name = "test_flag_string", help = "some string")
  public final Flag<String> flagMap = Flagz.valueOf("bar")
      .withValidator(Validators.isNotEmpty())
      .withListener(mockConsumer);
  final Set<Object> SET_OF_THIS_TEST = ImmutableSet.of(this);

  @Test
  public void testNoActionOnDefultValues() {
    String[] args = {};
    // This should not throw an exception, as default values are not subject to validators.
    Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    verify(mockConsumer, never()).accept(anyString());
  }

  @Test
  public void testRangeAcceptsCorrect() {
    String[] args = {};
    FlagFieldRegistry registry = Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    registry.setField("test_flag_int", "90");
  }

  @Test(expected = FlagException.BadValue.class)
  public void testRangeRejectsBad() {
    String[] args = {};
    FlagFieldRegistry registry = Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    registry.setField("test_flag_int", "-1");
  }

  @Test(expected = FlagException.BadValue.class)
  public void testNonEmptyRejectsBad() {
    String[] args = {};
    FlagFieldRegistry registry = Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    registry.setField("test_flag_string", "");
  }

  @Test
  public void testListenerFiredOnCommandLine() {
    String[] args = {"--test_flag_string=moo"};
    FlagFieldRegistry registry = Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    verify(mockConsumer).accept("moo");
  }

  @Test
  public void testListenerFiredOnDynamicChange() {
    String[] args = {};
    FlagFieldRegistry registry = Flagz.parse(args, EMPTY_PACKAGE_PREFIXES, SET_OF_THIS_TEST);
    registry.setField("test_flag_string", "foo");
    verify(mockConsumer).accept("foo");
  }
}
