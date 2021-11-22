package at.jku.cg.sar.pathfinder.vacuum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.jku.cg.sar.core.splitcontainer.SplitContainer;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneContinous;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;

/**
 * Based on an idea by Prof. Bimber
 * @author ortner
 *
 */
public class VacuumCleanerRadialChecker extends PathFinder {

	public VacuumCleanerRadialChecker() {
		super(PathFinderType.CONTINOUS_ITERATIVE);
	}
	

	@Override
	public PathFinderResult nextContinous(DroneContinous drone) {
		
		double remainingProbs = drone.getProbabilities().getValue();
//		System.err.println("Remaining Probs: %3.3f".formatted(remainingProbs));
		if(remainingProbs <= 0.0) return null;
		
		// ALWAYS PLAN ONE STEP AHEAD AND REPLAN
		List<RadialResult> results = replan(drone, settings);
		if(results.size() == 0) return fallback(drone, settings);
		RadialResult max = results.get(0);
		
		return new PathFinderResult(max.radial);
	}
	
	
	public static PathFinderResult fallback(DroneContinous drone, SimulatorSettings settings) {
		if(drone.visitedAll()) return null;
		
		List<SplitContainer> unvisited = drone.getProbabilities().getContainers().stream()
				.filter(gv -> gv.getValue() > 0.0)
				.collect(Collectors.toUnmodifiableList());
		
		
		SplitContainer max = null;
		double maxGradient = 0.0;
		for(SplitContainer c : unvisited) {
			double dist = distance(drone.getX(), drone.getY(), c.getCenterX(), c.getCenterY());
			double time = dist / settings.getSpeedScan();
			
			double gradient = c.getValue() / time;
			if(gradient > maxGradient) {
				max = c;
				maxGradient = gradient;
			}		
		}
		if(max == null) return null;
		double heading = (new WorldFlightLeg(drone.getX(), drone.getY(), max.getCenterX(), max.getCenterY(), 0.0)).getHeading();
		return new PathFinderResult(heading);
	}
	
	public static List<RadialResult> replan(DroneContinous drone, SimulatorSettings settings){
		
		List<RadialResult> results = new ArrayList<>(8);
		
		List<SplitContainer> unvisited = drone.getProbabilities().getContainers().stream()
				.filter(gv -> gv.getValue() > 0.0)
				.collect(Collectors.toUnmodifiableList());
		
		List<SplitContainer> closest = unvisited.stream()
				.sorted((c0, c1) -> {
					double dist0 = distance(drone.getX(), drone.getY(), c0.getCenterX(), c0.getCenterY());
					double dist1 = distance(drone.getX(), drone.getY(), c1.getCenterX(), c1.getCenterY());
					return Double.compare(dist0, dist1);
				})
				.limit(128)
				.collect(Collectors.toUnmodifiableList());
		List<SplitContainer> higest = unvisited.stream()
				.sorted((c0, c1) -> {
					return Double.compare(c0.getValue(), c1.getValue());
				})
				.limit(128)
				.collect(Collectors.toUnmodifiableList());
		
		
		Set<Double> radials = new HashSet<>();
		for(SplitContainer c : Stream.concat(closest.stream(), higest.stream()).collect(Collectors.toList())) {
			if((c.getCenterX() == drone.getX() && c.getCenterY() == drone.getY())) continue;
			double heading = (new WorldFlightLeg(drone.getX(), drone.getY(), c.getCenterX(), c.getCenterY(), 0)).getHeading();
			radials.add(heading);
		}
		System.err.println("Got %d radials".formatted(radials.size()));
		for(double radial : new ArrayList<>(radials)) {
			results.add(evaluateRadial(drone, settings, radial));
		}
		
		return results.stream()
				.sorted((res0, res1) -> Double.compare(res1.gradient, res0.gradient))
				.collect(Collectors.toUnmodifiableList());
	}
	
	
	public static RadialResult evaluateRadial(DroneContinous drone, SimulatorSettings settings, double radial) {
		
		List<SplitContainer> onRadial = onRadial(drone, radial, false);		
		double maxGradient = 0.0;
		double accumProb = 0.0;
		double accumTime = 0.0;
		
		double x = drone.getX(), y = drone.getY();
		for(SplitContainer c : onRadial) {
			double nextX = c.getCenterX(), nextY = c.getCenterY();
			double distance = distance(x, y, nextX, nextY);
			double time = distance / settings.getSpeedScan();
			
			accumProb += c.getValue();
			accumTime += time;
			double gradient = accumProb / accumTime;
			maxGradient = Math.max(maxGradient, gradient);												// Only fly fast to first valid cell
		}

		return new RadialResult(radial, maxGradient);
	}
	
	public static List<SplitContainer> onRadial(DroneContinous drone, double radial, boolean includeCurrent){
		double currentX = drone.getX(), currentY = drone.getY();
		
		List<SplitContainer> containers = new ArrayList<>();
		
		for(SplitContainer c : drone.getProbabilities().getContainers()) {
			if((c.getCenterX() == currentX && c.getCenterY() == currentY) && !includeCurrent) continue;
			double heading = (new WorldFlightLeg(currentX, currentY, c.getCenterX(), c.getCenterY(), 0)).getHeading();
			if(heading == radial) containers.add(c);
		}
		
		containers.sort((p0, p1) -> {
			double dist0 = distance(currentX, currentY, p0.getCenterX(), p0.getCenterY());
			double dist1 = distance(currentX, currentY, p1.getCenterX(), p1.getCenterY());
			return Double.compare(dist0, dist1);
		});
		
		return containers;
	}

	private static double distance(double x0, double y0, double x1, double y1) {
		double dx = x1 - x0;
		double dy = y1 - y0;
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public static class RadialResult {
		public double radial;
		public double gradient;
		
		public RadialResult(double radial, double gradient) {
			super();
			this.radial = radial;
			this.gradient = gradient;
		}
	}

	

	@Override
	public PathFinder newInstance() {
		return new VacuumCleanerRadialChecker();
	}

	@Override
	public String getName() {
		return "Vacuum Cleaner RadialChecker";
	}

}
