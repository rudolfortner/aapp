package at.jku.cg.sar.pathfinder.misc;

import java.util.Objects;

import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.drone.DroneDiscrete;

public class GridFinder extends PathFinder {

	private final boolean closest;
	private final boolean maxFirst;
	
	public GridFinder(boolean closest, boolean maxFirst) {
		super(PathFinderType.DISCRETE_ITERATIVE);
		this.closest = closest;
		this.maxFirst = maxFirst;
	}

	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		if(drone.visitedAll()) return null;
		
		if(maxFirst && drone.getStepCount() == 0) {
			double maxProb = 0.0;
			int maxX = drone.getX(), maxY = drone.getY();
			boolean foundMax = false;
			
			for(int x = 0; x < drone.getWidth(); x++) {
				for(int y = 0; y < drone.getHeight(); y++) {
					
					double prob = drone.getProbability(x, y);
					if(prob > maxProb) {
						foundMax = true;
						maxProb = prob;
						maxX = x;
						maxY = y;
					}
				}
			}
			if(foundMax) return new PathFinderResult(maxX, maxY, true);
		}
		
		for(int dist = 0; dist < Integer.MAX_VALUE; dist++) {
			
			boolean found = false;
			double closestDistance = Double.MAX_VALUE;
			int closestX = 0, closestY = 0;
			
			for(int dx = -dist; dx <= dist; dx++) {
				for(int dy = -dist; dy <= dist; dy++) {
					
					int newX = drone.getX() + dx;
					int newY = drone.getY() + dy;
					
					if(newX < 0 || newX >= drone.getWidth()) continue;
					if(newY < 0 || newY >= drone.getHeight()) continue;
					if(newX == drone.getX() && newY == drone.getY()) continue;					
					if(drone.isVisited(newX, newY)) continue;
					
					if(closest) {
						double distance = Math.sqrt(Math.pow(newX - drone.getX(), 2.0) + Math.pow(newY - drone.getY(), 2.0));
						if(distance < closestDistance) {
							closestDistance = distance;
							closestX = newX;
							closestY = newY;
							found = true;
						}
					}else {
						return new PathFinderResult(newX, newY, false);
					}					
				}
			}

			if(closest && found) return new PathFinderResult(closestX, closestY, false);
		}
		
		return null;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(closest, maxFirst);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		GridFinder other = (GridFinder) obj;
		return closest == other.closest && maxFirst == other.maxFirst;
	}

	@Override
	public String getName() {
		String closest = this.closest ? "closest" : "-";
		String maxFirst = this.maxFirst ? "maxFirst" : "-";
		return "Grid Finder (%s) (%s)".formatted(closest, maxFirst);
	}

}
