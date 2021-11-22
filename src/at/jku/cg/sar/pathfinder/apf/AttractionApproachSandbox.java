package at.jku.cg.sar.pathfinder.apf;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.gui.GridViewer;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.GridFlightLeg;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.SimpleTrajectory;

public class AttractionApproachSandbox extends PathFinder {

	/**
	 * Based on AttractionApproach but also using trajectory to calculate Attraction"
	 */
	public AttractionApproachSandbox() {
		super(PathFinderType.DISCRETE_ITERATIVE);
	}
	
	// Probability Map but weighted by exp(distance)
	public static Grid<Double> createTrajectoryMap(DroneDiscrete drone, SimulatorSettings settings, int positionX, int positionY){
		Grid<Double> T = new Grid<>(drone.getWidth(), drone.getHeight());
		
		for(int x = 0; x < drone.getWidth(); x++) {
			for(int y = 0; y < drone.getHeight(); y++) {
				SimpleTrajectory planner = new SimpleTrajectory(settings);
				planner.next(new PathFinderResult(drone.getPreviousX(), drone.getPreviousY(), false));
				planner.next(new PathFinderResult(drone.getX(), drone.getY(), false));
				
				FlightPath<WorldFlightLeg> flightPath = new FlightPath<>();
				flightPath.appendLegs(planner.next(new PathFinderResult(x, y, false)));
				
				double duration = flightPath.getDuration();
				double weight = Math.exp(duration / 60.0);
				
				double value = drone.getProbability(x, y) / weight;
				T.set(x, y, value);
			}
		}
		
		double maxVal = T.maxValue();
		for(int x = 0; x < drone.getWidth(); x++) {
			for(int y = 0; y < drone.getHeight(); y++) {
//				T.set(x, y, T.get(x, y) / maxVal);
//				T.set(x, y, Math.exp(-T.get(x, y) / maxVal));
			}
		}
		return T;
	}
	
	public static Grid<Double> createGradientMap(DroneDiscrete drone, SimulatorSettings settings, int positionX, int positionY){
		Grid<Double> G = new Grid<>(drone.getWidth(), drone.getHeight());
		
		for(int x = 0; x < drone.getWidth(); x++) {
			for(int y = 0; y < drone.getHeight(); y++) {
				SimpleTrajectory planner = new SimpleTrajectory(settings);
				planner.next(new PathFinderResult(drone.getPreviousX(), drone.getPreviousY(), false));
				planner.next(new PathFinderResult(drone.getX(), drone.getY(), false));
				
				FlightPath<WorldFlightLeg> flightPath = new FlightPath<>();
				flightPath.appendLegs(planner.next(new PathFinderResult(x, y, false)));
				
				double duration = flightPath.getDuration();
				double gradient = drone.getProbability(x, y) / duration;
//				double distance = Math.sqrt(Math.pow(x-positionX, 2.0) + Math.pow(y-positionY, 2.0));
				
//				gradient = drone.getProbability(x, y) / Math.pow(duration, 2.0);
//				gradient = drone.getProbability(x, y) / Math.pow(duration, 1.1);
				
//				System.err.println("Distance %f".formatted(distance));
//				gradient = drone.getProbability(x, y) / distance;
				G.set(x, y, gradient);
			}
		}
		return G;
	}
	
	// Probability Map but weighted by exp(distance)
	public static Grid<Double> createHeadingMap(DroneDiscrete drone, int positionX, int positionY){
		Grid<Double> H = new Grid<>(drone.getWidth(), drone.getHeight(), 0.0);
		
		double heading = (new GridFlightLeg(drone.getPreviousX(), drone.getPreviousY(), drone.getX(), drone.getY(), 0.0)).getHeading();
		
		for(int x = 0; x < drone.getWidth(); x++) {
			for(int y = 0; y < drone.getHeight(); y++) {
				if(x == positionX && y == positionY) continue;
				if(drone.isVisited(x, y)) continue;
				
				double distance = Math.sqrt(Math.pow(x-positionX, 2.0) + Math.pow(y-positionY, 2.0));
				
				double h = (new GridFlightLeg(positionX, positionY, x, y, 0.0)).getHeading();
				double diff = Math.abs(heading - h);
				if(diff > 180.0) diff = 360.0 - diff;
				
				
				double value = drone.getProbability(x, y) * Math.cos(Math.toRadians(diff / 2.0)) / distance;
				H.set(x, y, value);
			}
		}
		
//		double maxVal = H.maxValue();
//		for(int x = 0; x < drone.getWidth(); x++) {
//			for(int y = 0; y < drone.getHeight(); y++) {
//				H.set(x, y, H.get(x, y) / maxVal);
//			}
//		}
		return H;
	}


	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		if(drone.visitedAll()) return null;
		
		Grid<Double> T = createTrajectoryMap(drone, settings, drone.getX(), drone.getY());
		Grid<Double> G = createGradientMap(drone, settings, drone.getX(), drone.getY());
		Grid<Double> H = createHeadingMap(drone, drone.getX(), drone.getY());
		Grid<Double> A = AttractionApproach.createAttractionMapExp(drone.getProbabilities(), drone.getX(), drone.getY());
		
		GridValue<Double> max = AttractionApproach.selectPoint(H, drone.getX(), drone.getY());

		double maxVal = G.maxValue();
		for(int x = 0; x < drone.getWidth(); x++) {
			for(int y = 0; y < drone.getHeight(); y++) {
				G.set(x, y, G.get(x, y) / maxVal);
//				T.set(x, y, Math.exp(-T.get(x, y) / maxVal));
			}
		}
		
		if(false && drone.getStepCount() == 10) {
			System.err.println(T.maxValue());
			new GridViewer("Prob Map", drone.getProbabilities());
			new GridViewer("T", T);
			new GridViewer("G", G);
			new GridViewer("H", H);
			new GridViewer("Original A", A);
			return null;
		}	
		
		if(drone.getStepCount() > 2000) return null;
		
		return new PathFinderResult(max.getX(), max.getY(), false);
	}

	@Override
	public String getName() {
		return "Attraction Approach Sandbox";
	}

}
