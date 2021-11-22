package at.jku.cg.sar.pathfinder.astar;

import java.util.ArrayList;
import java.util.List;

import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.pathfinder.problem.AStar;
import at.jku.cg.sar.pathfinder.problem.ProblemNode;
import at.jku.cg.sar.pathfinder.problem.ProblemSpace;
import at.jku.cg.sar.sim.drone.DroneDiscrete;

public class AStarGlobalSimple extends PathFinder {

	private final int maxDepth;
	
	public AStarGlobalSimple(int maxDepth) {
		super(PathFinderType.DISCRETE_ITERATIVE);
		this.maxDepth = maxDepth;
	}

	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		
		SearchSpace space = new SearchSpace(drone);
		
		List<SearchNode> path = AStar.solve(space);
		System.err.println("AStar finished");
		
		if(path.size() <= 1) return null;
		SearchNode first = path.get(1);
		
		System.err.println("Next go to " + first.drone.getX() + " " + first.drone.getY());
//		System.exit(0);
		return new PathFinderResult(first.drone.getX(), first.drone.getY(), false);
	}

	@Override
	public String getName() {
		return "A* Global Simple (%d)".formatted(maxDepth);
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
	
	private final class SearchSpace extends ProblemSpace<SearchNode> {

		private final DroneDiscrete drone;
		
		public SearchSpace(DroneDiscrete drone) {
			this.drone = drone;
		}

		@Override
		public SearchNode getStartNode() {
			return new SearchNode(null, 0.0, drone);
		}

		@Override
		public boolean isGoal(SearchNode node) {
			if(node == null) return false;
			return node.getDepth() >= maxDepth || node.drone.visitedAll();
		}

		@Override
		public List<SearchNode> expand(SearchNode node) {
			List<SearchNode> successors = new ArrayList<>();
			if(node == null) return successors;
			if(node.getDepth() > maxDepth) return successors;

			for(int x = 0; x < drone.getWidth(); x++) {
				for(int y = 0; y < drone.getHeight(); y++) {
					if(x == node.drone.getX() && y == node.drone.getY()) continue;
					if(drone.isVisited(x, y)) continue;
					
					DroneDiscrete drone = node.drone.clone();
					drone.visit(x, y);
					double costs = node.getCost() + Math.sqrt(Math.pow(x - node.drone.getX(), 2.0) + Math.pow(y - node.drone.getY(), 2.0));
					
					successors.add(new SearchNode(node, costs, drone));
				}
			}
			return successors;
		}

		@Override
		public double heuristic(SearchNode node) {
			return -node.drone.countVisited();
		}
	}
}
