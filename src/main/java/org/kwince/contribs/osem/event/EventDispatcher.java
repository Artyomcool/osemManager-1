package org.kwince.contribs.osem.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.collect.Sets;
import org.kwince.contribs.osem.annotations.PostOsemCreate;
import org.kwince.contribs.osem.annotations.PostOsemDelete;
import org.kwince.contribs.osem.annotations.PostOsemRead;
import org.kwince.contribs.osem.annotations.PostOsemUpdate;
import org.kwince.contribs.osem.annotations.PreOsemCreate;
import org.kwince.contribs.osem.annotations.PreOsemDelete;
import org.kwince.contribs.osem.annotations.PreOsemRead;
import org.kwince.contribs.osem.annotations.PreOsemUpdate;
import org.kwince.contribs.osem.exceptions.OsemException;
import org.kwince.contribs.osem.util.ReflectionUtil;

public class EventDispatcher {
	
	private static class HandlerInfo{
		Object handler;
		Map<Class<? extends Annotation>,Method> methods = new HashMap<Class<? extends Annotation>,Method>();
	}
	
	@SuppressWarnings("unchecked")
	private final static List<Class<? extends Annotation>> lifecycleAnnotations = Arrays.asList(
			PostOsemCreate.class,
			PostOsemRead.class,
			PostOsemUpdate.class,
			PostOsemDelete.class,
			PreOsemCreate.class,
			PreOsemRead.class,
			PreOsemUpdate.class,
			PreOsemDelete.class);
	
	private final static Set<Class<? extends Annotation>> idAnnotations = Sets.newHashSet();
	static{
		idAnnotations.add(PreOsemRead.class);
	}
	
	
	private static Map<Class<?>,List<HandlerInfo>> handlers = new HashMap<Class<?>,List<HandlerInfo>>();
	
	private static Map<Class<?>,List<Class<?>>> hierarchyCache = new HashMap<Class<?>,List<Class<?>>>();
	
	private static HandlerInfo createHandlerInfo(Class<?> entityClass,Object handler) throws IllegalArgumentException {
		
		HandlerInfo info = new HandlerInfo();
		info.handler = handler;
		Class<?> clazz = handler.getClass();
		
		for(Method m:clazz.getMethods()){
			for(Class<? extends Annotation> c:lifecycleAnnotations){
				
				if(m.isAnnotationPresent(c)){
					
					if(m.getParameterTypes().length > 1)
						throw new IllegalArgumentException("The method "+m.getName()+" has wrong arguments count: "+m.getParameterTypes().length);
					
					if(m.getParameterTypes().length == 1){
						if(idAnnotations.contains(c)){
							if(m.getParameterTypes()[0]!=String.class)
								throw new IllegalArgumentException("The method`s "+m.getName()+" has a wrong argument type: "+m.getParameterTypes()[0].getName() + " (should be a String)");
						}else{
							if(!m.getParameterTypes()[0].isAssignableFrom(entityClass))
								throw new IllegalArgumentException("The method`s "+m.getName()+" has a wrong argument type: "+m.getParameterTypes()[0].getName() + " (should be assignable from "+entityClass.getName()+")");
						}
					}
					
					if(info.methods.containsKey(c))
						throw new IllegalArgumentException("The handler should have no more then one method for annotation "+c.getSimpleName());
					
					m.setAccessible(true);
					info.methods.put(c, m);
				}
				
			}
		}

		return info;
	}
	
	/**
	 * Registers handler for class and all subclasses. If <b>clazz</b> is interface all entities implemented such interface will be affected this handler.
	 * @param clazz Class of entities to handle by <b>handler</b>
	 * @param handler Handler for entities of <b>clazz</b>
	 * @throws IllegalArgumentException if handler has more then one method for one of entity`s life-cycle event annotations. 
	 */
	public void addHandler(Class<?> clazz,Object handler) throws IllegalArgumentException {
		List<HandlerInfo> h = handlers.get(clazz);
		if(h == null){
			h = new LinkedList<HandlerInfo>();
			handlers.put(clazz,h);
		}
		h.add(createHandlerInfo(clazz,handler));
	}

	/**
	 * Notifies for entity`s life-cycle event. Entity <i>can not</i> be null.
	 * @param event Event to notify
	 * @param entity Entity that caused the event
	 */
	public <E> void publish(Class<? extends Annotation> event, E entity) {
		publishInternal(event, entity, entity.getClass());
	}

	/**
	 * Notifies for entity`s life-cycle event. Entity <i>can</i> be null.
	 * @param event Event to notify
	 * @param entity Entity that caused the event
	 * @param clazz Class expected for entity to be
	 */
	public <E> void publish(Class<? extends Annotation> event, E entity, Class<E> clazz) {
		publishInternal(event, entity, clazz);
	}

	/**
	 * Notifies for entity`s life-cycle id-based event.
	 * @param event Event to notify
	 * @param entity Entity that caused the event
	 */
	public void publishId(Class<? extends Annotation> event, String id, Class<?> clazz) {
		publishInternal(event, id, clazz);
	}
	
	private void publishInternal(Class<? extends Annotation> event, Object entity, Class<?> clazz) {
		List<Class<?>> classes = hierarchyCache.get(clazz);
		if(classes == null){
			classes = Collections.unmodifiableList(ReflectionUtil.getInheritance(clazz));
			hierarchyCache.put(clazz, classes);
		}

		for(Class<?> c:classes)
			if(handlers.containsKey(c))
				for(HandlerInfo handler:handlers.get(c))
					invoke(handler.methods.get(event),handler.handler,entity);
	}
	
	private void invoke(Method method, Object handler, Object entity){
		if(method != null) 
			try {
								
				if(method.getParameterTypes().length == 1)
					method.invoke(handler, entity);
				else
					method.invoke(handler);
				
			}catch (IllegalAccessException e) {
				throw new OsemException("Can`t invoke event handler", e);
			} catch (InvocationTargetException e) {
				throw new OsemException("Can`t invoke event handler", e);
			}
	}
	
}