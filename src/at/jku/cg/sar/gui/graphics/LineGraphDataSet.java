package at.jku.cg.sar.gui.graphics;

import java.awt.Color;
import java.util.List;

import at.jku.cg.sar.util.Curve;
import at.jku.cg.sar.util.Curve.CurvePoint;

public class LineGraphDataSet {

	private final String name;
	private final Color color;
	private final Curve curve;
		
	public LineGraphDataSet(String name, Color color) {
		this(name, color, new Curve());
	}
	
	public LineGraphDataSet(String name, Color color, Curve curve) {
		this.name = name;
		this.color = color;
		this.curve = curve;
	}

	public void addPoint(double x, double y) {
		curve.addPoint(x, y);
	}	

	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}
	
	public double getValue(double x) {
		return curve.evaluate(x, false);
	}

	public List<CurvePoint> getPoints() {
		return curve.getPoints();
	}

	public double minX() {
		return curve.minX();
	}
	
	public double maxX() {
		return curve.maxX();
	}
	
	public double minY() {
		return curve.minY();
	}
	
	public double maxY() {
		return curve.maxY();
	}
	
	public int size() {
		return curve.size();
	}

	
}
