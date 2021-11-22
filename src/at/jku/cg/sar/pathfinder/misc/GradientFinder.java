package at.jku.cg.sar.pathfinder.misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.pathfinder.problem.ProblemNode;
import at.jku.cg.sar.pathfinder.problem.ProblemSpace;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.SimpleTrajectory;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;

public class GradientFinder extends PathFinder {

	public GradientFinder() {
		super(PathFinderType.DISCRETE_ITERATIVE);
	}	
	
	@Override
	public PathFinderResult nextDiscrete(DroneDiscrete drone) {
		if(drone.visitedAll()) return null;
		
		GradientSearchSpace space = new GradientSearchSpace(drone, settings);
	
		// Fringe holds already expanded nodes Queue -> BFS
		Queue<GradientNode> fringe = new LinkedList<>();
		
		// Start Node
		GradientNode root = space.getStartNode();
		fringe.add(root);
		
		// Store current node which has maximum gradient
		GradientNode maxGradientNode = root;
		
		while(fringe.size() > 0) {
			if(Thread.interrupted()) return null;
			
			GradientNode node = fringe.poll();
			if(node == null) break;
//			System.err.println("Fringe %d at depth %d and max %f".formatted(fringe.size(), node.getDepth(), maxGradientNode.gradient));
			// Check for new maximum
			if(node.gradient > maxGradientNode.gradient) {
				maxGradientNode = node;				
			}			
			
			// Expand node
			fringe.addAll(space.expand(node));
		}
		
		GradientNode current = maxGradientNode;
		while(current.getParent() != null && current.getParent().getParent() != null) {
			current = (GradientNode) current.getParent();
		}
		return new PathFinderResult(current.x, current.y, false);
	}
	
	
	private static final class GradientSearchSpace extends ProblemSpace<GradientNode> {

		private final DroneDiscrete drone;
		private final SimulatorSettings settings;
				
		public GradientSearchSpace(DroneDiscrete drone, SimulatorSettings settings) {
			super();
			this.drone = drone;
			this.settings = settings;
		}

		@Override
		public GradientNode getStartNode() {
			return new GradientNode(drone.getProbabilities(), drone.getVisited(), drone.getX(), drone.getY(), new SimpleTrajectory(settings));
		}

		@Override
		public boolean isGoal(GradientNode node) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public List<GradientNode> expand(GradientNode node) {
			boolean all = false;
			
//			return expandFancy(node);
			return expandNeighbours(node);
//			if(all) return expandAll(node);
//			else return expandRandom(node);
		}
		
		private List<GradientNode> expandAll(GradientNode node){
			List<GradientNode> expanded = new ArrayList<>();
			if(node.getDepth() > 1) return expanded;
			
			for(int x = 0; x < drone.getWidth(); x++) {
				for(int y = 0; y < drone.getHeight(); y++) {
					if(node.visited.get(x, y)) continue;
					if(x == node.x && y == node.y) continue;
					
					GradientNode next = new GradientNode(node, x, y);
					expanded.add(next);
					
					// TODO PRUNING
				}
			}
			return expanded;
		}
		
		private List<GradientNode> expandRandom(GradientNode node) {
			List<GradientNode> expanded = new ArrayList<>();
			Random random = new Random();

			List<GridValue<Boolean>> unvisited = new ArrayList<>(node.visited.getEqual(false));
			int childs = unvisited.size();
			if(node.getDepth() > 0) {
				int value = (int) Math.pow(4, node.getDepth());
				int min = 0;
				int max = Math.max(1, childs / value);
				childs = max > min ? min + random.nextInt(max - min) : min;
			}
			
			
			for(int i = 0; i < childs; i++) {
				int rand = random.nextInt(unvisited.size());
				GridValue<Boolean> randNext = unvisited.remove(rand);
			
				GradientNode next = new GradientNode(node, randNext.getX(), randNext.getY());
				expanded.add(next);
			}
			
			return expanded;
		}
		
		private List<GradientNode> expandFancy(GradientNode node) {
			List<GradientNode> expanded = new ArrayList<>();
			if(node.getDepth() > 4) return expanded;
			
			Grid<Double> unvisited = node.probs.clone();
			node.visited.getEqual(true).forEach(v -> unvisited.set(v.getX(), v.getY(), 0.0));
			
			int childs = node.visited.getEqual(false).size();
			int exp = (int) Math.pow(4, node.getDepth());
			int newChilds = Math.max(1, childs / exp);
			newChilds = 6-node.getDepth();
			
			Set<GridValue<Double>> locations = new HashSet<>();
			locations.addAll(getHighest(unvisited, newChilds));
			locations.addAll(getNearest(unvisited, node.x, node.y, newChilds));
			
			for(GridValue<Double> value : locations) {
				GradientNode next = new GradientNode(node, value.getX(), value.getY());
				expanded.add(next);
			}
			
			return expanded;
		}
		
		private List<GradientNode> expandNeighbours(GradientNode node) {
			List<GradientNode> expanded = new ArrayList<>();
			if(node.getDepth() > 4) return expanded;
			
			for(int r = 1; r < Math.max(node.probs.getWidth(), node.probs.getHeight()); r++) {
				
				for(int dx = -r; dx <= r; dx++) {
					for(int dy = -r; dy <= r; dy++) {
						int x = node.x + dx;
						int y = node.y + dy;
						if(x < 0 || x >= node.probs.getWidth()) continue;
						if(y < 0 || y >= node.probs.getHeight()) continue;
						if(node.visited.get(x, y)) continue;
						if(x == node.x && y == node.y) continue;
						
						GradientNode next = new GradientNode(node, x, y);
						expanded.add(next);
					}
				}
				if(expanded.size() > 0) break;
			}
						
			return expanded;
		}

		@Override
		public double heuristic(GradientNode node) {
			return 0;
		}
		
	}
	
	private static List<GridValue<Double>> getHighest(Grid<Double> grid, int count){
		List<GridValue<Double>> highest = grid.getValues().stream()
			.filter(v -> v.getValue() > 0.0)
			.sorted((v0, v1) -> Double.compare(v0.getValue(), v1.getValue()))
			.limit(count)
			.collect(Collectors.toUnmodifiableList());
		
		return highest;
	}
	
	private static List<GridValue<Double>> getNearest(Grid<Double> grid, int x, int y, int count){
		List<GridValue<Double>> nearest = grid.getValues().stream()
			.filter(v -> v.getValue() > 0.0)
			.sorted((v0, v1) -> {
				double dist0 = Math.sqrt(Math.pow(v0.getX()-x, 2.0) + Math.pow(v0.getY()-y, 2.0));
				double dist1 = Math.sqrt(Math.pow(v1.getX()-x, 2.0) + Math.pow(v1.getY()-y, 2.0));
				return Double.compare(dist0, dist1);
			})
			.limit(count)
			.collect(Collectors.toUnmodifiableList());
		
		return nearest;
	}
	
	private static final class GradientNode extends ProblemNode {
		final double accumProb, accumTime, gradient;
		
		// Map Data
		final int x, y;
		final Grid<Double> probs;
		final Grid<Boolean> visited;
		final TrajectoryPlanner planner;		
		
		// ROOT NODE
		public GradientNode(Grid<Double> probs, Grid<Boolean> visited, int x, int y, TrajectoryPlanner planner) {
			super(null, 0.0);
			
			this.probs = probs;
			this.visited = visited.clone();
			
			this.x = x;
			this.y = y;
			
			this.planner = planner.newInstance();
			
			FlightPath<WorldFlightLeg> path = new FlightPath<>();
			path.appendLegs(this.planner.next(new PathFinderResult(x, y, false)));
			
			this.visited.set(x, y, true);
			this.accumProb = probs.get(x, y);
			this.accumTime = path.getDuration();
			if(this.accumTime > 0) this.gradient = accumProb / accumTime;
			else this.gradient = 0.0;
		}
		
		public GradientNode(GradientNode parent, int nextX, int nextY) {
			super(parent, 0.0);
			
			this.probs = parent.probs;
			this.visited = parent.visited.clone();
			
			this.x = nextX;
			this.y = nextY;
			
			this.planner = parent.planner.clone();
			
			FlightPath<WorldFlightLeg> path = new FlightPath<>();
			path.appendLegs(this.planner.next(new PathFinderResult(x, y, false)));

			this.visited.set(x, y, true);
			this.accumProb = parent.accumProb + probs.get(nextX, nextY);
			this.accumTime = parent.accumTime + path.getDuration();
			this.gradient = accumProb / accumTime;
		}		
	}

	@Override
	public String getName() {
		return "Gradient Finder";
	}
}
