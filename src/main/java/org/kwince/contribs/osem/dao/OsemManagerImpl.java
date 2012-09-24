package org.kwince.contribs.osem.dao;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.kwince.contribs.osem.annotations.Id;
import org.kwince.contribs.osem.annotations.Lazy;
import org.kwince.contribs.osem.annotations.PostOsemDelete;
import org.kwince.contribs.osem.annotations.PostOsemRead;
import org.kwince.contribs.osem.annotations.PostOsemSave;
import org.kwince.contribs.osem.annotations.PreOsemDelete;
import org.kwince.contribs.osem.annotations.PreOsemRead;
import org.kwince.contribs.osem.annotations.PreOsemSave;
import org.kwince.contribs.osem.common.ClientWrapper;
import org.kwince.contribs.osem.common.WeakCache;
import org.kwince.contribs.osem.event.EventDispatcher;
import org.kwince.contribs.osem.exceptions.OsemException;
import org.kwince.contribs.osem.util.ReflectionUtil;
import org.kwince.contribs.osem.validation.Validator;

class OsemManagerImpl implements OsemManager {
	
	private static class Key{
		private Class<?> clazz;
		private String id;
		public Key(Class<?> clazz, String id) {
			super();
			this.clazz = clazz;
			this.id = id;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (clazz == null) {
				if (other.clazz != null)
					return false;
			} else if (!clazz.equals(other.clazz))
				return false;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}
	}
	
	private EventDispatcher dispatcher;
	
	private ClientWrapper client;
	
	private Map<Class<?>,String> indexNames = new HashMap<Class<?>,String>();
	
	private WeakCache<Key, Object> consistencyCache = new WeakCache<Key, Object>();
	
	OsemManagerImpl (ClientWrapper client,EventDispatcher dispatcher) {
		this.client = client;
		this.dispatcher = dispatcher;
	}

	@Override
	public <E> E save(E entity) {
		return save(entity,false);
	}

	@Override
	public <E> E save(E entity,boolean refresh) {
		E result = null;
		dispatcher.publish(PreOsemSave.class, entity);
		Validator.validate(entity.getClass());
		try {
			result = mapping(entity, refresh);
			dispatcher.publish(PostOsemSave.class, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public <E> E read(String id, Class<E> clazz) {
		@SuppressWarnings("unchecked")
		E result = (E) consistencyCache.get(new Key(clazz, id));
		if(result != null) return result;
		
		dispatcher.publishId(PreOsemRead.class, id, clazz);
		Validator.validate(clazz);
		ensureIndex(clazz);
		
		try {
			result = readInternal(id, clazz, new HashMap<String, Object>());
			dispatcher.publish(PostOsemRead.class, result, clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		return result;
	}

	@Override
	public <E> void delete(E entity){
		delete(entity,false);
	}
	
	@Override
	public <E> void delete(E entity,boolean refresh) {
		Class<?> clazz = entity.getClass();
		dispatcher.publish(PreOsemDelete.class, entity);
		Validator.validate(clazz);
		ensureIndex(clazz);
		
		delete(entity, clazz, refresh);
		dispatcher.publish(PostOsemDelete.class, entity);
	}
	
	@Override
	public <E> SearchResult<E> find(String query, int from, int size, Class<E> clazz) {
		WrapperQueryBuilder wrapper = new WrapperQueryBuilder(query);
		return find(wrapper,from,size,clazz);
	}

	@Override
	public <E> long count(String query,Class<E> clazz){
		WrapperQueryBuilder wrapper = new WrapperQueryBuilder(query);
		return count(wrapper,clazz);
	}
	
	@Override
	public <E> SearchResult<E> find(QueryBuilder query, int from, int size, Class<E> clazz,String... sortFields) {
					
	    SearchRequestBuilder req = client.getClient().prepareSearch()
	    		.setIndices(getIndexName(clazz))
	    		.setTypes(getTypeName(clazz))
	    		.setQuery(query)
	    		.setSize(size)
	    		.setFrom(from);
	    
	    for(String s:sortFields)
	    	req.addSort(s, SortOrder.ASC);
	     
	    SearchResponse searchResponse = req.execute().actionGet();
	    	    		
		final LinkedList<E> list = new LinkedList<E>();
		for(SearchHit hit : searchResponse.getHits()) {
			@SuppressWarnings("unchecked")
			E obj = (E) consistencyCache.get(new Key(clazz, hit.getId()));
			
			if(obj == null){
				obj = createObject(hit.sourceAsMap(),clazz,hit.getId());
				consistencyCache.put(new Key(clazz, hit.getId()), obj);
			}
			
			list.add(obj);
		}
		return new SearchResult<E>(searchResponse.getHits().getTotalHits(),from,size,list);
	}

	@Override
	public <E> long count(QueryBuilder query,Class<E> clazz){
		CountResponse countResponse = client.getClient().prepareCount()
			.setIndices(getIndexName(clazz))
			.setTypes(getTypeName(clazz))
    		.setQuery(query)
    		.execute().actionGet();
		
		return countResponse.getCount();
	}
	
	private static class MapWrapper{
		private Object original;
		private Map<String,Object> map = new HashMap<String,Object>();
		private String id;
		private Class<?> clazz;
		@Override
		public String toString() {
			return "MapWrapper [map=" + map + ", id=" + id + ", clazz=" + clazz
					+ "]";
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MapWrapper other = (MapWrapper) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}
	}
	
	private Class<?> getFirstGenericClass(Type type){
		
		if(!(type instanceof ParameterizedType))
			throw new OsemException("Wrong List parameter type: "+type);
		
	    ParameterizedType aType = (ParameterizedType) type;
	    Type[] fieldArgTypes = aType.getActualTypeArguments();
    	//TODO list of lists
        Class<?> fieldArgClass = (Class<?>) fieldArgTypes[0];
        
		return fieldArgClass;
	}
	
	private Object parse(Object entity,Type type,Map<String,Object> cache, String id){
				
		if(entity == null)return null;
		if(type == String.class
				|| type == Integer.class || type == Integer.TYPE
				|| type == Long.class || type == Long.TYPE
				|| type == Float.class || type == Float.TYPE
				|| type == Double.class || type == Double.TYPE)
			return entity;

		if(entity instanceof List){
	        Class<?> fieldArgClass = getFirstGenericClass(type);
	        
			List<?> l = (List<?>) entity;
			List<Object> r = new ArrayList<Object>(l.size());
			
			for(Object obj:l)
				r.add(parse(obj,fieldArgClass,cache,null));
			
			return r;
		}
		
		if(entity.getClass() == String.class){
			if(cache.containsKey(entity))
				return cache.get(entity);
			
			Object obj = readInternal((String)entity, (Class<?>)type, cache);
			
			cache.put((String)entity, obj);
			
			return obj;
		}
		
		if(entity instanceof Map && type != Map.class){
			@SuppressWarnings("unchecked")
			Map<String,Object> m = (Map<String, Object>) entity;
			
			try {
				Class<?> clazz = (Class<?>) type;
				Loader loader = null;
				Object ret;
				
				if(ReflectionUtil.getAnnotatedFileds(clazz, Lazy.class).isEmpty()){
					Constructor<?> constructor = clazz.getDeclaredConstructor();
					constructor.setAccessible(true);
					ret = constructor.newInstance();
				}else {
					loader = new Loader(clazz,this);
					ret = Enhancer.create(clazz, new Class[]{LazyAccessor.class}, loader);
				}
				
				if(id!=null)
					cache.put(id, ret);
				
				for(Field f:ReflectionUtil.getFields(clazz)){
					Type t = f.getGenericType();
					Object e = m.get(f.getName());
										
					if(f.isAnnotationPresent(Lazy.class)){
						if(e instanceof List){
							@SuppressWarnings({ "rawtypes", "unchecked" })
							LazyList l = new LazyList((List)e, this, getFirstGenericClass(t)); 
							f.set(ret, l);
						}else{
							loader.toLoad(f.getName(),(String)e);
						}
					} else {
						f.set(ret, parse(e, t, cache, null));
					}
				}

				if(id!=null)
					ReflectionUtil.setId(ret, id);
				return ret;
			} catch (IllegalAccessException e) {
				throw new OsemException("Can't create instance", e);
			} catch (SecurityException e) {
				throw new OsemException("Can't create instance", e);
			} catch (IllegalArgumentException e) {
				throw new OsemException("Can't create instance", e);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} 
		}
		//TODO deserialize maps
		throw new OsemException("Unexpected type: "+type);
	}
	
	@SuppressWarnings("unchecked")
	private <E> E createObject(Map<String,Object> map,Class<E> clazz,String id){
		return (E) parse(map,clazz,new HashMap<String,Object>(),id);
	}
	
	private static String getLazyValue(Object entity,Field f){
		if(entity instanceof LazyAccessor){
			return ((LazyAccessor) entity).CGLIB$getLazyField(f.getName());
		}
		
		return null;
	}
	
	private static Class<?> getRealClass(Class<?> c){
		return Enhancer.isEnhanced(c) ? c.getSuperclass() : c;
	}
	
	private static Object split(Object entity, Map<String,Object> cache, boolean loadLazy){
		
		if(entity == null)return null;
		
		if(entity instanceof String)return entity;
		if(entity instanceof Number)return entity;
		if(loadLazy && (entity instanceof LazyList)){
			LazyList<?> l = (LazyList<?>) entity;
			List<Object> r = new ArrayList<Object>();
			for(Object obj:l.combined())
				r.add(split(obj,cache,loadLazy));
		}
		if(entity instanceof List){
			List<?> l = (List<?>) entity;
			List<Object> r = new ArrayList<Object>();
			for(Object obj:l)
				r.add(split(obj,cache,loadLazy));
			return r;
		}
		
		Class<?> clazz = getRealClass(entity.getClass());
		
		if(ReflectionUtil.getAnnotatedFileds(clazz, Id.class).isEmpty()){
			Map<String,Object> map = new HashMap<String, Object>();
			for(Field f:ReflectionUtil.getFields(clazz))
				try {
					String lazyValue = getLazyValue(entity,f);
					map.put(f.getName(), lazyValue != null ? lazyValue : split(f.get(entity),cache,loadLazy));
				} catch (IllegalArgumentException e) {
					throw new OsemException("Can't access field "+f.getName()+" in class "+entity.getClass(),e);
				} catch (IllegalAccessException e) {
					throw new OsemException("Can't access field "+f.getName()+" in class "+entity.getClass(),e);
				}
			return map;
		}

		MapWrapper tree = new MapWrapper();
		
		tree.id = ReflectionUtil.ensureId(entity);
		tree.original = entity;
		tree.clazz = clazz;
				
		if(cache.containsKey(tree.id))
			return cache.get(tree.id);
		cache.put(tree.id, tree);
		
		for(Field f:ReflectionUtil.getFields(clazz)){
			
			if(f.isAnnotationPresent(Id.class))continue;
			String lazyValue = getLazyValue(entity,f);
			
			try {
				Object obj = tree.map.put(f.getName(), lazyValue != null ? lazyValue : split(f.get(entity),cache,loadLazy));
				if(obj != null)
					throw new OsemException("Not unique field "+f.getName()+" in class "+entity.getClass().getName());
			} catch (IllegalArgumentException e) {
				throw new OsemException("Can't access field "+f.getName()+" in class "+entity.getClass(),e);
			} catch (IllegalAccessException e) {
				throw new OsemException("Can't access field "+f.getName()+" in class "+entity.getClass(),e);
			}
		}
		return tree;
	}
	
	@SuppressWarnings("unchecked")
	private static void toPlain(MapWrapper tree,Set<MapWrapper> plain){
		if(!plain.add(tree))return;
		
		for(Map.Entry<String, Object> e:new HashSet<Map.Entry<String,Object>>(tree.map.entrySet())){
			if(e.getValue() instanceof MapWrapper){
				toPlain((MapWrapper) e.getValue(),plain);
				tree.map.put(e.getKey(), ((MapWrapper)e.getValue()).id);
			} else if(e.getValue() instanceof List){
				List<?> l = (List<?>) e.getValue();
				//TODO list of lists
				if(!l.isEmpty() && l.get(0) instanceof MapWrapper){
					List<String> ids = new ArrayList<String>(l.size());
					for(MapWrapper m:(List<MapWrapper>)l){
						toPlain(m,plain);
						ids.add(m.id);
					}
					tree.map.put(e.getKey(), ids);
				}
			}
		}
	}
	
	private <E> E mapping(E entity, boolean refresh) throws Exception {
		Object tree = split(entity,new HashMap<String,Object>(),false);
		Set<MapWrapper> objects = new HashSet<MapWrapper>();
		toPlain((MapWrapper) tree,objects);
		
		BulkRequestBuilder builder = client.getClient()
				.prepareBulk().setRefresh(refresh);
		for(MapWrapper obj:objects){
			ensureIndex(obj.clazz);
			consistencyCache.put(new Key(obj.clazz,obj.id), obj.original);
			builder.add(client.getClient()
						.prepareIndex(getIndexName(obj.clazz), getTypeName(obj.clazz), obj.id)
						.setSource(obj.map)
						.setRefresh(refresh));
		}
		
		builder.execute().actionGet();
		return entity;
	}
	
	private <E> E readInternal(String id, Class<E> clazz, Map<String,Object> cache) {
		GetResponse result = client.getClient().prepareGet(getIndexName(clazz), getTypeName(clazz), id).setRefresh(true).execute().actionGet();
        if (!result.isExists()) {
        	return null;
        }
		
		@SuppressWarnings("unchecked")
		E entity = (E) parse(result.sourceAsMap(), clazz, cache, id);
		consistencyCache.put(new Key(clazz, id), entity);
		
		return entity;
	}
	
	private void delete(Object entity,Class<?> clazz,boolean refresh) {
		Object tree = split(entity,new HashMap<String,Object>(),true);
		Set<MapWrapper> objects = new HashSet<MapWrapper>();
		toPlain((MapWrapper) tree,objects);
		
		BulkRequestBuilder builder = client.getClient()
				.prepareBulk().setRefresh(refresh);
		for(MapWrapper obj:objects){
			consistencyCache.remove(new Key(obj.clazz, obj.id));
			builder.add(client.getClient()
						.prepareDelete(
								getIndexName(obj.clazz),
								getTypeName(obj.clazz),
								obj.id).setRefresh(refresh));
		}
		
		builder.execute().actionGet();
	}
	
	private static Map<Class<?>,String> primitives = new HashMap<Class<?>, String>();
	static{
		primitives.put(Integer.class, "integer");
		primitives.put(Long.class, "long");
		primitives.put(Float.class, "float");
		primitives.put(Double.class, "double");
		primitives.put(Boolean.class, "boolean");
		primitives.put(Integer.TYPE, "integer");
		primitives.put(Long.TYPE, "long");
		primitives.put(Float.TYPE, "float");
		primitives.put(Double.TYPE, "double");
		primitives.put(Boolean.TYPE, "boolean");
		primitives.put(String.class, "string");
	}
	
	private Map<String,Object> toType(String type){
		return toObjectType("type",type);
	}
	
	private Map<String,Object> toObjectType(String name,Object type){
		Map<String,Object> map = new HashMap<String, Object>();
		map.put(name, type);
		return map;
	}
	
	private Object toType(Type f){
		if(f instanceof ParameterizedType && Collection.class.isAssignableFrom((Class<?>)((ParameterizedType) f).getRawType()))
			return toType(getFirstGenericClass(f));
		Class<?> c = (Class<?>) f;
		if(primitives.containsKey(c))
			return toType(primitives.get(c));
		else if(ReflectionUtil.getAnnotatedFileds(c, Id.class).isEmpty())
			return toObjectType("properties",getMapping(c));
		else
			return toType("string");
	}
	
	private Object getMapping(Class<?> clazz){
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		for(Field f:ReflectionUtil.getFields(clazz)){
			map.put(f.getName(), toType(f.getGenericType()));
		}
		
		return map;
	}
	
	private void ensureIndex(Class<?> clazz) {
		if(indexNames.containsKey(clazz))return;
		
		client.getClient().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
		
		String name = getIndexName(clazz);
		
		if(!client.getClient().admin().indices()
				.prepareExists(name).execute().actionGet().exists()){
			
			client.getClient().admin().indices()
				.prepareCreate(name).execute().actionGet();
		}
		
		
		Map<String,Object> src = toObjectType(name, toObjectType("properties",getMapping(clazz)));
		
		client.getClient().admin().indices()
			.preparePutMapping(name)
			.setType(name)
			.setIndices(name)
			.setSource(src)
			.execute().actionGet();
		
		indexNames.put(clazz, name);
	}

	public String getIndexName(Class<?> clazz) {
		return getTypeName(clazz);
	}

	public String getTypeName(Class<?> clazz) {
		if(Enhancer.isEnhanced(clazz))
			clazz = clazz.getSuperclass();
		return clazz.getName().toLowerCase().trim().replace('.', '_');
	}
	
	public void close(){
		if(client!=null){
			client.close();
		}
		client.close();
	}

	@Override
	public void complete(Class<?> cl) {
		
	}

	@Override
	public void dropCache() {
		consistencyCache.clear();
	}
	
}
	