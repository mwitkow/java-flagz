package org.flagz;

import com.google.common.base.Strings;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * General static methods.
 */
class Utils {

  /**
   * Generate the list of Flagz and print to StdOut.
   */
  static void printHelpPage(Collection<FlagField<?>> fields) {
    StringBuilder builder = new StringBuilder();
    List<FlagField<?>> sorted = fields
        .stream()
        .sorted((f1, f2) -> f1.name().compareTo((f2.name())))
        .collect(Collectors.toList());
    builder.append("List of Flagz available in this program:\n");
    builder.append("\n");
    for (FlagField<?> field : sorted) {
      String flag = flagDescriptorString(field);
      builder.append(String.format("%-35s\t%s\t[default='%s']\n", flag, field.help(),
                                   flagDefaultValue(field)));
    }
    System.out.println(builder.toString());
  }

  static String flagDescriptorString(FlagField<?> field) {
    return Strings.isNullOrEmpty(field.altName())
        ? String.format("--%s", field.name())
        : String.format("--%s [-%s]", field.name(), field.altName());
  }

  /**
   * String of a default value of a field. For type safety.
   */
  private static <T> String flagDefaultValue(FlagField<T> field) {
    return field.valueString(field.defaultValue());
  }

  /**
   * Return a map that takes each string of the form
   * "--flagName=stringValue"
   * and creates a map (flagName) -> (stringValue).
   *
   * @param args strings of the form "--flagName=stringValue"
   */
  static Map<String, String> parseArgsToFieldMap(String[] args) {
    Map<String, String> argsToFieldMap = new HashMap<>();
    for (String arg : args) {
      String flagName = "";
      String value = "";

      if (!arg.startsWith("-")) {
        continue; // skip this string
      } else if (arg.startsWith("--")) {
        // parse out --flag=value
        int equalsIndex = arg.indexOf("=");
        flagName = arg.substring(2);
        if (equalsIndex >= 2) {
          flagName = arg.substring(2, equalsIndex);
          value = arg.substring(equalsIndex + 1);
        }
      } else if (arg.startsWith("-")) {
        // parse out -f=value
        int equalsIndex = arg.indexOf("=");
        flagName = arg.substring(1);
        if (equalsIndex >= 1) {
          flagName = arg.substring(1, equalsIndex);
          value = arg.substring(equalsIndex + 1);
        }
      }
      argsToFieldMap.put(flagName, value);
    }
    return argsToFieldMap;
  }

}
