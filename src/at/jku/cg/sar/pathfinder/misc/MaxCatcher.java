package at.jku.cg.sar.pathfinder.misc;

import java.util.ArrayList;
import java.util.List;

import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.drone.DroneDiscrete;

public class MaxCatcher extends PathFinder {

	public MaxCatcher() {
		super(PathFinderType.DISCRETE_ITERATIVE);		
	}

	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {

		List<GridValue<Double>> points = new ArrayList<>();
		
		for(int x = 0; x < drone.getWidth(); x++) {
			for(int y = 0; y < drone.getHeight(); y++) {
				
				double prob = drone.getProbability(x, y);
				points.add(new GridValue<Double>(x, y, prob));
			}
		}
		
		points.sort((p0, p1) -> {
			int sort = 0;
			
			if(sort == 0) {
				// Sort by prob
				sort = Double.compare(p1.getValue(), p0.getValue());
			}
			if(sort == 0) {
				double dist0 = distance(drone.getX(), p0.getX(), drone.getY(), p0.getX());
				double dist1 = distance(drone.getX(), p1.getX(), drone.getY(), p1.getX());
				sort = Double.compare(dist1, dist0);
			}
			return sort;
		});
		
		for(GridValue<Double> p : points) {
			if(!drone.isVisited(p.getX(), p.getY())) return new PathFinderResult(p.getX(), p.getY(), false);
		}
		
		return null;
	}
	
	private double distance(int x0, int y0, int x1, int y1) {
		int dx = x0 - x1;
		int dy = y0 - y1;
		
		return Math.sqrt(dx*dx + dy*dy);
	}

	@Override
	public String getName() {
		return "Max Catcher";
	}

}
