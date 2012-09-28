package org.kwince.contribs.osem.dao;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * Intercepter for lazy-loading
 * @author Artyomcool
 *
 */
class Loader implements MethodInterceptor{
	
	/**
	 * Map of pair method-name/id-to-load
	 */
	private Map<String,String> fieldsToLoad = new HashMap<String, String>();
	
	/**
	 * Object class
	 */
	private Class<?> clazz;
	
	/**
	 * Manager to load
	 */
	private OsemManager manager;
	
	/**
	 * Constructs a new Loader
	 * @param clazz class of object
	 * @param manager to load entities
	 */
	Loader(Class<?> clazz,OsemManager manager) {
		this.clazz = clazz;
		this.manager = manager;
	}

	/**
	 * Checks fieldsToLoad for field name and loads object if need
	 */
	@Override
	public Object intercept(Object obj, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		String name = method.getName();
		
		if(name.equals("CGLIB$getLazyField")){
			return fieldsToLoad.get(args[0]);
		}
		
		boolean setter;
		if(name.startsWith("set"))
			setter = true;
		else if(name.startsWith("get"))
			setter = false;
		else
			return methodProxy.invokeSuper(obj, args);
		
		String prefix = setter ? "set" : "get";
		
		String field = name.substring(prefix.length()+1);
		field = Character.toLowerCase(name.charAt(prefix.length()))+field;
		
		if(setter){
			fieldsToLoad.remove(field);
		}else{
			String id = fieldsToLoad.get(field);
			if(id != null){
				Method set = clazz.getMethod("set"+name.substring(prefix.length()),
						method.getReturnType());
				set.invoke(obj, load(id,method.getReturnType()));
			}
		}
		return methodProxy.invokeSuper(obj, args);
	}
	
	/**
	 * Loads object
	 * @param id identifier of object to load
	 * @param clazz class to load
	 * @return loaded object
	 */
	private Object load(String id,Class<?> clazz){
		return manager.read(id, clazz);
	}

	/**
	 * Adds field to load
	 * @param name field name
	 * @param id object id
	 */
	void toLoad(String name, String id) {
		fieldsToLoad.put(name, id);
	}
}