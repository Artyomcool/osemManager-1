package org.kwince.contribs.osem.common;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Weak cache based on {@link Map} with weak-referenced values.
 * Keys cleans automatically when values become weak-accessible.
 * @author Artyomcool
 *
 * @param <K> key-class
 * @param <V> value-class
 */
public class WeakCache<K,V> {

	/**
	 * Reference with Key from {@link WeakCache}
	 * @author Artyomcool
	 *
	 * @param <K> key-class
	 * @param <V> value-class
	 */
	private static class CacheReference<K,V> extends WeakReference<V>{
		
		private K key;

		public CacheReference(K key, V value, ReferenceQueue<V> q) {
			super(value, q);
			this.key = key;
		}
		
	}
	
	/**
	 * Map for storing keys/values
	 */
	private Map<K,CacheReference<K,V>> cache = new HashMap<K, CacheReference<K,V>>(1000);
	
	/**
	 * Queue for tracking accessibility
	 */
	private ReferenceQueue<V> queue = new ReferenceQueue<V>();
	
	/**
	 * Cleans keys with weak-accessible values
	 */
	@SuppressWarnings("unchecked")
	private void cleanReferences(){
		while(true){
			CacheReference<K,V> cr = (CacheReference<K, V>) queue.poll();
			if(cr == null)return;
			cache.remove(cr.key);
		}
	}
	
	/**
	 * @see {@link Map#get(Object)}
	 * 
	 * @param key
	 * @return
	 */
	public V get(K key) {
		CacheReference<K,V> cr = cache.get(key);
		return cr == null ? null : cr.get();
	}

	/**
	 * @see {@link Map#put(Object, Object)}
	 * 
	 * @param key
	 * @return
	 */
	public void put(K key, V value) {
		cleanReferences();
		cache.put(key, new CacheReference<K, V>(key, value, queue));
	}

	/**
	 * @see {@link Map#remove(Object)}
	 * @param key
	 */
	public void remove(K key) {
		cleanReferences();
		cache.remove(key);
	}

	/**
	 * Clears cache
	 */
	public void clear() {
		cache.clear();
	}
	
}
