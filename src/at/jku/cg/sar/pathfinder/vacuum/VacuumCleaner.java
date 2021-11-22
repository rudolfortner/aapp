package at.jku.cg.sar.pathfinder.vacuum;

import at.jku.cg.sar.core.splitcontainer.SplitContainer;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.drone.DroneContinous;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;

public class VacuumCleaner extends PathFinder {

	public VacuumCleaner() {
		super(PathFinderType.CONTINOUS_ITERATIVE);
	}

	@Override
	public PathFinderResult nextContinous(DroneContinous drone) {
		
		double remainingProbs = drone.getProbabilities().getValue();
//		System.err.println("Remaining Probs: %3.3f".formatted(remainingProbs));
		if(remainingProbs <= 0.0) return null;
		
		SplitContainer max = drone.getProbabilities().getContainers().stream()
				.max((c0, c1) -> Double.compare(c0.getValue(), c1.getValue())).orElse(null);
		
		if(max == null) return null;
		
		double heading = (new WorldFlightLeg(drone.getX(), drone.getY(), max.getCenterX(), max.getCenterY(), 0.0)).getHeading();
		
		
		return new PathFinderResult(heading);
	}

	@Override
	public String getName() {
		return "Vacuum Cleaner";
	}

}
