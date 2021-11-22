package at.jku.cg.sar.pathfinder.misc;

import java.util.Objects;

import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.drone.DroneDiscrete;

public class SpiralFinder extends PathFinder {
	
	private final boolean ccw;
	
	public SpiralFinder() {
		this(false);
	}
	
	public SpiralFinder(boolean ccw) {
		super(PathFinderType.DISCRETE_ITERATIVE);
		this.ccw = ccw;
	}
	

	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		
		// Current position
		int cx = drone.getX();
		int cy = drone.getY();

		int cdx, cdy;
		// Current movement direction
		if(drone.getStepCount() == 0) {
			cdx = 1;
			cdy = 0;
		}else {
			cdx = drone.getX() - drone.getPreviousX();
			cdy = drone.getY() - drone.getPreviousY();
			
			// Handle fallback jumps greater than 1
			if(Math.abs(cdx) > 1 || Math.abs(cdy) > 1) {
				int max = Math.max(Math.abs(cdx), Math.abs(cdy));
				cdx = cdx / max;
				cdy = cdy / max;
			}
			if(Math.abs(cdx)+Math.abs(cdy) > 1) {
				cdx = 1;
				cdy = 0;
			}
		}
		
		// Change movement
		int[] left, right;
		if(ccw) {
			left = movementCCW(cdx, cdy);
			right = movementCW(cdx, cdy);
		}else {
			left = movementCW(cdx, cdy);
			right = movementCCW(cdx, cdy);
		}
		int ldx = left[0];
		int ldy = left[1];
		int rdx = right[0];
		int rdy = right[1];
		
		// Continue CW direction
		if(drone.inBounds(cx+rdx, cy+rdy) && !drone.isVisited(cx+rdx, cy+rdy)) {
			return new PathFinderResult(cx+rdx, cy+rdy, false);
		}
		
		// Continue same direction
		if(drone.inBounds(cx+cdx, cy+cdy) && !drone.isVisited(cx+cdx, cy+cdy)) {
			return new PathFinderResult(cx+cdx, cy+cdy, false);
		}

		// Continue CCW direction
		if(drone.inBounds(cx+ldx, cy+ldy) && !drone.isVisited(cx+ldx, cy+ldy)) {
			return new PathFinderResult(cx+ldx, cy+ldy, false);
		}
		
		// Continue nearest cell (to avoid dead ends!)
		if(!drone.visitedAll()) {
			int closestX = 0, closestY = 0;
			double closest = Double.POSITIVE_INFINITY;
			
			for(int x = 0; x < drone.getWidth(); x++) {
				for(int y = 0; y < drone.getHeight(); y++) {
					if(x == cx && y == cy) continue;
					if(drone.isVisited(x, y)) continue;
					
					double distance = Math.sqrt(Math.pow(cx-x, 2.0) + Math.pow(cy-y, 2.0));
					if(distance < closest) {
						closest = distance;
						closestX = x;
						closestY = y;
					}
				}
			}
			return new PathFinderResult(closestX, closestY, true);
		}
		
		return null;
	}

	private int[] movementCCW(int dx, int dy) {
		if(dx ==  1 && dy ==  0) return new int[] { 0, -1};
		if(dx ==  0 && dy == -1) return new int[] {-1,  0};
		if(dx == -1 && dy ==  0) return new int[] { 0,  1};
		if(dx ==  0 && dy ==  1) return new int[] { 1,  0};
		throw new IllegalStateException(dx + "/" + dy);
	}
	
	private int[] movementCW(int dx, int dy) {
		if(dx ==  1 && dy ==  0) return new int[] { 0,  1};
		if(dx ==  0 && dy == -1) return new int[] { 1,  0};
		if(dx == -1 && dy ==  0) return new int[] { 0, -1};
		if(dx ==  0 && dy ==  1) return new int[] {-1,  0};
		throw new IllegalStateException(dx + "/" + dy);
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(ccw);
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
		SpiralFinder other = (SpiralFinder) obj;
		return ccw == other.ccw;
	}

	@Override
	public String getName() {
		if(ccw) return "Spiral Finder CCW";
		return "Spiral Finder";
	}

}
