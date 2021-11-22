package at.jku.cg.sar.pathfinder.apf;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.SimpleTrajectory;

public class AttractionApproachGradient extends PathFinder {

	/**
	 * Based on AttractionApproach but using the gradient (prob/time) to calculate Attraction Map"
	 */
	public AttractionApproachGradient() {
		super(PathFinderType.DISCRETE_ITERATIVE);
	}
	
	public static Grid<Double> createGradientMap(DroneDiscrete drone, SimulatorSettings settings, int positionX, int positionY){
		Grid<Double> G = new Grid<>(drone.getWidth(), drone.getHeight(), 0.0);
		
		for(int x = 0; x < drone.getWidth(); x++) {
			for(int y = 0; y < drone.getHeight(); y++) {
				if(drone.isVisited(x, y)) continue;
				SimpleTrajectory planner = new SimpleTrajectory(settings);
				planner.next(new PathFinderResult(drone.getPreviousX(), drone.getPreviousY(), false));
				planner.next(new PathFinderResult(drone.getX(), drone.getY(), false));
				
				FlightPath<WorldFlightLeg> flightPath = new FlightPath<>();
				flightPath.appendLegs(planner.next(new PathFinderResult(x, y, false)));
				
				double duration = flightPath.getDuration();
				double gradient = drone.getProbability(x, y) / duration;
				G.set(x, y, gradient);
			}
		}
		return G;
	}

	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		if(drone.visitedAll()) return null;

		Grid<Double> G = createGradientMap(drone, settings,drone.getX(), drone.getY());
		GridValue<Double> max = AttractionApproach.selectPoint(G, drone.getX(), drone.getY());
		
		return new PathFinderResult(max.getX(), max.getY(), false);
	}

	@Override
	public String getName() {
		return "Gradient Approach";
	}

}
