package at.jku.cg.sar.test.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.util.Vector;

class VectorTest {

	public static final double EPSILON = 10E-6;
	
	@Test
	void fromHeading() {
		Vector north = Vector.fromHeading(0.0);
		assertEquals( 0.0, north.x, EPSILON);
		assertEquals(-1.0, north.y, EPSILON);
		
		Vector east = Vector.fromHeading(90.0);
		assertEquals(1.0, east.x, EPSILON);
		assertEquals(0.0, east.y, EPSILON);		
		
		Vector south = Vector.fromHeading(180.0);
		assertEquals(0.0, south.x, EPSILON);
		assertEquals(1.0, south.y, EPSILON);
		
		Vector west = Vector.fromHeading(270.0);
		assertEquals(-1.0, west.x, EPSILON);
		assertEquals( 0.0, west.y, EPSILON);	
	}

}
