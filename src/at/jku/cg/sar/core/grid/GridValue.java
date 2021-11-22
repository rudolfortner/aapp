package at.jku.cg.sar.core.grid;

public class GridValue<Data extends Comparable<Data>> extends GridPoint {

	private final Data value;
	
	public GridValue(int x, int y, Data prob) {
		super(x, y);
		this.value = prob;
	}

	public Data getValue() {
		return value;
	}
}
