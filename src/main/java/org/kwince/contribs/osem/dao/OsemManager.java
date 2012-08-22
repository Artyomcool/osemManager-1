package org.kwince.contribs.osem.dao;

import java.util.List;

public interface OsemManager {
	<E> E create(E entity);
	<E> E read(String id, Class<E> clazz);
	<E> E update(E entity);
    <E> void delete(E entity);
    
    <E> List<E> find(String query, Class<E> clazz);
	<E> List<E> findAll();
	void close();
}