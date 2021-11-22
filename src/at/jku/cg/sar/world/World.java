package at.jku.cg.sar.world;

import java.util.ArrayList;
import java.util.List;

import at.jku.cg.sar.core.grid.Grid;

public class World implements Cloneable {

	public static final int TILE_WIDTH = 30;	// meters
	
	protected final String name;
	protected final int width, height;
	protected final List<Grid<Double>> layers;

	public World(String name, Grid<Double> mainLayer) {
		this(name, mainLayer.getWidth(), mainLayer.getHeight());
		this.layers.add(mainLayer);
	}
	
	public World(Grid<Double> mainLayer) {
		this(mainLayer.getWidth(), mainLayer.getHeight());
		this.layers.add(mainLayer);
	}
	
	public World(int width, int height) {
		this("World", width, height);
	}
	
	public World(String name, int width, int height) {
		this.name = name;
		this.width = width;
		this.height = height;		
		this.layers = new ArrayList<>();
	}
	
	public String getName() {
		return name;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

	public Grid<Double> getProbabilities(){
		return this.layers.get(0).clone();
	}
	
	public Grid<Double> getProbabilities(int layer){
		if(layer < 0 || layer >= layers.size()) return new Grid<Double>(width, height, 0.0);
		return this.layers.get(layer).clone();
	}
	
	public double getProbability(int x, int y) {
		if(x < 0 || x >= width || y < 0 || y >= height) throw new IllegalArgumentException();
		return this.layers.get(0).get(x, y);
	}
	
	public double getProbability(int x, int y, int layer) {
		if(x < 0 || x >= width || y < 0 || y >= height) throw new IllegalArgumentException();
		if(layer < 0 || layer >= layers.size()) return 0.0;
		return this.layers.get(0).get(x, y);
	}

	@Override
	public World clone() {
		World world = new World(name, this.width, this.height);
		
		for(Grid<Double> layer : layers) {
			world.layers.add(layer.clone());
		}
		
		return world;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + ((layers == null) ? 0 : layers.hashCode());
		result = prime * result + width;
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
		World other = (World) obj;
		if(height != other.height)
			return false;
		if(layers == null) {
			if(other.layers != null)
				return false;
		}else if(!layers.equals(other.layers))
			return false;
		if(width != other.width)
			return false;
		return true;
	}
	
}
