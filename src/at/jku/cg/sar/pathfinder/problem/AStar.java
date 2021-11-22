package at.jku.cg.sar.pathfinder.problem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.jku.cg.sar.util.MyPriorityQueue;

public class AStar {
	
	public static <NodeType extends ProblemNode> List<NodeType> solve(ProblemSpace<NodeType> problemSpace) {
		
		MyPriorityQueue<NodeType> fringe = new MyPriorityQueue<>();
		Set<NodeType> visited = new HashSet<NodeType>();
		
		// Add start node to the fringe
		fringe.put(0.0, problemSpace.getStartNode());
		
		while(fringe.size() > 0) {
			if(Thread.interrupted()) return new ArrayList<>();
//			System.err.println("Fringe: %d , Visited: %d".formatted(fringe.size(), visited.size()));
			NodeType node = fringe.pop();

			if(visited.contains(node)) continue;
			
			if(problemSpace.isGoal(node)) return reconstructPath(node);
						
			visited.add(node);

			for(NodeType n : problemSpace.expand(node)) {
				double cost = n.getCost() + problemSpace.heuristic(n);
				fringe.put(cost, n);
			}
		}
				
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <NodeType extends ProblemNode> List<NodeType> reconstructPath(NodeType end) {
		
		List<NodeType> path = new ArrayList<>();
		NodeType current = end;
		while(current != null) {
			path.add(0, current);
			current = (NodeType) current.getParent();
		}
		
		return path;
	}
}
