package org.kwince.contribs.osem.dao;

import java.util.LinkedList;
import java.util.List;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.kwince.contribs.osem.exceptions.OsemException;
import org.kwince.contribs.osem.util.JSON;
import org.kwince.contribs.osem.util.ReflectionUtil;

//FIXME
/*
 * We need a way to have a dial with extending.
 */
public abstract class PersistenceService {
	
	protected Node node;
	private Client client;
	
	protected <E> E mapping(String id, E entity, boolean create) throws Exception {
		Class<?> clazz = entity.getClass(); //TODO extending
	
		IndexResponse result =  getClient().prepareIndex(getIndexName(clazz), getTypeName(clazz), id)
			.setSource(JSON.serialize(entity))
			.setCreate(create)
			.setRefresh(true)
			.execute()
			.actionGet();
		if (result.getId()==null || result.getId().isEmpty()) {
			return null;
		}
		ReflectionUtil.setId(entity,result.getId());
		return entity;
	}
	
	protected <E> E read(String id, Class<E> clazz) throws Exception {
		GetResponse result = getClient().prepareGet(getIndexName(clazz), getTypeName(clazz), id).execute().actionGet();
        if (!result.isExists()) {
        	return null;
        }
		E entity = JSON.deserialize(result.sourceAsString(), clazz);
		ReflectionUtil.setId(entity,id);
		return entity;
	}
	
	protected boolean delete(String id,Class<?> clazz) throws Exception {
		DeleteResponse result = getClient().prepareDelete(getIndexName(clazz), getTypeName(clazz), id).execute().actionGet();
		return (!result.isNotFound());
	}
	
	protected <E> List<E> query(String query, Class<E> clazz) throws Exception {
		
		WrapperQueryBuilder wrapper = new WrapperQueryBuilder(query);
	    SearchResponse searchResponse = getClient().prepareSearch()
	    		.setIndices(getIndexName(clazz))
	    		.setTypes(getTypeName(clazz))
	    		.setQuery(wrapper)
	    		.setSize(1000)
	    		.execute().actionGet();
	    	    		
		LinkedList<E> list = new LinkedList<E>();
		for(SearchHit hit : searchResponse.getHits()) {
			E obj = JSON.deserialize(hit.sourceAsString(), clazz);
			ReflectionUtil.setId(obj,hit.getId());
			list.add(obj);
		}
		return list;
	}
	
	public void checkIndex(Class<?> clazz) {
		if(!getClient().admin().indices().prepareExists(getIndexName(clazz)).execute().actionGet().exists())
			throw new OsemException("Invalid type. This type does not exist");
		client.admin().cluster().health(new ClusterHealthRequest(getIndexName(clazz)).waitForActiveShards(1)).actionGet();
	}
	
	private Client getClient(){
		if(client == null){
			client = node.client();
			//It is important to wait until shards become available
			client.admin().cluster().health(new ClusterHealthRequest().waitForGreenStatus()).actionGet();
		}
		return this.client;
	}

	public String getIndexName(Class<?> clazz) {
		return getTypeName(clazz);
	}

	public String getTypeName(Class<?> clazz) {
		return clazz.getName().toLowerCase().trim().replace('.', '_');
	}
	
	public void close(){
		if(client!=null){
			client.close();
		}
		node.close();
	}
}