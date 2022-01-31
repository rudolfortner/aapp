package at.jku.cg.sar.pathfinder.apf;

import java.util.ArrayList;
import java.util.List;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.drone.DroneDiscrete;

/**
 * Based on Attraction Approach from <a href="https://doi.org/10.1155/2018/6879419">"Intelligent UAV Map Generation and Discrete Path Planning for Search and Rescue Operations"</a>
 * @author ortner
 */
public class AttractionApproach extends PathFinder {

	public AttractionApproach() {
		super(PathFinderType.DISCRETE_ITERATIVE);
	}
	

	/**
	 * Creates an attraction map that equals the probability map
	 * @param grid Source grid (containing probabilities)
	 * @return Attraction map
	 */
	public static Grid<Double> createAttractionMap(Grid<Double> grid){
		Grid<Double> A = new Grid<>(grid.getWidth(), grid.getHeight());
		
		for(int x = 0; x < grid.getWidth(); x++) {
			for(int y = 0; y < grid.getHeight(); y++) {
				A.set(x, y, grid.get(x, y));
			}
		}		
		return A;
	}
	
	// Probability Map but weighted by exp(distance)
	/**
	 * Creates an attraction map that depends on a cells probability value
	 * weighted by the exponential of the euclidean distance in grid space.
	 * @param grid Source grid (containing probabilities)
	 * @param positionX Current X position
	 * @param positionY Current Y position
	 * @return Attraction map
	 */
	public static Grid<Double> createAttractionMapExp(Grid<Double> grid, int positionX, int positionY){
		Grid<Double> A = new Grid<>(grid.getWidth(), grid.getHeight());
		
		for(int x = 0; x < grid.getWidth(); x++) {
			for(int y = 0; y < grid.getHeight(); y++) {
				double prob = grid.get(x, y);
				
				double dist = Math.sqrt(Math.pow(x - positionX, 2.0) + Math.pow(y - positionY, 2.0));
				double weight = Math.exp(dist);
				
				A.set(x, y, prob / weight);
			}
		}		
		return A;
	}
	
	/**
	 * Calculates the density value at <b>positionX/positionY</b> for the given attraction map.
	 * All cells within the given radius (grid distance) are weighted by their distance and summed up to retrieve the density value
	 * @param data Attraction map
	 * @param positionX Target X position
	 * @param positionY Target Y position
	 * @param radius Radius to select how many nearby fields should be accounted for
	 * @return Density value
	 */
	public static double calculateDensity(Grid<Double> data, int positionX, int positionY, int radius) {
		double density = 0.0;
		
		for(int x = positionX-radius; x <= positionX+radius; x++) {
			for(int y = positionY-radius; y <= positionY+radius; y++) {
				if(x < 0 || x >= data.getWidth() || y < 0 || y >= data.getHeight()) continue;
				if(x == positionX && y == positionY) continue;
				
				double prob = data.get(x, y);
				double dist = Math.sqrt(Math.pow(x - positionX, 2.0) + Math.pow(y - positionY, 2.0));
				
				density += prob / dist;
			}
		}
		
		return density;
	}
	
	/**
	 * Selects a suitable point from given attraction map and current position.<br />
	 * The selection process looks like this:
	 * <ol>
	 * 	<li> Select point with the highest potential/attraction
	 * 	<li> If multiple points have the same highest value -> Calculate density of those points
	 * 	<li> Select point with highest density
	 * 	<li> If multiple points have the same density -> Select closest one from those points
	 * </ol>
	 * @param attractionMap Attraction map
	 * @param positionX Current X position
	 * @param positionY Current Y position
	 * @return {@link GridValue} representing the selected point
	 */
	public static GridValue<Double> selectPoint(Grid<Double> attractionMap, int positionX, int positionY) {
		List<GridValue<Double>> maxAttractedPoints = attractionMap.listMax();
		
		if(maxAttractedPoints.size() == 1) {
			// ONLY ONE MAXIMUM
			return maxAttractedPoints.get(0);
		}else {
			// MORE THAN ONE MAXIMUM (-> Search maximum density of those points)
			int radius = 1;
			List<GridValue<Double>> maxDensityPoints = new ArrayList<>();
			
			while(2*radius < Math.min(attractionMap.getWidth(), attractionMap.getHeight())) {
				maxDensityPoints.clear();
				
				for(GridValue<Double> maximum : maxAttractedPoints) {				
					double density = calculateDensity(attractionMap, maximum.getX(), maximum.getY(), radius);
					maxDensityPoints.add(new GridValue<Double>(maximum.getX(), maximum.getY(), density));
				}
				
				double maxDensity = maxDensityPoints.stream().mapToDouble(p -> p.getValue()).max().getAsDouble();
				long maxCount = maxDensityPoints.stream().filter(p -> p.getValue() == maxDensity).count();
				
				if(maxCount == 1) break;
				radius++;
			}		
			// Fallback if ALL points have the same density, use the closest one	
			if(maxDensityPoints.size() > 1) {
				maxDensityPoints.sort((p0, p1) -> {
					double dist0 = p0.distance(positionX, positionY);
					double dist1 = p1.distance(positionX, positionY);
					return Double.compare(dist0, dist1);
				});
			}
			return maxDensityPoints.get(0);			
		}		
		// NO MAXIMUM FOUND		
	}

	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		if(drone.visitedAll()) return null;
		
		Grid<Double> A = createAttractionMapExp(drone.getProbabilities(), drone.getX(), drone.getY());
		GridValue<Double> max = selectPoint(A, drone.getX(), drone.getY());
				
		return new PathFinderResult(max.getX(), max.getY(), false);
	}

	@Override
	public String getName() {
		return "Attraction Approach";
	}

}
