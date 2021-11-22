package at.jku.cg.sar.gui.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Locale;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.core.splitcontainer.SplitContainer;
import at.jku.cg.sar.core.splitcontainer.SplitGrid;
import at.jku.cg.sar.util.ColorRamp;
import at.jku.cg.sar.util.TextDrawer;
import at.jku.cg.sar.util.TextDrawer.HorizontalAlignment;
import at.jku.cg.sar.util.TextDrawer.VerticalAlignment;

public class GridRenderer {
	

	public static BufferedImage renderImage(Grid<Double> grid, int size, ColorRamp ramp) {	
		return renderImage(grid, size, ramp, true, null);
	}
	
	public static BufferedImage renderImage(Grid<Double> grid, int size, ColorRamp ramp, boolean showValues, GridCellRenderer<Double> renderer) {		

		int tileSize = Math.min(size / grid.getWidth(), size / grid.getHeight());
		BufferedImage image = new BufferedImage(grid.getWidth() * tileSize, grid.getHeight() * tileSize, BufferedImage.TYPE_INT_RGB);
				
		Graphics2D g = image.createGraphics();
		render(g, grid, 0, 0, tileSize, ramp, showValues, renderer);
		g.dispose();
		
		return image;
	}
	
	

	public static BufferedImage renderImage(SplitGrid c, int size, ColorRamp ramp) {
		return renderImage(c, size, ramp, true);
	}
	
	public static BufferedImage renderImage(SplitGrid c, int size, ColorRamp ramp, boolean showValues) {		

		double zoom = Math.min(size / c.getWidth(), size / c.getHeight());
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
				
		Graphics2D g = image.createGraphics();
		render(g, c, 0, 0, zoom, showValues, ramp);
		g.dispose();
		
		return image;
	}


	public static void render(Graphics2D g, Grid<Double> grid, int originX, int originY, int tileSize, ColorRamp ramp) {
		render(g, grid, originX, originY, tileSize, ramp, null);
	}
	
	public static void render(Graphics2D g, Grid<Double> grid, int originX, int originY, int tileSize, ColorRamp ramp, GridCellRenderer<Double> overlay) {
		render(g, grid, originX, originY, tileSize, ramp, false, overlay);
	}
	
	public static void render(Graphics2D g, Grid<Double> grid, int originX, int originY, int tileSize, ColorRamp ramp, boolean showValue, GridCellRenderer<Double> overlay) {
		
		GridCellRenderer<Double> renderer = new GridCellRenderer<>() {			
			@Override
			public void render(Graphics2D g, GridValue<Double> cell, int centerX, int centerY, int width, int height) {
				g.setColor(ramp.evaluate(cell.getValue()));
				g.fillRect(centerX - tileSize/2, centerY - tileSize/2, tileSize, tileSize);
				
				float strokeWidth = tileSize / 64.0f;
				int strokeHalf = (int) (strokeWidth / 2.0 - 0.5);
				g.setStroke(new BasicStroke(strokeWidth));
				g.setFont(new Font("", Font.PLAIN, tileSize/4));
				g.setColor(Color.BLACK);
				
				if(showValue) TextDrawer.drawString(g, String.format(Locale.ROOT, "%.2f", cell.getValue()), centerX, centerY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
				g.drawRect(centerX - tileSize/2 + strokeHalf, centerY - tileSize/2 + strokeHalf, tileSize-2*strokeHalf, tileSize-2*strokeHalf);
				
				if(overlay != null) overlay.render(g, cell, centerX, centerY, width, height);
			}
		};
		
		render(g, grid, originX, originY, tileSize, renderer);
	}
	

	public static void render(Graphics2D g, Grid<Double> grid, int originX, int originY, int tileSize, GridCellRenderer<Double> renderer) {
		for(int x = 0; x < grid.getWidth(); x++) {
			for(int y = 0; y < grid.getHeight(); y++) {
				int centerX = (int) (originX + (x+0.5) * tileSize);
				int centerY = (int) (originY + (y+0.5) * tileSize);
				
				renderer.render(g, grid.getValue(x, y), centerX, centerY, tileSize, tileSize);
			}
		}
	}
	
	public static void render(Graphics2D g, SplitContainer grid, int originX, int originY, double factor, ColorRamp ramp) {
		render(g, grid, originX, originY, factor, true, ramp);
	}
	
	public static void render(Graphics2D g, SplitContainer grid, int originX, int originY, double factor,
			boolean showValues, ColorRamp ramp) {
		for(SplitContainer c : grid.getContainers()) {			
			double x = originX + factor * c.getLeft();
			double y = originY + factor * c.getTop();
			double width = factor * c.getWidth();
			double height = factor * c.getHeight();
			double centerX = x + width/2.0;
			double centerY = y + height/2.0;

			g.setColor(ramp.evaluate(c.getValue()));
			g.fillRect((int) x, (int) y, (int) width, (int) height);
			
			g.setColor(Color.BLACK);
			if(showValues) TextDrawer.drawString(g, String.format(Locale.ROOT, "%.2f", c.getValue()), (int) centerX, (int) centerY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
			g.drawRect((int) x, (int) y, (int) width, (int) height);
		}
	}
}
