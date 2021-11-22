package at.jku.cg.sar.gui;

import java.util.List;

import javax.swing.JFrame;

import at.jku.cg.sar.gui.graphics.LineGraphDataSet;
import at.jku.cg.sar.gui.graphics.LineGraphPanel;

public class GraphViewer{

	
	private final JFrame frame;
	
	public GraphViewer(List<LineGraphDataSet> dataSets) {
		this.frame = new JFrame();
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setSize(640, 640);
		
		LineGraphPanel panel = new LineGraphPanel(dataSets);
		this.frame.add(panel);
		
		this.frame.setVisible(true);
	}

}
