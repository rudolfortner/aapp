package at.jku.cg.sar.util;

import java.util.Comparator;
import java.util.PriorityQueue;

public class MyPriorityQueue<T> {

	private final PriorityQueue<PriorityQueueElement<T>> queue;
	
	public MyPriorityQueue() {
		Comparator<PriorityQueueElement<T>> comparator = new Comparator<PriorityQueueElement<T>>() {
			@Override
			public int compare(PriorityQueueElement<T> o1, PriorityQueueElement<T> o2) {
				return Double.compare(o1.getOrdering(), o2.getOrdering());
			}
		};
		this.queue = new PriorityQueue<>(comparator);
	}
	
	
	public void put(double ordering, T value) {
		this.queue.add(new PriorityQueueElement<T>(ordering, value));
	}

	public T pop() {
		PriorityQueueElement<T> elem = queue.poll();
		if(elem != null) return elem.getValue();
		return null;
	}
	
	public T peek() {
		PriorityQueueElement<T> elem = queue.peek();
		if(elem != null) return elem.getValue();
		return null;
	}
	
	
	public int size() {
		return queue.size();
	}
	
	private final class PriorityQueueElement<ElemType> {
		final double ordering;
		final ElemType value;
		
		public PriorityQueueElement(double ordering, ElemType value) {
			this.ordering = ordering;
			this.value = value;
		}

		public double getOrdering() {
			return ordering;
		}

		public ElemType getValue() {
			return value;
		}
	}
}
