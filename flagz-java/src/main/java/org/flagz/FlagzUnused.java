package org.flagz;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used on fields of type Flag<?> to indicate that their value is never read (and, as
 * such, it is useless to specify them). Note that this is subtly stronger than {@link Deprecated}
 * in that deprecated flags might still be read whereas {@link FlagzUnused} may not.
 *
 * Any attempt to get a value from an unused flag will raise a {@link RuntimeException}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FlagzUnused {}
