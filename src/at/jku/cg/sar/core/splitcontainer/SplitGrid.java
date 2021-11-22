package at.jku.cg.sar.core.splitcontainer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import at.jku.cg.sar.core.grid.Grid;

public class SplitGrid extends SplitContainer implements Cloneable {

	int gridWidth, gridHeight;
	double cellSize;
	
	SplitTree[][] childs;
	
	public SplitGrid(Grid<Double> grid, double cellSize) {
		super(null, 0.0, 0.0, grid.getWidth() * cellSize, grid.getHeight() * cellSize);
		
		this.cellSize = cellSize;
		this.gridWidth = grid.getWidth();
		this.gridHeight = grid.getHeight();
		
		this.childs = new SplitTree[gridWidth][gridHeight];
		
		for(int x = 0; x < gridWidth; x++) {
			for(int y = 0; y < gridHeight; y++) {
				double left  = cellSize * x;
				double top   = cellSize * y;
				double right = cellSize + left;
				double bottom= cellSize + top;
				childs[x][y] = new SplitTree(this, left, top, right, bottom, grid.get(x, y));
			}
		}
	}
	
	public SplitGrid(SplitGrid grid) {
		super(null, 0.0, 0.0, grid.gridWidth * grid.cellSize, grid.gridHeight * grid.cellSize);
		
		this.gridWidth = grid.gridWidth;
		this.gridHeight = grid.gridHeight;
		this.gridWidth = grid.gridWidth;
		
		this.childs = new SplitTree[gridWidth][gridHeight];
		
		for(int x = 0; x < gridWidth; x++) {
			for(int y = 0; y < gridHeight; y++) {
				childs[x][y] = new SplitTree(this, grid.childs[x][y]);
			}
		}
	}
	
	

	public double getCellSize() {
		return cellSize;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public int getGridHeight() {
		return gridHeight;
	}

	@Override
	public double getArea() {
		return gridWidth * gridWidth * gridHeight * gridWidth;
	}

	
	@Override
	public double collectRectangle(double left, double top, double right, double bottom) {
		double sum = 0.0;
		int startX = (int) Math.floor(left / cellSize - 0.5);
		int startY = (int) Math.floor(top / cellSize - 0.5);
		int endX   = (int) Math.ceil(right / cellSize - 0.5);
		int endY   = (int) Math.ceil(bottom / cellSize - 0.5);
		
		startX	= Math.max(startX, 0);
		startY	= Math.max(startY, 0);
		endX	= Math.min(endX, gridWidth-1);
		endY	= Math.min(endY, gridHeight-1);
		
		for(int x = startX; x <= endX; x++) {
			for(int y = startY; y <= endY; y++) {
				sum += childs[x][y].collectRectangle(left, top, right, bottom);
			}
		}
		return sum;
	}
	
	@Override
	public void putRectangle(double left, double top, double right, double bottom, double value) {
		int startX = (int) Math.floor(left / cellSize - 0.5);
		int startY = (int) Math.floor(top / cellSize - 0.5);
		int endX   = (int) Math.ceil(right / cellSize - 0.5);
		int endY   = (int) Math.ceil(bottom / cellSize - 0.5);
		
		startX	= Math.max(startX, 0);
		startY	= Math.max(startY, 0);
		endX	= Math.min(endX, gridWidth-1);
		endY	= Math.min(endY, gridHeight-1);
		
		for(int x = startX; x <= endX; x++) {
			for(int y = startY; y <= endY; y++) {
				childs[x][y].putRectangle(left, top, right, bottom, value);
			}
		}
	}
	
	public SplitContainer getCell(int x, int y) {
		return childs[x][y];
	}

	@Override
	public List<SplitContainer> getContainers() {
		List<SplitContainer> containers = new ArrayList<>();
		for(int x = 0; x < gridWidth; x++) {
			for(int y = 0; y < gridHeight; y++) {
				containers.addAll(childs[x][y].getContainers());
			}
		}
		return containers;
	}

	@Override
	public void collapse() {
		for(int x = 0; x < gridWidth; x++) {
			for(int y = 0; y < gridHeight; y++) {
				childs[x][y].collapse();
			}
		}
	}

	@Override
	public SplitGrid clone() {
		return new SplitGrid(this);
	}

	@Override
	public double getValue() {
		double sum = 0.0;
		for(int x = 0; x < gridWidth; x++) {
			for(int y = 0; y < gridHeight; y++) {
				sum += childs[x][y].getValue();
			}
		}
		return sum;
	}

	@Override
	public SplitContainer getContainerAt(double x, double y) {
		if(x < 0.0 || x > width) return null;
		if(y < 0.0 || y > height) return null;

		int cx = (int) (x / cellSize);
		int cy = (int) (y / cellSize);
		
		return childs[cx][cy].getContainerAt(x, y);
	}

	@Override
	public double min() {
		double min = Double.POSITIVE_INFINITY;
		for(int x = 0; x < gridWidth; x++) {
			for(int y = 0; y < gridHeight; y++) {
				min = Math.min(min, childs[x][y].min());
			}
		}
		return min;
	}

	@Override
	public double max() {
		double max = Double.NEGATIVE_INFINITY;
		for(int x = 0; x < gridWidth; x++) {
			for(int y = 0; y < gridHeight; y++) {
				max = Math.max(max, childs[x][y].max());
			}
		}
		return max;
	}

	@Override
	public void modify(Function<Double, Double> function) {
		for(int x = 0; x < gridWidth; x++) {
			for(int y = 0; y < gridHeight; y++) {
				childs[x][y].modify(function);
			}
		}
	}

}
