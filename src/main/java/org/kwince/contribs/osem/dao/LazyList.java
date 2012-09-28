package org.kwince.contribs.osem.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Implementation of {@link List} for lazy access
 * @author Artyomcool
 *
 * @param <E>
 */
class LazyList<E> implements List<E> {
	
	/**
	 * Ids for objects that wasn't accessed 
	 */
	private List<String> toLoad;
	
	/**
	 * Actual elements 
	 */
	private List<E> elements;
	
	/**
	 * Class to read objects from OsemManager
	 */
	private Class<E> clazz;
	
	/**
	 * Manager to read objects
	 */
	private OsemManager osem;
	
	/**
	 * Main constructor
	 * @param toLoad ids to load
	 * @param osem manager to read objects
	 * @param clazz type of objects
	 */
	public LazyList(List<String> toLoad,OsemManager osem,Class<E> clazz){
		this.toLoad = toLoad;
		elements = new ArrayList<E>(toLoad.size());
		List<E> c = Collections.nCopies(toLoad.size(), null);
		elements.addAll(c);
		this.osem = osem;
		this.clazz = clazz;
	}

	/**
	 * Internal constructor for sublists
	 * @param subList ids
	 * @param subList2 elements
	 * @param osem manager
	 * @param clazz class
	 */
	private LazyList(List<String> subList, List<E> subList2,OsemManager osem,Class<E> clazz) {
		toLoad = subList;
		elements = subList2;
		this.clazz = clazz;
		this.osem = osem;
	}

	/**
	 * @see List#size()
	 */
	@Override
	public int size() {
		return elements.size();
	}


	/**
	 * @see List#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	/**
	 * <i>Note:</i> loads all elements before searching
	 * @see List#contains(Object)
	 */
	@Override
	public boolean contains(Object o) {
		loadAll();
		return elements.contains(o);
	}

	/**
	 * <i>Note:</i> loads all elements before iteration
	 * @see List#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		loadAll();
		return elements.iterator();
	}

	/**
	 * <i>Note:</i> loads all elements before making array
	 * @see List#toArray()
	 */
	@Override
	public Object[] toArray() {
		loadAll();
		return elements.toArray();
	}

	/**
	 * <i>Note:</i> loads all elements before making array
	 * @see List#toArray(Object[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		loadAll();
		return elements.toArray(a);
	}

	/**
	 * @see List#add(Object)
	 */
	@Override
	public boolean add(E e) {
		elements.add(e);
		toLoad.add(null);
		return true;
	}

	/**
	 * <i>Note:</i> loads all elements before removing
	 * @see List#remove(Object)
	 */
	@Override
	public boolean remove(Object o) {
		loadAll();
		return elements.remove(o);
	}


	/**
	 * <i>Note:</i> loads all elements before searching
	 * @see List#containsAll(Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		loadAll();
		return elements.containsAll(c);
	}

	/**
	 * @see List#addAll(Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		for(E e : c)
			add(e);
		return true;
	}

	/**
	 * @see List#addAll(int, Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		for(E e : c)
			add(index++,e);
		return true;
	}

	/**
	 * <i>Note:</i> loads all elements before removing
	 * @see List#removeAll(Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		loadAll();
		return elements.removeAll(c);
	}

	/**
	 * <i>Note:</i> loads all elements before retaining
	 * @see List#retainAll(Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		loadAll();
		return elements.retainAll(c);
	}
	
	/**
	 * @see List#clear()
	 */
	@Override
	public void clear() {
		toLoad.clear();
		elements.clear();
	}

	/**
	 * Loads element if needed
	 * @see {@link List#get(int)}
	 */
	@Override
	public E get(int index) {
		return load(index);
	}

	/**
	 * Always return null!
	 * @see {@link List#set(int, Object)}
	 */
	@Override
	public E set(int index, E element) {
		toLoad.set(index, null);
		elements.set(index, element);
		return null;
	}

	/**
	 * @see List#add(int,Object)
	 */
	@Override
	public void add(int index, E element) {
		toLoad.add(index, null);
		elements.add(index, element);
	}

	/**
	 * Always return null!
	 * @see {@link List#remove(int)}
	 */
	@Override
	public E remove(int index) {
		toLoad.remove(index);
		elements.remove(index);
		return null;
	}

	/**
	 * <i>Note:</i> loads all elements before searching
	 * @see List#indexOf(Object)
	 */
	@Override
	public int indexOf(Object o) {
		loadAll();
		return elements.indexOf(o);
	}

	/**
	 * <i>Note:</i> loads all elements before searching
	 * @see List#lastIndexOf(Object)
	 */
	@Override
	public int lastIndexOf(Object o) {
		loadAll();
		return elements.lastIndexOf(o);
	}

	/**
	 * <i>Note:</i> loads all elements before iteration
	 * @see List#listIterator()
	 */
	@Override
	public ListIterator<E> listIterator() {
		loadAll();
		return elements.listIterator();
	}

	/**
	 * <i>Note:</i> loads all elements before iteration
	 * @see List#listIterator(int)
	 */
	@Override
	public ListIterator<E> listIterator(int index) {
		loadAll();
		return elements.listIterator(index);
	}

	/**
	 * @see List#subList(int,int)
	 */
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new LazyList<E>(toLoad.subList(fromIndex, toIndex),
				elements.subList(fromIndex, toIndex),osem,clazz);
	}

	/**
	 * Loads all elements
	 */
	private void loadAll() {
		for(int i=0;i<toLoad.size();i++)
			load(i);
	}

	/**
	 * Loads element, if it isn't already loaded
	 * @param i index to load
	 * @return loaded object
	 */
	private E load(int i){
		String s = toLoad.get(i); 
		if(s != null){
			E e = osem.read(s,clazz);
			elements.set(i, e);
		}
		return elements.get(i);
	}

	/**
	 * Combines loaded and not loaded elements. Not loaded elements represented as an Id-string.
	 * @return combined list
	 */
	public List<Object> combined() {
		List<Object> lst = new ArrayList<Object>();
		for(int i=0;i<toLoad.size();i++)
			lst.add(toLoad.get(i) == null ? elements.get(i) : toLoad.get(i));
		return lst;
	}
	
}
