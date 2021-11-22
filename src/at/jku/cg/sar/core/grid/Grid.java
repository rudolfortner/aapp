package at.jku.cg.sar.core.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Grid<Data extends Comparable<Data>> implements Cloneable {

	private final int width, height;
	private final Object[][] data;
	
	public Grid(int width, int height) {
		this.width = width;
		this.height = height;
		this.data = new Object[width][height];
	}
	
	public Grid(int width, int height, Data initial) {
		this.width = width;
		this.height = height;
		this.data = new Object[width][height];
		
		setAll(initial);
	}
	
	public void set(int x, int y, Data value) {
		this.data[x][y] = value;
	}
	
	public void setAll(Data value) {
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				this.data[x][y] = value;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public Data get(int x, int y) {
		return (Data) data[x][y];
	}
	
	@SuppressWarnings("unchecked")
	public GridValue<Data> getValue(int x, int y){
		return new GridValue<Data>(x, y, (Data) data[x][y]);
	}
	
	@SuppressWarnings("unchecked")
	public List<GridValue<Data>> getValues(){
		List<GridValue<Data>> values = new ArrayList<>(width*height);
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				values.add(new GridValue<Data>(x, y, (Data) data[x][y]));
			}
		}
		return Collections.unmodifiableList(values);
	}
	
	@SuppressWarnings("unchecked")
	public List<Data> getData(){
		Set<Data> set = new HashSet<>();
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				set.add((Data) data[x][y]);
			}
		}
		return Collections.unmodifiableList(new ArrayList<>(set));
	}
	
	@SuppressWarnings("unchecked")
	public List<GridValue<Data>> getEqual(Data equal){
		List<GridValue<Data>> values = new ArrayList<>(width*height);
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(data[x][y].equals(equal)) values.add(new GridValue<Data>(x, y, (Data) data[x][y]));
			}
		}
		return Collections.unmodifiableList(values);
	}
	
	@SuppressWarnings("unchecked")
	public List<GridValue<Data>> getNotEqual(Data equal){
		List<GridValue<Data>> values = new ArrayList<>(width*height);
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(!data[x][y].equals(equal)) values.add(new GridValue<Data>(x, y, (Data) data[x][y]));
			}
		}
		return Collections.unmodifiableList(values);
	}
	
	public GridValue<Data> min() {
		return getValues().stream().min((v0, v1) -> v0.getValue().compareTo(v1.getValue())).get();
	}
	
	public List<GridValue<Data>> listMin() {
		Data min = minValue();
		return getValues().stream().filter(v -> v.getValue().equals(min)).collect(Collectors.toUnmodifiableList());
	}
	
	public Data minValue() {
		return min().getValue();
	}
	
	public GridValue<Data> max() {
		return getValues().stream().max((v0, v1) -> v0.getValue().compareTo(v1.getValue())).get();
	}
	
	public List<GridValue<Data>> listMax() {
		Data max = maxValue();
		return getValues().stream().filter(v -> v.getValue().equals(max)).collect(Collectors.toUnmodifiableList());
	}
	
	public Data maxValue() {
		return max().getValue();
	}
	
	public int count(Data value) {
		int count = 0;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(data[x][y].equals(value)) count++;
			}
		}
		return count;
	}
	
	
	@SuppressWarnings("unchecked")
	public void modify(Function<Data, Data> function) {
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				data[x][y] = function.apply((Data) data[x][y]);
			}
		}
	}
	
	

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(data);
		result = prime * result + height;
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
		Grid<?> other = (Grid<?>) obj;
		if(!Arrays.deepEquals(data, other.data))
			return false;
		if(height != other.height)
			return false;
		if(width != other.width)
			return false;
		return true;
	}

	@Override
	public Grid<Data> clone() {
		Grid<Data> grid = new Grid<>(this.width, this.height);
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				grid.data[x][y] = data[x][y];
			}
		}
		
		return grid;
	}
}
