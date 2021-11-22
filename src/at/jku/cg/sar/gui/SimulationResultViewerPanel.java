package at.jku.cg.sar.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import at.jku.cg.sar.gui.graphics.ColorRampRenderer;
import at.jku.cg.sar.gui.graphics.SimulationResultRenderer;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.util.ColorRamp;
import at.jku.cg.sar.util.TextDrawer;
import at.jku.cg.sar.util.TextDrawer.HorizontalAlignment;
import at.jku.cg.sar.util.TextDrawer.VerticalAlignment;
import at.jku.cg.sar.world.World;

public class SimulationResultViewerPanel extends BasePanel {

	private static final long serialVersionUID = 8138087486876778354L;
	
	private final SimulationResult result;
	private final World world;
	
	private final ColorRamp rampProbability, rampSpeed;
	private final int baseTileSize = 32;
	private int tileSize;
	
	// TODO showGrid is unused
	private boolean showGrid = true, showPlannerPath = true, showTrajectoryPath = false;
	private boolean showDrone = false, showScanRadius = false;
	private double time = 0.0;
	
	public SimulationResultViewerPanel(SimulationResult result) {
		this.result = result;
		this.world = result.getConfiguration().getWorld();
		
		this.addKeyListener(new ViewerKeyListener());
		
		rampProbability = new ColorRamp();
		rampProbability.add(0.0, new Color(102, 0, 153));
		rampProbability.add(0.2, Color.BLUE);
		rampProbability.add(0.5, Color.GREEN);
		rampProbability.add(0.7, Color.ORANGE);
		rampProbability.add(1.0, Color.RED);
		
		rampSpeed = new ColorRamp();
		rampSpeed.add(result.getConfiguration().getSettings().getSpeedSlow(), Color.BLUE);
		rampSpeed.add(result.getConfiguration().getSettings().getSpeedScan(), Color.GREEN);
		rampSpeed.add(result.getConfiguration().getSettings().getSpeedFast(), Color.RED);
	}

	public void drawPanel(Graphics2D g) {
		tileSize = (int) (baseTileSize * zoom);
		
		g.drawRect(0, 0, getWidth(), getHeight());
		
		// Render Heading
		String finderName	= result.getConfiguration().getFinders().get(result.getFinder());
		String plannerName	= result.getConfiguration().getPlanners().get(result.getPlanner());
		TextDrawer.drawString(g, finderName, originX + tileSize*world.getWidth()/2, originY-40, HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
		TextDrawer.drawString(g, plannerName, originX + tileSize*world.getWidth()/2, originY-20, HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
		
		
		// Render SimulationResult
		SimulationResultRenderer.renderRaw(g, result, originX, originY,tileSize,
				showPlannerPath, showTrajectoryPath,
				showDrone, showScanRadius, time,
				true, rampProbability, rampSpeed);
		
		// Render Legend
		int left = originX + tileSize * world.getWidth() + 25;
		int right = left + 25;
		int top = originY;
		int bot = top + tileSize * world.getHeight();
		ColorRampRenderer.render(g, rampProbability, left, top, right, bot);
		ColorRampRenderer.render(g, rampSpeed, right + 50, top, right + 75, bot);
		
		// Render timestamp of drone
		if(showDrone && (showPlannerPath || showTrajectoryPath)) {
			String timeString = "t = %4.2f".formatted(time);
			g.setFont(new Font("", 0, 32));
			TextDrawer.drawString(g, timeString, 10, 10, HorizontalAlignment.LEFT, VerticalAlignment.TOP);
		}
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
	
	private double getEndTime() {
		if(showPlannerPath) return result.getPlannerPath().getDuration();
		if(showTrajectoryPath)return result.getTrajectoryPath().getDuration();
		return 0.0;
	}
	
	private class ViewerKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_G) showGrid = !showGrid;
			if(e.getKeyCode() == KeyEvent.VK_P) showPlannerPath = !showPlannerPath;
			if(e.getKeyCode() == KeyEvent.VK_T) showTrajectoryPath = !showTrajectoryPath;
			if(e.getKeyCode() == KeyEvent.VK_D) showDrone = !showDrone;
			if(e.getKeyCode() == KeyEvent.VK_S) showScanRadius = !showScanRadius;
			if(e.getKeyCode() == KeyEvent.VK_LEFT) time -= 1.0;
			if(e.getKeyCode() == KeyEvent.VK_RIGHT) time += 1.0;
			if(e.getKeyCode() == KeyEvent.VK_HOME) time = 0.0;
			if(e.getKeyCode() == KeyEvent.VK_END) time = getEndTime();
			if(e.getKeyCode() == KeyEvent.VK_R) {
				SimulationResultRenderer.renderAnimation(result, new File("out", "test"), 1024, 64, true, true, true, true);
			}
			repaint();
		}		
	}

	public boolean isShowGrid() {
		return showGrid;
	}

	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}

	public boolean isShowPlannerPath() {
		return showPlannerPath;
	}

	public void setShowPlannerPath(boolean showPlannerPath) {
		this.showPlannerPath = showPlannerPath;
	}

	public boolean isShowTrajectoryPath() {
		return showTrajectoryPath;
	}

	public void setShowTrajectoryPath(boolean showTrajectoryPath) {
		this.showTrajectoryPath = showTrajectoryPath;
	}
}
