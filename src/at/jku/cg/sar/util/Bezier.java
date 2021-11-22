package at.jku.cg.sar.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.jku.cg.sar.world.WorldPoint;

public class Bezier {

	/**
	 * Pre-calculate the Bezier Curve for the given control points with given resolution
	 * @param points
	 * @param resolution 
	 * @return
	 */
	public static List<WorldPoint> createCurve(List<WorldPoint> points, int resolution){
		if(points == null) throw new IllegalArgumentException("Control Points needed");
		if(points.size() < 2) throw new IllegalArgumentException("At least two control points are needed");
		if(resolution < 1) throw new IllegalArgumentException("Resolution must be greater than 0");
				
		int pointCount = 1 << resolution;	
		List<WorldPoint> curve = new ArrayList<>(pointCount);
		
		for(double t = 0.0; t <= 1.0; t += 1.0 / (pointCount-1)) {
			WorldPoint point = getPoint(points, t);
			curve.add(point);
		}
		
		return Collections.unmodifiableList(curve);
	}
	
	/**
	 * Precalculate the Bezier Curve for the given control points to achieve a maximum distance between two resulting curve points
	 * @param points
	 * @param maxDistance
	 * @return
	 */
	public static List<WorldPoint> createCurve(List<WorldPoint> points, double maxDistance){
		if(points == null) throw new IllegalArgumentException("Control Points needed");
		if(points.size() < 2) throw new IllegalArgumentException("At least two control points are needed");
		if(maxDistance <= 0.0) throw new IllegalArgumentException("Max Distance must be greater than 0");
			
		int resolution = 1;
		while(true) {
			int pointCount = 1 << resolution;
			List<WorldPoint> curve = new ArrayList<>(pointCount);
			boolean validCurve = true;
			
			WorldPoint previous = null;
			for(double t = 0.0; t <= 1.0; t += 1.0 / (pointCount-1)) {
				WorldPoint point = getPoint(points, t);
				curve.add(point);				
				
				if(previous != null) {
					double dist = previous.distance(point);
					if(dist > maxDistance) {
						validCurve = false;
						break;
					}
				}				
				previous = point;
			}
			
			if(validCurve) return Collections.unmodifiableList(curve);
			resolution++;
		}		
	}
	
	/**
	 * Retrieve the Point on the Bezier Curve at t with given control points
	 * @param controlPoints
	 * @param t
	 * @return
	 */
	public static WorldPoint getPoint(List<WorldPoint> controlPoints, double t) {
		List<WorldPoint> points = new ArrayList<>(controlPoints.size());
		for(WorldPoint cp : controlPoints) {
			points.add(new WorldPoint(cp.getX(), cp.getY()));
		}
		
		int n = points.size();
		
		for(int k = 0; k < n-1; k++) {
			for(int i = 0; i < n-k-1; i++) {
				WorldPoint cp0 = points.get(i);
				WorldPoint cp1 = points.get(i+1);
				
				double x = (1.0 - t) * cp0.getX() + t * cp1.getX();
				double y = (1.0 - t) * cp0.getY() + t * cp1.getY();
				
				points.set(i, new WorldPoint(x, y));
			}
		}
		
		return points.get(0);
	}
	
	/**
	 * Retrieve the gradient of the Bezier Curve at t with given control points
	 * @param controlPoints
	 * @param t
	 * @return
	 */
	public static double getGradient(List<WorldPoint> controlPoints, double t) {
		List<WorldPoint> points = new ArrayList<>(controlPoints.size());
		for(WorldPoint cp : controlPoints) {
			points.add(new WorldPoint(cp.getX(), cp.getY()));
		}
		
		int n = points.size();
		
		double gradient = 0.0;
		for(int k = 0; k < n-1; k++) {
			for(int i = 0; i < n-k-1; i++) {
				WorldPoint cp0 = points.get(i);
				WorldPoint cp1 = points.get(i+1);
				
				double x = (1.0 - t) * cp0.getX() + t * cp1.getX();
				double y = (1.0 - t) * cp0.getY() + t * cp1.getY();				
				points.set(i, new WorldPoint(x, y));

				double dx = cp1.getX() - cp0.getX();
				double dy = cp1.getY() - cp0.getY();
				
				gradient = dy / dx;
			}
		}
		
		return gradient;
	}
	
}
