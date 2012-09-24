package org.kwince.contribs.osem.annotations.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Mapping {

	StringParam[] stringParams() default {};
	
	BooleanParam[] booleanParams() default {};
	
	IntParam[] intParams() default {};
	
	DoubleParam[] doubleParams() default {};
	
}
