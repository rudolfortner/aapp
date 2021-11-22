package at.jku.cg.sar.test.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.util.List;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.gui.LineGraphViewer;
import at.jku.cg.sar.gui.graphics.LineGraphDataSet;
import at.jku.cg.sar.util.CourseUtil;

class CourseUtilTest {

	@Test
	void testMaximumHeadingChange() {
		
		LineGraphDataSet heading = new LineGraphDataSet("headingChange", Color.RED);
		
		for(double speed = 0.0; speed <= 40.0; speed += 0.1) {
			double h = CourseUtil.maxHeadingChange(1.0, 1.5, speed);
			heading.addPoint(speed, h);
		}
		
		new LineGraphViewer(List.of(heading));
		
		try {
			Thread.sleep(10_000);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void testDeltaTimeNeeded() {
		double a = 1.0;
		double b = 1.5;
		double speed = 10.0;

		LineGraphDataSet setHeading0 = new LineGraphDataSet("Heading 0", Color.GREEN);
		LineGraphDataSet setHeading1 = new LineGraphDataSet("Heading 1", Color.BLUE);
		LineGraphDataSet setDiff = new LineGraphDataSet("Difference", Color.RED);
		
		for(double dt = 0.0; dt <= 5; dt += 0.05) {
			double heading0 = Math.atan2(a*dt, speed-b*dt);
			double heading1 = dt * Math.atan2(a, speed-b);
			double diff = Math.abs(heading1 - heading0);

			setHeading0.addPoint(dt, heading0);
			setHeading1.addPoint(dt, heading1);
			setDiff.addPoint(dt, diff);
			
			System.err.println("%f: %f %f %f".formatted(dt, heading0, heading1, diff));
		}
		
		new LineGraphViewer(List.of(setHeading0, setHeading1, setDiff));
		
		try {
			Thread.sleep(10_000);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void changeHeadingTest() {
		double next;
		
		next = CourseUtil.changeHeading(160, 190, 10);
		assertEquals(170, next);		
		next = CourseUtil.changeHeading(160, 110, 10);
		assertEquals(150, next);
		
		// Now switch over 0/360
		next = CourseUtil.changeHeading(20, 340, 10);
		assertEquals(10, next);

		next = CourseUtil.changeHeading(5, 340, 10);
		assertEquals(355, next);
	}
	
	@Test
	void getDifferenceTest() {
		double diff0, diff1;
		
		diff0 = CourseUtil.getDifference(10, 60);
		diff1 = CourseUtil.getDifference(320, 10);
		assertEquals(50.0, diff0);
		assertEquals(50.0, diff1);
		
		diff0 = CourseUtil.getDifference(60, 10);
		diff1 = CourseUtil.getDifference(10, 320);
		assertEquals(-50.0, diff0);
		assertEquals(-50.0, diff1);
	}
	
	@Test
	void getDifferenceAbsTest() {
		double diff0, diff1;
		
		diff0 = CourseUtil.getDifferenceAbs(10, 60);
		diff1 = CourseUtil.getDifferenceAbs(320, 10);

		assertEquals(50.0, diff0);
		assertEquals(50.0, diff1);
	}

}
