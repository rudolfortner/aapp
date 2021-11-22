package at.jku.cg.sar.sim.flightpath;

import at.jku.cg.sar.util.Interpolation;
import at.jku.cg.sar.world.WorldPoint;

public abstract class FlightLeg<Unit extends Number> {

	protected final Unit fromX, fromY;	
	protected double fromSpeed;
	
	protected final Unit toX, toY;
	protected final double toSpeed;
	
	protected final boolean scan;
	protected final int scanX, scanY;
	
	protected final boolean vacuum;
	protected final double vacuumProb;
	
	protected final double gridUnit;
	
	public FlightLeg(FlightLeg<Unit> from, FlightLeg<Unit> to) {
		this(from.getToX(), from.getToY(), from.getToSpeed(), to.getFromX(), to.getFromY(), to.getFromSpeed(), from.getGridUnit());
	}
	
	public FlightLeg(Unit fromX, Unit fromY, Unit toX, Unit toY, double gridUnit) {
		this(fromX, fromY, 0.0, toX, toY, 0.0, gridUnit);
	}
	
	public FlightLeg(Unit fromX, Unit fromY, double fromSpeed, Unit toX, Unit toY, double toSpeed, double gridUnit) {
		this(fromX, fromY, fromSpeed, toX, toY, toSpeed, false, 0, 0, gridUnit);
	}
	
	public FlightLeg(Unit fromX, Unit fromY, double fromSpeed, Unit toX, Unit toY, double toSpeed, boolean vacuum, double vacuumProb, double gridUnit) {
		this(fromX, fromY, fromSpeed, toX, toY, toSpeed, false, 0, 0, vacuum, vacuumProb, gridUnit);
	}
	
	public FlightLeg(Unit fromX, Unit fromY, double fromSpeed, Unit toX, Unit toY, double toSpeed, boolean scan, int scanX, int scanY, double gridUnit) {
		this(fromX, fromY, fromSpeed, toX, toY, toSpeed, scan, scanX, scanY, false, 0.0, gridUnit);
	}
	
	public FlightLeg(Unit fromX, Unit fromY, double fromSpeed, Unit toX, Unit toY, double toSpeed, boolean scan, int scanX, int scanY, boolean vacuum, double vacuumProb, double gridUnit) {
		super();
		if(scan && vacuum) throw new IllegalStateException();
		
		this.fromX = fromX;
		this.fromY = fromY;
		this.fromSpeed = fromSpeed;
		this.toX = toX;
		this.toY = toY;
		this.toSpeed = toSpeed;
		
		this.scan = scan;
		this.scanX = scanX;
		this.scanY = scanY;
		
		this.vacuum = vacuum;
		this.vacuumProb = vacuumProb;
		
		this.gridUnit = gridUnit;		
	}
	
	public abstract double getDistance();
	public abstract double getGridDistance();
	
	
	public double getAcceleration() {
		return (toSpeed - fromSpeed) / getDuration();
	}
			
	public double getAverageSpeed() {
		return (fromSpeed + toSpeed) / 2.0;
	}
	
	public double getDuration() {
		return 2.0 * getDistance() / (fromSpeed + toSpeed);
	}
	
	public boolean hasAcceleration() {
		return fromSpeed != toSpeed;
	}
	
	/**
	 * Returns the heading of the flight leg. 0° is equivalent to direction NORTH (flying from bottom to top)
	 * @return
	 */
	public double getHeading() {
		double dx = toX.doubleValue() - fromX.doubleValue();
		double dy = toY.doubleValue() - fromY.doubleValue();
		double angle = Math.toDegrees(Math.atan2(dy, dx));
		angle += 90.0;	// Heading North = 0.0
		angle = angle % 360.0;
		if(angle < 0.0) angle += 360.0;
		return angle;
	}
	
	/**
	 * Returns the mathematical direction in radians
	 * @return
	 */
	public double getDirection() {
		double dx = toX.doubleValue() - fromX.doubleValue();
		double dy = toY.doubleValue() - fromY.doubleValue();
		return Math.atan2(dy, dx);
	}
	
	
	public double getTravelledDistance(double t) {
		if(t < 0.0) throw new IllegalArgumentException();
		if(t - getDuration() > 10.0E-6) throw new IllegalArgumentException("t=" + t + " for total duration of " + getDuration());
		return (fromSpeed + toSpeed) * t / 2.0;
	}
	
	public WorldPoint getLocationAfterDistance(double distance) {		
		if(distance < 0.0) throw new IllegalArgumentException();

		double x = Interpolation.Linear(distance, 0, fromX.doubleValue(), getDistance(), toX.doubleValue());
		double y = Interpolation.Linear(distance, 0, fromY.doubleValue(), getDistance(), toY.doubleValue());
		
		return new WorldPoint(x, y);
	}
	
	public WorldPoint getLocationAfterTime(double t) {
		double dist = getTravelledDistance(t);
		double ratio = dist / getDistance();

		double x = fromX.doubleValue() + ratio * (toX.doubleValue() - fromX.doubleValue());
		double y = fromY.doubleValue() + ratio * (toY.doubleValue() - fromY.doubleValue());
		
		return new WorldPoint(x, y);
	}
	
	
	public abstract FlightLeg<Unit> createSubLeg(double time);
	
	
	public static boolean checkColinear(FlightLeg<?> leg0, FlightLeg<?> leg1) {

		// Slopes
		double k0 = (leg0.toY.doubleValue() - leg0.fromY.doubleValue()) / (leg0.toX.doubleValue() - leg0.fromX.doubleValue());
		double k1 = (leg1.toY.doubleValue() - leg1.fromY.doubleValue()) / (leg1.toX.doubleValue() - leg1.fromX.doubleValue());
		if(Math.abs(k0) != Math.abs(k1)) return false;

		double d0 = leg0.fromY.doubleValue() - k0 * leg0.fromX.doubleValue();
		double d1 = leg1.fromY.doubleValue() - k1 * leg1.fromX.doubleValue();
		if(Double.isNaN(d0) && Double.isNaN(d1)) return true;
		if(d0 != d1) return false;
		
		return true;
	}
	
	// GETTERS
	
	public Unit getFromX() {
		return fromX;
	}
	
	public Unit getFromY() {
		return fromY;
	}
	
	public double getFromSpeed() {
		return fromSpeed;
	}
	
	public Unit getToX() {
		return toX;
	}
	
	public Unit getToY() {
		return toY;
	}
	
	public double getToSpeed() {
		return toSpeed;
	}

	public boolean isScan() {
		return scan;
	}

	public int getScanX() {
		return scanX;
	}

	public int getScanY() {
		return scanY;
	}
	
	public boolean isVacuum() {
		return vacuum;
	}

	public double getVacuumProb() {
		return vacuumProb;
	}

	public double getGridUnit() {
		return gridUnit;
	}
}
