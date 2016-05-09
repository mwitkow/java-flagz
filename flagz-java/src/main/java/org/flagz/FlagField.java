package org.flagz;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * {@link Flag} that is tied to a concrete Java field.
 *
 * Due to Java's type erasure, we need to reflectively find the type of the {@link Flag} that
 * is being processed. This class abstracts that problem away, by filling out the missing fields
 * of {@link BaseFlag}, and serving as the base class for all concrete implementations of flags.
 *
 * You may subclass this, making all your custom {@link Flag} classes discoverable through
 * reflection.
 */
public abstract class FlagField<T> extends BaseFlag<T> {

  // Data from reflection o the field, to avoid type erasure.
  private Field containingField;
  private Type containingFieldType;

  FlagField(T defaultValue) {
    super(defaultValue);
  }

  protected Type fieldType() {
    Preconditions.checkNotNull(
        containingFieldType,
        "The field reference must be set to work around type erasure.");
    return containingFieldType;
  }

  protected abstract void parseString(String value) throws FlagException;

  public String valueString(T value) {
    return value.toString();
  }

  /**
   * Updates the reflected reference to the {@link Field} this object is defined in.
   * <p>
   * This is needed to work around type erasure.
   */
  void bind(Field containingField) {
    Preconditions.checkArgument(
        Flag.class.isAssignableFrom(containingField.getType()),
        "Passed Field is not a Flag<?>.");
    FlagInfo[] annotations = containingField.getAnnotationsByType(FlagInfo.class);
    Preconditions.checkArgument(
        annotations.length == 1,
        "FlagField containing field must contain exactly one @FlagInfo annotation.");
    FlagInfo annotation = annotations[0];
    this.name = Strings.isNullOrEmpty(annotation.name())
        ? containingField.getName()
        : annotation.name();
    this.altName = annotation.altName();
    this.help = annotation.help();
    this.containingField = containingField;
    // Extract the Class<X> or ParametrizedType<X> from Flag<X>
    this.containingFieldType = ((ParameterizedType) containingField.getGenericType())
        .getActualTypeArguments()[0];
    this.unusedMarker = containingField.isAnnotationPresent(FlagzUnused.class);
  }

  Field containingField() {
    return containingField;
  }

  @Override
  public String toString() {
    return String.format("FlagField<%s>(%s)", containingFieldType, name);
  }

}
