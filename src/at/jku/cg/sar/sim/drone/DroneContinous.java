package at.jku.cg.sar.sim.drone;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.splitcontainer.SplitGrid;

public class DroneContinous extends DroneBase<Double> {

	// MAP DATA
	private final int width, height;
	private final double cellSize;
	private final SplitGrid probabilities;
	private final Grid<Integer> visitCount;
	
	private final double scanRadius;

	// LOCATION DATA moved to DroneBase
	
	public DroneContinous(Grid<Double> probabilities, double cellSize, Double x, Double y) {
		super(x, y);
		// INIT MAP DATA
		this.width = probabilities.getWidth();
		this.height = probabilities.getHeight();
		this.cellSize = cellSize;
		this.scanRadius = cellSize / 2.0;
		this.probabilities = new SplitGrid(probabilities, cellSize);
		this.visitCount = new Grid<>(width, height, 0);
		this.visit(x, y);
		this.stepCount = 0;
	}
		
	public DroneContinous(DroneContinous drone) {
		super(drone);
		
		// COPY MAP DATA
		this.width = drone.width;
		this.height = drone.height;
		this.cellSize = drone.cellSize;
		this.scanRadius = drone.scanRadius;
		this.probabilities = drone.probabilities.clone();
		this.visitCount = drone.visitCount.clone();
	}

	/**
	 * Changes the Drone's location
	 * @param x
	 * @param y
	 */
	public void visit(Double x, Double y) {
		super.visit(x, y);
				
		this.probability = this.probabilities.collectRectangle(x-scanRadius, y-scanRadius, x+scanRadius, y+scanRadius);
		this.probabilities.collapse();
		
		// TODO World to grid conversion correct?
		int gridX = (int) (x / cellSize - 0.5);
		int gridY = (int) (y / cellSize - 0.5);
		if(gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
			this.visitCount.set(gridX, gridY, visitCount.get(gridX, gridY) + 1);
		}
	}

	public int countVisited() {
		int count = 0;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(visitCount.get(x, y) > 0) count++;
			}
		}
		return count;
	}
	
	public boolean visitedAll() {
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(visitCount.get(x, y) == 0) return false;
			}
		}
		return true;
	}
	
	public double getProbability(int x, int y) {
		if(x < 0 || x >= width || y < 0 || y >= height) throw new IllegalArgumentException();
		return this.probabilities.getCell(x, y).getValue();
	}
	
	public boolean isVisited(int x, int y) {
		if(x < 0 || x >= width || y < 0 || y >= height) throw new IllegalArgumentException();
		return this.visitCount.get(x, y) > 0;
	}
	
	public boolean inBounds(int x, int y) {
		return !(x < 0 || x >= width || y < 0 || y >= height);
	}
	
	public SplitGrid getProbabilities(){
		return probabilities;
	}
	
	public Grid<Boolean> getVisited(){
		Grid<Boolean> v = new Grid<Boolean>(width, height, false);
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(visitCount.get(x, y) > 0) v.set(x, y, true);
			}
		}
		return v;
	}


	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public double getCellWidth() {
		return cellSize;
	}

	public Grid<Integer> getVisitCount() {
		return visitCount;
	}

	public double getTrack() {
		return track;
	}

	public int getStepCount() {
		return stepCount;
	}
	
	public double getProbability() {
		return probability;
	}

	public double getScanRadius() {
		return scanRadius;
	}

	@Override
	public DroneContinous clone() {
		return new DroneContinous(this);
	}

	@Override
	public String toString() {
		return "DroneContinous [width=" + width + ", height=" + height + ", cellSize=" + cellSize + ", probabilities="
				+ probabilities + ", visitCount=" + visitCount + ", scanRadius=" + scanRadius + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(cellSize);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + height;
		result = prime * result + ((probabilities == null) ? 0 : probabilities.hashCode());
		temp = Double.doubleToLongBits(scanRadius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((visitCount == null) ? 0 : visitCount.hashCode());
		result = prime * result + width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!super.equals(obj))
			return false;
		if(getClass() != obj.getClass())
			return false;
		DroneContinous other = (DroneContinous) obj;
		if(Double.doubleToLongBits(cellSize) != Double.doubleToLongBits(other.cellSize))
			return false;
		if(height != other.height)
			return false;
		if(probabilities == null) {
			if(other.probabilities != null)
				return false;
		}else if(!probabilities.equals(other.probabilities))
			return false;
		if(Double.doubleToLongBits(scanRadius) != Double.doubleToLongBits(other.scanRadius))
			return false;
		if(visitCount == null) {
			if(other.visitCount != null)
				return false;
		}else if(!visitCount.equals(other.visitCount))
			return false;
		if(width != other.width)
			return false;
		return true;
	}	
}
