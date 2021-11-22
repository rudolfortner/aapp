package at.jku.cg.sar.pathfinder.astar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.pathfinder.problem.AStar;
import at.jku.cg.sar.pathfinder.problem.ProblemNode;
import at.jku.cg.sar.pathfinder.problem.ProblemSpace;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.SimpleTrajectory;

public class AStarGlobalTest extends PathFinder {

	private final int maxDepth;
	
	public AStarGlobalTest(int maxDepth) {
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
		return "A* Global TEST (%d)".formatted(maxDepth);
	}
	
	
	private final class SearchNode extends ProblemNode {
		
		final SimpleTrajectory planner;
		final FlightPath<WorldFlightLeg> path;
		
		final DroneDiscrete drone;
		
		public SearchNode(SearchNode parent, double cost, SimpleTrajectory planner, FlightPath<WorldFlightLeg> path, DroneDiscrete drone) {
			super(parent, cost);
			
			this.planner = planner;
			this.path = path;
			
			this.drone = drone;
		}
		
		public double getPathProb() {
			SearchNode current = this;
			double prob = 0.0;
			while(current != null) {
				prob += current.drone.getProbability();
				current = (SearchNode) current.getParent();
			}
			return prob;
		}

		@Override
		public String toString() {
			return "SearchNode [planner=" + planner + ", path=" + path + ", drone=" + drone + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((drone == null) ? 0 : drone.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			result = prime * result + ((planner == null) ? 0 : planner.hashCode());
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
			if(path == null) {
				if(other.path != null)
					return false;
			}else if(!path.equals(other.path))
				return false;
			if(planner == null) {
				if(other.planner != null)
					return false;
			}else if(!planner.equals(other.planner))
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
			SimpleTrajectory planner = new SimpleTrajectory(settings);
			FlightPath<WorldFlightLeg> path = new FlightPath<WorldFlightLeg>();
			path.appendLegs(planner.next(new PathFinderResult(drone.getX(), drone.getY(), false)));
			return new SearchNode(null, 0.0, planner, path, drone);
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
					
					double prob = node.drone.getProbability(x, y);
					
					DroneDiscrete drone = node.drone.clone();
					drone.visit(x, y);					
					
					// CREATE SLOW FLIGHT
					SimpleTrajectory plannerSlow = node.planner.clone();
					FlightPath<WorldFlightLeg> pathSlow = new FlightPath<>(node.path);
					pathSlow.appendLegs(plannerSlow.next(new PathFinderResult(x, y, false)));

					double costsSlow = pathSlow.getDuration() / (node.getPathProb() + prob);
					successors.add(new SearchNode(node, costsSlow, plannerSlow, pathSlow, drone));
//					successors.add(new SearchNode(node, node.getCost()+1, null, null, newWorld, x, y, prob));
					
					
//					// CREATE FAST FLIGHT
					SimpleTrajectory plannerFast = node.planner.clone();
					FlightPath<WorldFlightLeg> pathFast = new FlightPath<>(node.path);
					pathFast.appendLegs(plannerFast.next(new PathFinderResult(x, y, true)));

					double costsFast = pathFast.getDuration() / (node.getPathProb() + prob);
					successors.add(new SearchNode(node, costsFast, plannerFast, pathFast, drone));
				}
			}

			return successors;
		}

		@Override
		public double heuristic(SearchNode node) {
			double heuristic = 0;
			heuristic = (node.drone.getWidth() * node.drone.getHeight() - node.drone.countVisited()) * 30.0;
//			heuristic *= 1.0 - node.probability;
			
			double probsRemaining = node.drone.getProbabilities().getNotEqual(0.0).stream()
					.mapToDouble(val -> val.getValue()).sum();
			
			heuristic = heuristic / probsRemaining;
			
//			for(GridValue<Double> value : node.world.getProbabilities().getNotEqual(0.0)) {				
//				double duration = evaluate(node.currentX, node.currentY, value.getX(), value.getY(), false);
//				heuristic += duration;
//			}
			
//			for(GridValue<Double> value : node.world.getProbabilities().getNotEqual(0.0)) {				
//				heuristic += value.getValue();
//			}

			
			
			
			heuristic += (new Random()).nextDouble();
			return heuristic;
		}
	}
}
