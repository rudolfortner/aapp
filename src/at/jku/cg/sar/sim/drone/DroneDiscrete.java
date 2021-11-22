package at.jku.cg.sar.sim.drone;

import at.jku.cg.sar.core.grid.Grid;

public class DroneDiscrete extends DroneBase<Integer> {

	// MAP DATA
	private final int width, height;
	private final Grid<Double> probabilities;
	private final Grid<Integer> visitCount;

	// LOCATION DATA moved to DroneBase
	
	public DroneDiscrete(Grid<Double> probabilities, Integer x, Integer y) {
		super(x, y);
		// INIT MAP DATA
		this.width			= probabilities.getWidth();
		this.height			= probabilities.getHeight();
		this.probabilities	= probabilities.clone();
		this.visitCount		= new Grid<>(width, height, 0);
		visit(x, y);
		this.stepCount = 0;
	}
	
	public DroneDiscrete(Grid<Double> probabilities, Grid<Integer> visitCounts, Integer x, Integer y) {
		super(x, y);
		// INIT MAP DATA
		this.width			= probabilities.getWidth();
		this.height			= probabilities.getHeight();
		this.probabilities	= probabilities.clone();
		this.visitCount		= visitCounts.clone();
		visit(x, y);
		this.stepCount = 0;
	}
		
	public DroneDiscrete(DroneDiscrete drone) {
		super(drone);
		
		// COPY MAP DATA
		this.width = drone.width;
		this.height = drone.height;
		this.probabilities = drone.probabilities.clone();
		this.visitCount = drone.visitCount.clone();
	}

	/**
	 * Changes the Drone's location
	 * @param x
	 * @param y
	 */
	public void visit(Integer x, Integer y) {
		super.visit(x, y);
				
		this.probability = this.probabilities.get(x, y);
		this.probabilities.set(x, y, 0.0);
		this.visitCount.set(x, y, visitCount.get(x, y) + 1);
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
		return this.probabilities.get(x, y);
	}
	
	public boolean isVisited(int x, int y) {
		if(x < 0 || x >= width || y < 0 || y >= height) throw new IllegalArgumentException();
		return this.visitCount.get(x, y) > 0;
	}
	
	public boolean inBounds(int x, int y) {
		return !(x < 0 || x >= width || y < 0 || y >= height);
	}
	
	public Grid<Double> getProbabilities(){
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

	@Override
	public DroneDiscrete clone() {
		return new DroneDiscrete(this);
	}

	@Override
	public String toString() {
		return "DroneDiscrete [width=" + width + ", height=" + height + ", probabilities=" + probabilities
				+ ", visitCount=" + visitCount + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + height;
		result = prime * result + ((probabilities == null) ? 0 : probabilities.hashCode());
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
		DroneDiscrete other = (DroneDiscrete) obj;
		if(height != other.height)
			return false;
		if(probabilities == null) {
			if(other.probabilities != null)
				return false;
		}else if(!probabilities.equals(other.probabilities))
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
