package org.flagz;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for discovering {@link Flag} annotated fields.
 *
 * All fields annotated with {@link FlagInfo} will be dynamically discovered through reflection on
 * {@link Flagz#parse}, and exposed as command line arguments arccording to information
 * in this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FlagInfo {
  /** User-visible information on how to use this flag. */
  String help();


  /**
   * Override of default flag name (inferred from variable name).
   *
   * Flag will be available on the commandline as "--flag_name=<value>"
   */
  String name() default "";

  /**
   * Alternate name for flag, useful for shorthands.
   *
   * Flag will be available on the command line as "-f=<value>"
   */
  String altName() default "";
}