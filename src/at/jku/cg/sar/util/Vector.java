package at.jku.cg.sar.util;

import java.util.List;

public class Vector implements Cloneable {

	public double x, y;
	
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector(double x0, double y0, double x1, double y1) {
		this.x = x1 - x0;
		this.y = y1 - y0;
	}
	
	public static Vector fromHeading(double heading) {
		double angle = Math.toRadians(heading - 90.0);
		
		double x = Math.cos(angle);
		double y = Math.sin(angle);
		
		return new Vector(x, y);
	}
	
	public void add(double x, double y) {
		this.x += x;
		this.y += y;
	}
	
	public void add(Vector v) {
		this.x += v.x;
		this.y += v.y;
	}
	
	public Vector Add(double x, double y) {
		Vector vector = this.clone();
		vector.add(x, y);
		return vector;
	}
	
	public static Vector Add(Vector...vectors) {
		Vector vector = new Vector(0, 0);
		for(Vector v : vectors) {
			vector.add(v.x, v.y);
		}
		return vector;
	}
	

	public static Vector Add(List<Vector> vectors) {
		Vector vector = new Vector(0, 0);
		for(Vector v : vectors) {
			vector.add(v.x, v.y);
		}
		return vector;
	}
	
	
	public void scale(double scalar) {
		this.x *= scalar;
		this.y *= scalar;
	}
	
	public Vector Scale(double scalar) {
		Vector vector = this.clone();
		vector.scale(scalar);
		return vector;
	}
	
	public double getLength() {
		return Math.sqrt(x*x + y*y);
	}
	
	public void normalize() {
		if(x == 0.0 || y == 0.0) return;
		double lenInv = 1.0 / getLength();
		x *= lenInv;
		y *= lenInv;
	}

	public Vector Normalize() {
		Vector vector = this.clone();
		vector.normalize();
		return vector;
	}
	
	/**
	 * Returns the heading of the flight leg. 0° is equivalent to direction NORTH (flying from bottom to top)
	 * @return
	 */
	public double getHeading() {
		double angle = Math.toDegrees(Math.atan2(y, x));
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
		return Math.atan2(y, x);
	}
	
	@Override
	public String toString() {
		return "Vector [x=" + x + ", y=" + y + "]";
	}

	@Override
	public Vector clone() {
		return new Vector(x, y);
	}
}
