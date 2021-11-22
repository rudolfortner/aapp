package at.jku.cg.sar.gui;

import java.awt.Color;
import java.awt.Graphics2D;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.gui.graphics.GridRenderer;
import at.jku.cg.sar.util.ColorRamp;

public class GridPanel extends BasePanel {

	private static final long serialVersionUID = 1752453004515456981L;
	
	private final Grid<?> grid;
	private final ColorRamp ramp;
	private final int baseTileSize = 32;
	
	public GridPanel(Grid<?> grid) {
		this.grid = grid;
		this.ramp = new ColorRamp();
		ramp.add(0.0, new Color(102, 0, 153));
		ramp.add(0.2, Color.BLUE);
		ramp.add(0.5, Color.GREEN);
		ramp.add(0.7, Color.ORANGE);
		ramp.add(1.0, Color.RED);
	}
	
	public GridPanel(Grid<?> grid, ColorRamp ramp) {
		this.grid = grid;
		this.ramp = ramp;
	}

	@Override
	public void drawPanel(Graphics2D g) {
		int tileSize = (int) (baseTileSize * zoom);
		GridRenderer.render(g, (Grid<Double>) grid, originX, originY, tileSize, ramp);
	}

	@Override
	public void frameAll(int left, int top, int right, int bottom) {
		// FIT Map into full panel size
		
		int width = getWidth();
		int height = getHeight(); 	
		
		int usableWidth = width  - left - right;
		int usableHeight= height - top - bottom;
		
		int mapWidth = grid.getWidth();
		int mapHeight = grid.getHeight();
		
		int tileSizeX = (int) Math.floor(usableWidth / mapWidth);
		int tileSizeY = (int) Math.floor(usableHeight / mapHeight);
		
		int tileSize = Math.min(tileSizeX, tileSizeY);
		
		// ORIGIN
		int originX = (width - tileSize*mapWidth) / 2;
		int originY = (height - tileSize*mapHeight) / 2;
		
		this.zoom = 1.0 * tileSize / baseTileSize;
		this.originX = originX;
		this.originY = originY;
		
		this.repaint();
	}

}
