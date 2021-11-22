package at.jku.cg.sar.pathfinder;

import java.util.List;

import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneContinous;
import at.jku.cg.sar.sim.drone.DroneDiscrete;

public abstract class PathFinder implements Comparable<PathFinder> {
	
	protected final PathFinderType type;
	protected SimulatorSettings settings;
	
	public PathFinder(PathFinderType type) {
		this.type = type;
		this.settings = null;
	}
	
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		throw new UnsupportedOperationException();
	}
	
	public List<PathFinderResult> solveFull(DroneDiscrete drone) {
		throw new UnsupportedOperationException();
	}
	
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
