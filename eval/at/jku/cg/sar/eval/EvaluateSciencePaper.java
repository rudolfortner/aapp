package at.jku.cg.sar.eval;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import at.jku.cg.core.latex.LatexTableWriter;
import at.jku.cg.core.latex.LatexTableWriter.LatexTableRow;
import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridPoint;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.gui.graphics.FlightPathRenderer;
import at.jku.cg.sar.gui.graphics.GridCellRenderer;
import at.jku.cg.sar.gui.graphics.GridRenderer;
import at.jku.cg.sar.main.Comparator;
import at.jku.cg.sar.main.Configuration;
import at.jku.cg.sar.metric.AUPCMetric;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.trajectory.SimpleTrajectory;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;
import at.jku.cg.sar.util.ColorRamp;
import at.jku.cg.sar.world.World;

public class EvaluateSciencePaper {

	private static final File dir;
	private static final List<GridPoint> path;
	private static final Grid<Double> probs;
	
	private static BufferedImage mapBackground;
	private static final ColorRamp probRampPaper;
	
	static {
		
		dir = new File("out", "map");
		dir.mkdirs();

		List<GridPoint> points05 = new ArrayList<>();
		points05.add(new GridPoint(0, 6));
		points05.add(new GridPoint(0, 7));
		points05.add(new GridPoint(0, 8));
		points05.add(new GridPoint(0, 9));
		points05.add(new GridPoint(1, 4));
		points05.add(new GridPoint(1, 5));
		points05.add(new GridPoint(1, 6));
		points05.add(new GridPoint(1, 7));
		points05.add(new GridPoint(2, 3));
		points05.add(new GridPoint(2, 4));
		points05.add(new GridPoint(2, 5));
		
		List<GridPoint> points10 = new ArrayList<>();
		for(int x = 1; x <= 8; x++) {
			points10.add(new GridPoint(x, 8));
			points10.add(new GridPoint(x, 9));
		}
		points10.add(new GridPoint(2, 6));
		points10.add(new GridPoint(2, 7));
		points10.add(new GridPoint(3, 7));
		points10.add(new GridPoint(5, 7));
		points10.add(new GridPoint(6, 7));
		
	
		List<GridPoint> points30 = new ArrayList<>();
		for(int x = 3; x <= 5; x++) {
			for(int y = 3; y <= 6; y++) {
				points30.add(new GridPoint(x, y));
			}
		}
		points30.add(new GridPoint(4, 7));
		

		List<GridPoint> points50 = new ArrayList<>();
		for(int y = 4; y <= 11; y++) {
			points50.add(new GridPoint( 9, y));
			points50.add(new GridPoint(10, y));
		}
		for(int x = 7; x <= 8; x++) {
			for(int y = 5; y <= 7; y++) {
				points50.add(new GridPoint(x, y));
			}
		}
		points50.add(new GridPoint(8, 4));
		
		
		probs = new Grid<Double>(12, 12, 0.0);
		putProbs(probs, points05, 0.05);
		putProbs(probs, points10, 0.10);
		putProbs(probs, points30, 0.30);
		putProbs(probs, points50, 0.50);
		
		path = new ArrayList<>();
//		path.add(new GridPoint( 9, 11)); // IS START
		path.add(new GridPoint(10, 11));
		path.add(new GridPoint(10, 10));
		path.add(new GridPoint(10,  9));
		path.add(new GridPoint(10,  8));
		path.add(new GridPoint(10,  7));
		path.add(new GridPoint(10,  6));
		path.add(new GridPoint(10,  5));
		path.add(new GridPoint(10,  4));

		path.add(new GridPoint( 9,  4));
		path.add(new GridPoint( 8,  4));

		path.add(new GridPoint( 7,  5));
		path.add(new GridPoint( 7,  6));
		path.add(new GridPoint( 7,  7));

		path.add(new GridPoint( 8,  7));
		path.add(new GridPoint( 9,  7));

		path.add(new GridPoint( 9,  8));
		path.add(new GridPoint( 9,  9));
		path.add(new GridPoint( 9, 10));

		path.add(new GridPoint( 8,  9));
		path.add(new GridPoint( 8,  8));
		path.add(new GridPoint( 8,  6));
		path.add(new GridPoint( 8,  5));

		path.add(new GridPoint( 9,  5));
		path.add(new GridPoint( 9,  6));

		path.add(new GridPoint( 7,  8));
		path.add(new GridPoint( 6,  8));
		path.add(new GridPoint( 5,  8));

		path.add(new GridPoint( 4,  7));
		path.add(new GridPoint( 4,  6));
		path.add(new GridPoint( 4,  5));
		path.add(new GridPoint( 4,  4));
		
		try {
			mapBackground = ImageIO.read(new File("in/map_background.png"));
		} catch (IOException e) {
			e.printStackTrace();
			mapBackground = null;
		}
		
		probRampPaper = new ColorRamp();
		probRampPaper.add(0.05, Color.BLUE);
		probRampPaper.add(0.10, new Color(126, 4, 168));
		probRampPaper.add(0.30, new Color(248, 148, 64));
		probRampPaper.add(0.50, new Color(240, 250, 32));
		
	}
	
	private static void putProbs(Grid<Double> probs, List<GridPoint> locations, double value) {
		for(GridPoint point : locations) {
			probs.set(point.getX(), point.getY(), value);
		}
	}
	
	@Test
	void test() {
		int startX =  9;
		int startY = 11;
		
		SimulatorSettings settingsDefault = new SimulatorSettings();
		SimulatorSettings settingsInfiniteAccel = new SimulatorSettings();
		
		settingsInfiniteAccel.setAcceleration(1_000);
		settingsInfiniteAccel.setDeceleration(1_000);
		
		TrajectoryPlanner plannerNone = null;
		TrajectoryPlanner plannerNormal = new SimpleTrajectory(settingsDefault, false);
		TrajectoryPlanner plannerInfiniteAccel = new SimpleTrajectory(settingsInfiniteAccel, false);
		List<TrajectoryPlanner> planners = new ArrayList<>();
		planners.add(plannerNone);
		planners.add(plannerNormal);
		planners.add(plannerInfiniteAccel);
		
		
		System.err.println("START");
		
		LatexTableWriter writer = new LatexTableWriter();
		writer.setLeftColumn("Planner");
		writer.setCaption("Planner comparison");
		writer.setLabel("tab:trajectory_planner_comparison");
		
		Comparator comp = new Comparator(true);
		Configuration config = new Configuration(List.of(new PredefinedFinder()), planners, EvaluationConfig.settings, new AUPCMetric(), startX, startY, new World(probs));
		List<SimulationResult> results = comp.evaluate(config);

		File file_none		= new File(dir, "01_comp_planner_none.png");
		File file_normal	= new File(dir, "02_comp_planner_normal.png");
		File file_infinit	= new File(dir, "03_comp_planner_infinit.png");
		File file_combined	= new File(dir, "00_comp_planner_combined.png");
		File file_combinedNT	= new File(dir, "00_comp_planner_combined_NT.png");
				
		int imageSize = 1024;
		int border = 0;
		String format = "%8.2f";
		for(SimulationResult result : results) {
			FlightPath<?> path = null;
			String plannerName = "N/A";
			String accelerationString = "-";
			if(result.getPlanner() == plannerNone) {
				render(file_none, result, imageSize, border, true, false);
				path = result.getPlannerPath();
				plannerName = "None";
			}else {
				if(result.getPlanner().equals(plannerNormal)) {
					render(file_normal, result, imageSize, border, false, true);
					plannerName = "Normal";
					accelerationString = String.format(Locale.ROOT, format, settingsDefault.getAcceleration());
				}
				
				if(result.getPlanner().equals(plannerInfiniteAccel)) {
					render(file_infinit, result, imageSize, border, false, true);
					plannerName = "Infinit";
					accelerationString = String.format(Locale.ROOT, format, settingsInfiniteAccel.getAcceleration());
				}
				path = result.getTrajectoryPath();
			}
			
			LatexTableRow row = new LatexTableRow();
			row.addEntry("Planner", plannerName);
			row.addEntry("Acceleration [$\\frac{m}{s^2}$]", accelerationString);
			row.addEntry("Distance [m]", format.formatted(path.getDistance()));
			row.addEntry("Duration [s]", format.formatted(path.getDuration()));
			writer.addRow(row);
		}
		
		writer.export(new File(dir, "planner_comparison.txt"));
		

		System.err.println("Combining images");
		
		File originalProbRamp = new File("in/science_paper_ramp.png");
		File probRamp = new File(dir, "probRamp.png");
		File speedRamp = new File(dir, "speedRamp.png");
		File probRampNT = new File(dir, "probRamp_NT.png");
		File speedRampNT = new File(dir, "speedRamp_NT.png");

		EvaluationUtils.exportColorRamp(probRampPaper, 4096, false, "Probability", true, probRamp);
		EvaluationUtils.exportColorRamp(EvaluationConfig.rampSpeed, 4096, false, "Speed (m/s)", false, speedRamp);
		EvaluationUtils.exportColorRamp(probRampPaper, 4096, false, "Probability", true, val->val, true, probRampNT);
		EvaluationUtils.exportColorRamp(EvaluationConfig.rampSpeed, 4096, false, "Speed (m/s)", false, val->val, true, speedRampNT);
		
		
		EvaluationUtils.combineImages(imageSize, 32, List.of(probRamp, file_infinit, file_normal, speedRamp), 
				List.of("", "(a)", "(b)"), Color.WHITE, 32, 4,
				file_combined, false);
		
		EvaluationUtils.combineImages(imageSize, 32, List.of(probRampNT, file_infinit, file_normal, speedRampNT), 
				List.of(), Color.WHITE, 0, 4,
				file_combinedNT, false);
		
		
		System.err.println("DONE");
	}
	
	private static void render(File file, SimulationResult result, int size, int border,  boolean showPlannerPath, boolean showTrajectoryPath) {
		
		int tileSize = (size - 2*border) / Math.max(result.getConfiguration().getWorld().getWidth(), result.getConfiguration().getWorld().getHeight());
		int borderX = (size - tileSize*result.getConfiguration().getWorld().getWidth()) / 2;
		int borderY = (size - tileSize*result.getConfiguration().getWorld().getHeight()) / 2;
		
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		g.setBackground(new Color(255, 255, 255, 0));
		g.clearRect(0, 0, size, size);
		g.drawImage(mapBackground, borderX, borderY, tileSize*result.getConfiguration().getWorld().getWidth(), tileSize*result.getConfiguration().getWorld().getHeight(), null);
		
		GridCellRenderer<Double> renderer = new GridCellRenderer<>() {			
			@Override
			public void render(Graphics2D g, GridValue<Double> cell, int centerX, int centerY, int width, int height) {
				Color col = probRampPaper.evaluate(cell.getValue());
				Color color = new Color(col.getRed(), col.getGreen(), col.getBlue(), 128);
				
				if(cell.getValue() > 0.0) {
					g.setColor(color);
					g.fillRect(centerX - tileSize/2, centerY - tileSize/2, tileSize, tileSize);
				}				
				
				g.setFont(new Font("", Font.PLAIN, tileSize/4));
				g.setColor(Color.BLACK);
				//if(showValue) TextDrawer.drawString(g, "%.2f".formatted(cell.getValue()), centerX, centerY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
				g.drawRect(centerX - tileSize/2, centerY - tileSize/2, tileSize, tileSize);
			}
		};
		
		GridRenderer.render(g, result.getConfiguration().getWorld().getProbabilities(), borderX, borderY, tileSize, renderer);
		// RENDER FLIGHTPATH OR -TRAJECTORY
		double factor = tileSize / result.getConfiguration().getSettings().getCellSize();
		if(showPlannerPath)
			FlightPathRenderer.render(g, result.getPlannerPath(), borderX, borderY, tileSize, EvaluationConfig.rampSpeed, 4.0);
		if(showTrajectoryPath)
			FlightPathRenderer.render(g, result.getTrajectoryPath(), borderX, borderY, factor, EvaluationConfig.rampSpeed, 4.0);
		
		g.dispose();
				
		
		try {
			ImageIO.write(image, "PNG", file);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private final class PredefinedFinder extends PathFinder {

		public PredefinedFinder() {
			super(PathFinderType.DISCRETE_FULL);
		}

		@Override
		public List<PathFinderResult> solveFull(DroneDiscrete drone) {
			
			return path.stream()
					.map(pp -> new PathFinderResult(pp.getX(), pp.getY(), true))
					.collect(Collectors.toUnmodifiableList());
			
		}
		
		@Override
		public String getName() {
			return "Science Paper";
		}		
	}
	
}
