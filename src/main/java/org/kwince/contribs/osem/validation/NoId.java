package org.kwince.contribs.osem.validation;

import java.lang.reflect.Field;
import java.util.List;

import org.kwince.contribs.osem.annotations.Id;
import org.kwince.contribs.osem.util.ReflectionUtil;

public class NoId implements Constraint {

	@Override
	public void check(Class<?> clazz) {
		List<Field> fields = ReflectionUtil.getAnnotatedFileds(clazz, Id.class);
		if (fields.isEmpty()) {
			try {
				throw new Exception("No Id");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
