package at.jku.cg.sar.core.splitcontainer;

public interface SplitOperation {
	public double execute(SplitTree child, boolean isTarget, double splitValue);
}
