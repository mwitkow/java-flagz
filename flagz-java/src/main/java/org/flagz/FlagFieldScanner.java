package org.flagz;

import com.google.common.base.Preconditions;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Predicates.not;


/**
 * Scanners yielding {@link FlagInfo} annotated {@link FlagField}s objects.
 *
 * These objects are guaranteed to be bound to their respective {@link Field} Java objects
 * to maintain typing.
 */
abstract class FlagFieldScanner {

  /**
   * Scans for {@link FlagField} objects and binds them to the {@link Field} to preserve type.
   */
  public abstract Set<FlagField<?>> scanAndBind();

  private final FlagPropertySyncer propertySyncer = new FlagPropertySyncer();


  protected FlagField<?> boundFlagField(Field field, Object declaredIn) {
    FlagField<?> flagField = null;
    if (!Flag.class.isAssignableFrom(field.getType())) {
      throw new RuntimeException(
          String.format(
              "Field(%s#%s) annotated with @FlagInfo but not a Flag.",
              field.getDeclaringClass().getCanonicalName(),
              field.getName()));
    }
    try {
      final boolean wasAccessible = field.isAccessible();
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }
      flagField = (FlagField<?>) field.get(declaredIn);
      flagField.bind(field);
      FlagProperty property = propertySyncer.fieldPropertyAnnotation(field);
      if (property != null) {
        propertySyncer.handlePropertyAnnotaton(flagField, property);
      }
      if (!wasAccessible) {
        field.setAccessible(false);
      }
    } catch (IllegalAccessException exception) {
      // Should never happen because access is granted, but let's get a nice stack trace.
      throw new RuntimeException("Unexpected problem parsing Field " + field.toString(), exception);
    }
    return flagField;
  }

  /**
   * Scans the entire Java classpath to find static final fields of {@link Flag}.
   */
  static class StaticFinalScanner extends FlagFieldScanner {

    private final List<String> prefixes;

    public StaticFinalScanner(final List<String> prefixes) {
      this.prefixes = Preconditions.checkNotNull(prefixes);
    }

    @Override
    public Set<FlagField<?>> scanAndBind() {
      Reflections reflections = ReflectionsCache.reflectionsForPrefixes(prefixes);
      Set<FlagField<?>> fields = new HashSet<>();
      for (Field field : reflections.getFieldsAnnotatedWith(FlagInfo.class)) {
        if (!Modifier.isStatic(field.getModifiers())
            || !Modifier.isFinal(field.getModifiers())) {
          continue;
        }
        fields.add(boundFlagField(field, null)); // null is ok, these are static.
      }

      return fields;
    }
  }

  /** Scans a set of objects, finding bound final fields. Useful for Scala 'objects'. */
  static class ObjectBoundFinalScanner extends FlagFieldScanner {

    private final Set<Object> objectsToScan;

    public ObjectBoundFinalScanner(Set<Object> objectsToScan) {
      this.objectsToScan = Preconditions.checkNotNull(objectsToScan);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<FlagField<?>> scanAndBind() {
      Set<FlagField<?>> fields = new HashSet<>();
      for (Object obj : objectsToScan) {
        ReflectionUtils.getAllFields(
            obj.getClass(),
            ReflectionUtils.withTypeAssignableTo(Flag.class),
            ReflectionUtils.withAnnotation(FlagInfo.class),
            ReflectionUtils.withModifier(Modifier.FINAL),
            not(ReflectionUtils.withModifier(Modifier.STATIC)))
            .stream()
            .map(f -> boundFlagField(f, obj))
            .forEach(fields::add);
      }
      return fields;
    }
  }
}
