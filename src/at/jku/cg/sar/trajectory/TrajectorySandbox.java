package at.jku.cg.sar.trajectory;

import java.util.ArrayList;
import java.util.List;

import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.flightpath.GridFlightLeg;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.sim.flightpath.WorldFlightLegBezier;
import at.jku.cg.sar.world.WorldPoint;

public class TrajectorySandbox extends TrajectoryPlanner implements Cloneable {
	
	private final List<WorldFlightLeg> legs = new ArrayList<>();
	private WorldFlightLeg previousScanLeg = null;
	private PathFinderResult previousPathFinderResult = null;

	public TrajectorySandbox(SimulatorSettings settings) {
		super(settings);
	}


	@Override
	public List<WorldFlightLeg> next(PathFinderResult next) {
		legs.clear();
		
		WorldFlightLeg scanLeg = null;

		// Handle end
		if(next == null) {
			return new ArrayList<>(legs);
		}
		
		// Handle first scanLeg
		if(previousScanLeg == null && previousPathFinderResult != null) {
			GridFlightLeg direction = new GridFlightLeg(previousPathFinderResult.getPosX(), previousPathFinderResult.getPosY(), next.getPosX(), next.getPosY(), settings.getCellSize());
						
			ScanPattern pattern = ScanPattern.fromHeading(direction.getHeading());
			scanLeg = createScanLeg(previousPathFinderResult.getPosX(), previousPathFinderResult.getPosY(), pattern);
			previousScanLeg = scanLeg;
			
			WorldFlightLeg accelerationLeg	= extendLeg(scanLeg, 0.0, false);
			
			legs.add(accelerationLeg);
			legs.add(scanLeg);
		}
		
		// Handle legs
		if(previousScanLeg != null && previousPathFinderResult != null) {
			GridFlightLeg direction = new GridFlightLeg(previousPathFinderResult.getPosX(), previousPathFinderResult.getPosY(), next.getPosX(), next.getPosY(), settings.getCellSize());
			
			ScanPattern pattern = ScanPattern.fromHeading(direction.getHeading());
			scanLeg = createScanLeg(next.getPosX(), next.getPosY(), pattern);
						
			connectScanLegs(previousScanLeg, scanLeg, next.isFastFlight());
			
			// APPEND AFTER CONNECTING LEGS
			legs.add(scanLeg);
		}
	
		previousPathFinderResult = next;
		previousScanLeg = scanLeg;
		
		return new ArrayList<>(legs);
	}
	
	
	/**
	 * Creates a simple STRAIGHT connection between two legs
	 * @param from
	 * @param to
	 */
	private void connectScanLegs(WorldFlightLeg from, WorldFlightLeg to, boolean fastFlight) {

		// start and end are the same
		if(from.getToX().equals(to.getFromX()) && from.getToY().equals(to.getFromY())) return;

		WorldFlightLeg decelerationLeg	= extendLeg(from, 0.0, true);
		WorldFlightLeg accelerationLeg	= extendLeg(to, 0.0, false);
		
		WorldFlightLegBezier bezier = WorldFlightLegBezier.Create(decelerationLeg, accelerationLeg, List.of(new WorldPoint(decelerationLeg.getToX(), accelerationLeg.getFromY())), 4, settings.getCellSize());
		
		legs.add(decelerationLeg);
		legs.add(bezier);
		legs.add(accelerationLeg);
	}	

	@Override
	public String getName() {
		String name = "Trajectory Sandbox";
		return name;
	}

	@Override
	public TrajectoryPlanner newInstance() {
		return new TrajectorySandbox(settings);
	}
	
	@Override
	public TrajectoryPlanner newInstance(SimulatorSettings settings) {
		return new TrajectorySandbox(settings);
	}

	
	@Override
	public TrajectorySandbox clone() {
		TrajectorySandbox planner = new TrajectorySandbox(settings);
		planner.previousScanLeg = this.previousScanLeg;
		planner.previousPathFinderResult = this.previousPathFinderResult;
		return planner;
	}
}
