package at.jku.cg.sar.pathfinder;

import at.jku.cg.sar.core.grid.Grid;

@Deprecated
public class PathFinderData implements Cloneable {

	public static final int TILE_WIDTH = 30;	// meters
	
	protected final int width, height;
	protected final Grid<Double> probabilities;
	protected final Grid<Boolean> visited;	
	
	public PathFinderData(int width, int height, Grid<Double> probabilities, Grid<Boolean> visited) {
		this.width = width;
		this.height = height;
		this.probabilities = probabilities;
		this.visited = visited;
	}
	
	public PathFinderData(int width, int height) {
		this.width = width;
		this.height = height;
		
		// Allocate data
		this.probabilities = new Grid<>(width, height);
		this.visited = new Grid<>(width, height);
		
		// Init data just to be sure
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				probabilities.set(x, y, 0.0);
				visited.set(x, y, false);
			}
		}
	}
	
	public Grid<Double> getProbabilities(){
		return probabilities;
	}
	
	public Grid<Boolean> getVisited(){
		return visited;
	}

	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setProbability(int x, int y, double probability) {
		if(x < 0 || x >= width || y < 0 || y >= height) throw new IllegalArgumentException();
		this.probabilities.set(x, y, probability);
	}
	
	public double getProbability(int x, int y) {
		if(x < 0 || x >= width || y < 0 || y >= height) throw new IllegalArgumentException();
		return this.probabilities.get(x, y);
	}
	
	public boolean isVisited(int x, int y) {
		if(x < 0 || x >= width || y < 0 || y >= height) throw new IllegalArgumentException();
		return this.visited.get(x, y);
	}
	
	public void setVisited(int x, int y, boolean visited) {
		if(x < 0 || x >= width || y < 0 || y >= height) throw new IllegalArgumentException();
		this.visited.set(x, y, visited);
	}
	
	public int countVisited() {
		int count = 0;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(visited.get(x, y)) count++;
			}
		}
		return count;
	}
	
	public boolean visitedAll() {
		if(countVisited() == width*height) return true;
		return false;
	}


	@Override
	public PathFinderData clone() {
		PathFinderData map = new PathFinderData(this.width, this.height);
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				map.probabilities.set(x, y, this.probabilities.get(x, y));
				map.visited.set(x, y, this.visited.get(x, y));
			}
		}
		
		return map;
	}

}
