package at.jku.cg.sar.sim;

import at.jku.cg.sar.main.Configuration;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.sim.drone.DroneContinous;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.GridFlightLeg;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;
import at.jku.cg.sar.util.CourseUtil;
import at.jku.cg.sar.world.World;

public class SimpleSimulator extends Simulator {
	
	public SimpleSimulator(Configuration config) {
		super(config);
	}


	@Override
	public void runContinousIterative(SimulationResult result, World world, PathFinder finder, TrajectoryPlanner planner) {
		double cellSize = config.getSettings().getCellSize();
		double startX = (config.getStartX() + 0.5) * cellSize;
		double startY = (config.getStartY() + 0.5) * cellSize;
		
		FlightPath<WorldFlightLeg> trajectoryResult = new FlightPath<>();
		
		DroneContinous drone = new DroneContinous(world.getProbabilities(), cellSize, startX, startY);
		/*
		 * Needed for not missing the probability that is collected at the starting position
		 * Otherwise it does not get count in evaluateFlightPath() of our AUPC metric
		 * Then the two graphs would not have the same probability sum on the y-axis
		 */
		WorldFlightLeg ghost = new WorldFlightLeg(drone.getX(), drone.getY(), -0.00001, drone.getX(), drone.getY(), 0.0, true, drone.getProbability(), cellSize);
		trajectoryResult.appendLeg(ghost);
		
		double dt = 1.0;
		while(true) {
			// Retrieve next heading from PathFinder
			PathFinderResult next = runPathFinder(result, finder, drone);

			// PathFinder finished
			if(next == null) break;
			
			// Calculate next position
			double speed = config.getSettings().getSpeedScan();		// TODO SLOW AND FAST ?
			double distance = dt * speed;
			
			double maxHeadingChange = CourseUtil.maxHeadingChange(config.getSettings().getAcceleration(), config.getSettings().getDeceleration(), speed, dt);
			double heading = CourseUtil.changeHeading(drone.getTrack(), next.getHeading(), maxHeadingChange);
			double direction = Math.toRadians(heading - 90.0);
			double dx = distance * Math.cos(direction);
			double dy = distance * Math.sin(direction);
			
			// Move simulated Drone to next position
			drone.visit(drone.getX() + dx, drone.getY() + dy);
			
			// Add Leg for trajectory
			WorldFlightLeg leg = new WorldFlightLeg(drone.getPreviousX(), drone.getPreviousY(), speed, drone.getX(), drone.getY(), speed, true, drone.getProbability(), cellSize);
			trajectoryResult.appendLeg(leg);
		}		
		
		// Save Simulation Results		
		result.setTrajectoryPath(trajectoryResult);
	}

	@Override
	public void runDiscreteIterative(SimulationResult result, World world, PathFinder finder, TrajectoryPlanner planner) {		
		FlightPath<GridFlightLeg> pathResult = new FlightPath<>();	
		FlightPath<WorldFlightLeg> trajectoryResult = new FlightPath<>();		
		
		// SIMULATION
		DroneDiscrete drone = new DroneDiscrete(world.getProbabilities(), config.getStartX(), config.getStartY());
		
		if(planner != null) trajectoryResult.appendLegs(planner.next(new PathFinderResult(config.getStartX(), config.getStartY(), true)));
		
		while(true) {
			// Retrieve next grid position from PathFinder
			PathFinderResult next = runPathFinder(result, finder, drone);
			if(planner != null) trajectoryResult.appendLegs(runTrajectoryPlanner(result, planner, next));
			
			// PathFinder finished
			if(next == null) break;
			
			// Pack Results
			double speed = next.isFastFlight() ? config.getSettings().getSpeedFast() : config.getSettings().getSpeedScan();
			pathResult.appendLeg(new GridFlightLeg(drone.getX(), drone.getY(), speed, next.getPosX(), next.getPosY(), speed,
					true, next.getPosX(), next.getPosY(), config.getSettings().getCellSize()));
//			System.err.println((planner == null) + "   " + (trajectoryResult == null) + "   " + (next==null));
			
			
			// Move simulated Drone to next GridPosition
			drone.visit(next.getPosX(), next.getPosY());		
		}
		
		// Save Simulation Results
		result.setFinderPath(pathResult);		
		if(planner != null) result.setTrajectoryPath(trajectoryResult);
	}


	@Override
	public void runDiscreteFull(SimulationResult result, World world, PathFinder finder, TrajectoryPlanner planner) {
		FlightPath<GridFlightLeg> pathResult = new FlightPath<>();	
		FlightPath<WorldFlightLeg> trajectoryResult = new FlightPath<>();
		
		DroneDiscrete drone = new DroneDiscrete(world.getProbabilities(), config.getStartX(), config.getStartY());
		
		if(planner != null) trajectoryResult.appendLegs(planner.next(new PathFinderResult(config.getStartX(), config.getStartY(), true)));
		
		for(PathFinderResult next : finder.solveFull(drone)) {
			// Pack Results
			double speed = next.isFastFlight() ? config.getSettings().getSpeedFast() : config.getSettings().getSpeedScan();
			pathResult.appendLeg(new GridFlightLeg(drone.getX(), drone.getY(), speed, next.getPosX(), next.getPosY(), speed,
					true, next.getPosX(), next.getPosY(), config.getSettings().getCellSize()));
//						System.err.println((planner == null) + "   " + (trajectoryResult == null) + "   " + (next==null));
			if(planner != null) trajectoryResult.appendLegs(runTrajectoryPlanner(result, planner, next));
			
			
			// Move simulated Drone to next GridPosition
			drone.visit(next.getPosX(), next.getPosY());
		}
		if(planner != null) trajectoryResult.appendLegs(runTrajectoryPlanner(result, planner, null));
		
		// Save Simulation Results
		result.setFinderPath(pathResult);		
		if(planner != null) result.setTrajectoryPath(trajectoryResult);
	}

}
