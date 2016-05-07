package org.flagz;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Public interface visible to users of {@link FlagField}.
 *
 * Flags are Java field variables that are exposed as CLI or dynamic properties. Each such field
 * must be annotated using {@link FlagInfo} and processed through {@link Flagz#parse} which
 * discovers all fields throughout the classpath.
 *
 * Flag objects must be tied to fields that are final. Static final fields
 * (private/protected/public) will be automatically picked up if they exist on the Java classpath.
 * Final fields in instantiated objects can only be discovered if said objects are passed
 * to {@link Flagz#parse}.
 *
 * @param <T> the type this flag holds.
 */
public interface Flag<T> extends Supplier<T>, Consumer<T> {

  /** Returns the value of this flag. */
  T get();

  /** Returns the default value of this flag. */
  T defaultValue();

  /** Sets the value of this flag. */
  void accept(T flagValue);

  /**
   * Returns the user-visible flag name.
   */
  String name();

  /**
   * Returns a user-visible alternative name for the flag, if any.
   */
  @Nullable
  String altName();

  /**
   * Returns the user-visible help string of the flag.
   */
  String help();


  Flag<T> withValidator(Predicate<T> predicate);

  /**
   * Add a listener for changes to this flag.
   *
   * Whenever the flag is changed (parsed from command line, or set dynamically), the given
   * listener will be called.
   */
  Flag<T> withListener(Consumer<T> predicate);
}
