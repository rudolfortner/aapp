package at.jku.cg.sar.gui;

import java.awt.Color;
import java.awt.Graphics2D;

import at.jku.cg.sar.core.splitcontainer.SplitContainer;
import at.jku.cg.sar.gui.graphics.GridRenderer;
import at.jku.cg.sar.util.ColorRamp;

public class SplitContainerPanel extends BasePanel {

	private static final long serialVersionUID = 1752453004515456981L;
	
	private final SplitContainer grid;
	private final ColorRamp ramp;
	
	public SplitContainerPanel(SplitContainer grid) {
		this.grid = grid;
		this.ramp = new ColorRamp();
		ramp.add(0.0, new Color(102, 0, 153));
		ramp.add(0.2, Color.BLUE);
		ramp.add(0.5, Color.GREEN);
		ramp.add(0.7, Color.ORANGE);
		ramp.add(1.0, Color.RED);
	}
	
	public SplitContainerPanel(SplitContainer grid, ColorRamp ramp) {
		this.grid = grid;
		this.ramp = ramp;
	}

	@Override
	public void drawPanel(Graphics2D g) {
		GridRenderer.render(g, grid, originX, originY, zoom, ramp);
	}

	@Override
	public void frameAll(int left, int top, int right, int bottom) {
		// FIT Map into full panel size
		
		int width = getWidth();
		int height = getHeight(); 	
		
		int usableWidth = width  - left - right;
		int usableHeight= height - top - bottom;

		double zoomX = usableWidth / grid.getWidth();
		double zoomY = usableHeight / grid.getHeight();
		
		double zoom = Math.min(zoomX, zoomY);
		
		// ORIGIN
		int originX = (int) ((width - zoom*grid.getWidth()) / 2);
		int originY = (int) ((height - zoom*grid.getHeight()) / 2);
		
		this.zoom = zoom;
		this.originX = originX;
		this.originY = originY;
		
		this.repaint();
	}

}
