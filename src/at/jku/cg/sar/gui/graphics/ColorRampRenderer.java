package at.jku.cg.sar.gui.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Locale;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.Function;

import at.jku.cg.sar.util.ColorRamp;
import at.jku.cg.sar.util.Interpolation;
import at.jku.cg.sar.util.TextDrawer;
import at.jku.cg.sar.util.TextDrawer.HorizontalAlignment;
import at.jku.cg.sar.util.TextDrawer.VerticalAlignment;

public class ColorRampRenderer {
	

	public static void render(Graphics2D g, ColorRamp ramp, int left, int top, int right, int bottom) {
		render(g, ramp, left, top, right, bottom, false);
	}
	
	public static void render(Graphics2D g, ColorRamp ramp, int left, int top, int right, int bottom, boolean horizontal) {
		render(g, ramp, left, top, right, bottom, horizontal, null);
	}
	

	public static void render(Graphics2D g, ColorRamp ramp, int left, int top, int right, int bottom, boolean horizontal, Font font) {
		render(g, ramp, left, top, right, bottom, horizontal, font, true);
	}
	
	public static void render(Graphics2D g, ColorRamp ramp, int left, int top, int right, int bottom, boolean horizontal,
			Font font, boolean showDecimals) {
		render(g, ramp, left, top, right, bottom, horizontal, font, showDecimals, value -> value);
	}
	
	public static void render(Graphics2D g, ColorRamp ramp, int left, int top, int right, int bottom, boolean horizontal,
			Font font, boolean showDecimals, Function<Double, Double> remapLabels) {
		render(g, ramp, left, top, right, bottom, horizontal, font, showDecimals, remapLabels, null);
	}
	
	public static void render(Graphics2D g, ColorRamp ramp, int left, int top, int right, int bottom, boolean horizontal,
			Font font, boolean showDecimals, Function<Double, Double> remapLabels, String label) {
		render(g, ramp, left, top, right, bottom, horizontal, font, showDecimals, 1, remapLabels, label);
	}

	public static void render(Graphics2D g, ColorRamp ramp, int left, int top, int right, int bottom, boolean horizontal,
			Font font, boolean showDecimals, int decimalCount, Function<Double, Double> remapLabels, String label) {
		Color oldColor = g.getColor();
		Font oldFont = g.getFont();
		
		// Color Bars
		if(horizontal) {
			for(int i = left; i < right; i++) {
				double value = Interpolation.Linear(i, left, ramp.min(), right, ramp.max());
				g.setColor(ramp.evaluate(value));
				g.drawLine(i, top, i, bottom);
			}
		}else {
			for(int i = top; i < bottom; i++) {
				double value = Interpolation.Linear(i, top, ramp.max(), bottom, ramp.min());
				g.setColor(ramp.evaluate(value));
				g.drawLine(left, i, right, i);
			}
		}
		
		
		// Border
		g.setColor(Color.BLACK);
		int width = right-left;
		int height = bottom-top;
		
		float strokeWidth = horizontal ? width/1024.0f : height/1024.0f;
		g.setStroke(new BasicStroke(strokeWidth));
		if(font != null) g.setFont(font);
		g.drawRect(left, top, width, height);
		
		int biggestLabel = 0;
		int offset = horizontal ? height / 16 : width / 16;;
		// Axis Labels
		for(int i = 0; i <= 10; i++) {
			double value = Interpolation.Linear(i, 10, ramp.max(), 0, ramp.min());
			value = remapLabels.apply(value);
			String valueString = String.format(Locale.ROOT, showDecimals ? "%."+decimalCount+"f" : "%.0f", value);			
			
			if(horizontal) {
				int x = (int) (left - (left-right) * i / 10.0);
				biggestLabel = Math.max(biggestLabel, g.getFontMetrics().getHeight());
				g.drawLine(x, bottom, x, bottom + offset);
				TextDrawer.drawString(g, valueString, x, bottom+offset, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
			}else {
				int y = (int) (bottom - (bottom-top) * i / 10.0);
				biggestLabel = Math.max(biggestLabel, g.getFontMetrics().stringWidth(valueString));
				g.drawLine(right, y, right + offset, y);
				TextDrawer.drawString(g, valueString, right+ 2*offset, y, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
			}
		}
		
		// Ramp Label
		if(label != null) {
			if(horizontal) {
				int centerX = left + (right - left) / 2;
				TextDrawer.drawString(g, label, centerX, bottom + 16 + biggestLabel + offset, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
			}else {
				int centerY = bottom + (top - bottom) / 2;
				TextDrawer.drawString(g, label, right + 16 + biggestLabel + 2*offset, centerY, -90.0, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
			}
		}
		
		g.setColor(oldColor);
		g.setFont(oldFont);
	}
	
	// TODO Add method with label for the color ramp

}
