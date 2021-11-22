package at.jku.cg.sar.pathfinder.apf;

import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.pathfinder.misc.GridFinder;
import at.jku.cg.sar.sim.drone.DroneDiscrete;

public class SimplePotentialField extends PathFinder {

	public SimplePotentialField() {
		super(PathFinderType.DISCRETE_ITERATIVE);
	}

	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		
		double forceX = 0.0, forceY = 0.0;
		for(int x = 0; x < drone.getWidth(); x++) {
			for(int y = 0; y < drone.getHeight(); y++) {
				
				double prob = drone.getProbability(x, y);
				if(drone.isVisited(x, y)) prob = 0.0;

				forceX += (x - drone.getX()) * prob;
				forceY += (y - drone.getY()) * prob;
			}
		}
		
		double headingRadians = Math.atan2(forceX, forceY);
//		double heading = Math.toDegrees(headingRadians);
//		System.err.println("Heading " + heading);
		
		double samplingDistance = 0.1;
		double distance = 0.5;
		
		while(true) {
			double dx = distance * Math.sin(headingRadians);
			double dy = distance * Math.cos(headingRadians);

			int newX = (int) Math.round(drone.getX() + dx);
			int newY = (int) Math.round(drone.getY() + dy);
			
			if(newX < 0 || newX >= drone.getWidth() || newY < 0 || newY >= drone.getHeight()) {
				GridFinder finder = new GridFinder(true, false);
				return finder.nextDiscrete(drone);
			}
			
			if(!drone.isVisited(newX, newY)) return new PathFinderResult(newX, newY, distance > 2.0);
			distance += samplingDistance;
		}
	}

	@Override
	public String getName() {
		return "Simple Potential Field";
	}

}
