package org.flagz;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import java.util.Set;
import java.util.function.BiFunction;

/**
 * Flag implementations for all primitive Java types.
 */
class PrimitiveFlagField {


  static class EnumFlagField<E extends Enum<E>> extends FlagField<E> {
    public EnumFlagField(E defaultValue) {
      super(defaultValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void parseString(String value) throws FlagException {
      accept(fromString(value, (Class<E>) fieldType(), this));
    }

    public static <T extends Enum<T>> T fromString(String value, Class<T> clazz, Flag flag)
        throws FlagException {
      try {
        return Enum.valueOf(clazz, value);
      } catch (IllegalArgumentException exception) {
        throw new FlagException.IllegalFormat(flag, value, exception);
      }
    }
  }

  static class StringFlagField extends FlagField<String> {

    public StringFlagField(String defaultValue) {
      super(defaultValue);
    }

    @Override
    protected void parseString(String value) {
      accept(value);
    }
  }

  static class NumberFlagField<T extends Number> extends FlagField<T> {

    NumberFlagField(T defaultValue) {
      super(defaultValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void parseString(String value) throws FlagException {
      accept((T) fromString(value, (Class<?>) fieldType(), this));
    }

    public static Number fromString(String value, Class<?> clazz, Flag flag) throws FlagException {
      try {
        if (clazz.equals(Byte.class)) {
          // Special parsing of Bytes, so we can work around Java's crappy singed bytes.
          int intVal = withRadixPrefix(value, Integer::parseInt);
          if (intVal > 255 || intVal < 0) {
            throw new NumberFormatException(String.format("Input out of range input: %d", intVal));
          }
          return (byte) intVal;
        } else if (clazz.equals(Short.class)) {
          return withRadixPrefix(value, Short::parseShort);
        } else if (clazz.equals(Integer.class)) {
          return withRadixPrefix(value, Integer::parseInt);
        } else if (clazz.equals(Long.class)) {
          return withRadixPrefix(value, Long::parseLong);
        } else if (clazz.equals(Float.class)) {
          return Float.parseFloat(value);
        } else if (clazz.equals(Double.class)) {
          return Double.parseDouble(value);
        }
        throw new FlagException.UnsupportedType(flag, clazz);
      } catch (NumberFormatException exception) {
        throw new FlagException.IllegalFormat(flag, value, exception);
      }
    }

    private static <N> N withRadixPrefix(String value, BiFunction<String, Integer, N> parseFunc) {
      if (value.startsWith("0x")) {
        return parseFunc.apply(value.substring(2), 16);
      } else {
        return parseFunc.apply(value, 10);
      }
    }
  }

  static class BooleanFlagField extends FlagField<Boolean> {

    private static final Set<String> VALID_VALUES = ImmutableSet.of("true", "false");

    BooleanFlagField(Boolean defaultValue) {
      super(defaultValue);
    }

    @Override
    protected void parseString(String value) {
      // Handle shorthand of Boolean flags. E.g. --my_flag is equal to --my_flag=true
      if (Strings.isNullOrEmpty(value)) {
        accept(true);
      } else {
        String lowerValue = value.toLowerCase();
        if (!VALID_VALUES.contains(lowerValue)) {
          throw new FlagException.IllegalFormat(
              this, value,
              new IllegalArgumentException("Accepted values ['true', 'false']."));
        }
        accept(lowerValue.equals("true"));
      }
    }
  }
}
