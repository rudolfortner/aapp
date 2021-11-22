package at.jku.cg.sar.pathfinder.misc;

import java.util.Random;

import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.drone.DroneDiscrete;

public class RandomFinder extends PathFinder{

	public RandomFinder() {
		super(PathFinderType.DISCRETE_ITERATIVE);
	}

	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		if(drone.visitedAll()) return null;
		
		Random random = new Random();
		int nextX = drone.getX();
		int nextY = drone.getY();
		
		while(drone.isVisited(nextX, nextY)) {
			nextX = random.nextInt(drone.getWidth());
			nextY = random.nextInt(drone.getHeight());
		}
		return new PathFinderResult(nextX, nextY, false);
	}

	@Override
	public String getName() {
		return "Random Finder";
	}

}
