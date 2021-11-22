package at.jku.cg.sar.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import at.jku.cg.sar.gui.graphics.LineGraphDataSet;
import at.jku.cg.sar.gui.graphics.LineGraphPanel;
import at.jku.cg.sar.sim.SimulationResult;

public class SimulationResultViewer {

	private final SimulationResult result;
	
	private final JFrame frame;
	
	private final JSplitPane splitPane;
	
	private final JPanel mapPanel;
	private final SimulationResultViewerPanel mapPath, mapTrajectory;
	
	private final JPanel graphPanel;
	private final LineGraphPanel panelPath, panelTrajectory;
	
	public SimulationResultViewer(SimulationResult result) {
		this.result = result;
		
		this.frame = new JFrame();
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.frame.setSize(640, 640);
		this.frame.setTitle(result.getFinder().getName());
		this.frame.setLayout(new BorderLayout());
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.5);
		
		// MAP Synchronizer
		BasePanelListener listener = new BasePanelListener() {
			@Override
			public void viewChanged(int originX, int originY, double zoom, boolean byUser) {
				if(byUser) {
					mapPath.setView(originX, originY, zoom);
					mapTrajectory.setView(originX, originY, zoom);
				}				
			}
		};
		
		// MAPS
		mapPanel = new JPanel();
		mapPanel.setLayout(new GridLayout(1, 2));
		mapPanel.setPreferredSize(new Dimension(800, 400));
		
		mapPath = new SimulationResultViewerPanel(result);
		mapPath.addListener(listener);
		mapPanel.add(mapPath, BorderLayout.CENTER);
		
		mapTrajectory = new SimulationResultViewerPanel(result);
		mapTrajectory.addListener(listener);
		mapTrajectory.setShowPlannerPath(false);
		mapTrajectory.setShowTrajectoryPath(true);
		mapPanel.add(mapTrajectory, BorderLayout.CENTER);

		splitPane.add(mapPanel);
		
		// GRAPHS
		graphPanel = new JPanel();
		graphPanel.setLayout(new GridLayout(1, 2));
		graphPanel.setPreferredSize(new Dimension(800, 400));
		
		List<LineGraphDataSet> path = result.getPlannerPath() != null ? result.getPlannerPath().createGraph() : null;
		panelPath = new LineGraphPanel("Path Finder", path);
		graphPanel.add(this.panelPath, BorderLayout.WEST);
		
		List<LineGraphDataSet> trajectory = result.getTrajectoryPath() != null ? result.getTrajectoryPath().createGraph() : null;
		panelTrajectory = new LineGraphPanel("Trajectory", trajectory);
		graphPanel.add(this.panelTrajectory, BorderLayout.EAST);

		splitPane.add(graphPanel);
		
		frame.add(splitPane, BorderLayout.CENTER);
		
		this.frame.setVisible(true);
		
		SwingUtilities.invokeLater(() -> {
			splitPane.setDividerLocation(0.6);
			mapPath.frameAll();
			mapTrajectory.frameAll();
			frame.repaint();
		});
	}
}
