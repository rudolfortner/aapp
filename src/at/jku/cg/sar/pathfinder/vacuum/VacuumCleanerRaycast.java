package at.jku.cg.sar.pathfinder.vacuum;

import at.jku.cg.sar.core.splitcontainer.SplitGrid;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneContinous;
import at.jku.cg.sar.util.Vector;

public class VacuumCleanerRaycast extends PathFinder {

	public VacuumCleanerRaycast() {
		super(PathFinderType.CONTINOUS_ITERATIVE);
	}
	
	@Override
	public PathFinderResult nextContinous(DroneContinous drone) {
		
		double remainingProbs = drone.getProbabilities().getValue();
//		System.err.println("Remaining Probs: %3.3f".formatted(remainingProbs));
		if(remainingProbs <= 0.0) return null;
		
		double bestRadial	= 0.0;
		double bestValue	= 0.0;
		
		for(double h = 0.0; h < 360.0; h += 5.0) {
			double value = evaluateRay(drone, settings, h, 1.0, 1000.0);
			
			if(value > bestValue) {
				bestValue = value;
				bestRadial = h;
			}
		}
		
		if(bestValue <= 0.0) return VacuumCleanerGradient.findMaxGradient(drone, settings);
		
		return new PathFinderResult(bestRadial);
	}
	
	public static double evaluateRay(DroneContinous drone, SimulatorSettings settings, double radial, double stepSize, double maxDistance) {
		
		SplitGrid grid = drone.getProbabilities().clone();
		
		int maxSteps = (int) (maxDistance / stepSize);
		double stepTime = stepSize / settings.getSpeedScan();
		
		Vector position	= new Vector(drone.getX(), drone.getY());
		Vector step	= Vector.fromHeading(radial).Scale(stepSize);
		
		
		double maxGradient = 0.0;
		double accumProbs = 0.0;
		double accumTime = 0.0;
		
		for(int i = 0; i < maxSteps; i++) {
			position.add(step);
			
			double r = drone.getScanRadius();
			double prob = grid.collectRectangle(position.x-r, position.y-r, position.x+r, position.y+r);
						
			accumProbs += prob;
			accumTime  += stepTime;
			
			double gradient = accumProbs / accumTime;
			maxGradient = Math.max(maxGradient, gradient);
		}
		
		return maxGradient;
	}

	@Override
	public String getName() {
		return "Vacuum Cleaner Raycast";
	}

}
