package at.jku.cg.sar.test.util;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cg.sar.util.MyPriorityQueue;

class MyPriorityQueueTest {

	private MyPriorityQueue<Integer> queue;
	
	@BeforeEach
	void init() {
		queue = new MyPriorityQueue<>();
	}
	
	@Test
	void insert() {
		queue.put(4, 4);
		queue.put(2, 2);
		queue.put(8, 8);
		queue.put(12, 12);
		queue.put(1, 1);		
		assertEquals(5, queue.size());
	}
	
	@Test
	void insertAndRemove() {
		queue.put(4, 4);
		queue.put(2, 2);
		queue.put(8, 8);
		queue.put(12, 12);
		queue.put(1, 1);		
		assertEquals(5, queue.size());

		assertEquals( 1, queue.pop());
		assertEquals( 2, queue.pop());
		assertEquals( 4, queue.pop());
		assertEquals( 8, queue.pop());
		assertEquals(12, queue.pop());		
		assertEquals(0, queue.size());
		
		assertNull(queue.pop());
	}
	
	

}
