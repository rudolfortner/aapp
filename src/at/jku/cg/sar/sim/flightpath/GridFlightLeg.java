package at.jku.cg.sar.sim.flightpath;

public class GridFlightLeg extends FlightLeg<Integer> {
	
	public GridFlightLeg(FlightLeg<Integer> from, FlightLeg<Integer> to) {
		super(from, to);
	}

	public GridFlightLeg(Integer fromX, Integer fromY, Integer toX, Integer toY, double gridUnit) {
		super(fromX, fromY, toX, toY, gridUnit);
	}

	public GridFlightLeg(Integer fromX, Integer fromY, double fromSpeed, Integer toX, Integer toY, double toSpeed, double gridUnit) {
		super(fromX, fromY, fromSpeed, toX, toY, toSpeed, gridUnit);
	}
	
	public GridFlightLeg(Integer fromX, Integer fromY, double fromSpeed, Integer toX, Integer toY, double toSpeed,
			boolean scan, int scanX, int scanY, double gridUnit) {
		super(fromX, fromY, fromSpeed, toX, toY, toSpeed, scan, scanX, scanY, gridUnit);
	}

	@Override
	public double getDistance() {
		return gridUnit * getGridDistance();
	}

	@Override
	public double getGridDistance() {
		int dx = toX - fromX;
		int dy = toY - fromY;
		return Math.sqrt(dx*dx + dy*dy);
	}

	@Override
	public FlightLeg<Integer> createSubLeg(double time) {
		throw new UnsupportedOperationException();
	}

}
