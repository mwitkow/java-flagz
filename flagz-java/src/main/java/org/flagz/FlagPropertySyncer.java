package org.flagz;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * Handles {@link FlagProperty} annotations on {@link FlagField}.
 *
 * Sets default values from properties, adds listeners for changes.
 */
class FlagPropertySyncer {

  @Nullable
  FlagProperty fieldPropertyAnnotation(Field javaField) {
    FlagProperty property = null;
    FlagProperty[] properties = javaField.getAnnotationsByType(FlagProperty.class);
    if (properties.length > 0) {
      property = properties[0];
      Preconditions.checkNotNull(
          Strings.emptyToNull(property.value()),
          "FlagProperty can't be empty or null.");
    }
    return property;
  }

  <T> void handlePropertyAnnotaton(FlagField<T> flagField,
                                   @Nullable FlagProperty propertyAnnotation) {
    if (propertyAnnotation == null) {
      return;
    }
    String propertyName = propertyAnnotation.value();
    String propertyValue = System.getProperty(propertyName);
    if (propertyValue != null) {
      flagField.parseString(propertyValue);
    }
    if (propertyAnnotation.sync()) {
      flagField.withListener(
          (flag) -> System.setProperty(propertyAnnotation.value(), flagField.valueString(flag)));
      // Trigger an override.
      flagField.accept(flagField.get());
    }
  }

}
