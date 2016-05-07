package org.flagz;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Super-type and all exceptions used within the Flagz library.
 */
public class FlagException extends RuntimeException {

  protected Flag flag;
  protected String message;


  FlagException() {
  }

  FlagException(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    if (flag != null) {
      return String.format("%s %s", flag, message);
    } else {
      return message;
    }
  }

  /**
   * Thrown when the given flag is not found in the system.
   */
  public static class UnknownFlag extends FlagException {

    UnknownFlag(String flagName) {
      this.message = String.format("Could not find Flag for name/alt-name '%s'", flagName);
    }

  }

  /**
   * Thrown when the value passed to {@link Flag} does not parse for the given type.
   */
  public static class IllegalFormat extends FlagException {

    IllegalFormat(Flag flag, String value, Throwable exception) {
      this.flag = flag;
      this.message = String.format("Value '%.30s' failed parsing due to: %s", value, exception);
    }

  }

  /**
   * Thrown when the value of the {@link Flag} was correctly parsed but is invalid.
   */
  public static class BadValue extends FlagException {

    BadValue(Flag flag, Object value, Throwable exception) {
      this.flag = flag;
      this.message = String.format("Value '%.30s' failed validation due to: %s", value, exception);
    }
  }

  /**
   * Thrown when multiple {@link Flag} fields have same names/alt-names.
   */
  public static class NameConflict extends FlagException {

    NameConflict(Flag... conflicting) {
      this.message = "Conflict on Flag names/alt-names, with the following:\n";
      for (Flag flag : conflicting) {
        Field field = ((FlagField) flag).containingField();
        this.message = String.format(
            "\t%s declared in Field(%s#%s)\n",
            flag,
            field.getDeclaringClass().getCanonicalName(),
            field.getName());
      }
    }
  }

  /**
   * Thrown when {@link Flag} is expected to handle a type it doesn't support.
   */
  public static class UnsupportedType extends FlagException {

    public UnsupportedType(Flag flag, Type type) {
      this.flag = flag;
      this.message = String.format("Does not support Type(%s)", type.getTypeName());
    }
  }
}
