package at.jku.cg.sar.pathfinder;

public class PathFinderResult {

	private final int posX, posY;
	private final boolean fastFlight;
	private final double heading;
	
	// FOR DISCRETE PATHFINDERS
	public PathFinderResult(int posX, int posY, boolean fastFlight) {
		this.posX = posX;
		this.posY = posY;
		this.fastFlight = fastFlight;
		this.heading = Double.NaN;
	}
	
	// FOR CONTINOUS PATHFINDERS
	public PathFinderResult(double heading) {
		this.posX = -1;
		this.posY = -1;
		this.fastFlight = false;
		this.heading = heading;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public boolean isFastFlight() {
		return fastFlight;
	}

	public double getHeading() {
		return heading;
	}
	
}
