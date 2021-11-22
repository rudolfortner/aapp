package at.jku.cg.sar.sim.flightpath;

import at.jku.cg.sar.util.Interpolation;

public class WorldFlightLeg extends FlightLeg<Double> {
	
	public WorldFlightLeg(FlightLeg<Double> from, FlightLeg<Double> to) {
		super(from, to);
	}

	public WorldFlightLeg(Double fromX, Double fromY, Double toX, Double toY, double gridUnit) {
		super(fromX, fromY, toX, toY, gridUnit);
	}

	public WorldFlightLeg(Double fromX, Double fromY, double fromSpeed, Double toX, Double toY, double toSpeed, double gridUnit) {
		super(fromX, fromY, fromSpeed, toX, toY, toSpeed, gridUnit);
	}
	
	public WorldFlightLeg(Double fromX, Double fromY, double fromSpeed, Double toX, Double toY, double toSpeed, boolean vacuum, double vacuumProb, double gridUnit) {
		super(fromX, fromY, fromSpeed, toX, toY, toSpeed, vacuum, vacuumProb, gridUnit);
	}
	
	public WorldFlightLeg(Double fromX, Double fromY, double fromSpeed, Double toX, Double toY, double toSpeed,
			boolean scan, int scanX, int scanY, double gridUnit) {
		super(fromX, fromY, fromSpeed, toX, toY, toSpeed, scan, scanX, scanY, gridUnit);
	}

	private WorldFlightLeg(Double fromX, Double fromY, double fromSpeed, Double toX, Double toY, double toSpeed,
			boolean scan, int scanX, int scanY, boolean vacuum, double vacuumProb, double gridUnit) {
		super(fromX, fromY, fromSpeed, toX, toY, toSpeed, scan, scanX, scanY, vacuum, vacuumProb, gridUnit);
	}

	@Override
	public double getDistance() {
		double dx = toX - fromX;
		double dy = toY - fromY;
		return Math.sqrt(dx*dx + dy*dy);
	}

	@Override
	public double getGridDistance() {
		return getDistance() / gridUnit;
	}

	@Override
	public FlightLeg<Double> createSubLeg(double time) {
		double total = getDuration();

		double x = Interpolation.Linear(time, 0.0, fromX, total, toX);
		double y = Interpolation.Linear(time, 0.0, fromY, total, toY);
		double speed = Interpolation.Linear(time, 0.0, fromSpeed, total, toSpeed);
		
		return new WorldFlightLeg(fromX, fromY, fromSpeed, x, y, speed, scan, scanX, scanY, vacuum, vacuumProb, gridUnit);
	}

}
