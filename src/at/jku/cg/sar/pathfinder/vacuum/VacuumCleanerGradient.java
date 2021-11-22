package at.jku.cg.sar.pathfinder.vacuum;

import at.jku.cg.sar.core.splitcontainer.SplitContainer;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneContinous;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.util.Vector;

public class VacuumCleanerGradient extends PathFinder {

	public VacuumCleanerGradient() {
		super(PathFinderType.CONTINOUS_ITERATIVE);
	}
	
	@Override
	public PathFinderResult nextContinous(DroneContinous drone) {
		
		double remainingProbs = drone.getProbabilities().getValue();
//		System.err.println("Remaining Probs: %3.3f".formatted(remainingProbs));
		if(remainingProbs <= 0.0) return null;
		
		return findMaxGradient(drone, settings);
	}
	
	public static PathFinderResult findMaxGradient(DroneContinous drone, SimulatorSettings settings) {
		SplitContainer max = null;
		double maxGradient = 0;
		for(SplitContainer c : drone.getProbabilities().getContainers()) {
			
			double distance = (new Vector(drone.getX(), drone.getY(), c.getCenterX(), c.getCenterY())).getLength();
			double duration = distance / settings.getSpeedScan();
			double prob = c.getValue();
			double gradient = prob / duration;
			
			if(gradient > maxGradient) {
				max = c;
				maxGradient = gradient;
			}			
		}
		
		double heading = (new WorldFlightLeg(drone.getX(), drone.getY(), max.getCenterX(), max.getCenterY(), 0.0)).getHeading();
				
		return new PathFinderResult(heading);
	}

	@Override
	public String getName() {
		return "Vacuum Cleaner Gradient";
	}

}
