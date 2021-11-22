package at.jku.cg.sar.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import at.jku.cg.sar.main.Configuration;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.sim.drone.DroneBase;
import at.jku.cg.sar.sim.drone.DroneContinous;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;
import at.jku.cg.sar.world.World;

public abstract class Simulator {
	
	private final ExecutorService service = Executors.newSingleThreadExecutor();
	
	protected final Configuration config;

	public Simulator(Configuration configuration) {
		this.config = configuration;
	}

	public SimulationResult run(World world, PathFinder finder, TrajectoryPlanner planner) {
		if(world == null) throw new IllegalArgumentException();
		if(config.getStartX() < 0 || config.getStartX() >= world.getWidth()
				|| config.getStartY() < 0 || config.getStartY() >= world.getHeight()) throw new IllegalArgumentException();
		
		SimulationResult simResult = new SimulationResult(config, finder, planner);
		
		long startTime = System.currentTimeMillis();
		
		switch (finder.getType()) {
			case CONTINOUS_ITERATIVE:	runContinousIterative(simResult, world, finder, planner); break;
			case DISCRETE_ITERATIVE:	runDiscreteIterative(simResult, world, finder, planner); break;
			case DISCRETE_FULL:			runDiscreteFull(simResult, world, finder, planner); break;
		}
		
		long endTime = System.currentTimeMillis();
		simResult.setSimulationTime((endTime - startTime));
		
		return simResult;
	}
	
	public abstract void runContinousIterative(SimulationResult result, World world, PathFinder finder, TrajectoryPlanner planner);
	public abstract void runDiscreteIterative(SimulationResult result, World world, PathFinder finder, TrajectoryPlanner planner);
	public abstract void runDiscreteFull(SimulationResult result, World world, PathFinder finder, TrajectoryPlanner planner);
		
	public PathFinderResult runPathFinder(SimulationResult simResult, PathFinder finder, DroneBase<?> drone) {
		FutureTask<PathFinderResult> future = new FutureTask<>(() -> {
			PathFinderResult result = null;
			long startTime = System.nanoTime();
			if(drone instanceof DroneDiscrete) result = finder.nextDiscrete((DroneDiscrete) drone);
			if(drone instanceof DroneContinous) result = finder.nextContinous((DroneContinous) drone);
			long endTime = System.nanoTime();
			simResult.pushExecutionTimeFinderNanos(endTime - startTime);
			return result;
		});

		
		service.submit(future);
		
		PathFinderResult result = null;
		try {
			if(config.getSettings().isUsePathFinderTimeout()) {
				result = future.get(config.getSettings().getPathFinderTimeout(), TimeUnit.MILLISECONDS);
			}else {
				result = future.get();
			}
		}catch (InterruptedException | TimeoutException e) {
			e.printStackTrace();
			future.cancel(true);
			System.err.println("PathFinder \"%s\" took too long".formatted(finder.getName()));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public List<WorldFlightLeg> runTrajectoryPlanner(SimulationResult simResult, TrajectoryPlanner planner, PathFinderResult next) {
		FutureTask<List<WorldFlightLeg>> future = new FutureTask<>(() -> {
			long startTime = System.nanoTime();
			List<WorldFlightLeg> legs = planner.next(next);
			long endTime = System.nanoTime();
			simResult.pushExecutionTimePlannerNanos(endTime - startTime);
			return legs;
		});
		
		service.submit(future);
		
		List<WorldFlightLeg> result = new ArrayList<>(0);
		try {
			if(config.getSettings().isUseTrajectoryPlannerTimeout()) {
				result = future.get(config.getSettings().getTrajectoryPlannerTimeout(), TimeUnit.MILLISECONDS);				
			}else {
				result = future.get();
			}
		}catch (InterruptedException | TimeoutException e) {
			e.printStackTrace();
			future.cancel(true);
			System.err.println("TrajectoryPlanner \"%s\" took too long".formatted(planner.getName()));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public Configuration getConfiguration() {
		return this.config;
	}
}
