package org.kwince.contribs.osem.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to support synthetic fields.
 * This fields are calculated by calling method annotated as <b>@Synthetic</b> before saving en entity.
 * The value of synthetic fields don't loaded back when entity is read from ES.
 * @author Artyomcool
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Synthetic {
	
	/**
	 * The name of synthetic field as it will be stored in ES index.
	 * @return the name of field
	 */
	String value();
	
}
