package at.jku.cg.sar.gui;

import java.awt.Color;
import java.awt.Graphics2D;

import at.jku.cg.sar.gui.graphics.ColorRampRenderer;
import at.jku.cg.sar.gui.graphics.GridRenderer;
import at.jku.cg.sar.util.ColorRamp;
import at.jku.cg.sar.world.World;

public class WorldViewerPanel extends BasePanel {

	private static final long serialVersionUID = 8138087486876778354L;
	
	
	private final World world;
	
	private final ColorRamp RAMP;
	private final int baseTileSize = 32;
	private int tileSize;
	
	public WorldViewerPanel(World world) {
		this.world = world;
				
		RAMP = new ColorRamp();
		RAMP.add(0.0, new Color(102, 0, 153));
		RAMP.add(0.2, Color.BLUE);
		RAMP.add(0.5, Color.GREEN);
		RAMP.add(0.7, Color.ORANGE);
		RAMP.add(1.0, Color.RED);
	}

	@Override
	public void drawPanel(Graphics2D g) {
		g.drawRect(0, 0, getWidth(), getHeight());
		
		GridRenderer.render(g, world.getProbabilities(), originX, originY, tileSize, RAMP);
				
		int left = originX + tileSize * world.getWidth() + 50;
		int right = left + 25;
		int top = originY;
		int bot = top + tileSize * world.getHeight();
		ColorRampRenderer.render(g, RAMP, left, top, right, bot);
	}
	
	public void frameAll(int left, int top, int right, int bottom) {
		// FIT Map into full panel size
		
		int width = getWidth();
		int height = getHeight(); 	
		
		int usableWidth = width  - left - right;
		int usableHeight= height - top - bottom;
		
		int mapWidth = world.getWidth();
		int mapHeight = world.getHeight();
		
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
