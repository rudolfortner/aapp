package at.jku.cg.sar.pathfinder.problem;

import java.util.List;

public abstract class ProblemSpace<Node extends ProblemNode> {

	public ProblemSpace() {
		// TODO Auto-generated constructor stub
	}

	public abstract Node getStartNode();
	public abstract boolean isGoal(Node node);
	public abstract List<Node> expand(Node node);
	public abstract double heuristic(Node node);
	
}
