package org.flagz;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking {@link Flag} objects to be synchronized with the given System property.
 *
 * The purpose of this functionality is to provide backwards compatibility with libraries that
 * expect to be configured through {@link System#getProperty}. It allows for both one way, and two
 * way synchronization of properties and Flags, including synchronizing changes made through
 * dynamic mechanisms (JMX, Etc.d).
 * The behaviour for {@link FlagProperty} annotated {@link Flag}s is as follows:
 *
 *  - if a System property exists upon {@link Flagz#parse} call, it is parsed in exactly the same
 *    way as command line arguments and *overrides* the defaultValue set using {@link Flagz#valueOf}
 *  - if the {@link Flag} later is found on the command line or a {@link Flag#accept} is called,
 *    the value overrides both {@link Flag} value, as well as the System property string is updated
 *    according to the command line format. This behaviour may be turned off
 *    using {@link FlagProperty#sync} = false
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FlagProperty {

  /** System property, similar to {@link System#getProperty}. E.g. "path.separator". */
  String value();

  /** Whether the System property should be updated upon modifications to {@link Flag}. */
  boolean sync() default true;
}