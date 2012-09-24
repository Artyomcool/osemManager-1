package org.kwince.contribs.osem.annotations.mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IntParam {

	String name();
	int value();
	
}
