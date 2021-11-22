package at.jku.cg.sar.gui.graphics;

import java.awt.Graphics2D;

import at.jku.cg.sar.core.grid.GridValue;

public interface GridCellRenderer<Data extends Comparable<Data>> {
	public void render(Graphics2D g, GridValue<Data> cell, int centerX, int centerY, int width, int height);
}
