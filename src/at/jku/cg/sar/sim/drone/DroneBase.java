package at.jku.cg.sar.sim.drone;

import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;

public abstract class DroneBase<DataType extends Number> implements Cloneable {
	
	// LOCATION DATA
	protected DataType previousX, previousY, x, y;
	protected double probability;	// At current location (map is already 0.0)
	protected double track;
	protected int stepCount;
	
	public DroneBase(DataType x, DataType y) {		
		// INIT LOCATION DATA
		this.previousX = this.x = x;
		this.previousY = this.y = y;
		this.track = 0.0;
		this.stepCount = 0;
	}
		
	public DroneBase(DroneBase<DataType> drone) {		
		// COPY LOCATION DATA
		this.previousX = drone.previousX;
		this.previousY = drone.previousY;
		this.x = drone.x;
		this.y = drone.y;
		this.probability = drone.probability;
		this.track = drone.track;
		this.stepCount = drone.stepCount;
	}

	/**
	 * Changes the Drone's location
	 * @param x
	 * @param y
	 */
	public void visit(DataType x, DataType y) {
		this.previousX = this.x;
		this.previousY = this.y;
		this.x = x;
		this.y = y;
		this.track = (new WorldFlightLeg(previousX.doubleValue(), previousY.doubleValue(), x.doubleValue(), y.doubleValue(), 0)).getHeading();
		this.stepCount++;
	}

	public DataType getX() {
		return x;
	}

	public void setX(DataType x) {
		this.x = x;
	}

	public DataType getY() {
		return y;
	}

	public void setY(DataType y) {
		this.y = y;
	}

	public DataType getPreviousX() {
		return previousX;
	}

	public DataType getPreviousY() {
		return previousY;
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
	public String toString() {
		return "DroneBase [previousX=" + previousX + ", previousY=" + previousY + ", x=" + x + ", y=" + y
				+ ", probability=" + probability + ", track=" + track + ", stepCount=" + stepCount + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((previousX == null) ? 0 : previousX.hashCode());
		result = prime * result + ((previousY == null) ? 0 : previousY.hashCode());
		long temp;
		temp = Double.doubleToLongBits(probability);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + stepCount;
		temp = Double.doubleToLongBits(track);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		DroneBase<?> other = (DroneBase<?>) obj;
		if(previousX == null) {
			if(other.previousX != null)
				return false;
		}else if(!previousX.equals(other.previousX))
			return false;
		if(previousY == null) {
			if(other.previousY != null)
				return false;
		}else if(!previousY.equals(other.previousY))
			return false;
		if(Double.doubleToLongBits(probability) != Double.doubleToLongBits(other.probability))
			return false;
		if(stepCount != other.stepCount)
			return false;
		if(Double.doubleToLongBits(track) != Double.doubleToLongBits(other.track))
			return false;
		if(x == null) {
			if(other.x != null)
				return false;
		}else if(!x.equals(other.x))
			return false;
		if(y == null) {
			if(other.y != null)
				return false;
		}else if(!y.equals(other.y))
			return false;
		return true;
	}
	
}
