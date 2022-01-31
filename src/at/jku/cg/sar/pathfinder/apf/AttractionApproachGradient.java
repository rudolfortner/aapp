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

/**
 * Based on {@link AttractionApproach} but using the gradient (probability/time) to calculate an attraction map
 * @author ortner
 */
public class AttractionApproachGradient extends PathFinder {

	public AttractionApproachGradient() {
		super(PathFinderType.DISCRETE_ITERATIVE);
	}
	
	/**
	 * Creates an attraction map based on the gradient (probability over time)
	 * A {@link SimpleTrajectory} instance is used to estimate the drones travel duration for each particular cell.
	 * @param drone Current state of the drone (position and probability map)
	 * @param settings Holds parameters concerning velocities, accelerations and the grid dimensions
	 * @param positionX
	 * @param positionY
	 * @return
	 */
	public static Grid<Double> createGradientMap(DroneDiscrete drone, SimulatorSettings settings){
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

		Grid<Double> G = createGradientMap(drone, settings);
		GridValue<Double> max = AttractionApproach.selectPoint(G, drone.getX(), drone.getY());
		
		return new PathFinderResult(max.getX(), max.getY(), false);
	}

	@Override
	public String getName() {
		return "Gradient Approach";
	}

}
