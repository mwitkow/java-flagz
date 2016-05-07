package org.flagz;


import com.google.common.base.Strings;
import com.google.common.primitives.Primitives;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A generic for a Flag that contains other types.
 *
 * An equivalent of a {@link Collection} for {@link FlagField}. Has two available implementations:
 *
 * - {@link CollectionFlagField} - allows building Flagz for {@link Collection}
 * types (e.g. List, Set), where values are primitives that are comma-separated, e.g.
 * `--my_set=foo,bar,car`, `--my_list=2,3,1,4`.
 * - {@link MapFlagField} - allows building Flagz for {@link Map} types, where both keys and
 * values are primitives that are comma-separated entries with a colon dividing key and
 * value, e.g. `--my_map=foo:123,bar:456`.
 */
abstract class ContainerFlagField<T> extends FlagField<T> {

  private final Supplier<T> constructor;

  public ContainerFlagField(T defaultValue, Supplier<T> constructor) {
    super(defaultValue);
    this.constructor = constructor;
  }

  protected abstract T addItem(T existing, String value) throws FlagException;

  @Override
  protected void parseString(String value) throws FlagException {
    T newValue = constructor.get();
    String stripped = value.replaceAll("^\"|\"$", "");
    for (String token : stripped.split(",")) {
      if (!Strings.isNullOrEmpty(token)) {
        newValue = addItem(newValue, token);
      }
    }
    accept(newValue);
  }

  @SuppressWarnings("unchecked")
  protected static <X> X itemFromString(String value, Class<X> clazz, Flag flag)
      throws FlagException {
    // In case we get a primitive type (e.g. from Scala), get the boxed version to know
    // how to serialize.
    clazz = Primitives.wrap(clazz);
    if (Number.class.isAssignableFrom(clazz)) {
      return clazz.cast(PrimitiveFlagField.NumberFlagField.fromString(value, clazz, flag));
    } else if (Boolean.class.isAssignableFrom(clazz)) {
      return clazz.cast(Boolean.valueOf(value));
    } else if (String.class.isAssignableFrom(clazz)) {
      return clazz.cast(value);
    } else if (clazz.isEnum()) {
      return (X) PrimitiveFlagField.EnumFlagField.fromString(value, (Class<Enum>) clazz, flag);
    }
    throw new FlagException.UnsupportedType(flag, clazz);
  }

  /**
   * A {@link FlagField} that supports {@link Collection} construction of primitive values.
   */
  static class CollectionFlagField<E, T extends Collection<E>> extends ContainerFlagField<T> {

    private Class<E> elementClazz;

    public CollectionFlagField(T defaultValue, Supplier<T> constructor) {
      super(defaultValue, constructor);
    }

    @Override
    protected T addItem(T existing, String value) throws FlagException {
      E item = itemFromString(value, elementClazz, this);
      existing.add(item);
      return existing;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void bind(Field containingField) {
      super.bind(containingField);
      try {
        elementClazz = (Class<E>) ((ParameterizedType) fieldType()).getActualTypeArguments()[0];
      } catch (ClassCastException exception) {
        throw new FlagException.UnsupportedType(this, fieldType());
      }
    }

    @Override
    public String valueString(T value) {
      return value.stream()
          .map(Object::toString)
          .collect(Collectors.joining(","));
    }
  }

  /**
   * A {@link FlagField} that supports {@link Map} construction of primitive keys and values.
   */
  static class MapFlagField<K, V, T extends Map<K, V>> extends ContainerFlagField<T> {

    private Class<K> keyClazz;
    private Class<V> valueClazz;

    public MapFlagField(T defaultValue, Supplier<T> constructor) {
      super(defaultValue, constructor);
    }

    @Override
    protected T addItem(T existing, String itemString) throws FlagException {
      String[] components = itemString.split(":");
      if (components.length != 2) {
        throw new FlagException.IllegalFormat(this, itemString, null);
      }
      K key = itemFromString(components[0], keyClazz, this);
      V value = itemFromString(components[1], valueClazz, this);
      existing.put(key, value);
      return existing;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void bind(Field containingField) {
      super.bind(containingField);
      try {
        keyClazz = (Class<K>) ((ParameterizedType) fieldType()).getActualTypeArguments()[0];
        valueClazz = (Class<V>) ((ParameterizedType) fieldType()).getActualTypeArguments()[1];
      } catch (ClassCastException exception) {
        throw new FlagException.UnsupportedType(this, fieldType());
      }
    }

    @Override
    public String valueString(T value) {
      return value.entrySet().stream()
          .map(e -> e.getKey().toString() + ":" + e.getValue().toString())
          .collect(Collectors.joining(","));
    }
  }
}
