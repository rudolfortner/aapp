package at.jku.cg.sar.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ListWrapper<ElemType> implements List<ElemType>, Comparable<ListWrapper<ElemType>> {

	private final List<ElemType> list;
	
	public ListWrapper(List<ElemType> list) {
		this.list = list;
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<ElemType> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean add(ElemType e) {
		return list.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends ElemType> c) {
		return list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends ElemType> c) {
		return list.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public ElemType get(int index) {
		return list.get(index);
	}

	@Override
	public ElemType set(int index, ElemType element) {
		return list.set(index, element);
	}

	@Override
	public void add(int index, ElemType element) {
		list.add(index, element);
	}

	@Override
	public ElemType remove(int index) {
		return list.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<ElemType> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<ElemType> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public List<ElemType> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public int compareTo(ListWrapper<ElemType> o) {
		return Integer.compare(size(), o.size());
	}

}
