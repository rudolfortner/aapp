package at.jku.cg.sar.pathfinder.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.FlightLeg;
import at.jku.cg.sar.trajectory.SimpleTrajectory;

public class SimulatedAnnealing extends PathFinder {

	private final Random random;
	
	public SimulatedAnnealing() {
		super(PathFinderType.DISCRETE_FULL);
		this.random = new Random();
	}

	@Override
	public List<PathFinderResult> solveFull(DroneDiscrete drone) {
		
		double T = 1.0;
		double Tmin = 0.001;
		double alpha = 0.99;
		
		// 1. Generate random solution
		List<SAStep> solution = drone.getProbabilities()
				.getValues()
				.stream()
				.filter(gv -> !drone.isVisited(gv.getX(), gv.getY()))
				.map(gv -> new SAStep(gv.getX(), gv.getY(), random.nextBoolean(), gv.getValue()))
				.collect(Collectors.toList());
		
		if(solution.size() == 0) return null;
				
		Collections.shuffle(solution);

		// 2. Calculate cost of random solution
		double cost = cost(solution, drone.getProbabilities());
		int variants = 0;
		
		while(T > Tmin) {
			if(Thread.interrupted()) return null;
			System.out.println("Simulated Annealing with T=%f\t%d solutions checked".formatted(T, variants));
			for(int i = 0; i < 100; i++) {
				// 3. Generate random neighbor solution
				List<SAStep> newSolution = neighbor(solution);
				variants++;
				
				// 4. Calculate cost of neighbor solution
				double newCost = cost(makePath(newSolution, drone.getX(), drone.getY()), drone.getProbabilities());
				double aP = acceptanceProbabilty(cost, newCost, T);
				
				// 5. Compare them
				if(newCost < cost || aP > random.nextDouble()) {
					solution = newSolution;
					cost = newCost;
				}
			}
			T = T * alpha;
		}

		List<PathFinderResult> result = new ArrayList<>();
		for(SAStep next : solution) {
			result.add(new PathFinderResult(next.x, next.y, next.fastFlight));
		}
		System.err.println("Simulated Annealing finished (checked %d solutions and got best %f".formatted(variants, cost));
		return result;
	}
	
	private double acceptanceProbabilty(double oldCost, double newCost, double T) {
		return Math.exp(-(newCost - oldCost) / T);
	}
	
	private List<SAStep> makePath(List<SAStep> solution, int currentX, int currentY){
		List<SAStep> path = new ArrayList<>(solution);
		path.add(0, new SAStep(currentX, currentY, false, 0.0));
		return solution;
	}
	
	private double cost(List<SAStep> solution, Grid<Double> map) {
		SimpleTrajectory planner = new SimpleTrajectory(settings);
		double accumProbs = 0.0;
		double accumTime  = 0.0;
		double accumArea  = 0.0;
		
		for(SAStep next : solution) {
			for(FlightLeg<?> leg : planner.next(new PathFinderResult(next.x, next.y, next.fastFlight))) {
				double dt = leg.getDuration();
				double oldProbs = accumProbs;
				
				accumTime += dt;
				if(leg.isScan()) {
					accumProbs += map.get(leg.getScanX(), leg.getScanY());
				}
				accumArea += (oldProbs + accumProbs) * dt / 2.0;
			}
		}

//		System.err.println("Area: %f \t\t Time: %f \t\t Probs: %f".formatted(accumArea, accumTime, accumProbs));
		return accumProbs*accumTime - accumArea;
	}
	
	private List<SAStep> neighbor(List<SAStep> solution) {
		List<SAStep> newSolution = new ArrayList<>(solution);
		
		if(random.nextDouble() <= 0.75) {
			// SIMPLE SWAP
			int swap0 = random.nextInt(solution.size());
			int swap1 = random.nextInt(solution.size());
			Collections.swap(newSolution, swap0, swap1);
		}else {
			// SPEED CHANGE
			int change = random.nextInt(solution.size());
			SAStep previous = newSolution.get(change);
			newSolution.set(change, new SAStep(previous.x, previous.y, !previous.fastFlight, previous.prob));
		}
		
		return newSolution;
	}

	@Override
	public String getName() {
		return "Simulated Annealing";
	}
	
	private final class SAStep{
		final int x, y;
		final boolean fastFlight;
		final double prob;
		
		public SAStep(int x, int y, boolean fastFlight, double prob) {
			super();
			this.x = x;
			this.y = y;
			this.fastFlight = fastFlight;
			this.prob = prob;
		}		
	}

}
