package at.jku.cg.sar.test.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.util.Curve;

class CurveTest {

	@Test
	void test() {
		Curve curve = new Curve();
		curve.addPoint(0.0, 0.0);
		curve.addPoint(1.0, 1.0);
		
		assertEquals(0.5, curve.evaluate(0.5, false));
		
	}

}
