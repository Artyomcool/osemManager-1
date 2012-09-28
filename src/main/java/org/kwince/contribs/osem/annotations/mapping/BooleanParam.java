package org.kwince.contribs.osem.annotations.mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation to provide field with <b>boolean</b> value.
 * @see Mapping
 * @author Artyomcool
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BooleanParam {

	String name();
	boolean value();
	
}
