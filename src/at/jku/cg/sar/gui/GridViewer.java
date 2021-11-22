package at.jku.cg.sar.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.splitcontainer.SplitContainer;

public class GridViewer {

	private final JFrame frame;
	

	public GridViewer(Grid<?> grid) {
		this("Grid Viewer", grid);
	}
	
	public GridViewer(String title, Grid<?> grid) {
		this(title, new GridPanel(grid));
	}
	
	public GridViewer(SplitContainer c) {
		this("SplitContainer Viewer", c);
	}
	
	public GridViewer(String title, SplitContainer c) {
		this(title, new SplitContainerPanel(c));
	}
	
	public GridViewer(String title, JPanel panel) {
		this.frame = new JFrame();
		this.frame.setSize(800, 600);
		this.frame.setTitle(title);
		this.frame.setLayout(new BorderLayout());
		
		this.frame.add(panel, BorderLayout.CENTER);
		
		this.frame.setVisible(true);
	}
	
	public void update() {
		this.frame.repaint();
	}

}
