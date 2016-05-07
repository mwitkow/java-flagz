package org.flagz;

import com.google.common.base.Strings;

import java.util.function.Predicate;

/**
 * Readily-available validators for {@link Flag#withValidator}.
 */
public class Validators {

  /** Validates that the given integer is greater than a value. */
  public static Predicate<Integer> greaterThan(Integer than) {
    return integer -> {
      if (integer > than) {
        return true;
      } else {
        throw new IllegalArgumentException(String.format("%d not greater than %d", integer, than));
      }
    };
  }

  /** Validates whether the given integer is within [lower, upper] range. */
  public static Predicate<Integer> inRange(Integer lower, Integer upper) {
    return integer -> {
      if (lower <= integer && integer <= upper) {
        return true;
      } else {
        throw new IllegalArgumentException(
            String.format("%d not in range [%d, %d]", integer, lower, upper));
      }
    };
  }

  /** Validates that the string is not empty. */
  public static Predicate<String> isNotEmpty() {
    return string -> {
      if (!Strings.isNullOrEmpty(string)) {
        return true;
      } else {
        throw new IllegalArgumentException("passed string is empty");
      }
    };
  }
}
