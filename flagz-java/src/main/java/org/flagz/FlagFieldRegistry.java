package org.flagz;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Holds a name to {@link Flag} resolution and allows for updating of flags from strings.
 */
public class FlagFieldRegistry {

  private final Set<FlagFieldScanner> scanners;
  private Map<String, FlagField<?>> nameToField = Maps.newHashMap();
  private Map<String, FlagField<?>> allNamesToField = Maps.newHashMap();

  FlagFieldRegistry(Set<FlagFieldScanner> scanners) {
    this.scanners = scanners;
  }

  /** Retrieves the Flag by its default name. */
  public Flag<?> getField(String name) throws FlagException {
    BaseFlag<?> field = nameToField.get(name);
    if (field == null) {
      throw new FlagException.UnknownFlag(name);
    }
    return field;
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
    for (String name : nameToValue.keySet()) {
      FlagField<?> field = allNamesToField.get(name);
      field.parseString(nameToValue.get(name));
    }
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
