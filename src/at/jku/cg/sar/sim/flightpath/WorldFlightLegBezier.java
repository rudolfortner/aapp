package at.jku.cg.sar.sim.flightpath;

import java.util.ArrayList;
import java.util.List;

import at.jku.cg.sar.util.Bezier;
import at.jku.cg.sar.world.WorldPoint;

public class WorldFlightLegBezier extends WorldFlightLeg {

	private List<WorldPoint> curve;

	private WorldFlightLegBezier(FlightLeg<Double> from, FlightLeg<Double> to) {
		super(from, to);
	}

	private WorldFlightLegBezier(Double fromX, Double fromY, Double toX, Double toY, double gridUnit) {
		super(fromX, fromY, toX, toY, gridUnit);
	}

	private WorldFlightLegBezier(Double fromX, Double fromY, double fromSpeed, Double toX, Double toY, double toSpeed,
			double gridUnit) {
		super(fromX, fromY, fromSpeed, toX, toY, toSpeed, gridUnit);
	}

	private WorldFlightLegBezier(Double fromX, Double fromY, double fromSpeed, Double toX, Double toY, double toSpeed,
			boolean scan, int scanX, int scanY, double gridUnit) {
		super(fromX, fromY, fromSpeed, toX, toY, toSpeed, scan, scanX, scanY, gridUnit);
	}
	
	

	public static WorldFlightLegBezier Create(FlightLeg<Double> from, FlightLeg<Double> to, List<WorldPoint> points,
			int resolution, double gridUnit) {
		WorldFlightLegBezier bezierLeg = new WorldFlightLegBezier(from, to);

		List<WorldPoint> controlPoints = new ArrayList<>();
		controlPoints.add(new WorldPoint(from.getToX(), from.getToY()));
		controlPoints.addAll(points);
		controlPoints.add(new WorldPoint(to.getFromX(), to.getFromY()));

		bezierLeg.curve = Bezier.createCurve(controlPoints, resolution);
		return bezierLeg;
	}
	
	public static WorldFlightLegBezier Create(List<WorldPoint> points, double gridUnit, int resolution) {
		WorldPoint from = points.get(0);
		WorldPoint to  = points.get(points.size()-1);
		WorldFlightLegBezier bezierLeg = new WorldFlightLegBezier(from.getX(), from.getY(), to.getX(), to.getY(), gridUnit);
		
		bezierLeg.curve = Bezier.createCurve(points, resolution);
		return bezierLeg;
	}

	public static WorldFlightLegBezier Create(Double fromX, Double fromY, Double toX, Double toY, double gridUnit,
			List<WorldPoint> points, int resolution) {
		WorldFlightLegBezier bezierLeg = new WorldFlightLegBezier(fromX, fromY, toX, toY, gridUnit);

		List<WorldPoint> controlPoints = new ArrayList<>();
		controlPoints.add(new WorldPoint(fromX, fromY));
		controlPoints.addAll(points);
		controlPoints.add(new WorldPoint(toX, toY));

		bezierLeg.curve = Bezier.createCurve(controlPoints, resolution);
		return bezierLeg;
	}

	public static WorldFlightLegBezier Create(Double fromX, Double fromY, double fromSpeed, Double toX, Double toY,
			double toSpeed, double gridUnit, List<WorldPoint> points, int resolution) {
		WorldFlightLegBezier bezierLeg = new WorldFlightLegBezier(fromX, fromY, fromSpeed, toX, toY, toSpeed, gridUnit);

		List<WorldPoint> controlPoints = new ArrayList<>();
		controlPoints.add(new WorldPoint(fromX, fromY));
		controlPoints.addAll(points);
		controlPoints.add(new WorldPoint(toX, toY));

		bezierLeg.curve = Bezier.createCurve(controlPoints, resolution);
		return bezierLeg;
	}
	
	public static WorldFlightLegBezier Create(List<WorldPoint> points, double fromSpeed, double toSpeed, double gridUnit, int resolution) {
		WorldPoint from = points.get(0);
		WorldPoint to  = points.get(points.size()-1);
		WorldFlightLegBezier bezierLeg = new WorldFlightLegBezier(from.getX(), from.getY(), fromSpeed, to.getX(), to.getY(), toSpeed, gridUnit);
		
		bezierLeg.curve = Bezier.createCurve(points, resolution);
		return bezierLeg;
	}

	public static WorldFlightLegBezier Create(Double fromX, Double fromY, double fromSpeed, Double toX, Double toY,
			double toSpeed, boolean scan, int scanX, int scanY, double gridUnit, List<WorldPoint> points,
			int resolution) {
		WorldFlightLegBezier bezierLeg = new WorldFlightLegBezier(fromX, fromY, fromSpeed, toX, toY, toSpeed, scan, scanX, scanY, gridUnit);

		List<WorldPoint> controlPoints = new ArrayList<>();
		controlPoints.add(new WorldPoint(fromX, fromY));
		controlPoints.addAll(points);
		controlPoints.add(new WorldPoint(toX, toY));

		bezierLeg.curve = Bezier.createCurve(controlPoints, resolution);
		return bezierLeg;
	}
	
	public static WorldFlightLegBezier Create(List<WorldPoint> points, double fromSpeed,
			double toSpeed, boolean scan, int scanX, int scanY, double gridUnit,
			int resolution) {
		WorldPoint from = points.get(0);
		WorldPoint to  = points.get(points.size()-1);
		WorldFlightLegBezier bezierLeg = new WorldFlightLegBezier(from.getX(), from.getY(), fromSpeed, to.getX(), to.getY(), toSpeed, scan, scanX, scanY, gridUnit);

		bezierLeg.curve = Bezier.createCurve(points, resolution);
		return bezierLeg;
	}

	
	public List<WorldPoint> getCurve(){
		return curve;
	}
	
	
	// TODO
	// TODO
	@Override
	public double getDistance() {
		if(curve == null)
			throw new IllegalStateException("Curve was not calculated");

		double distance = 0.0;
		for(int p = 0; p < curve.size()-1; p++) {
			distance += curve.get(p).distance(curve.get(p+1));
		}
		return distance;
	}
	
	public WorldPoint getLocationAfterTime(double t) {
		double currentTime = 0.0;
		
		for(int p = 0; p < curve.size()-1; p++) {
			WorldFlightLeg leg = new WorldFlightLeg(curve.get(p).getX(), curve.get(p).getY(), curve.get(p+1).getX(), curve.get(p+1).getY(), gridUnit);
			double duration = leg.getDuration();
			
			if(currentTime + duration < t) {
				currentTime += duration;
			}else {
				double delta = t - currentTime;
				return leg.getLocationAfterTime(delta);
			}
		}
		throw new IllegalStateException();
	}

}
