package at.jku.cg.sar.eval;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.gui.graphics.GridCellRenderer;
import at.jku.cg.sar.gui.graphics.GridRenderer;
import at.jku.cg.sar.main.Comparator;
import at.jku.cg.sar.main.Configuration;
import at.jku.cg.sar.metric.AUPCMetric;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;
import at.jku.cg.sar.util.Interpolation;
import at.jku.cg.sar.world.World;
import at.jku.cg.sar.world.WorldPoint;

public class EvaluationCoverage {
	
	@Test
	void testCoverageCreation() {
		EvaluationCoverage.create(EvaluationConfig.worldSmoothedSpots, EvaluationConfig.finderVacuumCleanerGradient, EvaluationConfig.plannerSimple,
				EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
				128, EvaluationConfig.SCAN_RADIUS, true, new File("coverageTest.png"), null, null, null);
	}
	
	public static void create(World world, PathFinder finder, TrajectoryPlanner planner,
			int startX, int startY, int mapSubdivision, double scanRadius, 
			boolean drawGrid, File fileImage, File fileInfo, File fileColorRamp, File fileColorRampNT) {
		create(world, finder, planner,
				startX, startY, mapSubdivision, scanRadius, false, 0, 0,
				drawGrid, fileImage, fileInfo, fileColorRamp, fileColorRampNT);
	}
	
	public static void create(World world, PathFinder finder, TrajectoryPlanner planner,
			int startX, int startY, int mapSubdivision, double scanRadius, boolean overrideMinMax, int newMin, int newMax,
			boolean drawGrid, File fileImage, File fileInfo, File fileColorRamp, File fileColorRampNT) {
		
		Comparator comp = new Comparator(true);
		List<SimulationResult> results = comp.evaluate(new Configuration(List.of(finder), List.of(planner), EvaluationConfig.settings,
				new AUPCMetric(), startX, startY, world));
		SimulationResult result = results.get(0);
		
		createCoverageMap(result, mapSubdivision, scanRadius,
				drawGrid, false, 0.0, overrideMinMax, newMin, newMax,
				fileImage, fileInfo, fileColorRamp, fileColorRampNT);
	}
	
	public static BufferedImage createCoverageMap(SimulationResult result, int mapSubdivision, double scanRadius,
			boolean drawGrid, File fileImage, File fileInfo, File fileColorRamp, File fileColorRampNT) {
		return createCoverageMap(result, mapSubdivision, scanRadius, drawGrid, false, 0.0, fileImage, fileInfo, fileColorRamp, fileColorRampNT);
	}
	
	public static BufferedImage createCoverageMap(SimulationResult result, int mapSubdivision, double scanRadius,
			boolean drawGrid, boolean limitTime, double endTime,
			File fileImage, File fileInfo, File fileColorRamp, File fileColorRampNT) {
		return createCoverageMap(result, mapSubdivision, scanRadius,
				drawGrid, limitTime, endTime, false, 0, 0,
				fileImage, fileInfo, fileColorRamp, fileColorRampNT);
	}
	
	public static BufferedImage createCoverageMap(SimulationResult result, int mapSubdivision, double scanRadius,
			boolean drawGrid, boolean limitTime, double endTime, boolean overrideMinMax, int newMin, int newMax,
			File fileImage, File fileInfo, File fileColorRamp, File fileColorRampNT) {
		
		FlightPath<WorldFlightLeg> flightPath = result.getTrajectoryPath();
		if(limitTime) flightPath = flightPath.createSubPath(endTime);
		
		int mapSize = mapSubdivision * EvaluationConfig.WIDTH;
		double cellSize = EvaluationConfig.CELL_SIZE / mapSubdivision;
		Grid<Double> coverage = new Grid<Double>(mapSize, mapSize, 0.0);
		
		switch (result.getFinder().getType()) {
			case DISCRETE_ITERATIVE:
			case DISCRETE_FULL:
				calculateCoverageDiscrete(coverage, flightPath, scanRadius, cellSize);
				break;
			case CONTINOUS_ITERATIVE:
				calculateCoverageContinuous(coverage, flightPath, scanRadius, cellSize);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value:");
		}
		
		double minCoverage = coverage.minValue();
		double avgCoverage = coverage.getValues().stream().mapToDouble(gc -> gc.getValue()).average().orElseThrow();
		double maxCoverage = coverage.maxValue();
		
		double min = overrideMinMax ? newMin : minCoverage;
		double max = overrideMinMax ? newMax : maxCoverage;
		if(overrideMinMax) {
			if(minCoverage < newMin) System.err.println("!!! WARNING: coverage value is smaller than selected minimum !!!");
			if(maxCoverage > newMax) System.err.println("!!! WARNING: coverage value is bigger  than selected maximum !!!");
		}
		
		// NORMALIZE BETWEEN 0.0 AND 1.0
		coverage.modify(value -> Interpolation.Linear(value, min, 0.0, max, 1.0));

		
		// RENDER IMAGE
		BufferedImage image = new BufferedImage(mapSize, mapSize, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		GridCellRenderer<Double> borderRenderer = new GridCellRenderer<>() {			
			@Override
			public void render(Graphics2D g, GridValue<Double> cell, int centerX, int centerY, int width, int height) {
				float strokeWidth = width / 64.0f;
				int strokeHalf = (int) (strokeWidth / 2.0 - 0.5);
				g.setStroke(new BasicStroke(strokeWidth));
				
				g.setColor(Color.BLACK);
				g.drawRect(centerX - width/2 + strokeHalf, centerY - height/2 + strokeHalf,
						width - 2*strokeHalf, height - 2*strokeHalf);
			}
		};
		GridCellRenderer<Double> coverageRenderer = new GridCellRenderer<>() {			
			@Override
			public void render(Graphics2D g, GridValue<Double> cell, int centerX, int centerY, int width, int height) {
				g.setColor(EvaluationConfig.rampProbability.evaluate(cell.getValue()));
				g.fillRect(centerX - width/2, centerY - height/2, width, height);
			}
		};

		int tileSizeBig = mapSize / Math.max(result.getConfiguration().getWorld().getWidth(), result.getConfiguration().getWorld().getHeight());
		GridRenderer.render(g, coverage, 0, 0, 1, coverageRenderer);
		if(drawGrid) GridRenderer.render(g, coverage, 0, 0, tileSizeBig, borderRenderer);
		g.dispose();
		
		
		// OUTPUT		
		try {			
			// IMAGE
			if(fileImage != null) {
				ImageIO.write(image, "PNG", fileImage);
			}			
			
			// INFO
			if(fileInfo != null) {
				FileWriter fileWriter = new FileWriter(fileInfo);
				BufferedWriter writer = new BufferedWriter(fileWriter);
				writer.write("min:\t%04f\n".formatted(minCoverage));
				writer.write("max:\t%04f\n".formatted(maxCoverage));
				writer.write("avg:\t%04f\n".formatted(avgCoverage));
				writer.close();
				fileWriter.close();
			}
			
			// COLOR RAMP
			if(fileColorRamp != null) {
				EvaluationUtils.exportColorRamp(EvaluationConfig.rampProbability, mapSize, false,
						"number of samples taken", false, val -> Interpolation.Linear(val, 0.0, min, 1.0, max), false, fileColorRamp);
			}
			if(fileColorRampNT != null) {
				EvaluationUtils.exportColorRamp(EvaluationConfig.rampProbability, mapSize, false,
						"number of samples taken", false, val -> Interpolation.Linear(val, 0.0, min, 1.0, max), true, fileColorRampNT);
			}
		
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		return image;
	}
	
	private static void calculateCoverageDiscrete(Grid<Double> coverage, FlightPath<WorldFlightLeg> flightPath,
			double scanRadius, double cellSize) {

		double prevX = Double.NaN;
		double prevY = Double.NaN;
		
		// ALL OTHER LEGS
		for(WorldFlightLeg leg : flightPath.getLegs()) {
			if(leg.isVacuum()) throw new IllegalStateException();
			if(!leg.isScan()) continue;
			
			double distance = leg.getDistance();
			int scanCount = (int) (distance / EvaluationConfig.SCAN_DISTANCE + 1);
			
			for(int i = 0; i < scanCount; i++) {
				double x = Interpolation.Linear(i, 0, leg.getFromX(), scanCount-1, leg.getToX());
				double y = Interpolation.Linear(i, 0, leg.getFromY(), scanCount-1, leg.getToY());
				if(!(x == prevX && y == prevY)) putCoverage(coverage, x, y, scanRadius, cellSize);
				prevX = x;
				prevY = y;
			}
		}
	}
	
	private static void calculateCoverageContinuous(Grid<Double> coverage, FlightPath<WorldFlightLeg> flightPath,
			double scanRadius, double cellSize) {
		
		double distance = flightPath.getDistance();
		
		double t = 0.0;
		for(;t <= distance; t += EvaluationConfig.SCAN_DISTANCE) {
			WorldPoint p = flightPath.getLocationAfterDistance(t);
			putCoverage(coverage, p.getX(), p.getY(), scanRadius, cellSize);
		}
		if(t != distance) {
			WorldPoint p = flightPath.getLocationAfterDistance(distance);
			putCoverage(coverage, p.getX(), p.getY(), scanRadius, cellSize);
		}
	}
	
	private static void putCoverage(Grid<Double> coverage, double posX, double posY, double radius, double cellSize) {
		double left		= posX - radius;
		double top		= posY - radius;
		double right	= posX + radius;
		double bottom	= posY + radius;

		int leftDiscrete	= (int) (left / cellSize);
		int topDiscrete		= (int) (top / cellSize);
		int rightDiscrete	= (int) (right / cellSize);
		int bottomDiscrete	= (int) (bottom / cellSize);
		
		for(int x = leftDiscrete; x < rightDiscrete; x++) {
			for(int y = topDiscrete; y < bottomDiscrete; y++) {
				if(x < 0 || x >= coverage.getWidth()) continue;
				if(y < 0 || y >= coverage.getHeight()) continue;
				
				coverage.set(x, y, coverage.get(x, y) + 1.0);
			}
		}
	}

}
