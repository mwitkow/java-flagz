package org.flagz;

import com.google.common.collect.ImmutableList;
import org.reflections.Reflections;
import scala.DelayedInit;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Scanner for Scala objects annotated with {@link FlagContainer}.
 *
 * Scala objects are really classes with a "$" suffix that initialize a singleton static field.
 * However, Scala doesn't execute the initialization until the object is referenced. This class
 * reflects on the whole classpath, finds the classes for objects, and initializes them, returning
 * the singleton instances of the Scala objects.
 */
class ScalaObjectScanner {

  static synchronized Set<Object> scanFlagObjects(List<String> packagePrefixes) {

    Reflections reflections = ReflectionsCache.reflectionsForPrefixes(packagePrefixes);
    Set<Object> objects = new HashSet<>();
    for (Class<?> clazz : reflections.getSubTypesOf(FlagContainer.class)) {
      checkDelayedInit(clazz);
      // Scala objects have a MODULE$ static field.
      // http://grahamhackingscala.blogspot.co.uk/2009/11/scala-under-hood-of-hello-world.html
      try {
        Field staticModuleField = clazz.getDeclaredField("MODULE$");
        boolean wasAccessible = staticModuleField.isAccessible();
        if (!wasAccessible) {
          staticModuleField.setAccessible(true);
        }
        objects.add(staticModuleField.get(null));  // null is fine, this is a static field.
        staticModuleField.setAccessible(wasAccessible);
      } catch (NoSuchFieldException | IllegalAccessException exception) {
        throw new IllegalArgumentException(
            "Error reflecting on Scala object. " + clazz.getCanonicalName(), exception);
      }
    }
    return objects;
  }

  /** Checks if the flags are defined in a DelayedInit class, that isn't on the call trace. */
  private static void checkDelayedInit(Class<?> clazz) {
    if (DelayedInit.class.isAssignableFrom(clazz)) {
      Optional<StackTraceElement> onStack = ImmutableList
          .copyOf(Thread.currentThread().getStackTrace())
          .stream()
          .filter(x -> x.getClassName().startsWith(clazz.getCanonicalName()))
          .findFirst();
      if (!onStack.isPresent()) {
        throw new FlagException(
            "A DelayedInit (e.g. App) object defines a Flag but is not initialized. Consider "
                + "moving to a non DelayedInit object. Class: " + clazz.getCanonicalName());
      }
    }
  }
}
