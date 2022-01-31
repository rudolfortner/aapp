package at.jku.cg.sar.pathfinder.apf;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.GridFlightLeg;

/**
 * Based on {@link AttractionApproach} but using heading to calculate Attraction
 * @author ortner
 */
public class AttractionApproachHeading extends PathFinder {

	public AttractionApproachHeading() {
		super(PathFinderType.DISCRETE_ITERATIVE);
	}
	
	
	/**
	 * Creates an attraction map based on probability, distance and heading difference
	 * @param drone Current state of the drone (position and probability map)
	 * @return Attraction map
	 */
	public static Grid<Double> createHeadingMap(DroneDiscrete drone){
		Grid<Double> H = new Grid<>(drone.getWidth(), drone.getHeight(), 0.0);
		
		// Current heading of the drone (in discretized grid space)
		double heading = (new GridFlightLeg(drone.getPreviousX(), drone.getPreviousY(), drone.getX(), drone.getY(), 0.0)).getHeading();
		
		for(int x = 0; x < drone.getWidth(); x++) {
			for(int y = 0; y < drone.getHeight(); y++) {
				if(x == drone.getX() && y == drone.getY()) continue;
				if(drone.isVisited(x, y)) continue;
				
				double distance = Math.sqrt(Math.pow(x-drone.getX(), 2.0) + Math.pow(y-drone.getY(), 2.0));
				
				double h = (new GridFlightLeg(drone.getX(), drone.getY(), x, y, 0.0)).getHeading();
				double diff = Math.abs(heading - h);
				if(diff > 180.0) diff = 360.0 - diff;
				
				double headingWeight = Math.cos(Math.toRadians(diff / 2.0));
				double value = drone.getProbability(x, y) * headingWeight / distance;
				H.set(x, y, value);
			}
		}
		return H;
	}


	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		if(drone.visitedAll()) return null;

		Grid<Double> G = createHeadingMap(drone);
		GridValue<Double> max = AttractionApproach.selectPoint(G, drone.getX(), drone.getY());	
		
		return new PathFinderResult(max.getX(), max.getY(), false);
	}

	@Override
	public String getName() {
		return "Attraction Approach Heading";
	}

}
