package at.jku.cg.sar.test.sim.flightpath;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.sim.flightpath.FlightLeg;
import at.jku.cg.sar.sim.flightpath.GridFlightLeg;

public class FlightLegTest {

	
	@Test
	public void checkColinearTest() {

		FlightLeg<?> leg0, leg1;
		boolean result = true;
		assertTrue(result);
		
		leg0 = new GridFlightLeg(0, 0, 0, 1, 0);
		leg1 = new GridFlightLeg(0, 1, 0, 0, 0);		
		result = FlightLeg.checkColinear(leg0, leg1);
		assertTrue(result);
		
		
		leg0 = new GridFlightLeg(0, 0, 1, 0, 0);
		leg1 = new GridFlightLeg(0, 0, 0, 1, 0);		
		result = FlightLeg.checkColinear(leg0, leg1);
		assertFalse(result);
		
		
		
		// Same heading but NOT colinear
		leg0 = new GridFlightLeg(0, 0, 1, 1, 0);
		leg1 = new GridFlightLeg(3, 2, 4, 3, 0);
		assertTrue(leg0.getHeading() == leg1.getHeading());
		result = FlightLeg.checkColinear(leg0, leg1);
		assertFalse(result);
		
		// TODO Maybe other tests as well? More corner cases?
	}
	
	
	
}
