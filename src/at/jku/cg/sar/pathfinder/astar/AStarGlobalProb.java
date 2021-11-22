package at.jku.cg.sar.pathfinder.astar;

import java.util.ArrayList;
import java.util.List;

import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.pathfinder.problem.AStar;
import at.jku.cg.sar.pathfinder.problem.ProblemNode;
import at.jku.cg.sar.pathfinder.problem.ProblemSpace;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.FlightLeg;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.SimpleTrajectory;

public class AStarGlobalProb extends PathFinder {

	public AStarGlobalProb(int maxDepth) {
		super(PathFinderType.DISCRETE_FULL);
	}

	@Override
	public List<PathFinderResult> solveFull(DroneDiscrete drone) {
		List<PathFinderResult> result = new ArrayList<>();
		
		SearchSpace space = new SearchSpace(drone);

		List<SearchNode> path = AStar.solve(space);
		System.err.println("AStar finished");
		
		if(path.size() <= 1) return null;
		path.remove(0);
		
		for(SearchNode node : path) {
			result.add(new PathFinderResult(node.drone.getX(), node.drone.getY(), node.fastFlight));
		}
		return result;
	}

	@Override
	public String getName() {
		return "A* Global Prob";
	}
	
	
	private final class SearchNode extends ProblemNode {
		
		final SimpleTrajectory planner;
		
		final DroneDiscrete drone;
		final boolean fastFlight;
		
		final double accumTime, accumProbs, accumArea;
		
		public SearchNode(SearchNode parent, double cost, SimpleTrajectory planner, DroneDiscrete drone, double accumTime, double accumProbs, double accumArea, boolean fastFlight) {
			super(parent, cost);
			
			this.planner = planner;
			
			this.drone = drone;
			
			this.accumTime = accumTime;
			this.accumProbs = accumProbs;
			this.accumArea = accumArea;
			
			this.fastFlight = fastFlight;
		}

		@Override
		public String toString() {
			return "SearchNode [planner=" + planner + ", drone=" + drone + ", fastFlight=" + fastFlight + ", accumTime="
					+ accumTime + ", accumProbs=" + accumProbs + ", accumArea=" + accumArea + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			long temp;
			temp = Double.doubleToLongBits(accumArea);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(accumProbs);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(accumTime);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + ((drone == null) ? 0 : drone.hashCode());
			result = prime * result + (fastFlight ? 1231 : 1237);
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
			if(Double.doubleToLongBits(accumArea) != Double.doubleToLongBits(other.accumArea))
				return false;
			if(Double.doubleToLongBits(accumProbs) != Double.doubleToLongBits(other.accumProbs))
				return false;
			if(Double.doubleToLongBits(accumTime) != Double.doubleToLongBits(other.accumTime))
				return false;
			if(drone == null) {
				if(other.drone != null)
					return false;
			}else if(!drone.equals(other.drone))
				return false;
			if(fastFlight != other.fastFlight)
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
			if(planner.next(new PathFinderResult(drone.getX(), drone.getY(), false)).size() > 0) throw new IllegalStateException();
			return new SearchNode(null, 0.0, planner, drone, 0.0, 0.0, 0.0, false);
		}

		@Override
		public boolean isGoal(SearchNode node) {
			if(node == null) return false;
			return node.drone.visitedAll();
		}

		@Override
		public List<SearchNode> expand(SearchNode node) {
			List<SearchNode> successors = new ArrayList<>();
			if(node == null) return successors;
			
			for(int x = 0; x < drone.getWidth(); x++) {
				for(int y = 0; y < drone.getHeight(); y++) {
					if(x == node.drone.getX() && y == node.drone.getY()) continue;
					if(drone.isVisited(x, y)) continue;
					
					double prob = node.drone.getProbability(x, y);
					
					DroneDiscrete drone = node.drone.clone();
					drone.visit(x, y);					
					
					// CREATE SLOW FLIGHT
					SimpleTrajectory plannerSlow = node.planner.clone();
					double accumTime = node.accumTime;
					double accumProbs = node.accumProbs;
					double accumArea = node.accumArea;
					
					for(FlightLeg<?> leg : plannerSlow.next(new PathFinderResult(x, y, false))) {
						double dt = leg.getDuration();
						double oldProbs = accumProbs;
						
						accumTime += dt;
						if(leg.isScan()) {
							accumProbs += prob;
						}
						
						accumArea += (oldProbs + accumProbs) * dt / 2.0;
					}

					double costsSlow = -accumArea;
					successors.add(new SearchNode(node, costsSlow, plannerSlow, drone, accumTime, accumProbs, accumArea, false));
										
//					// CREATE FAST FLIGHT
					SimpleTrajectory plannerFast = node.planner.clone();
					accumTime = node.accumTime;
					accumProbs = node.accumProbs;
					accumArea = node.accumArea;
					
					for(FlightLeg<?> leg : plannerSlow.next(new PathFinderResult(x, y, false))) {
						double dt = leg.getDuration();
						double oldProbs = accumProbs;
						
						accumTime += dt;
						if(leg.isScan()) {
							accumProbs += prob;
						}
						
						accumArea += (oldProbs + accumProbs) * dt / 2.0;
					}

					double costsFast = -accumArea;
					successors.add(new SearchNode(node, costsFast, plannerFast, drone, accumTime, accumProbs, accumArea, true));
				}
			}

			return successors;
		}

		@Override
		public double heuristic(SearchNode node) {
			double heuristic = 0;
			heuristic = (node.drone.getWidth() * node.drone.getHeight() - node.drone.countVisited()) * 30.0;
//			heuristic *= 1.0 - node.probability;
			
//			double probsRemaining = node.world.getProbabilities().getNotEqual(0.0).stream()
//					.mapToDouble(val -> val.getValue()).sum();
//			
//			heuristic = heuristic / probsRemaining;
			
//			for(GridValue<Double> value : node.world.getProbabilities().getNotEqual(0.0)) {				
//				double duration = evaluate(node.currentX, node.currentY, value.getX(), value.getY(), false);
//				heuristic += duration;
//			}
			
//			for(GridValue<Double> value : node.world.getProbabilities().getNotEqual(0.0)) {				
//				heuristic += value.getValue();
//			}

//			for(GridValue<Double> value : node.world.getProbabilities().getNotEqual(0.0)) {	
//				double duration = evaluate(node.currentX, node.currentY, value.getX(), value.getY(), false);
//				
//				heuristic += value.getValue() * duration;
//			}
			
			
//			heuristic += (new Random()).nextDouble();
			return heuristic;
		}
	}
}
