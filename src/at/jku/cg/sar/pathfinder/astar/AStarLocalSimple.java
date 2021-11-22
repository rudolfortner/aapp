package at.jku.cg.sar.pathfinder.astar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.pathfinder.problem.AStar;
import at.jku.cg.sar.pathfinder.problem.ProblemNode;
import at.jku.cg.sar.pathfinder.problem.ProblemSpace;
import at.jku.cg.sar.sim.drone.DroneDiscrete;

/**
 * Closest maximum is selected and A* is used to find a path to this location.
 * Metric is more refined
 * @author ortner
 *
 */
public class AStarLocalSimple extends PathFinder {

	public AStarLocalSimple() {
		super(PathFinderType.DISCRETE_ITERATIVE);
	}

	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		
		if(drone.visitedAll()) {
			System.err.println("Visited all");
			return null;
		}
		
		// Select the next Goal
		List<GridValue<Double>> maxValues = drone.getProbabilities().listMax().stream()
				.filter(v -> !drone.isVisited(v.getX(), v.getY()))
				.sorted((v0, v1) -> Double.compare(v0.distance(drone.getX(), drone.getY()), v1.distance(drone.getX(), drone.getY())))
				.collect(Collectors.toUnmodifiableList());
		
		GridValue<Double> max = maxValues.get(0);
		System.err.println("Next location is %d/%d with value %f".formatted(max.getX(), max.getY(), max.getValue()));
		
		// A* for pathfinding to goal
		SearchSpace space = new SearchSpace(drone, max.getX(), max.getY());
		List<SearchNode> path = AStar.solve(space);
		System.err.println("AStar reconstruction finished");
		
		if(path.size() <= 1) {
			System.err.println("PATH TOO SHORT");
			return null;
		}
		SearchNode first = path.get(1);
		
		System.err.println("Next go to " + first.currentX + " " + first.currentY);
//		System.exit(0);
		return new PathFinderResult(first.currentX, first.currentY, false);
	}

	@Override
	public String getName() {
		return "A* Local Simple";
	}
	
	
	private final class SearchNode extends ProblemNode {

		final int currentX, currentY;
		
		public SearchNode(SearchNode parent, double cost, int currentX, int currentY) {
			super(parent, cost);
			this.currentX = currentX;
			this.currentY = currentY;
		}
		
		

		@Override
		public String toString() {
			return "SearchNode [currentX=" + currentX + ", currentY=" + currentY + "] " + this.hashCode();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + currentX;
			result = prime * result + currentY;
			return result;
		}

	}
	
	private final class SearchSpace extends ProblemSpace<SearchNode> {

		private final DroneDiscrete drone;
		private final int goalX, goalY;
		
		public SearchSpace(DroneDiscrete drone, int goalX, int goalY) {
			this.drone = drone;
			this.goalX = goalX;
			this.goalY = goalY;
		}

		@Override
		public SearchNode getStartNode() {
			return new SearchNode(null, 0.0, drone.getX(), drone.getY());
		}

		@Override
		public boolean isGoal(SearchNode node) {
			if(node == null) return false;
			return node.currentX == goalX && node.currentY == goalY;
		}

		@Override
		public List<SearchNode> expand(SearchNode node) {
			List<SearchNode> successors = new ArrayList<>();
			if(node == null) return successors;

			// ONLY NEIGHBOURS FOR NOW
			for(int dx = -1; dx <= 1; dx++) {
				for(int dy = -1; dy <= 1; dy++) {
					
					int newX = node.currentX + dx;
					int newY = node.currentY + dy;
					
					if(newX < 0 || newX >= drone.getWidth()) continue;
					if(newY < 0 || newY >= drone.getHeight()) continue;
					if(newX == drone.getX() && newY == drone.getY()) continue;
					
					double costs = node.getCost() + Math.pow(node.currentX - newX, 2.0) + Math.pow(node.currentY - newY, 2.0);
					
					successors.add(new SearchNode(node, costs, newX, newY));
				}
			}

			return successors;
		}

		@Override
		public double heuristic(SearchNode node) {
			return Math.pow(node.currentX - goalX, 2.0) + Math.pow(node.currentY - goalY, 2.0);
		}
	}
}
