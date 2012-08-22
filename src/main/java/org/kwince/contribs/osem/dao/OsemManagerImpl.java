package org.kwince.contribs.osem.dao;

import java.util.Collections;
import java.util.List;

import org.elasticsearch.node.Node;
import org.kwince.contribs.osem.annotations.PostOsemCreate;
import org.kwince.contribs.osem.annotations.PostOsemDelete;
import org.kwince.contribs.osem.annotations.PostOsemRead;
import org.kwince.contribs.osem.annotations.PostOsemUpdate;
import org.kwince.contribs.osem.annotations.PreOsemCreate;
import org.kwince.contribs.osem.annotations.PreOsemDelete;
import org.kwince.contribs.osem.annotations.PreOsemRead;
import org.kwince.contribs.osem.annotations.PreOsemUpdate;
import org.kwince.contribs.osem.event.EventDispatcher;
import org.kwince.contribs.osem.exceptions.OsemException;
import org.kwince.contribs.osem.util.ReflectionUtil;
import org.kwince.contribs.osem.validation.Validator;

class OsemManagerImpl extends PersistenceService implements OsemManager {
	
	private EventDispatcher dispatcher;
	
	OsemManagerImpl (Node client,EventDispatcher dispatcher) {
		this.node = client;
		this.dispatcher = dispatcher;
	}

	@Override
	public <E> E create(E entity) {
		E result = null;
		dispatcher.publish(PreOsemCreate.class, entity);
		Validator.validate(entity.getClass());
		try {
			String id = ReflectionUtil.getId(entity);
			result = super.mapping(id, entity, true);
			dispatcher.publish(PostOsemCreate.class, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public <E> E read(String id, Class<E> clazz) {
		E result = null;
		dispatcher.publishId(PreOsemRead.class, id, clazz);
		Validator.validate(clazz);
		checkIndex(clazz);
		
		try {
			result = super.read(id, clazz);
			dispatcher.publish(PostOsemRead.class, result, clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		return result;
	}

	@Override
	public <E> E update(E entity) {
		E result = null;
		dispatcher.publish(PreOsemUpdate.class, entity);
		Validator.validate(entity.getClass());
		checkIndex(entity.getClass());
		
		try {
			String id = ReflectionUtil.getId(entity);
			result = (E) super.mapping(id, entity, false);
			dispatcher.publish(PostOsemUpdate.class, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public <E> void delete(E entity) {
		Class<?> clazz = entity.getClass();
		boolean result = false;
		dispatcher.publish(PreOsemDelete.class, entity);
		Validator.validate(clazz);
		checkIndex(clazz);
		
		try {
			String id = ReflectionUtil.getId(entity);
			result = super.delete(id, clazz);
			if (result) {
				dispatcher.publish(PostOsemDelete.class, entity);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public <E> List<E> find(String query, Class<E> clazz) {
		try{
			checkIndex(clazz);
		}catch(OsemException e){
			return Collections.emptyList();
		}
		
		try {
			List<E> lst = query(query, clazz); 
			return lst;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public <E> List<E> findAll() {
		return null;
	}
	
}