package org.kwince.contribs.osem.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.kwince.contribs.osem.annotations.Id;

public class ReflectionUtil
{
	public static Field[] getAnnotatedFileds(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		
		Field[] allFields = clazz.getDeclaredFields();
		List<Field> annotatedFields = new LinkedList<Field>();

		for (Field field : allFields) {
			if(field.isAnnotationPresent(annotationClass))
				annotatedFields.add(field);
		}

		return annotatedFields.toArray(new Field[annotatedFields.size()]);
	}
	
	private static String capitalize(String str){
		if(str == null || str.isEmpty())
			return str;
		StringBuilder b = new StringBuilder(str);
		b.setCharAt(0, Character.toUpperCase(b.charAt(0)));
		return b.toString();
	}
	
	public static String getId(Object object) throws Exception {
		Object ret = null;
		Method method = null;
		Class<?> clazz = object.getClass();
		
		Field[] fields = getAnnotatedFileds(clazz, Id.class);
		method = clazz.getMethod("get" + capitalize(fields[0].getName()), new Class[] {});
		ret = method.invoke(object, new Object[] {});
		
		if (ret==null) {
			return null;
		}
		
		String id = ret.toString();
		if (id.length()==0) {
			throw new RuntimeException("Not accept Id equals empty");
		}
		
		return id;
	}
	
	public static void setId(Object object,String id) throws Exception{
		Class<?> clazz = object.getClass();
		
		Field[] fields = getAnnotatedFileds(clazz, Id.class);
		String name = "set" + capitalize(fields[0].getName());
		for(Method m:clazz.getMethods()){
			if(m.getName().equals(name)){
				Class<?> param = m.getParameterTypes()[0];
				Object _id = param == String.class ? id : param.getMethod("valueOf", String.class).invoke(null, id);
				m.invoke(object, _id);
			}
		}
	}
	
	/**
	 * Returns the full hierarchy of class <b>in</b> in order: <br>
	 * 1. Class itself<br>
	 * 2. All interfaces implemented by class<br>
	 * 3. Superclass<br>
	 * 4. All interfaces implemented by superclass<br>
	 * 5... Superclass of superclass, etc.<br>
	 * @param in class to build a hierarchy
	 * @return the hierarchy of <b>in</b> 
	 */
	public static List<Class<?>> getInheritance(Class<?> in)
	{
        LinkedHashSet<Class<?>> result = new LinkedHashSet<Class<?>>();

        result.add(in);
        getInheritance(in, result);

        return new ArrayList<Class<?>>(result);
	}

	/**
	 * Get inheritance of type.
	 * 
	 * @param in
	 * @param result
	 */
	private static void getInheritance(Class<?> in, Set<Class<?>> result)
	{
        Class<?> superclass = getSuperclass(in);

        if(superclass != null)
        {
            result.add(superclass);
            getInheritance(superclass, result);
        }

        getInterfaceInheritance(in, result);
	}
	
	/**
	 * Get interfaces that the type inherits from.
	 * 
	 * @param in
	 * @param result
	 */
	private static void getInterfaceInheritance(Class<?> in, Set<Class<?>> result)
	{
        for(Class<?> c : in.getInterfaces())
        {
            result.add(c);

            getInterfaceInheritance(c, result);
        }
	}
	
	/**
	 * Get superclass of class.
	 * 
	 * @param in
	 * @return
	 */
	private static Class<?> getSuperclass(Class<?> in)
	{
        if(in == null)
        {
            return null;
        }

        if(in.isArray() && in != Object[].class)
        {
            Class<?> type = in.getComponentType();

            while(type.isArray())
            {
                type = type.getComponentType();
            }

            return type;
        }

        return in.getSuperclass();
	}
}
