package at.jku.cg.sar.world;

public class WorldPoint {

	private final double x, y;
	
	public WorldPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public double distance(WorldPoint point) {
		return this.distance(point.getX(), point.getY());
	}
	
	public double distance(double x, double y) {
		return Math.sqrt(Math.pow(x - this.x, 2.0) + Math.pow(y - this.y, 2.0));
	}
}
