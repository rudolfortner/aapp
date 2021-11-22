package at.jku.cg.sar.pathfinder.astar;

import java.util.ArrayList;
import java.util.List;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.pathfinder.apf.AttractionApproach;
import at.jku.cg.sar.pathfinder.problem.AStar;
import at.jku.cg.sar.pathfinder.problem.ProblemNode;
import at.jku.cg.sar.pathfinder.problem.ProblemSpace;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.SimpleTrajectory;

/**
 * Closest maximum is selected and A* is used to find a path to this location.
 * Metric is more refined
 * @author ortner
 *
 */
public class AStarAttracted extends PathFinder {

	public AStarAttracted() {
		super(PathFinderType.DISCRETE_ITERATIVE);
	}

	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		
		if(drone.visitedAll()) {
			System.err.println("Visited all");
			return null;
		}
		
		// Select the next Goal
		Grid<Double> A = AttractionApproach.createAttractionMapExp(drone.getProbabilities(), drone.getX(), drone.getY());
		GridValue<Double> max = AttractionApproach.selectPoint(A, drone.getX(), drone.getY());
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
		
		System.err.println("Next go to " + first.drone.getX() + " " + first.drone.getY());
//		System.exit(0);
		return new PathFinderResult(first.drone.getX(), first.drone.getY(), false);
	}

	@Override
	public String getName() {
		return "A* Attracted";
	}
	
	
	private final class SearchNode extends ProblemNode {

		final DroneDiscrete drone;
		
		public SearchNode(SearchNode parent, double cost, DroneDiscrete drone) {
			super(parent, cost);
			this.drone = drone;
		}

		@Override
		public String toString() {
			return "SearchNode [drone=" + drone + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((drone == null) ? 0 : drone.hashCode());
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
			SearchNode other = (SearchNode) obj;
			if(drone == null) {
				if(other.drone != null)
					return false;
			}else if(!drone.equals(other.drone))
				return false;
			return true;
		}		
	}
	
	private final double evaluate(int fromX, int fromY, int toX, int toY, boolean fastFlight) {
		SimpleTrajectory t = new SimpleTrajectory(settings);

		FlightPath<WorldFlightLeg> path = new FlightPath<>();
		path.appendLegs(t.next(new PathFinderResult(fromX, fromY, false)));
		path.appendLegs(t.next(new PathFinderResult(toX, toY, true)));

		return path.getDuration();
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
			return new SearchNode(null, 0.0, drone);
		}

		@Override
		public boolean isGoal(SearchNode node) {
			if(node == null) return false;
			return node.drone.getX() == goalX && node.drone.getY() == goalY;
		}

		@Override
		public List<SearchNode> expand(SearchNode node) {
			List<SearchNode> successors = new ArrayList<>();
			if(node == null) return successors;
			
			for(int x = 0; x < drone.getWidth(); x++) {
				for(int y = 0; y < drone.getHeight(); y++) {
					if(x == node.drone.getX() && y == node.drone.getY()) continue;
					if(drone.isVisited(x, y)) continue;
					
//					double prob = node.drone.getProbability(x, y);					
					DroneDiscrete drone = node.drone.clone();
					drone.visit(x, y);
					
					double costsSlow = evaluate(node.drone.getX(), node.drone.getY(), x, y, false);
					double costsFast = evaluate(node.drone.getX(), node.drone.getY(), x, y, true);

					successors.add(new SearchNode(node, node.getCost()+costsSlow, drone));
					successors.add(new SearchNode(node, node.getCost()+costsFast, drone));
				}
			}

			return successors;
		}

		@Override
		public double heuristic(SearchNode node) {
			return Math.pow(node.drone.getX() - goalX, 2.0) + Math.pow(node.drone.getY() - goalY, 2.0);
		}
	}
}
