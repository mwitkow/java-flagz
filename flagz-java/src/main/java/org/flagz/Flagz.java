package org.flagz;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper class containing utility methods for working with {@link Flag}
 * objects. Flag objects must be annotated with {@link FlagInfo} in order to
 * be recognized as a flag.
 *
 * To create a new flag, create a new static final {@link Flag} field. The parameter
 * type of the field will be the type of the flag. Then annotate the field
 * with {@link FlagInfo} and provide the necessary fields. Example:
 *
 * ```
 * {@literal @}FlagInfo(help = "max number of threads to use", altName = "n")
 * private static final Flag{@literal <}Integer{@literal >} maxNumThreads =
 * Flag<>.valueOf(4);
 * }
 * ```
 *
 * This example declares a new flag indicating the maximum number of threads
 * to use. On the right hand side, you may provide a default value for the flag.
 * To pass in the value via command line, run the class with flags passed in
 * the format:
 *
 * ```
 * java MyApp --maxNumThreads=5 -shortName=foo --booleanFlag ...
 * ```
 *
 * To parse the flags from the command line, use {@link #parse} and try to catch
 * {@link FlagException} when parsing.
 */
public class Flagz {

  @FlagInfo(name = "help", altName = "h", help = "display this help menu")
  private static final Flag<Boolean> showHelp = Flagz.valueOf(false);

  protected Flagz() {
  }

  public static <N extends Number> Flag<N> valueOf(N defaultValue) {
    return new PrimitiveFlagField.NumberFlagField<>(defaultValue);
  }

  public static Flag<Boolean> valueOf(Boolean defaultValue) {
    return new PrimitiveFlagField.BooleanFlagField(defaultValue);
  }

  public static <E extends Enum<E>> Flag<E> valueOf(E defaultValue) {
    return new PrimitiveFlagField.EnumFlagField<>(defaultValue);
  }

  public static Flag<String> valueOf(String defaultValue) {
    return new PrimitiveFlagField.StringFlagField(defaultValue);
  }

  public static <K, V> Flag<Map<K, V>> valueOf(Map<K, V> defaultMap) {
    return new ContainerFlagField.MapFlagField<>(defaultMap, HashMap::new);
  }

  public static <V> Flag<List<V>> valueOf(List<V> defaultList) {
    return new ContainerFlagField.CollectionFlagField<>(defaultList, ArrayList::new);
  }

  public static <V> Flag<Set<V>> valueOf(Set<V> defaultSet) {
    return new ContainerFlagField.CollectionFlagField<>(defaultSet, HashSet::new);
  }


  /**
   * Parses the command line arguments and updates as necessary all {@link Flag}
   * objects annotated with {@link FlagInfo}.
   *
   * If "--help" of "-h" is passed in at the command line, then the help menu
   * will be printed and the JVM will exit with a 0 exit status.
   *
   * @param args            command line arguments in the form
   *                        "--defaultFlagName=value --booleanFlag -c=foo ..."
   * @param packagePrefixes list of Java packages to be scanned for Flag objects, keeping the scope
   *                        narrow makes it start fast. By default an empty list is used, meaning
   *                        all classes will be reflected on.
   * @param objects         Set of objects that should be reflected to discover non-static final
   *                        {@link Flag} fields.
   * @return A registry that can be used for accessing all flags.
   */
  public static FlagFieldRegistry parse(String[] args, List<String> packagePrefixes,
                                        Set<Object> objects) {
    Set<FlagFieldScanner> scanners = ImmutableSet.of(
        new FlagFieldScanner.ObjectBoundFinalScanner(objects),
        new FlagFieldScanner.StaticFinalScanner(packagePrefixes));
    FlagFieldRegistry registry = new FlagFieldRegistry(scanners);
    registry.init();
    registry.parseAll(Utils.parseArgsToFieldMap(args));

    if (showHelp.get()) {
      Utils.printHelpPage(registry.allFields());
      System.exit(0);
    }
    return registry;
  }


  public static FlagFieldRegistry parse(String[] args) {
    return parse(args, ImmutableList.<String>of(), ImmutableSet.<Object>of());
  }
}
