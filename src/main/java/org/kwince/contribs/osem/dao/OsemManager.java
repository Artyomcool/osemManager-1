package org.kwince.contribs.osem.dao;

import org.elasticsearch.index.query.QueryBuilder;

public interface OsemManager {
	<E> E save(E entity);
	<E> E save(E entity,boolean refresh);
	<E> E read(String id, Class<E> clazz);
    <E> void delete(E entity);
    <E> void delete(E entity,boolean refresh);

    <E> SearchResult<E> find(String query, int from, int size, Class<E> clazz);
	<E> long count(String query, Class<E> clazz);

    <E> SearchResult<E> find(QueryBuilder query, int from, int size, Class<E> clazz, String... sortFields);
	<E> long count(QueryBuilder query, Class<E> clazz);
    
	void close();
	void complete(Class<?> cl);
	void dropCache();
}