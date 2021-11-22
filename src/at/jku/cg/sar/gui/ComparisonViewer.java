package at.jku.cg.sar.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import at.jku.cg.sar.gui.graphics.LineGraphDataSet;
import at.jku.cg.sar.gui.graphics.LineGraphPanel;
import at.jku.cg.sar.main.Configuration;
import at.jku.cg.sar.sim.SimulationResult;

public class ComparisonViewer {
	
	Color[] colors = new Color[]{Color.RED, Color.BLUE, Color.GREEN, Color.BLACK, Color.ORANGE, Color.CYAN, Color.PINK, Color.MAGENTA, Color.YELLOW};
	
	private final JFrame frame;
	private final JSplitPane splitPane;
	private final List<SimulationResultViewerPanel> panels;
	private final List<SimulationResult> results;
	
	public ComparisonViewer(Configuration config, List<SimulationResult> results, boolean exitOnClose) {
		this.frame = new JFrame();
		this.frame.setDefaultCloseOperation(exitOnClose ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
		this.frame.setSize(1080, 720);
		this.frame.setTitle("Comparison");
		this.frame.setLayout(new BorderLayout());
		
		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.splitPane.setResizeWeight(0.5);
		
		int count = results.size();
		this.panels = new ArrayList<>();
		this.results = new ArrayList<>(results);
		this.results.sort((r0, r1) -> r0.getFinder().getName().compareTo(r1.getFinder().getName()));
		
		JPanel panelMaps = new JPanel();
		panelMaps.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 1.0;
		c.weighty = 1.0;
		
		c.gridx = 0;
		c.gridy = 0;
		for(int i = 0; i < count; i++) {
			SimulationResultViewerPanel viewer = new SimulationResultViewerPanel(this.results.get(i));
			viewer.addMouseListener(new MapMouseListener(this.results.get(i)));
			panels.add(viewer);
			panelMaps.add(viewer, c);
			
			c.gridx++;
			if(c.gridx >= 6) {
				c.gridx = 0;
				c.gridy++;
			}
		}
		splitPane.add(panelMaps);
		

		List<LineGraphDataSet> curves = new ArrayList<>();
		int col = 0;
		for(SimulationResult result : this.results) {
			String pathFinderName	= config.getFinders().get(result.getFinder());
			String plannerName		= config.getPlanners().get(result.getPlanner());
			
			String name = "%s with %s \t (%f)".formatted(pathFinderName , plannerName, result.getMetricResult().getScore());
			LineGraphDataSet dataSet = new LineGraphDataSet(name, colors[col % colors.length], result.getMetricResult().getCurve());
			curves.add(dataSet);
			col++;
		}
		this.splitPane.add(new LineGraphPanel(curves));
		
		
		this.frame.add(splitPane, BorderLayout.CENTER);
		this.frame.setVisible(true);
		
		SwingUtilities.invokeLater(() -> {
			splitPane.setDividerLocation(0.6);
			panels.stream().forEach(panel -> panel.frameAll());
			frame.repaint();
		});
	}
	
	private class MapMouseListener extends MouseAdapter{

		private final SimulationResult result;
		
		public MapMouseListener(SimulationResult result) {
			this.result = result;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2) new SimulationResultViewer(result);
		}
	}

}
