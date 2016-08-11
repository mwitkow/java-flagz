package org.flagz;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Holds a name to {@link Flag} resolution and allows for updating of flags from strings.
 */
public class FlagFieldRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(FlagFieldRegistry.class);
  private final Set<FlagFieldScanner> scanners;
  private Map<String, FlagField<?>> nameToField = Maps.newHashMap();
  private Map<String, FlagField<?>> allNamesToField = Maps.newHashMap();

  FlagFieldRegistry(Set<FlagFieldScanner> scanners) {
    this.scanners = scanners;
  }

  /** Transforms unused {@link FlagField} objects for pretty printing */
  static String[] unusedFlagsMessages(Set<FlagField<?>> unusedFlags) {
    String header = "The following flag is unused - it will have no effect on the system: ";
    return unusedFlags.stream()
        .map(flag -> header + Utils.flagDescriptorString(flag))
        .toArray(String[]::new);
  }

  /** Retrieves the Flag by its default name. */
  public Flag<?> getField(String name) throws FlagException {
    BaseFlag<?> field = nameToField.get(name);
    if (field == null) {
      throw new FlagException.UnknownFlag(name);
    }
    return field;
  }

  /**
   * Returns a reference to all the flags contained in this registry. Note that value changes which
   * happen after calling this method will be reflected in the returned list.
   */
  public Set<Flag<?>> getAllFields() {
    return ImmutableSet.copyOf(nameToField.values());
  }

  /**
   * Returns a reference to all the flags contained in this registry which are annotated with an
   * annotation of the supplied type. Note that value changes which happen after calling this
   * method will be reflected in the returned list.
   */
  public Set<Flag<?>> getFieldsAnnotatedWith(Class<? extends Annotation> annotationType) {
    ImmutableSet.Builder<Flag<?>> result = ImmutableSet.builder();
    nameToField.values().forEach(flag -> {
      if (flag.containingField().isAnnotationPresent(annotationType)) {
        result.add(flag);
      }
    });
    return result.build();
  }

  /** Sets the value of the Flag, parsing it from string. */
  public void setField(String name, String value) throws FlagException {
    FlagField<?> field = (FlagField<?>) getField(name);
    field.parseString(value);
  }

  void init() throws FlagException {
    Set<FlagField<?>> fields = scanners.stream()
        .flatMap(s -> s.scanAndBind().stream())
        .collect(Collectors.toSet());
    // Add all maps, and throw exceptions if there are conflicts.
    addFieldNamesToMap(nameToField, fields, FlagField::name);
    allNamesToField.putAll(nameToField);
    addFieldNamesToMap(allNamesToField, fields, FlagField::altName);
  }

  void parseAll(Map<String, String> nameToValue) throws FlagException {
    // Check for all missing values to ease the pain of having to start up the binary many
    // times to resolve them.
    Set<String> unknownNames = Sets.difference(nameToValue.keySet(), allNamesToField.keySet());
    if (unknownNames.size() > 0) {
      throw new FlagException.UnknownFlag(unknownNames.stream().collect(Collectors.joining(",")));
    }
    Set<FlagField<?>> unusedFlags = unusedFlags(nameToValue);
    if (!unusedFlags.isEmpty()) {
      Arrays.stream(unusedFlagsMessages(unusedFlags)).forEach(LOG::warn);
    }
    for (String name : nameToValue.keySet()) {
      FlagField<?> field = allNamesToField.get(name);
      field.parseString(nameToValue.get(name));
    }
  }

  /** Returns a set of all user-passed flags which are marked as unused. */
  Set<FlagField<?>> unusedFlags(Map<String, String> nameToValue) {
    return nameToValue.keySet().stream()
        .map(key -> allNamesToField.get(key))
        .filter(flag -> flag != null && flag.unusedMarker)
        .collect(Collectors.toSet());
  }

  Set<FlagField<?>> allFields() {
    return ImmutableSet.copyOf(nameToField.values());
  }

  /** Adds fields to map for a given name retrieval function. Throws exceptions on conflicts. */
  private static void addFieldNamesToMap(Map<String, FlagField<?>> map,
                                         Set<FlagField<?>> fields,
                                         Function<FlagField<?>, String> fieldNameGetter)
      throws FlagException.NameConflict {
    for (FlagField field : fields) {
      String name = fieldNameGetter.apply(field);
      if (Strings.isNullOrEmpty(name)) {
        continue;
      }
      FlagField previousExisted = map.put(name, field);
      if (previousExisted != null) {
        throw new FlagException.NameConflict(previousExisted, field);
      }
    }
  }

}
