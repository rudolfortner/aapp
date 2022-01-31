package at.jku.cg.sar.pathfinder;

import java.util.List;

import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneContinous;
import at.jku.cg.sar.sim.drone.DroneDiscrete;

/**
 * {@link PathFinder} is the main class each pathfinding algorithm should extend from.
 * For each {@link PathFinderType} there is a separate method that has to be used.
 * @author ortner
 */
public abstract class PathFinder implements Comparable<PathFinder> {
	
	protected final PathFinderType type;
	protected SimulatorSettings settings;
	
	public PathFinder(PathFinderType type) {
		this.type = type;
		this.settings = null;
	}
	
	/**
	 * When type is set to {@link PathFinderType#DISCRETE_ITERATIVE} this method is called in a iterative fashion
	 * @param drone Current state of the drone (position and probability map)
	 * @return {@link PathFinderResult} representing the next cell to visit
	 */
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * When type is set to {@link PathFinderType#DISCRETE_FULL} this method is called once and solves for visiting the whole map
	 * @param drone Current state of the drone (position and probability map)
	 * @return List of {@link PathFinderResult} for visiting the whole map
	 */
	public List<PathFinderResult> solveFull(DroneDiscrete drone) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * When type is set to {@link PathFinderType#CONTINOUS_ITERATIVE} this method is called in an iterative fashion
	 * @param drone Current state of the drone (position, heading and probability map)
	 * @return {@link PathFinderResult} representing the next heading to fly
	 */
	public PathFinderResult nextContinous(DroneContinous drone) {
		throw new UnsupportedOperationException();
	}
	
	public PathFinderType getType() {
		return type;
	}
	
	public PathFinder newInstance() {
		return this;
	}
	
	public PathFinder newInstance(SimulatorSettings settings) {
		this.settings = settings;
		return this;
	}
	
	public abstract String getName();
	
	

	@Override
	public int compareTo(PathFinder o) {
		return this.getName().compareTo(o.getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		PathFinder other = (PathFinder) obj;
		if(type != other.type)
			return false;
		return true;
	}
}
