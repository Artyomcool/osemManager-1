package org.kwince.contribs.osem.annotations.mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleParam {

	String name();
	double value();
	
}
