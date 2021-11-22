package at.jku.cg.sar.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;

import at.jku.cg.sar.gui.graphics.LineGraphDataSet;
import at.jku.cg.sar.gui.graphics.LineGraphPanel;

public class LineGraphViewer {

	private final JFrame frame;
	private final LineGraphPanel panel;
	

	public LineGraphViewer(List<LineGraphDataSet> data) {
		this("Line Graph Viewer", data);
	}
	
	public LineGraphViewer(String title, List<LineGraphDataSet> data) {
		this.frame = new JFrame();
		this.frame.setSize(800, 600);
		this.frame.setTitle(title);
		this.frame.setLayout(new BorderLayout());
		
		this.panel = new LineGraphPanel(data);
		this.frame.add(panel, BorderLayout.CENTER);
		
		this.frame.setVisible(true);
	}

}
