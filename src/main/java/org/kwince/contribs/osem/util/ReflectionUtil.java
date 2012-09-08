package org.kwince.contribs.osem.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.kwince.contribs.osem.annotations.Id;
import org.kwince.contribs.osem.exceptions.OsemException;

public class ReflectionUtil
{
	public static List<Field> getFields(Class<?> clazz) {
		List<Field> result = new ArrayList<Field>();
		while(clazz != Object.class){
			Field[] fields = clazz.getDeclaredFields();
			for(Field f:fields){
				if((f.getModifiers() & (Modifier.FINAL | Modifier.STATIC | Modifier.TRANSIENT)) != 0)
					continue;
				f.setAccessible(true);
				result.add(f);
			}
			clazz = clazz.getSuperclass();
		}
		return result;
	}
	
	public static List<Field> getAnnotatedFileds(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		
		List<Field> allFields = getFields(clazz);
		List<Field> annotatedFields = new LinkedList<Field>();

		for (Field field : allFields) {
			if(field.isAnnotationPresent(annotationClass))
				annotatedFields.add(field);
		}

		return annotatedFields;
	}
		
	private static Field getIdField(Class<?> clazz){
		
		List<Field> fields = getAnnotatedFileds(clazz, Id.class);
		
		if(fields.isEmpty())
			throw new OsemException("Class "+clazz.getName()+" doesn't contain an id");
		else if(fields.size() > 1)
			throw new OsemException("Class "+clazz.getName()+" contains multiple ids");
		
		Field f = fields.get(0);
		f.setAccessible(true);
		
		return f;
	}
	
	public static String getId(Object object) {
		try {
			return (String)getIdField(object.getClass()).get(object);
		} catch (SecurityException e) {
			throw new OsemException("Can't get Id",e);
		} catch (IllegalArgumentException e) {
			throw new OsemException("Can't get Id",e);
		} catch (IllegalAccessException e) {
			throw new OsemException("Can't get Id",e);
		}
	}
	
	public static void setId(Object object,String id) {
		try {
			getIdField(object.getClass()).set(object,id);
		} catch (SecurityException e) {
			throw new OsemException("Can't get Id",e);
		} catch (IllegalArgumentException e) {
			throw new OsemException("Can't get Id",e);
		} catch (IllegalAccessException e) {
			throw new OsemException("Can't get Id",e);
		}
	}

	public static void trySetId(Object ret, String id) {
		if(!getAnnotatedFileds(ret.getClass(), Id.class).isEmpty())
			setId(ret,id);
	}

	public static String ensureId(Object entity) {
		try {
			
			Field f = getIdField(entity.getClass());
			
			String id = (String)f.get(entity); 
			
			if(id == null){
				id = GuidComb.generate();
				f.set(entity, id);
			}
			
			return id;
			
		} catch (SecurityException e) {
			throw new OsemException("Can't get Id",e);
		} catch (IllegalArgumentException e) {
			throw new OsemException("Can't get Id",e);
		} catch (IllegalAccessException e) {
			throw new OsemException("Can't get Id",e);
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
