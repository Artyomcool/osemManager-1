package org.kwince.contribs.osem.common;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;


public class WeakCache<K,V> {

	private static class CacheReference<K,V> extends WeakReference<V>{
		
		private K key;

		public CacheReference(K key, V value, ReferenceQueue<V> q) {
			super(value, q);
			this.key = key;
		}
		
	}
	
	private Map<K,CacheReference<K,V>> cache = new HashMap<K, CacheReference<K,V>>(1000);
	private ReferenceQueue<V> queue = new ReferenceQueue<V>();
	
	@SuppressWarnings("unchecked")
	private void cleanReferences(){
		while(true){
			CacheReference<K,V> cr = (CacheReference<K, V>) queue.poll();
			if(cr == null)return;
			cache.remove(cr.key);
		}
	}
	
	public V get(K key) {
		CacheReference<K,V> cr = cache.get(key);
		return cr == null ? null : cr.get();
	}
	
	public void put(K key, V value) {
		cleanReferences();
		cache.put(key, new CacheReference<K, V>(key, value, queue));
	}

	public void remove(K key) {
		cleanReferences();
		cache.remove(key);
	}
	
}
