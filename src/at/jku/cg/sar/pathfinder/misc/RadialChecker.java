package at.jku.cg.sar.pathfinder.misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.gui.LineGraphViewer;
import at.jku.cg.sar.gui.graphics.LineGraphDataSet;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.GridFlightLeg;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.SimpleTrajectory;

/**
 * Based on an idea by Prof. Bimber
 * @author ortner
 *
 */
public class RadialChecker extends PathFinder {

	private static final boolean DEBUG = false;
	
	private final boolean allRadials;
	
	private final double r0, r1;
	private final boolean iterativeReplan;
	private final List<PathFinderResult> queue;
	
	public RadialChecker() {
		this(true);
	}
	
	public RadialChecker(boolean iterativeReplan) {
		this(iterativeReplan, 0.0, Double.POSITIVE_INFINITY);
	}
	
	public RadialChecker(boolean iterativeReplan, double r0, double r1) {
		this(null, iterativeReplan, r0, r1, true);
	}

	public RadialChecker(SimulatorSettings settings, boolean iterativeReplan, double r0, double r1, boolean allRadials) {
		super(PathFinderType.DISCRETE_ITERATIVE);
		this.settings = settings;
		if(r0 >= r1) throw new IllegalArgumentException("r0 must be smaller than r1");
		this.iterativeReplan = iterativeReplan;
		this.r0 = r0;
		this.r1 = r1;
		this.allRadials = allRadials;
		this.queue = new LinkedList<>();
	}
	

	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		
		PathFinderResult next = null;
		if(iterativeReplan) {
			// ALWAYS PLAN ONE STEP AHEAD AND REPLAN
			List<RadialResult> results = replan(drone, settings, r0, r1, allRadials);
			if(results.size() == 0) return fallback(drone, settings);
			RadialResult max = results.get(0);
			if(max.path.size() <= 0) return fallback(drone, settings);
			next = max.path.get(0);
		}else {
			// IF QUEUE EMPTY PLAN ANOTHER FULL RADIAL, IF NOT, STAY ON RADIAL
			if(queue.size() == 0) {
				// LOOK FOR NEXT RADIAL
				List<RadialResult> results = replan(drone, settings, r0, r1, allRadials);
				if(results.size() == 0) return fallback(drone, settings);
				RadialResult max = results.get(0);
				queue.addAll(max.path);
			}
			if(queue.size() == 0) return fallback(drone, settings);
			return queue.remove(0);
		}
		
		return next;
	}
	
	
	public static PathFinderResult fallback(DroneDiscrete drone, SimulatorSettings settings) {
		if(drone.visitedAll()) return null;
		
		SimpleTrajectory planner = new SimpleTrajectory(settings);
		if(drone.getStepCount() > 0) planner.next(new PathFinderResult(drone.getPreviousX(), drone.getPreviousY(), false));
		planner.next(new PathFinderResult(drone.getX(), drone.getY(), false));
		
		List<GridValue<Double>> unvisited = drone.getProbabilities().getNotEqual(0.0).stream()
				.filter(gv -> !drone.isVisited(gv.getX(), gv.getY()))
				.collect(Collectors.toUnmodifiableList());
		
		
		GridValue<Double> max = null;
		double maxGradient = 0.0;
		for(GridValue<Double> cell : unvisited) {
			
			FlightPath<WorldFlightLeg> path = new FlightPath<>();
			path.appendLegs(planner.clone().next(new PathFinderResult(cell.getX(), cell.getY(), false)));
			
			double gradient = cell.getValue() / path.getDuration();
			if(gradient > maxGradient) {
				max = cell;
				maxGradient = gradient;
			}		
		}
		if(max == null) return null;
		return new PathFinderResult(max.getX(), max.getY(), false);
	}
	
	public static List<RadialResult> replan(DroneDiscrete drone, SimulatorSettings settings, double r0, double r1, boolean allRadials){
		
		List<RadialResult> results = new ArrayList<>(8);
		if(allRadials) {
			Set<Double> radials = new HashSet<>();
			for(int x = 0; x < drone.getWidth(); x++) {
				for(int y = 0; y < drone.getHeight(); y++) {
					if(drone.isVisited(x, y)) continue;	
					if((x == drone.getX() && y == drone.getY())) continue;
					double heading = (new GridFlightLeg(drone.getX(), drone.getY(), x, y, 0)).getHeading();
					radials.add(heading);
				}
			}
			for(double radial : new ArrayList<>(radials)) {
				results.add(evaluateRadial(drone, settings, radial, r0, r1));
			}
		}else {
			for(double radial = 0.0; radial < 360; radial += 45.0) {
				results.add(evaluateRadial(drone, settings, radial, r0, r1));
			}				
		}
		
		return results.stream()
				.sorted((res0, res1) -> Double.compare(res1.gradient, res0.gradient))
				.collect(Collectors.toUnmodifiableList());
	}
	
	
	public static RadialResult evaluateRadial(DroneDiscrete drone, SimulatorSettings settings, double radial, double r0, double r1) {
		
		SimpleTrajectory planner = new SimpleTrajectory(settings);
		if(drone.getStepCount() > 0) planner.next(new PathFinderResult(drone.getPreviousX(), drone.getPreviousY(), false));
		planner.next(new PathFinderResult(drone.getX(), drone.getY(), false));
		
		List<PathFinderResult> path = new ArrayList<>();
		LineGraphDataSet set = new LineGraphDataSet("probs", Color.RED);
		set.addPoint(0, 0);
		
		List<GridValue<Double>> onRadial = onRadial(drone, radial, false);
		GridValue<Double> currentMax = null;
		double maxGradient = 0.0;
		double accumProb = 0.0;
		double accumTime = 0.0;
		for(GridValue<Double> g : onRadial) {
			int x = g.getX(), y = g.getY();
			if(drone.isVisited(x, y)) continue;								// Jump over visited cells
			
			double distance = g.distance(drone.getX(), drone.getY());		// Calculate distance from current position
			if(distance <= r0) continue;									// All cells closer than r0 are overflown fast
			if(distance > r1) break;										// All cells beyond r1 are not considered
			
			FlightPath<WorldFlightLeg> flightPath = new FlightPath<>();		// FlightPath object from current pos to next cell or from previous cell to next cell
			PathFinderResult next = new PathFinderResult(x, y, true);		// PathFinder next position
			flightPath.appendLegs(planner.next(next));						// TrajectoryPlanner plans for next position
			path.add(next);													// Add next to intermediate result
			
			accumProb += g.getValue();										// Accumulate probabilities
			accumTime += flightPath.getDuration();							// Accumulate time
			double gradient = accumProb / accumTime;						// Calculate gradient up to now
			set.addPoint(accumTime, accumProb);
			
			if(gradient >= maxGradient) {
				currentMax = g;
				maxGradient = gradient;
			}
		}
		
		// Final Path only contains cells up to the point with the maximum gradient
		List<PathFinderResult> finalPath = new ArrayList<>();
		for(PathFinderResult r : path) {
			finalPath.add(r);
			if(r.getPosX() == currentMax.getX() && r.getPosY() == currentMax.getY()) break;
		}
		if(DEBUG) new LineGraphViewer(List.of(set));
		return new RadialResult(radial, maxGradient, finalPath);
	}
	
	public static List<GridValue<Double>> onRadial(DroneDiscrete drone, double radial, boolean includeCurrent){
		int currentX = drone.getX(), currentY = drone.getY();
		
		List<GridValue<Double>> points = new ArrayList<>();
		for(int x = 0; x < drone.getWidth(); x++) {
			for(int y = 0; y < drone.getHeight(); y++) {
				if((x == currentX && y == currentY) && !includeCurrent) continue;
				double heading = (new GridFlightLeg(currentX, currentY, x, y, 0)).getHeading();
				if(heading == radial) points.add(new GridValue<Double>(x, y, drone.getProbability(x, y)));
			}
		}
		
		points.sort((p1, p2) -> Double.compare(p1.distance(currentX, currentY), p2.distance(currentX, currentY)));
		
		return points;
	}
	
	public boolean isAllRadials() {
		return allRadials;
	}

	public double getR0() {
		return r0;
	}

	public double getR1() {
		return r1;
	}

	public boolean isIterativeReplan() {
		return iterativeReplan;
	}





	public static class RadialResult {
		public double radial;
		public double gradient;
		public List<PathFinderResult> path;
		
		public RadialResult(double radial, double gradient, List<PathFinderResult> path) {
			super();
			this.radial = radial;
			this.gradient = gradient;
			this.path = Collections.unmodifiableList(path);
		}
	}

	@Override
	public PathFinder newInstance() {
		return new RadialChecker(settings, iterativeReplan, r0, r1, allRadials);
	}
	
	@Override
	public PathFinder newInstance(SimulatorSettings settings) {
		return new RadialChecker(settings, iterativeReplan, r0, r1, allRadials);
	}

	@Override
	public String getName() {
		String planning = iterativeReplan ? "Iterative Replan" : "Full Replan";
		return "Radial Checker (%s) (%f - %f)".formatted(planning, r0, r1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (allRadials ? 1231 : 1237);
		result = prime * result + (iterativeReplan ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(r0);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(r1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!super.equals(obj))
			return false;
		if(getClass() != obj.getClass())
			return false;
		RadialChecker other = (RadialChecker) obj;
		if(allRadials != other.allRadials)
			return false;
		if(iterativeReplan != other.iterativeReplan)
			return false;
		if(Double.doubleToLongBits(r0) != Double.doubleToLongBits(other.r0))
			return false;
		if(Double.doubleToLongBits(r1) != Double.doubleToLongBits(other.r1))
			return false;
		return true;
	}	
}
