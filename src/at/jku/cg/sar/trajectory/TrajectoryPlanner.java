package at.jku.cg.sar.trajectory;

import java.util.ArrayList;
import java.util.List;

import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;

public abstract class TrajectoryPlanner implements Cloneable {
	
	protected final SimulatorSettings settings;
	
	public TrajectoryPlanner(SimulatorSettings settings) {
		this.settings = settings;
	}
	
	// Interface methods every TrajectoryPlanner should have
	public abstract List<WorldFlightLeg> next(PathFinderResult next);
	public abstract String getName();	
	public abstract TrajectoryPlanner newInstance();
	public abstract TrajectoryPlanner newInstance(SimulatorSettings settings);
	
	
	// Predefined methods every TrajectoryPlanner should have access to
	
	public WorldFlightLeg createScanLeg(int gridX, int gridY, ScanPattern pattern) {
		double fromX, fromY, toX, toY;
		fromX = toX = gridToWorld(gridX);
		fromY = toY = gridToWorld(gridY);
		
		double half = settings.getCellSize() / 2.0;

		switch (pattern) {
			case T_B:
				fromY -= half;
				toY += half;
				break;
			case B_T:
				fromY += half;
				toY -= half;
				break;
			case L_R:
				fromX -= half;
				toX += half;
				break;
			case R_L:
				fromX += half;
				toX -= half;
				break;
			case LT_RB:
				fromX -= half;
				fromY -= half;
				toX += half;
				toY += half;
				break;
			case RB_LT:
				fromX += half;
				fromY += half;
				toX -= half;
				toY -= half;
				break;
			case LB_RT:
				fromX -= half;
				fromY += half;
				toX += half;
				toY -= half;
				break;
			case RT_LB:
				fromX += half;
				fromY -= half;
				toX -= half;
				toY += half;
				break;
		}
		
		return new WorldFlightLeg(fromX, fromY, settings.getSpeedScan(), toX, toY, settings.getSpeedScan(), true,
				gridX, gridY, settings.getCellSize());
	}
	
	public WorldFlightLeg extendLeg(WorldFlightLeg leg, double speed, boolean after) {
		// Speeds
		double speedA, speedB;		
		if(after){
			speedA = leg.getToSpeed();
			speedB = speed;
		}else {
			speedA = speed;
			speedB = leg.getFromSpeed();
		}
		
		// Acceleration / Deceleration
		double speedDiff = speedB - speedA;
		boolean isAcceleration = speedDiff >= 0.0;
		double acceleration = isAcceleration ? settings.getAcceleration() : -settings.getDeceleration();
		
		// Distance for acceleration/deceleration
		double distance = accelerationDistance(speedA, speedB, acceleration);
		
		// Calculate position change needed for new leg
		double direction = leg.getDirection();
		double dx = distance * Math.cos(direction);
		double dy = distance * Math.sin(direction);
		
		// Start and End positions for leg extension
		double fromX, fromY, toX, toY;
		if(after) {
			fromX = leg.getToX();
			fromY = leg.getToY();
			toX	  = fromX + dx;
			toY   = fromY+ dy;
		}else {
			toX   = leg.getFromX();
			toY   = leg.getFromY();
			fromX = toX - dx;
			fromY = toY - dy;
		}
		
		return new WorldFlightLeg(fromX, fromY, speedA, toX, toY, speedB, settings.getCellSize());
	}
	
	public static List<WorldFlightLeg> connectDirect(WorldFlightLeg from, WorldFlightLeg to, double speed, double acceleration, double deceleration){
		List<WorldFlightLeg> legs = new ArrayList<>(3);
		
		// Single connection Leg used for distance and direction calculation
		WorldFlightLeg single = new WorldFlightLeg(from, to);
		double direction = single.getDirection();
		
		// Calculate the maximum speed we can accelerate to, depending on distance, etc.
		double vmax = maxSpeed(single.getDistance(), from.getToSpeed(), to.getFromSpeed(), acceleration, deceleration);
		speed = Math.min(vmax, speed);
		boolean capped = speed == vmax;		
		
		// Variables for calculating new leg destinations
		double dx, dy;
		
		// Acceleration Leg
		double aDistance = accelerationDistance(from.getToSpeed(), speed, acceleration);
		dx = aDistance * Math.cos(direction);
		dy = aDistance * Math.sin(direction);
		double toX = from.getToX() + dx;
		double toY = from.getToY() + dy;		
		WorldFlightLeg aLeg = new WorldFlightLeg(from.getToX(), from.getToY(), from.getToSpeed(), toX, toY, speed, from.getGridUnit());
		
		// Deceleration Leg
		double dDistance = accelerationDistance(speed, to.getFromSpeed(), deceleration);
		dx = dDistance * Math.cos(direction);
		dy = dDistance * Math.sin(direction);
		double fromX = to.getFromX() - dx;
		double fromY = to.getFromY() - dy;		
		WorldFlightLeg dLeg = new WorldFlightLeg(fromX, fromY, speed, to.getFromX(), to.getFromY(), to.getFromSpeed(), to.getGridDistance());
				
		// Middle leg
		WorldFlightLeg vLeg = new WorldFlightLeg(aLeg, dLeg);
		
		legs.add(aLeg);
		if(!capped) legs.add(vLeg);	// Middle leg is zero when maximum speed is flown
		legs.add(dLeg);
		
		return legs;
	}
	
	public static double accelerationDistance(double v1, double v2, double a) {
		if((v2-v1) < 0.0 && a > 0.0) throw new IllegalArgumentException("Negativ acceleration needed for slow down!");
		if((v2-v1) > 0.0 && a < 0.0) throw new IllegalArgumentException("Positiv acceleration needed for slow down!");
		return (v2*v2 - v1*v1) / (2.0 * a);
	}
	
	public static double maxSpeed(double s, double v1, double v3, double a, double d) {
		if(s <= 0.0) throw new IllegalArgumentException("Distance must be greater than 0!");
		if(a <= 0.0) throw new IllegalArgumentException("Acceleration must be greater than 0!");
		if(d >= 0.0) throw new IllegalArgumentException("Deceleration must be smaller than 0!");
		return Math.sqrt((2*a*d*s + d*v1*v1 - a*v3*v3) / (d - a));
	}
	
	private double gridToWorld(int grid) {
		return (grid + 0.5) * settings.getCellSize();
	}
	
	@Override
	public abstract TrajectoryPlanner clone();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((settings == null) ? 0 : settings.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		TrajectoryPlanner other = (TrajectoryPlanner) obj;
		if(settings == null) {
			if(other.settings != null)
				return false;
		}else if(!settings.equals(other.settings))
			return false;
		return true;
	}	
		
}
