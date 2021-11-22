package at.jku.cg.sar.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Curve {

	private final List<CurvePoint> points;
	
	public Curve() {
		this.points = new ArrayList<>();
	}
	
	public void addPoint(double x, double y) {
		CurvePoint point = new CurvePoint(x, y);
		this.points.add(point);
		this.points.sort((p0, p1) -> Double.compare(p0.x, p1.x));
	}
	
	public List<CurvePoint> getPoints() {
		return Collections.unmodifiableList(points);
	}

	public double minX() {
		return points.stream().mapToDouble(point -> point.getX()).min().orElse(0.0);
	}
	
	public double maxX() {
		return points.stream().mapToDouble(point -> point.getX()).max().orElse(0.0);
	}
	
	public double minY() {
		return points.stream().mapToDouble(point -> point.getY()).min().orElse(0.0);
	}
	
	public double maxY() {
		return points.stream().mapToDouble(point -> point.getY()).max().orElse(0.0);
	}
	
	public int size() {
		return points.size();
	}
	
	
	public double evaluate(double x, boolean extrapolate) {
		CurvePoint first = points.get(0);
		CurvePoint last = points.get(points.size()-1);
				
		if(x < first.getX()) {
			return extrapolate ? first.getY() : 0.0;
		}
		
		if(x > last.getX()) {
			return extrapolate ? last.getY() : 0.0;
		}
		
		// Search for exact point
		for(CurvePoint p : points) {
			if(p.x == x) return p.y;
		}
		
		for(int p = 0; p < points.size()-1; p++) {
			CurvePoint from = points.get(p);
			CurvePoint to = points.get(p+1);
			
			if(from.getX() < x && x < to.getX()) {
				return Interpolation.Linear(x, from.getX(), from.getY(), to.getX(), to.getY());
			}
		}
		
		throw new IllegalStateException();
	}
	
	public double evaluateGradient(double x, boolean extrapolate) {
		if(points.size() <= 1) return 0.0;

		CurvePoint first = points.get(0);
		CurvePoint second = points.get(1);
		CurvePoint scondLast = points.get(points.size()-2);
		CurvePoint last = points.get(points.size()-1);
		
		if(x < first.getX()) {
			return extrapolate ? (second.y - first.y) / (second.x - first.x) : 0.0;
		}
		
		if(x > last.getX()) {
			return extrapolate ? (last.y - scondLast.y) / (last.x - scondLast.x) : 0.0;
		}
		
		// Search for exact point
		for(int p = 0; p < points.size(); p++) {
			CurvePoint point = points.get(p);
			if(point.x == x) {
				if(p == 0) {
					CurvePoint next = points.get(1);
					return (next.y - point.y) / (next.x - point.x);
				}else if(p == points.size()-1) {
					CurvePoint prev = points.get(1);
					return (point.y - prev.y) / (point.x - prev.x);
				}else {
					CurvePoint before = points.get(p-1);
					CurvePoint after = points.get(p+1);
					return (after.y - before.y) / (after.x - before.x);
				}
			}
		}
		
		for(int p = 0; p < points.size()-1; p++) {
			CurvePoint from = points.get(p);
			CurvePoint to = points.get(p+1);
			
			if(from.getX() < x && x < to.getX()) {
				return (to.y - from.y) / (to.x - from.x);
			}
		}
		
		throw new IllegalStateException();
	}
	
	public double integrate(double a, double b, boolean extrapolate) {
		double integral = 0.0;
				
		int steps = 10000;
		double dx = Math.abs(b-a) / steps;
		
		double lastX = a;
		double lastY = evaluate(a, extrapolate);
		
		for(int i = 0; i < steps; i++) {
			double x0 = lastX;
			double y0 = lastY;
			
			double x1 = x0 + dx;
			double y1 = evaluate(x1, extrapolate);
			
			lastX = x1;
			lastY = y1;			
			
			double area = (y0 + y1) * dx / 2.0;
			integral += area;
		}
		
		// Use the fact that we have line segments !!! More performance
		// Integrate all segments
//		for(int p = 0; p < points.size()-1; p++) {
//			CurvePoint from = points.get(p);
//			CurvePoint to = points.get(p+1);
//
//			double x0 = Math.max(from.getX(), a);
//			double x1 = Math.min(to.getX(), b);
//
//			double y0 = evaluate(x0, extrapolate);
//			double y1 = evaluate(x1, extrapolate);
//			
//			integral += (y0 + y1) * Math.abs(x1-x0) / 2.0;
//		}
		return integral;
	}
	
	public class CurvePoint {
		private final double x, y;
		
		public CurvePoint(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		public double getX() {
			return this.x;
		}
		
		public double getY() {
			return this.y;
		}
	}

}
