package at.jku.cg.sar.pathfinder.problem;

public abstract class ProblemNode {

	private final ProblemNode parent;	// State we are coming from
	private final double cost;	// Costs of the action leading to this node
	private final int depth;
	
	// State and Action in the actual implementation
	
	public ProblemNode(ProblemNode parent, double cost) {
		this.parent = parent;
		this.depth = parent == null ? 0 : parent.depth+1;
		this.cost = cost;
	}

	public ProblemNode getParent() {
		return parent;
	}

	public double getCost() {
		return cost;
	}

	public int getDepth() {
		return depth;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(cost);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + depth;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return hashCode() == obj.hashCode();
	}
}
