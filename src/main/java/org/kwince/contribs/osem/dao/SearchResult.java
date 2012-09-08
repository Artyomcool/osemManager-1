package org.kwince.contribs.osem.dao;

import java.util.Collections;
import java.util.List;

public class SearchResult<E> {

	private final long total;
	private final int from;
	private final int size;
	private final List<E> result;
	
	public SearchResult(long total,int from,int size,List<E> result){
		this.total = total;
		this.from = from;
		this.size = size;
		this.result = result;
	}
	
	public long total(){
		return total;
	}
	
	public int from(){
		return from;
	}
	
	public int size(){
		return size;
	}
	
	public List<E> result(){
		return Collections.unmodifiableList(result);
	}
	
}
