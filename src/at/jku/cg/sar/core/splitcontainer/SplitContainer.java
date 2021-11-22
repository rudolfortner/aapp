package at.jku.cg.sar.core.splitcontainer;

import java.util.List;
import java.util.function.Function;

import at.jku.cg.sar.world.WorldPoint;

public abstract class SplitContainer implements Cloneable {

	protected final SplitContainer parent;
	protected final double top, right, bottom, left;
	protected final double width, height;
	
	public SplitContainer(SplitContainer parent, double left, double top, double right, double bottom) {
		this.parent = parent;
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		
		this.width = right - left;
		this.height = bottom - top;
	}

	public SplitContainer(SplitContainer parent, SplitContainer container) {
		this(parent, container.left, container.top, container.right, container.bottom);
	}

	public abstract double getArea();
	public abstract void collapse();
	public abstract double collectRectangle(double left, double top, double right, double bottom);
	public abstract void putRectangle(double left, double top, double right, double bottom, double value);
	public abstract List<SplitContainer> getContainers();
	
	public abstract SplitContainer getContainerAt(double x, double y);
	public abstract double getValue();

	public abstract double min();
	public abstract double max();
	public abstract void modify(Function<Double, Double> function);
	
	public double collectCircle(double centerX, double centerY, double radius) {
		return collectCircle(centerX, centerY, radius, 8);
	}	

	public double collectCircle(double centerX, double centerY, double radius, double maxStepWidth) {
		int stepCount = (int) Math.ceil(2.0 * radius / maxStepWidth);
		int resolution = (int) Math.ceil(Log2(stepCount));
		return collectCircle(centerX, centerY, radius, resolution);
	}
	
	private double Log2(double x) {
		return Math.log(x) / Math.log(2.0);
	}
	
	public double collectCircle(double centerX, double centerY, double radius, int resolution) {	
		int stepCount = 1 << resolution;
		double stepHeight = 2.0 * radius / stepCount;
		double sum = 0.0;
		
		for(int i = 0; i < stepCount/2; i++) {
			double top = centerY - radius + i * stepHeight;
			double bottom = top + stepHeight;
			
			double t2 = radius*radius - Math.pow(centerY - top, 2.0);
			if(t2 <= 0.0) continue;
			double x = Math.sqrt(t2);
//			System.err.println("%f %f %f".formatted(top, bottom, x));
			sum += collectRectangle(centerX - x, top, centerX + x, bottom);
		}
		for(int i = 0; i < stepCount/2; i++) {
			double top = centerY + i * stepHeight;
			double bottom = top + stepHeight;
			
			double t2 = radius*radius - Math.pow(centerY - bottom, 2.0);
			if(t2 <= 0.0) continue;
			double x = Math.sqrt(t2);
//			System.err.println("%f %f %f".formatted(top, bottom, x));
			sum += collectRectangle(centerX - x, top, centerX + x, bottom);
		}
		
		return sum;		
	}

	public double getValueAt(double x, double y) {
		SplitContainer container = getContainerAt(x, y);
		if(container == null) return 0.0;
		return container.getValue();
	}
	
	public WorldPoint getCornerTopLeft() {
		return new WorldPoint(left, top);
	}
	
	public WorldPoint getCornerTopRight() {
		return new WorldPoint(right, top);
	}
	
	public WorldPoint getCornerBottomLeft() {
		return new WorldPoint(left, bottom);
	}
	
	public WorldPoint getCornerBottomRight() {
		return new WorldPoint(right, bottom);
	}
	
	public double getCenterX() {
		return (left + right) / 2.0;
	}
	
	public double getCenterY() {
		return (top + bottom) / 2.0;
	}

	public SplitContainer getParent() {
		return parent;
	}

	public double getTop() {
		return top;
	}

	public double getRight() {
		return right;
	}

	public double getBottom() {
		return bottom;
	}

	public double getLeft() {
		return left;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}
	
}
