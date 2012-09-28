package org.kwince.contribs.osem.dao;

import org.elasticsearch.index.query.QueryBuilder;

/**
 * Manager for ES entities
 * @author Artyomcool
 *
 */
public interface OsemManager {
	
	/**
	 * Persists/updates an entity (and all referenced entities)
	 * @param entity
	 * @return persisted entity
	 */
	<E> E save(E entity);
	
	/**
	 * Persists/updates an entity (and all referenced entities)
	 * @param entity
	 * @param refresh index immediately, will be slow
	 * @return persisted entity
	 */
	<E> E save(E entity,boolean refresh);
	
	/**
	 * Reads an entity from ES (and all referenced entities)
	 * @param id entity identifier
	 * @param clazz Class of object to load
	 * @return loaded object
	 */
	<E> E read(String id, Class<E> clazz);
	
	/**
	 * Deletes an entity from ES (and all referenced entities)
	 * @param entity
	 */
    <E> void delete(E entity);
	
	/**
	 * Deletes an entity from ES (and all referenced entities)
	 * @param entity
	 * @param refresh index immediately, will be slow
	 */
    <E> void delete(E entity,boolean refresh);

    /**
     * Finds the entities from ES using a <b>query</b> 
     * @param query string query to search
     * @param from first index to load
     * @param size max size
     * @param clazz class of entities
     * @return SearchResult - total count of elements and list of loaded entities
     */
    <E> SearchResult<E> find(String query, int from, int size, Class<E> clazz);

    /**
     * Finds the entities from ES using a <b>query</b>, but instead loading them just return their count 
     * @param query string query to search
     * @param from first index to load
     * @param size max size
     * @param clazz class of entities
     * @return total count of elements match the query
     */
	<E> long count(String query, Class<E> clazz);

    /**
     * Finds the entities from ES using a <b>query</b> 
     * @param query query to search
     * @param from first index to load
     * @param size max size
     * @param clazz class of entities
     * @param sortFields the list of fields to sort
     * @return SearchResult - total count of elements and list of loaded entities
     */
    <E> SearchResult<E> find(QueryBuilder query, int from, int size, Class<E> clazz, String... sortFields);


    /**
     * Finds the entities from ES using a <b>query</b>, but instead loading them just return their count 
     * @param query query to search
     * @param from first index to load
     * @param size max size
     * @param clazz class of entities
     * @return total count of elements match the query
     */
	<E> long count(QueryBuilder query, Class<E> clazz);
    
	/**
	 * Call this method to released all used resources
	 */
	void close();
	
	/**
	 * <b>For tests only</b><br>
	 * Cleans cache of objects, can make objects inconsistent
	 */
	void dropCache();
}