package at.jku.cg.sar.gui.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import at.jku.cg.sar.eval.EvaluationConfig;
import at.jku.cg.sar.eval.EvaluationCoverage;
import at.jku.cg.sar.eval.EvaluationUtils;
import at.jku.cg.sar.gui.DrawUtils;
import at.jku.cg.sar.gui.ProgressBarWindow;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.util.ColorRamp;
import at.jku.cg.sar.util.TextDrawer;
import at.jku.cg.sar.util.TextDrawer.HorizontalAlignment;
import at.jku.cg.sar.util.TextDrawer.VerticalAlignment;
import at.jku.cg.sar.world.WorldPoint;

public class SimulationResultRenderer {
	
	public static BufferedImage renderImage(SimulationResult result, int size, int border,
			boolean showPlannerPath, boolean showTrajectoryPath, ColorRamp rampProbability, ColorRamp rampSpeed) {
		return renderImage(result, size, border,
				showPlannerPath, showTrajectoryPath,
				false, false, 0.0,
				false, rampProbability, rampSpeed);
	}
	
	public static BufferedImage renderImage(SimulationResult result, int size, int border,
			boolean showPlannerPath, boolean showTrajectoryPath,
			boolean showDrone, boolean showScanRadius, double time, 
			boolean showGridValues, ColorRamp rampProbability, ColorRamp rampSpeed) {
		
		int tileSize = (size - 2*border) / Math.max(result.getConfiguration().getWorld().getWidth(), result.getConfiguration().getWorld().getHeight());
		int borderX = (size - tileSize*result.getConfiguration().getWorld().getWidth()) / 2;
		int borderY = (size - tileSize*result.getConfiguration().getWorld().getHeight()) / 2;
		
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setBackground(new Color(255, 255, 255, 0));
		g.clearRect(0, 0, size, size);
		SimulationResultRenderer.renderRaw(g, result, borderX, borderY, tileSize,
				showPlannerPath, showTrajectoryPath, showDrone, showScanRadius, time,
				showGridValues, rampProbability, rampSpeed);
		g.dispose();
		
		return image;
	}

	public static void renderRaw(Graphics2D g, SimulationResult result, int originX, int originY, int tileSize,
			boolean showPlannerPath, boolean showTrajectoryPath, ColorRamp rampProbability, ColorRamp rampSpeed) {
		
		renderRaw(g, result, originX, originY, tileSize,
				showPlannerPath, showTrajectoryPath,
				false, false, 0.0,
				false, rampProbability, rampSpeed);
	}
	
	public static void renderRaw(Graphics2D g, SimulationResult result, int originX, int originY, int tileSize,
			boolean showPlannerPath, boolean showTrajectoryPath,
			boolean showDrone, boolean showScanRadius, double time, 
			boolean showGridValues, ColorRamp rampProbability, ColorRamp rampSpeed) {
		renderRaw(g, result, originX, originY, tileSize,
				showPlannerPath, showTrajectoryPath, 2.0,
				showDrone, showScanRadius, time,
				showGridValues, rampProbability, rampSpeed);
	}
	
	public static void renderRaw(Graphics2D g, SimulationResult result, int originX, int originY, int tileSize,
			boolean showPlannerPath, boolean showTrajectoryPath, double pathWidth,
			boolean showDrone, boolean showScanRadius, double time, 
			boolean showGridValues, ColorRamp rampProbability, ColorRamp rampSpeed) {

		// RENDER GRID
		GridRenderer.render(g, result.getConfiguration().getWorld().getProbabilities(), originX, originY, tileSize, rampProbability, showGridValues, null);

		// RENDER FLIGHTPATH OR -TRAJECTORY
		double factor = tileSize / result.getConfiguration().getSettings().getCellSize();
		if(showPlannerPath) FlightPathRenderer.render(g, result.getPlannerPath(), originX, originY, tileSize, rampSpeed, pathWidth);
		if(showTrajectoryPath) FlightPathRenderer.render(g, result.getTrajectoryPath(), originX, originY, factor, rampSpeed, pathWidth);
		
		
		// RENDER DRONE
		if(showPlannerPath && showDrone) {
			WorldPoint p = result.getPlannerPath().getLocationAfterTime(time);
			int x0 = (int) (originX + (p.getX()+0.5) * tileSize);
			int y0 = (int) (originY + (p.getY()+0.5) * tileSize);
			DrawUtils.fillCircleBordered(g, x0, y0, (int) (8 * factor), Color.ORANGE, Color.BLACK);
			
			if(showScanRadius) {
				int radius = (int) (result.getConfiguration().getSettings().getScanRadius() * factor);
				DrawUtils.drawDashedRect(g, x0, y0, 2*radius, 2*radius, 0.75*factor, 4.0*factor, Color.BLACK);
			}
		}
		
		if(showTrajectoryPath && showDrone) {
			WorldPoint p = result.getTrajectoryPath().getLocationAfterTime(time);
			int x0 = (int) (originX + p.getX() * factor);
			int y0 = (int) (originY + p.getY() * factor);
			DrawUtils.fillCircleBordered(g, x0, y0, (int) (8 * factor), Color.ORANGE, Color.BLACK);

			if(showScanRadius) {
				int radius = (int) (result.getConfiguration().getSettings().getScanRadius() * factor);
				DrawUtils.drawDashedRect(g, x0, y0, 2*radius, 2*radius, 0.75*factor, 4.0*factor, Color.BLACK);
			}
		}
	}
	
	
	public static BufferedImage renderOverview(SimulationResult result,
			int height,
			boolean showPlannerPath, boolean showTrajectoryPath, ColorRamp rampProbability, ColorRamp rampSpeed) {
		
		BufferedImage image = new BufferedImage(height, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		renderOverview(g, result, height, showPlannerPath, showTrajectoryPath, rampProbability, rampSpeed);
		g.dispose();
		return image;
	}
	
	public static void renderOverview(Graphics2D g, SimulationResult result,
			int height,
			boolean showPlannerPath, boolean showTrajectoryPath, ColorRamp rampProbability, ColorRamp rampSpeed) {
		
		if(result == null) return;
		
		// BACKGROUND
		g.setColor(Color.GRAY);
		g.fillRect(0, 0, height, height);

		// RESULT
		int border = (int) (0.1 * height);
		int tileSize = (height - 2*border) / Math.max(result.getConfiguration().getWorld().getWidth(), result.getConfiguration().getWorld().getHeight());
		
		renderRaw(g, result, border, border, tileSize,
				showPlannerPath, showTrajectoryPath,
				rampProbability, rampSpeed);
		
		
		// INFO
		String algoName = result.getFinder().getName();
		g.setFont(new Font("", Font.PLAIN, border/2));
		while(true) {
			FontMetrics m = g.getFontMetrics();
			if(m.stringWidth(algoName) <= (height - 2*border)) break;
			g.setFont(new Font("", Font.PLAIN, g.getFont().getSize()-1));
		}
		TextDrawer.drawString(g, algoName, height/2, border/2, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		
		int infoTop = height - border + 5;
		int infoSize = border / (3 + 1);
		g.setFont(new Font("", Font.PLAIN, infoSize));
		String format = "%8.2f";
		
		List<ListItem> infos = new ArrayList<>();
		infos.add(new ListItem("World", result.getConfiguration().getWorld().getName()));
		infos.add(new ListItem("Duration", format.formatted(result.getFinalPath().getDuration())));
		infos.add(new ListItem("Score", format.formatted(result.getMetricResult().getScore())));
		
		ListItem.renderList(g, infos, border, infoTop, 64);
	}
	
	public static BufferedImage renderComparison(SimulationResult r0, SimulationResult r1,
			int height,
			boolean showPlannerPath, boolean showTrajectoryPath, ColorRamp rampProbability, ColorRamp rampSpeed) {
		
		return renderComparison(List.of(r0, r1), height, showPlannerPath, showTrajectoryPath, rampProbability, rampSpeed);
	}
	
	public static BufferedImage renderComparison(List<SimulationResult> results,
			int height,
			boolean showPlannerPath, boolean showTrajectoryPath, ColorRamp rampProbability, ColorRamp rampSpeed) {
		
		int number = results.size();
		int rows = (int) Math.sqrt(number);
		int columns = (int) Math.ceil(1.0 * number / rows);
		
		BufferedImage image = new BufferedImage(columns*height, rows*height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		renderComparison(g, results, height, showPlannerPath, showTrajectoryPath, rampProbability, rampSpeed);
		g.dispose();
		return image;
	}
	
	public static void renderComparison(Graphics2D g, SimulationResult r0, SimulationResult r1,
			int height,
			boolean showPlannerPath, boolean showTrajectoryPath, ColorRamp rampProbability, ColorRamp rampSpeed) {
		
		renderComparison(g, List.of(r0, r1), height, showPlannerPath, showTrajectoryPath, rampProbability, rampSpeed);
	}
	
	public static void renderComparison(Graphics2D g, List<SimulationResult> results,
			int height,
			boolean showPlannerPath, boolean showTrajectoryPath, ColorRamp rampProbability, ColorRamp rampSpeed) {
				
		int number = results.size();
		int rows = (int) Math.sqrt(number);
		int columns = (int) Math.ceil(1.0 * number / rows);
		
		renderComparison(g, results,
				height, rows, columns,
				showPlannerPath, showTrajectoryPath,  rampProbability, rampSpeed);
	}
	
	public static void renderComparison(Graphics2D g, List<SimulationResult> results,
			int height, int rows, int columns,
			boolean showPlannerPath, boolean showTrajectoryPath, ColorRamp rampProbability, ColorRamp rampSpeed) {

		int index = 0;
		for(int y = 0; y < rows; y++) {
			for(int x = 0; x < columns; x++) {
				SimulationResult result = index < results.size() ? results.get(index++) : null;
				g.translate(x * height, y * height);
				renderOverview(g, result, height, showPlannerPath, showTrajectoryPath, rampProbability, rampSpeed);
				g.translate(-x * height, -y * height);
			}
		}
	}
	
	public static void renderAnimation(SimulationResult result, File directory, int size, int border,
			boolean useTrajectory, boolean showTime, boolean showScanArea, boolean showCoverage) {
		int threadCount = Runtime.getRuntime().availableProcessors();
		renderAnimation(result, directory, size, border, useTrajectory, showTime, showScanArea, showCoverage, threadCount);
	}
	
	public static void renderAnimation(SimulationResult result, File directory, int size, int border,
			boolean useTrajectory, boolean showTime, boolean showScanArea, boolean showCoverage,
			int threadCount) {
		renderAnimation(result, directory, size, border,
				useTrajectory, showTime, showScanArea, showCoverage,
				Color.BLACK, Color.WHITE,
				threadCount);
	}
	
	public static void renderAnimation(SimulationResult result, File directory, int size, int border,
			boolean useTrajectory, boolean showTime, boolean showScanArea, boolean showCoverage,
			Color backgroundColor, Color textColor,
			int threadCount) {
		
		if(directory == null) throw new IllegalArgumentException();
		if(!directory.exists()) directory.mkdirs();

		if(!useTrajectory	&& result.getPlannerPath() == null) throw new IllegalArgumentException();
		if(useTrajectory	&& result.getTrajectoryPath() == null) throw new IllegalArgumentException();
		
		ExecutorService service = Executors.newFixedThreadPool(threadCount);
		
		// Create task for each frame
		List<Future<?>> tasks = new ArrayList<>();
		double end = useTrajectory ? result.getTrajectoryPath().getDuration() : result.getPlannerPath().getDuration();
		int frameIndex = 1;
		for(double t = 0.0; t <= end; t+=1.0) {			
			AnimationRendererTask task = new AnimationRendererTask(result, directory, size, border,
					useTrajectory, showTime, showScanArea, showCoverage,
					backgroundColor, textColor,
					t, frameIndex);
			tasks.add(service.submit(task));
			frameIndex++;
		}

		// Wait for all tasks to finish
		ProgressBarWindow bar = new ProgressBarWindow("Rendering...");		
		for(int i = 1; i <= tasks.size(); i++) {
			try {
				tasks.get(i-1).get();
			}catch (InterruptedException | ExecutionException e) {
				// TODO Cancel all other tasks?
				e.printStackTrace();
			}
			double progress = i * 100.0 / tasks.size();
			bar.setProgress((int) progress);
			String progressString = String.format(Locale.ROOT, "%03.2f %%", progress);
			System.err.print("Rendering Video ... " + progressString + " \r");
		}
		System.err.print("\n");
		bar.close();
	}
	
	private static final class AnimationRendererTask implements Runnable {

		// GENERAL DATA
		private final SimulationResult result;
		private final File directory;
		private final int size, border;
		private final boolean useTrajectory, showTime, showScanArea, showCoverage;
		
		private final Color backgroundColor, textColor;
		
		// TASK SPECIFIC
		private final double t;
		private final int frameIndex;
		
		public AnimationRendererTask(SimulationResult result, File directory, int size, int border,
				boolean useTrajectory, boolean showTime, boolean showScanArea, boolean showCoverage,
				Color backgroundColor, Color textColor,
				double t, int frameIndex) {
			super();
			this.result = result;
			this.directory = directory;
			this.size = size;
			this.border = border;
			this.useTrajectory = useTrajectory;
			this.showTime = showTime;
			this.showScanArea = showScanArea;
			this.showCoverage = showCoverage;
			
			this.backgroundColor = backgroundColor;
			this.textColor = textColor;
			
			this.t = t;
			this.frameIndex = frameIndex;
		}

		@Override
		public void run() {
//			System.err.println("Rendering frame at time %4.0f".formatted(t));
			
			BufferedImage imageMap = renderImage(result, size, border,
					!useTrajectory, useTrajectory, true, showScanArea, t, true,
					EvaluationConfig.rampProbability, EvaluationConfig.rampSpeed);
			
			BufferedImage imageCoverage = EvaluationCoverage.createCoverageMap(result, 64, result.getConfiguration().getSettings().getScanRadius(),
					true, true, t,
					null, null, null, null);
			imageCoverage = EvaluationUtils.fitImage(imageCoverage, size, 64);
			
			BufferedImage image = new BufferedImage(showCoverage ? 2*size : size, size, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			
			g.setColor(backgroundColor);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			
			g.drawImage(imageMap, 0, 0, size, size, null);
			if(showCoverage) g.drawImage(imageCoverage, size, 0, size, size, null);
			
			if(showTime) {
				String timeString = String.format(Locale.ROOT, "t = %06.1f", t);
				g.setFont(new Font("", 0, border/2));
				g.setColor(textColor);
				TextDrawer.drawString(g, timeString, image.getWidth()/2, size-8, HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
			}
			g.dispose();
			
			try {
				ImageIO.write(image, "PNG", new File(directory, "frame_%04d.png".formatted(frameIndex)));
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
