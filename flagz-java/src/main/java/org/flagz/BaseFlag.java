package org.flagz;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Base implementation of a {@link Flag}.
 *
 * This class provides most of the _pure_ functionality of a the public interface of {@link Flag}.
 * Flags are not discoverable without the {@link FlagField}, so please subclass that instead.
 */
class BaseFlag<T> implements Flag<T> {

  private static final Logger LOG = LoggerFactory.getLogger(FlagFieldRegistry.class);

  private volatile T value;
  private final T defaultValue;

  protected String name;
  protected String altName;
  protected String help;
  protected boolean unusedMarker;

  private final List<Predicate<T>> validators = new LinkedList<>();
  private final List<Consumer<T>> listeners = new LinkedList<>();

  BaseFlag(T defaultValue) {
    this.value = defaultValue;
    this.defaultValue = defaultValue;
  }

  @Override
  public T get() {
    if (unusedMarker) {
      String errMsg = String.format("Trying to get a value from flag: %s, which is marked as unused.", name);
      LOG.error(errMsg);
    }
    return value;
  }

  @Override
  public T defaultValue() {
    return defaultValue;
  }

  @Override
  public void accept(T value) {
    checkValidators(value);
    this.value = value;
    notifyListeners(value);
  }

  @Override
  public String name() {
    Preconditions
        .checkState(name != null, "Name and other Flag metadata must be set before proceeding.");
    return name;
  }

  @Nullable
  @Override
  public String altName() {
    return Strings.emptyToNull(altName);
  }

  @Override
  public String help() {
    return Strings.emptyToNull(help);
  }

  /**
   * Add a validator predicate.
   *
   * If the predicate returns false for any value (parsed from command line, or set dynamically) it
   * will be rejected and the Flag's current value will remain unchained.
   *
   * In order to make predicate failures easier to understand, consider throwing a
   * {@link IllegalArgumentException} with a human-readable message.
   *
   * Validators are evaluated in order they are registered.
   */
  public BaseFlag<T> withValidator(Predicate<T> predicate) {
    validators.add(predicate);
    return this;
  }

  /**
   * Add a listener for changes to this flag.
   *
   * Whenever the flag is changed (parsed from command line, or set dynamically), the given
   * listener will be called.
   */
  public BaseFlag<T> withListener(Consumer<T> predicate) {
    listeners.add(predicate);
    return this;
  }


  private void checkValidators(T value) throws FlagException.BadValue {
    for (Predicate<T> predicate : validators) {
      try {
        if (!predicate.test(value)) {
          throw new FlagException.BadValue(
              this, value, new IllegalArgumentException("Predicate failed."));
        }
      } catch (IllegalArgumentException exception) {
        throw new FlagException.BadValue(this, value, exception);
      }
    }
  }

  private void notifyListeners(T value) {
    for (Consumer<T> listener : listeners) {
      listener.accept(value);
    }
  }
}
