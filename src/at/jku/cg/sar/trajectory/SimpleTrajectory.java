package at.jku.cg.sar.trajectory;

import java.util.ArrayList;
import java.util.List;

import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.flightpath.FlightLeg;
import at.jku.cg.sar.sim.flightpath.GridFlightLeg;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;

public class SimpleTrajectory extends TrajectoryPlanner implements Cloneable {

	private final boolean allowDiagonals;
	
	private final List<WorldFlightLeg> legs = new ArrayList<>();
	private WorldFlightLeg previousScanLeg = null;
	private PathFinderResult previousPathFinderResult = null;

	public SimpleTrajectory(SimulatorSettings settings) {
		this(settings, true);
	}
	
	public SimpleTrajectory(SimulatorSettings settings, boolean allowDiagonals) {
		super(settings);
		this.allowDiagonals = allowDiagonals;
	}

	@Override
	public List<WorldFlightLeg> next(PathFinderResult next) {
		legs.clear();
		
		WorldFlightLeg scanLeg = null;

		// Handle end
		if(next == null) {
			// After last leg, decelerate to 0.0 speed
			WorldFlightLeg decelerationLeg	= extendLeg(previousScanLeg, 0.0, settings.getAcceleration(), -settings.getDeceleration(), true);
			legs.add(decelerationLeg);
			return new ArrayList<>(legs);
		}
		
		// Handle first scanLeg
		if(previousScanLeg == null && previousPathFinderResult != null) {
			GridFlightLeg direction = new GridFlightLeg(previousPathFinderResult.getPosX(), previousPathFinderResult.getPosY(), next.getPosX(), next.getPosY(), settings.getCellSize());
						
			ScanPattern pattern = allowDiagonals ? ScanPattern.fromHeading(direction.getHeading()) : ScanPattern.fromHeading4(direction.getHeading());
			scanLeg = createScanLeg(previousPathFinderResult.getPosX(), previousPathFinderResult.getPosY(), pattern);
			previousScanLeg = scanLeg;
			
			WorldFlightLeg accelerationLeg	= extendLeg(scanLeg, 0.0, settings.getAcceleration(), -settings.getDeceleration(), false);
			
			legs.add(accelerationLeg);
			legs.add(scanLeg);
		}
		
		// Handle legs
		if(previousScanLeg != null && previousPathFinderResult != null) {
			GridFlightLeg direction = new GridFlightLeg(previousPathFinderResult.getPosX(), previousPathFinderResult.getPosY(), next.getPosX(), next.getPosY(), settings.getCellSize());
			
			ScanPattern pattern = allowDiagonals ? ScanPattern.fromHeading(direction.getHeading()) : ScanPattern.fromHeading4(direction.getHeading());
			scanLeg = createScanLeg(next.getPosX(), next.getPosY(), pattern);

			connectScanLegs(previousScanLeg, scanLeg, next.isFastFlight() || settings.isForceFastFlight());
			
			// APPEND AFTER CONNECTING LEGS
			legs.add(scanLeg);
		}
	
		previousPathFinderResult = next;
		previousScanLeg = scanLeg;
		
		return new ArrayList<>(legs);
	}
	
	
	/**
	 * Connects two scan legs (those directly over a cell). This is done in the following way:<br />
	 * <ul>
	 * 		<li> <b>from</b> leg gets extended by a deceleration leg
	 * 		<li> <b>to</b> leg gets extended by an acceleration leg
	 * 		<li> Both new legs get directly connected
	 * </ul>
	 * Special case: <b>from</b> and <b>to</b> are colinear and can be directly connected without acceleration/deceleration leg
	 * @param from
	 * @param to
	 * @param fastFlight
	 */
	private void connectScanLegs(WorldFlightLeg from, WorldFlightLeg to, boolean fastFlight) {

		// start and end are the same
		if(from.getToX().equals(to.getFromX()) && from.getToY().equals(to.getFromY())) return;

		double speed = fastFlight ? settings.getSpeedFast() : settings.getSpeedScan();
		
		// START HEADING AND END HEADING ARE THE SAME (no direction change needed, no stopping)
		// TODO Only if both legs lie on the same line, then connect directly
		if(from.getHeading() == to.getHeading() && FlightLeg.checkColinear(from, to)) {
			List<WorldFlightLeg> connect = connectDirect(from, to, speed, settings.getAcceleration(), -settings.getDeceleration());
			legs.addAll(connect);
			return;
		}

		// TO ALLOW DIRECTIONAL CHANGES, DECELLERATION AND ACCELERATION LEGS ARE NEEDED
		WorldFlightLeg decelerationLeg	= extendLeg(from, 0.0, settings.getAcceleration(), -settings.getDeceleration(), true);
		WorldFlightLeg accelerationLeg	= extendLeg(to, 0.0, settings.getAcceleration(), -settings.getDeceleration(), false);
		List<WorldFlightLeg> connect = connectDirect(decelerationLeg, accelerationLeg, speed, settings.getAcceleration(), -settings.getDeceleration());
		
		legs.add(decelerationLeg);
		legs.addAll(connect);
		legs.add(accelerationLeg);
	}	

	@Override
	public String getName() {
		String name = "Simple Trajectory";
		if(allowDiagonals) name += " (allow diagonals)";
		return name;
	}

	@Override
	public TrajectoryPlanner newInstance() {
		return new SimpleTrajectory(settings, allowDiagonals);
	}

	@Override
	public TrajectoryPlanner newInstance(SimulatorSettings settings) {
		return new SimpleTrajectory(settings, allowDiagonals);
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (allowDiagonals ? 1231 : 1237);
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
		SimpleTrajectory other = (SimpleTrajectory) obj;
		if(allowDiagonals != other.allowDiagonals)
			return false;
		return true;
	}

	@Override
	public SimpleTrajectory clone() {
		SimpleTrajectory planner = new SimpleTrajectory(settings);
		planner.previousScanLeg = this.previousScanLeg;
		planner.previousPathFinderResult = this.previousPathFinderResult;
		return planner;
	}

}
