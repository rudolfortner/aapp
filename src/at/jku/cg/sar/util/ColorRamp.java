package at.jku.cg.sar.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorRamp {

	private final List<ColorRampEntry> colors;

	public ColorRamp() {
		this.colors = new ArrayList<>();
	}
	
	public static double clamp(double value, double min, double max) {
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	
	public static Color interpolate(double x, double x0, Color y0, double x1, Color y1) {
		double red		= Interpolation.Linear(x, x0, y0.getRed(), x1, y1.getRed());
		double green	= Interpolation.Linear(x, x0, y0.getGreen(), x1, y1.getGreen());
		double blue		= Interpolation.Linear(x, x0, y0.getBlue(), x1, y1.getBlue());

		int R = (int) clamp(red, 0.0, 255.0);
		int G = (int) clamp(green, 0.0, 255.0);
		int B = (int) clamp(blue, 0.0, 255.0);
		
		return new Color(R, G, B);
	}
	
	public void add(double value, Color color) {
		colors.add(new ColorRampEntry(value, color));
		colors.sort((c0, c1) -> (int) (c0.value - c1.value));
	}
	
	public double min() {
		return colors.stream().mapToDouble(entry -> entry.value).min().orElse(0.0);
	}
	
	public double max() {
		return colors.stream().mapToDouble(entry -> entry.value).max().orElse(1.0);
	}
	
	public int size() {
		return this.colors.size();
	}
	
	public ColorRampEntry get(int index) {
		return this.colors.get(index);
	}
	
	public Color evaluate(double value) {
		if(colors.size() == 0) return Color.red;
		if(value <= colors.get(0).value) return colors.get(0).color;
		
		for(int h = 0; h < colors.size()-1; h++) {
			ColorRampEntry c0 = colors.get(h);
			ColorRampEntry c1 = colors.get(h + 1);

			if(c0.value == value) return c0.color;
			if(c1.value == value) return c1.color;

			// INTERPOLATE
			if(c0.value < value && value < c1.value) {
				return interpolate(value, c0.value, c0.color, c1.value, c1.color);
			}				
		}
		return Color.RED;
	}
	
	public void print() {
		for(ColorRampEntry color :  colors) {
			System.out.printf("%05f :  %s\n", color.value, color.color.toString());
		}
	}

	
	
	
	public final class ColorRampEntry {

		private final double value;
		private final Color color;
		
		public ColorRampEntry(double value, Color color) {
			super();
			this.value = value;
			this.color = color;
		}
		
		public double getValue() {
			return value;
		}
		
		public Color getColor() {
			return color;
		}
	}

}
