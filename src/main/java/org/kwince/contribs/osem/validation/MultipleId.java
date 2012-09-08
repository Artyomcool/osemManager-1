package org.kwince.contribs.osem.validation;

import java.lang.reflect.Field;
import java.util.List;

import org.kwince.contribs.osem.annotations.Id;
import org.kwince.contribs.osem.util.ReflectionUtil;


public class MultipleId implements Constraint {

	@Override
	public void check(Class<?> clazz) {
		List<Field> fields = ReflectionUtil.getAnnotatedFileds(clazz, Id.class);
		if (fields.size() > 1) {
			try {
				throw new Exception("Multiple Ids");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
