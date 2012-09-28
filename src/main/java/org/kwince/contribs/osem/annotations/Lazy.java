package org.kwince.contribs.osem.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to make fields load their content on the access.
 * For {@link java.util.List} fields will be provided a special implementation that loads only accessed (directly or inderectly) elements.
 * Can be used for synthetic methods.
 * @see Synthetic
 * @author Artyomcool
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Lazy {
}
