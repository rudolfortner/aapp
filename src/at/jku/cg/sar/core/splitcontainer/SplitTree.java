package at.jku.cg.sar.core.splitcontainer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SplitTree extends SplitContainer {

	double value;
	
	List<SplitTree> childs;
	boolean hasChilds = false;
	
	public SplitTree(SplitContainer parent, double left, double top, double right, double bottom, double value) {
		super(parent, left, top, right, bottom);
		this.value = value;
	}
	
	public SplitTree(SplitContainer parent, SplitTree tree) {
		this(parent, tree.left, tree.top, tree.right, tree.bottom, tree.value);
		if(hasChilds) {
			for(SplitTree c : tree.childs) {
				childs.add(new SplitTree(this, c));
			}
		}
	}

	@Override
	public double getArea() {
		double width = Math.abs(right - left);
		double height= Math.abs(bottom - top);
		return width * height;
	}
	
	@Override
	public void putRectangle(double left, double top, double right, double bottom, double value) {
		if(left > this.right || right < this.left) return;
		if(top > this.bottom || bottom < this.top) return;
		
		if(hasChilds) {
			for(SplitContainer child : childs) {
				child.putRectangle(left, top, right, bottom, value);
			}
		}else {
			splitPut(left, top, right, bottom, value);
		}
	}
	
	private void splitPut(double left, double top, double right, double bottom, double value) {
		SplitOperation putOperation = new SplitOperation() {
			
			@Override
			public double execute(SplitTree child, boolean isTarget, double splitValue) {
				if(isTarget) {
					child.setValue(splitValue + value);
				}else {
					child.setValue(splitValue);
				}
				return 0.0;
			}
		};
		splitOperation(left, top, right, bottom, putOperation);
	}

	@Override
	public double collectRectangle(double left, double top, double right, double bottom) {
		if(left > this.right || right < this.left) return 0.0;
		if(top > this.bottom || bottom < this.top) return 0.0;
		
		if(hasChilds) {
			double sum = 0.0;
			for(SplitContainer child : childs) {
				sum += child.collectRectangle(left, top, right, bottom);
			}
			return sum;
		}else {
			return splitCollect(left, top, right, bottom);
		}
	}
	
	private double splitCollect(double left, double top, double right, double bottom) {
		SplitOperation collectOperation = new SplitOperation() {
			
			@Override
			public double execute(SplitTree child, boolean isTarget, double splitValue) {
				if(isTarget) {
					return splitValue;
				}else {
					child.setValue(splitValue);
				}
				return 0.0;
			}
		};
		
		return splitOperation(left, top, right, bottom, collectOperation);
	}

	private double splitOperation(double left, double top, double right, double bottom, SplitOperation ops) {
		if(Math.abs(left-right) <= 0.0) return 0.0;
		if(Math.abs(top-bottom) <= 0.0) return 0.0;
		
		if(left > right || top > bottom) {
			System.err.println("Left %f \t Top %f \t Right %f \t Bottom %f".formatted(left, top, right, bottom));
			throw new IllegalArgumentException();
		}
		
		double collectedValue = 0.0;
		
		// Clamp collection area into current cell
		left = Math.max(left, this.left);
		right = Math.min(right, this.right);
		top = Math.max(top, this.top);
		bottom = Math.min(bottom, this.bottom);
		
		this.childs = new ArrayList<>();
		hasChilds = true;
		
		for(int x = 0; x < 3; x++) {
			if(x == 0 && left == this.left) continue;
			if(x == 2 && right == this.right) continue;
			
			for(int y = 0; y < 3; y++) {
				if(y == 0 && top == this.top) continue;
				if(y == 2 && bottom == this.bottom) continue;
								
				double newLeft, newRight;
				if(x == 0) {
					newLeft = this.left;
					newRight = left;
				}else if(x == 1) {
					newLeft = left;
					newRight = right;
				}else {
					newLeft = right;
					newRight=this.right;
				}
				
				double newTop, newBottom;
				if(y == 0) {
					newTop = this.top;
					newBottom = top;
				}else if(y == 1) {
					newTop = top;
					newBottom = bottom;
				}else {
					newTop = bottom;
					newBottom = this.bottom;
				}
				
				// Avoid NaN's when calculating ratio
				if(newLeft == newRight) continue;
				if(newTop == newBottom) continue;
				
				// Generate the new child
				SplitTree child = new SplitTree(this, newLeft, newTop, newRight, newBottom, 0.0);
				double ratio = child.getArea() / this.getArea();
				double prob = this.value * ratio;

				if(Double.isNaN(prob)) {
					System.err.println("NaN with area %f".formatted(child.getArea()));
					System.err.println("Left %f \t Top %f \t Right %f \t Bottom %f".formatted(left, top, right, bottom));
					System.err.println("NewLeft %f \t NewTop %f \t NewRight %f \t NewBottom %f".formatted(newLeft, newTop, newRight, newBottom));
					throw new IllegalStateException();
				}

				boolean isTarget = (newLeft == left && newTop == top && newRight == right && newBottom == bottom);
				collectedValue += ops.execute(child, isTarget, prob);
				childs.add(child);
			}
		}
		this.value = 0.0;
		return collectedValue;
	}

	@Override
	public List<SplitContainer> getContainers() {
		if(hasChilds) {
			List<SplitContainer> containers = new ArrayList<>();
			for(SplitContainer c : childs) {
				containers.addAll(c.getContainers());
			}
			return containers;
		}else {
			return List.of(this);
		}
	}

	@Override
	public void collapse() {
		if(hasChilds) {
			// Collapse all childs
			for(SplitContainer c : childs) {
				c.collapse();
			}
			
			// Check if all childs can be collapse into parent
			for(SplitContainer c : childs) {
				if(c.getContainers().size() > 1) return;
				if(c.getValue() != 0.0) return;
			}
			
			// PERFORM COLLAPS
			this.childs = null;
			this.hasChilds = false;
			this.value = 0.0;
		}
	}

	@Override
	public double getValue() {
		if(hasChilds) {
			return childs.stream().mapToDouble(c -> c.getValue()).sum();
		}else {
			return value;
		}
	}
	
	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public SplitContainer getContainerAt(double x, double y) {
		if(x < left || x > right) return null;
		if(y < top || y > bottom) return null;
		
		if(hasChilds) {
			for(SplitTree child : childs) {
				SplitContainer below = child.getContainerAt(x, y);
				if(below != null) return below;
			}
			throw new IllegalStateException();
		}else {
			return this;
		}
	}

	@Override
	public double min() {
		if(hasChilds) {
			double min = Double.POSITIVE_INFINITY;
			for(SplitContainer child : childs) {
				min = Math.min(min, child.min());
			}
			return min;
		}else {
			return value;
		}
	}

	@Override
	public double max() {
		if(hasChilds) {
			double max = Double.NEGATIVE_INFINITY;
			for(SplitContainer child : childs) {
				max = Math.min(max, child.max());
			}
			return max;
		}else {
			return value;
		}
	}

	@Override
	public void modify(Function<Double, Double> function) {
		if(hasChilds) {
			for(SplitContainer child : childs) {
				child.modify(function);
			}
		}else {
			this.value = function.apply(this.value);
		}
	}
}
