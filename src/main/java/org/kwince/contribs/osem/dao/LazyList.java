package org.kwince.contribs.osem.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class LazyList<E> implements List<E> {
	
	private List<String> toLoad;
	private List<E> elements;
	private Class<E> clazz;
	private OsemManager osem;
	
	public LazyList(List<String> toLoad,OsemManager osem,Class<E> clazz){
		this.toLoad = toLoad;
		elements = new ArrayList<E>(toLoad.size());
		List<E> c = Collections.nCopies(toLoad.size(), null);
		elements.addAll(c);
		this.osem = osem;
		this.clazz = clazz;
	}

	private LazyList(List<String> subList, List<E> subList2,OsemManager osem,Class<E> clazz) {
		toLoad = subList;
		elements = subList2;
		this.clazz = clazz;
		this.osem = osem;
	}

	@Override
	public int size() {
		return elements.size();
	}

	@Override
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		loadAll();
		return elements.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		loadAll();
		return elements.iterator();
	}

	@Override
	public Object[] toArray() {
		loadAll();
		return elements.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		loadAll();
		return elements.toArray(a);
	}

	@Override
	public boolean add(E e) {
		elements.add(e);
		toLoad.add(null);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		loadAll();
		return elements.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		loadAll();
		return elements.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		for(E e : c)
			add(e);
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		for(E e : c)
			add(index++,e);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		loadAll();
		return elements.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		loadAll();
		return elements.retainAll(c);
	}

	@Override
	public void clear() {
		toLoad.clear();
		elements.clear();
	}

	@Override
	public E get(int index) {
		return load(index);
	}

	/**
	 * Always return null!
	 */
	@Override
	public E set(int index, E element) {
		toLoad.set(index, null);
		elements.set(index, element);
		return null;
	}

	@Override
	public void add(int index, E element) {
		toLoad.add(index, null);
		elements.add(index, element);
	}

	/**
	 * Always return null!
	 */
	@Override
	public E remove(int index) {
		toLoad.remove(index);
		elements.remove(index);
		return null;
	}

	@Override
	public int indexOf(Object o) {
		loadAll();
		return elements.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		loadAll();
		return elements.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		loadAll();
		return elements.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		loadAll();
		return elements.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new LazyList<E>(toLoad.subList(fromIndex, toIndex),
				elements.subList(fromIndex, toIndex),osem,clazz);
	}

	private void loadAll() {
		for(int i=0;i<toLoad.size();i++)
			load(i);
	}
	
	private E load(int i){
		String s = toLoad.get(i); 
		if(s != null){
			E e = osem.read(s,clazz);
			elements.set(i, e);
		}
		return elements.get(i);
	}

	public List<Object> combined() {
		List<Object> lst = new ArrayList<Object>();
		for(int i=0;i<toLoad.size();i++)
			lst.add(toLoad.get(i) == null ? elements.get(i) : toLoad.get(i));
		return lst;
	}
	
}
