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
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.SimpleTrajectory;

public class AStarGlobalAdvancedProb extends PathFinder {

	private final int maxDepth;
	
	public AStarGlobalAdvancedProb(int maxDepth) {
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
		return new PathFinderResult(first.drone.getX(), first.drone.getY(), first.fastFlight);
	}

	@Override
	public String getName() {
		return "A* Global Advanced Prob (%d)".formatted(maxDepth);
	}
	
	
	private final class SearchNode extends ProblemNode {

		final DroneDiscrete drone;
		final boolean fastFlight;
		
		public SearchNode(SearchNode parent, double cost, DroneDiscrete drone, boolean fast) {
			super(parent, cost);
			this.drone = drone;
			this.fastFlight = fast;
		}

		@Override
		public String toString() {
			return "SearchNode [drone=" + drone + ", fastFlight=" + fastFlight + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((drone == null) ? 0 : drone.hashCode());
			result = prime * result + (fastFlight ? 1231 : 1237);
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
			if(fastFlight != other.fastFlight)
				return false;
			return true;
		}		
	}
	
	private final double evaluate(int fromX, int fromY, int toX, int toY, boolean fastFlight) {
		SimpleTrajectory t = new SimpleTrajectory(settings);

		FlightPath<WorldFlightLeg> path = new FlightPath<>();
		path.appendLegs(t.next(new PathFinderResult(fromX, fromY, false)));
		path.appendLegs(t.next(new PathFinderResult(toX, toY, fastFlight)));

		return path.getDuration();
	}
	
	private final class SearchSpace extends ProblemSpace<SearchNode> {

		private final DroneDiscrete drone;
		
		public SearchSpace(DroneDiscrete drone) {
			this.drone = drone;
		}

		@Override
		public SearchNode getStartNode() {
			return new SearchNode(null, 0.0, drone, false);
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
			
			boolean fullMap = true;
			
			if(fullMap) {
				// FULL MAP
				for(int x = 0; x < drone.getWidth(); x++) {
					for(int y = 0; y < drone.getHeight(); y++) {
						if(x == node.drone.getX() && y == node.drone.getY()) continue;
						if(drone.isVisited(x, y)) continue;
						
						createSuccessors(successors, node, x, y);
					}
				}
			}else {
				// MINIMAL VERSION (ONLY NEAREST NEIGHBOR)
				int maxDistance = Math.max(drone.getWidth(), drone.getHeight());
				for(int dist = 1; dist <= maxDistance; dist++) {
					boolean found = false;
					
					for(int dx = -dist; dx <= dist; dx++) {
						for(int dy = -dist; dy <= dist; dy++) {
							
							int x = drone.getX() + dx;
							int y = drone.getY() + dy;
							
							if(x < 0 || x >= drone.getWidth()) continue;
							if(y < 0 || y >= drone.getHeight()) continue;
							if(x == drone.getX() && y == drone.getY()) continue;					
							if(drone.isVisited(x, y)) continue;						
							found = true;

							createSuccessors(successors, node, x, y);						
						}
					}
					
					if(found) break;
				}
			}
			return successors;
		}
		
		private void createSuccessors(List<SearchNode> successors, SearchNode node, int x, int y) {
			double prob = node.drone.getProbability(x, y);
			
			DroneDiscrete drone = node.drone.clone();
			drone.visit(x, y);
			
			double timeSlow = evaluate(node.drone.getX(), node.drone.getY(), x, y, false);
			double timeFast = evaluate(node.drone.getX(), node.drone.getY(), x, y, true);
			
			// PROB TEST
			double costsSlow = timeSlow - 30.0*prob;
			double costsFast = timeFast - 30.0*prob;
			

			double newCostSlow = node.getCost() + costsSlow;
			double newCostFast = node.getCost() + costsFast;

			successors.add(new SearchNode(node, newCostSlow, drone, false));
			successors.add(new SearchNode(node, newCostFast, drone, true));
		}

		@Override
		public double heuristic(SearchNode node) {
			double heuristic = 0;
			heuristic = 30.0 * (node.drone.getWidth() * node.drone.getHeight() - node.drone.countVisited());
			heuristic -= 2 * node.drone.getProbabilities().getNotEqual(0.0).stream().mapToDouble(v -> v.getValue()).sum();
			heuristic -= 5 * node.drone.getProbability();
			return heuristic;
		}
	}
}
