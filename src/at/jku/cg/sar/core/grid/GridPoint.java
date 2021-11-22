package at.jku.cg.sar.core.grid;

public class GridPoint {

	private final int x, y;
	
	public GridPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public double distance(GridPoint point) {
		return this.distance(point.getX(), point.getY());
	}
	
	public double distance(double x, double y) {
		return Math.sqrt(Math.pow(x - this.x, 2.0) + Math.pow(y - this.y, 2.0));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		GridPoint other = (GridPoint) obj;
		if(x != other.x)
			return false;
		if(y != other.y)
			return false;
		return true;
	}
	
}
