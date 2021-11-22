package at.jku.cg.sar.eval;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import at.jku.cg.core.latex.LatexTableWriter;
import at.jku.cg.core.latex.LatexTableWriter.LatexTableEntry;
import at.jku.cg.core.latex.LatexTableWriter.LatexTableRow;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.gui.graphics.SimulationResultRenderer;
import at.jku.cg.sar.main.Comparator;
import at.jku.cg.sar.main.Configuration;
import at.jku.cg.sar.metric.AUPCMetric;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.trajectory.SimpleTrajectory;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;
import at.jku.cg.sar.world.World;
import at.jku.cg.sar.world.WorldGenerator;

class EvaluateTrajectoryPlanner {

	private static final File dir;
	
	static {
		dir = new File("out", "trajectory");
		dir.mkdirs();
	}
	
	@Test
	void compare() {
		
		World world = EvaluationConfig.worldSmoothedSpots;
		world = new World((new WorldGenerator(0)).SmoothedSpots(4, 4, 0.2, 0.05, 1.0));

		SimulatorSettings settingsDefault = new SimulatorSettings();
		SimulatorSettings settingsInfiniteAccel = new SimulatorSettings();
		settingsInfiniteAccel.setAcceleration(1_000_000);
		settingsInfiniteAccel.setDeceleration(1_000_000);
		
		settingsInfiniteAccel.setAcceleration(1_000);
		settingsInfiniteAccel.setDeceleration(1_000);
		
		TrajectoryPlanner plannerNone = null;
		TrajectoryPlanner plannerNormal = new SimpleTrajectory(settingsDefault);
		TrajectoryPlanner plannerInfiniteAccel = new SimpleTrajectory(settingsInfiniteAccel);
		List<TrajectoryPlanner> planners = new ArrayList<>();
		planners.add(plannerNone);
		planners.add(plannerNormal);
		planners.add(plannerInfiniteAccel);
		
		
		System.err.println("START");
		
		LatexTableWriter writer = new LatexTableWriter();
		writer.setLeftColumn("Planner");
		writer.setCaption("Planner comparison");
		writer.setLabel("tab:trajectory_planner_comparison");
		
		GridValue<Double> max = world.getProbabilities().max();
		System.err.println("MAX %f is at %d/%d".formatted(max.getValue(), max.getX(), max.getY()));
		int startX = max.getX();
		int startY = max.getY();
		
		Comparator comp = new Comparator(true);
		Configuration config = new Configuration(List.of(EvaluationConfig.finderRadialChecker), planners, EvaluationConfig.settings, new AUPCMetric(), startX, startY, world);
		List<SimulationResult> results = comp.evaluate(config);
				
		int imageSize = 1024;
		double border = 0.1;
		String format = "%8.2f";
		for(SimulationResult result : results) {
			FlightPath<?> path = null;
			String plannerName = "N/A";
			String accelerationString = "-";
			if(result.getPlanner() == plannerNone) {
				render(dir, "01_comp_planner_none.png", result, imageSize, border, true, false);
				path = result.getPlannerPath();
				plannerName = "None";
			}else {
				if(result.getPlanner().equals(plannerNormal)) {
					render(dir, "02_comp_planner_normal.png", result, imageSize, border, false, true);
					plannerName = "Normal";
					accelerationString = String.format(Locale.ROOT, format, settingsDefault.getAcceleration());
				}
				
				if(result.getPlanner().equals(plannerInfiniteAccel)) {
					render(dir, "03_comp_planner_infinit.png", result, imageSize, border, false, true);
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
		System.err.println("DONE");
	}
	
	private void render(File dir, String filename, SimulationResult result, int imageSize, double border, boolean showPlannerPath, boolean showTrajectoryPath) {
		int borderSize = (int) (imageSize * border);
		int gridSize = imageSize - 2 * borderSize;		
		int tileSize = Math.min(gridSize / result.getConfiguration().getWorld().getWidth(), gridSize / result.getConfiguration().getWorld().getHeight());
		
		BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		SimulationResultRenderer.renderRaw(g, result, borderSize, borderSize, tileSize,
				showPlannerPath, showTrajectoryPath,
				false, false, 0.0, false,
				EvaluationConfig.rampProbability, EvaluationConfig.rampSpeed);
		g.dispose();
				
		try {
			ImageIO.write(image, "PNG", new File(dir, filename));
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void evaluateDiagonals() {
		
		LatexTableWriter writer = new LatexTableWriter();
		writer.setLeftColumn("World");
		writer.setCaption("Diagonal comparison");
		writer.setLabel("tab:diagonal_comparison");
		
		SimpleTrajectory plannerNoDia = new SimpleTrajectory(EvaluationConfig.settings, false);
		
		for(World world : EvaluationConfig.worlds) {
			
			Comparator comp = new Comparator(true);

			Configuration configDia = new Configuration(List.of(EvaluationConfig.finderRadialChecker), List.of(EvaluationConfig.plannerSimple), EvaluationConfig.settings,
					new AUPCMetric(), EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y, world);
			Configuration configNoDia = new Configuration(List.of(EvaluationConfig.finderRadialChecker), List.of(plannerNoDia), EvaluationConfig.settings,
					new AUPCMetric(), EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y, world);

			List<SimulationResult> resultsDia = comp.evaluate(configDia);
			List<SimulationResult> resultsNoDia = comp.evaluate(configNoDia);
			
			double scoreDia		= resultsDia.get(0).getMetricResult().getScore();
			double scoreNoDia	= resultsNoDia.get(0).getMetricResult().getScore();
			boolean diaWins		= scoreDia > scoreNoDia;
			
			LatexTableRow row = new LatexTableRow();
			row.addEntry("World", world.getName());
			row.addEntry("With Diagonals", new LatexTableEntry(""+scoreDia, diaWins ? Color.GREEN : Color.WHITE));
			row.addEntry("No Diagonals", new LatexTableEntry(""+scoreNoDia, diaWins ? Color.WHITE : Color.GREEN));
			writer.addRow(row);
		}
		
		writer.export(new File(dir, "compareDiagonals.txt"));
	}
	
	@Test
	void evaluateDiagonalsDetailed() {
		
		LatexTableWriter writer = new LatexTableWriter();
		writer.setLeftColumn("World");
		writer.setCaption("Diagonal comparison detailed");
		writer.setLabel("tab:diagonal_comparison_detailed");
		
		
		List<Future<DiaResult>> futures = new ArrayList<>();
		List<DiaResult> results = new ArrayList<>();
		
		for(World world : EvaluationConfig.worlds) {			
			for(int x = 0; x < EvaluationConfig.WIDTH; x++) {
				for(int y = 0; y < EvaluationConfig.WIDTH; y++) {
					Future<DiaResult> future = EvaluationConfig.service.submit(new DiaChecker(world, x, y));
					futures.add(future);
				}
			}
		}
		
		for(Future<DiaResult> future : futures) {
			try {
				results.add(future.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		for(World world : EvaluationConfig.worlds) {
			int winsDia = (int) results.stream()
					.filter(res -> res.world.equals(world))
					.filter(res -> res.diaWins)
					.count();
			int winsNoDia = (int) results.stream()
					.filter(res -> res.world.equals(world))
					.filter(res -> !res.diaWins)
					.count();
			
			boolean diaWinsSum = winsDia > winsNoDia;
			LatexTableRow row = new LatexTableRow();
			row.addEntry("World", world.getName());
			row.addEntry("With Diagonals", new LatexTableEntry(""+winsDia, diaWinsSum ? Color.GREEN : Color.WHITE));
			row.addEntry("No Diagonals", new LatexTableEntry(""+winsNoDia, diaWinsSum ? Color.WHITE : Color.GREEN));
			writer.addRow(row);
			
		}
		
		writer.export(new File(dir, "compareDiagonalsDetailed.txt"));
	}
	
	private final class DiaChecker implements Callable<DiaResult> {

		private final World world;
		private final int x, y;

		private final SimpleTrajectory plannerNoDia = new SimpleTrajectory(EvaluationConfig.settings, false);
		
		public DiaChecker(World world, int x, int y) {
			super();
			this.world = world;
			this.x = x;
			this.y = y;
		}

		@Override
		public DiaResult call() throws Exception {
			System.err.print("RUNNING EVALUATION AT %02d/%02d\r".formatted(x, y));
			
			Comparator comp = new Comparator(true);

			Configuration configDia = new Configuration(List.of(EvaluationConfig.finderRadialChecker), List.of(EvaluationConfig.plannerSimple), EvaluationConfig.settings,
					new AUPCMetric(), x, y, world);
			Configuration configNoDia = new Configuration(List.of(EvaluationConfig.finderRadialChecker), List.of(plannerNoDia), EvaluationConfig.settings,
					new AUPCMetric(), x, y, world);

			List<SimulationResult> resultsDia = comp.evaluate(configDia);
			List<SimulationResult> resultsNoDia = comp.evaluate(configNoDia);
			
			double scoreDia		= resultsDia.get(0).getMetricResult().getScore();
			double scoreNoDia	= resultsNoDia.get(0).getMetricResult().getScore();
			boolean diaWins		= scoreDia > scoreNoDia;
			
			return new DiaResult(world, x, y, diaWins);
		}
		
	}
	
	private final class DiaResult {
		public final World world;
		public final int x, y;
		public final boolean diaWins;
		
		public DiaResult(World world, int x, int y, boolean diaWins) {
			super();
			this.world = world;
			this.x = x;
			this.y = y;
			this.diaWins = diaWins;
		}		
	}
	
}
