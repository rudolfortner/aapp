package at.jku.cg.sar.eval;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import at.jku.cg.core.latex.LatexTableWriter;
import at.jku.cg.core.latex.LatexTableWriter.LatexTableRow;
import at.jku.cg.sar.core.grid.GridIO;
import at.jku.cg.sar.gui.graphics.GridRenderer;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.apf.AttractionApproachGradient;
import at.jku.cg.sar.pathfinder.misc.GridFinder;
import at.jku.cg.sar.pathfinder.misc.RadialChecker;
import at.jku.cg.sar.pathfinder.misc.SpiralFinder;
import at.jku.cg.sar.pathfinder.vacuum.VacuumCleaner;
import at.jku.cg.sar.pathfinder.vacuum.VacuumCleanerGradient;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.trajectory.SimpleTrajectory;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;
import at.jku.cg.sar.util.ColorRamp;
import at.jku.cg.sar.world.World;
import at.jku.cg.sar.world.WorldGenerator;

public final class EvaluationConfig {
	
	// THREADING
	public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
	public static final int MIN_THREAD_COUNT = 1;
	public static final int MAX_THREAD_COUNT = 64;
	public static final int THREAD_COUNT = Math.min(Math.max(NUM_THREADS-1, MIN_THREAD_COUNT), MAX_THREAD_COUNT);
	public static final ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
	
	// OUTPUT
	private static final File dir = new File("out");
	private static final File dirWorlds = new File(dir, "worlds");
	private static final String DUMP_LEFT_NAME = "Name";
	private static final String DUMP_RIGHT_NAME = "Data";
	private static final String UNIT_ACCEL = "\\frac{m}{s^2}";
	private static final String UNIT_SPEED = "\\frac{m}{s}";


	public static final SimulatorSettings settings = new SimulatorSettings();
	
	// WORLD
	public static final List<World> worlds = new ArrayList<>();
	public static final double CELL_SIZE = 30.0;
	public static final double SCAN_RADIUS = 30.0 / 2.0;
	public static final double SCAN_DISTANCE = 1.0;
	public static final int WIDTH = 16;
	public static final double minProb = 0.05;

	public static final ColorRamp rampProbability, rampSpeed;
	
	// PATH FINDERS
	public static final Map<PathFinder, String> findersDiscreteShortNames = new HashMap<>();
	public static final Map<PathFinder, String> findersCombinedShortNames = new HashMap<>();
	public static final List<PathFinder> findersDiscrete = new ArrayList<>();
	public static final List<PathFinder> findersContinuous = new ArrayList<>();

	// DISCRETE
	public static final PathFinder finderSpiral, finderGrid;
	public static final PathFinder finderRadialChecker, finderAAG;
	
	// CONTINUOUS
	public static final PathFinder finderVacuumCleaner, finderVacuumCleanerGradient;
	
	// PLANNERS
	public static final List<TrajectoryPlanner> planners = new ArrayList<>();
	public static final TrajectoryPlanner plannerSimple, plannerSimpleInfinit;
	
	public static final World worldSpots, worldSmoothedSpots, worldSmoothedSpotsNorm;	
	public static final World worldGaussian, worldPatches, worldNoise, worldUniform;	
	public static final World worldManual, worldManualRandomized, worldSimulated;
	
	// EXAMPLE
	public static final int EXAMPLE_START_X = 4;
	public static final int EXAMPLE_START_Y = 8;
	
	public static final int IMAGE_SIZE = 4096;
	
	// IMPORTANT MAPS SHOWN IN THE PAPER
	public static final List<World> importantWorlds = new ArrayList<>();
	
	static {
		WorldGenerator generator = new WorldGenerator(0);
		
		worldSpots			= new World("Spots", generator.Spots(WIDTH, WIDTH, 0.2, minProb, 1.0));
		worldSmoothedSpots	= new World("Smoothed Spots", generator.SmoothedSpots(WIDTH, WIDTH, 0.2, minProb, 1.0));
		worldSmoothedSpotsNorm= new World("Smoothed Spots Normalized", generator.SmoothedSpotsNormalized(WIDTH, WIDTH, 0.2, minProb, 1.0));

		worldGaussian			= new World("Gaussian", generator.Gaussian(WIDTH, WIDTH, WIDTH/5, minProb, 1.0));
		worldPatches			= new World("Patches", generator.Patches(WIDTH, WIDTH, WIDTH/6, 3, minProb, 1.0));
		worldNoise			= new World("Noise", generator.Noise(WIDTH, WIDTH));
		worldUniform			= new World("Uniform", generator.Uniform(WIDTH, WIDTH, 0.05));
		
		worldManual			= new World("Manual World", GridIO.fromImageFile(new File("in/map_smooth.png"), true, minProb));
		worldManualRandomized	= new World("Manual World Randomized", GridIO.fromImageFile(new File("in/map_randomized.png"), true, minProb));

		worldSimulated		= new World("Simulated World", GridIO.fromImageFile(new File("in/map_simulated.png"), true, minProb));

		
		worlds.add(worldSpots);
		worlds.add(worldSmoothedSpots);
		worlds.add(worldSmoothedSpotsNorm);

		worlds.add(worldGaussian);
		worlds.add(worldPatches);
		worlds.add(worldNoise);
		worlds.add(worldUniform);

		worlds.add(worldManual);
		worlds.add(worldManualRandomized);

		worlds.add(worldSimulated);
		
		importantWorlds.add(worldSpots);
		importantWorlds.add(worldSmoothedSpots);
		importantWorlds.add(worldPatches);
		importantWorlds.add(worldGaussian);
		importantWorlds.add(worldManual);
		importantWorlds.add(worldSimulated);
		
		
		// DISCRETE
		finderSpiral = new SpiralFinder();
		finderGrid = new GridFinder(true, false);
		finderRadialChecker = new RadialChecker(true);
		finderAAG = new AttractionApproachGradient();
		
		findersDiscrete.add(finderSpiral);
		findersDiscrete.add(finderGrid);
		findersDiscrete.add(finderRadialChecker);
		findersDiscrete.add(finderAAG);
		
		findersDiscreteShortNames.put(finderSpiral, "Spiral");
		findersDiscreteShortNames.put(finderGrid, "Grid");
		findersDiscreteShortNames.put(finderAAG, "Gradient");
		findersDiscreteShortNames.put(finderRadialChecker, "Radial");
		
		// CONTINUOUS
		finderVacuumCleaner = new VacuumCleaner();
		finderVacuumCleanerGradient = new VacuumCleanerGradient();

		findersContinuous.add(finderVacuumCleaner);
		findersContinuous.add(finderVacuumCleanerGradient);
		
		
		// COMBINED
		findersCombinedShortNames.put(finderSpiral, "Spiral");
		findersCombinedShortNames.put(finderGrid, "Grid");
		findersCombinedShortNames.put(finderAAG, "Gradient");
		findersCombinedShortNames.put(finderRadialChecker, "Radial");
		findersCombinedShortNames.put(finderVacuumCleanerGradient, "Continuous Gradient");
		
		
		// PLANNERS
		plannerSimple = new SimpleTrajectory(settings, true);
		planners.add(plannerSimple);
		
		settings.setForceFastFlight(true);
		
		SimulatorSettings settingsInfinit = new SimulatorSettings();
		settingsInfinit.setForceFastFlight(true);
		settingsInfinit.setAcceleration(1000.0);
		settingsInfinit.setDeceleration(1000.0);
		plannerSimpleInfinit = new SimpleTrajectory(settingsInfinit, true);
//		planners.add(plannerSimpleInfinit);
		
		

		// Color Ramps used for rendering
		rampProbability = new ColorRamp();
		rampProbability.add(0.0, new Color(102, 0, 153));
		rampProbability.add(0.2, Color.BLUE);
		rampProbability.add(0.5, Color.GREEN);
		rampProbability.add(0.7, Color.ORANGE);
		rampProbability.add(1.0, Color.RED);
		
		rampSpeed = new ColorRamp();
		rampSpeed.add(settings.getSpeedSlow(), Color.BLUE);
		rampSpeed.add(settings.getSpeedScan(), Color.GREEN);
		rampSpeed.add(settings.getSpeedFast(), Color.RED);
	}
	
	@Test
	private void dumpAll() {
		dumpConfig();
		dumpRamps();
		dumpWorlds();
	}
	
	
	private static void dumpConfig() {
		LatexTableWriter writer = new LatexTableWriter();
		writer.setLeftColumn(DUMP_LEFT_NAME);
		writer.setCaption("Evaluation Configuration");
		writer.setLabel("tab:evaluation_config");

		dumpConfigRow(writer, "Map Size", "%dx%d".formatted(WIDTH, WIDTH));
		dumpConfigRow(writer, "Cell Size", formatUnit(settings.getCellSize(), "m"));

		dumpConfigRow(writer, "$v_A$", formatUnit(settings.getSpeedSlow(), UNIT_SPEED));
		dumpConfigRow(writer, "$v_S$", formatUnit(settings.getSpeedScan(), UNIT_SPEED));
		dumpConfigRow(writer, "$v_F$", formatUnit(settings.getSpeedFast(), UNIT_SPEED));

		dumpConfigRow(writer, "$a$", formatUnit(settings.getAcceleration(), UNIT_ACCEL));
		dumpConfigRow(writer, "$b$", formatUnit(settings.getDeceleration(), UNIT_ACCEL));
		
		writer.export(new File(dir, "evaluationConfig.txt"));
	}
	
	private static void dumpConfigRow(LatexTableWriter writer, String configName, String configData) {
		LatexTableRow row = new LatexTableRow();
		row.addEntry(DUMP_LEFT_NAME, configName);
		row.addEntry(DUMP_RIGHT_NAME, configData);
		writer.addRow(row);
	}
	
	private static String formatUnit(Number number, String unit) {
		String template = "";
		if(number instanceof Double) template = "%1.1f";
		if(number instanceof Integer) template = "%1d";
		
		
		StringBuilder builder = new StringBuilder();
		builder.append("$");
		builder.append(String.format(Locale.ROOT, template, number));
		if(unit != null) {
			builder.append("\\ ");
			builder.append(unit);
		}
		builder.append("$");
		
		return builder.toString();
	}
	
	private static void dumpRamps() {
		File fileProb = new File(dir, "ramp_probability.png");
		File fileSpeed = new File(dir, "ramp_speed.png");	
		File fileProbNT = new File(dir, "ramp_probability_NT.png");
		File fileSpeedNT = new File(dir, "ramp_speed_NT.png");		

		EvaluationUtils.exportColorRamp(rampProbability, IMAGE_SIZE, true,
				"Probability", true,
				fileProb);
		EvaluationUtils.exportColorRamp(rampProbability, IMAGE_SIZE, true,
				"Probability", true, val->val, true,
				fileProbNT);
		
		
		EvaluationUtils.exportColorRamp(rampSpeed, IMAGE_SIZE, false,
				"Speed [m/s]", true,
				fileSpeed);
		EvaluationUtils.exportColorRamp(rampSpeed, IMAGE_SIZE, false,
				"Speed [m/s]", true, val->val, true,
				fileSpeedNT);
	}
	
	private static void dumpWorlds() {
		List<File> files = new ArrayList<>();
		for(World w : worlds) {
			String worldName = "world_" + w.getName().toLowerCase().replace(" ", "_") + ".png";
			File file = new File(dirWorlds, worldName);
			if(importantWorlds.contains(w)) files.add(file);
			file.mkdirs();
			
			BufferedImage gridImage = GridRenderer.renderImage(w.getProbabilities(), IMAGE_SIZE, rampProbability, false, null);
			
			try {
				ImageIO.write(gridImage, "PNG", file);
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Prepare World overview
		File combinedMaps = new File(dir, "worlds_combined.png");
		File combinedRamp = new File(dir, "worlds_ramped.png");
		File combinedMapsNT = new File(dir, "worlds_combined_NT.png");
		File combinedRampNT = new File(dir, "worlds_ramped_NT.png");
		
		EvaluationUtils.combineImagesGrid(IMAGE_SIZE, 32, 2, 3, files,
				List.of("(a)", "(b)", "(c)", "(d)", "(e)", "(f)"), Color.WHITE, EvaluationConfig.IMAGE_SIZE/32, 8,
				combinedMaps);
		
		EvaluationUtils.combineImagesGrid(IMAGE_SIZE, 32, 2, 3, files,
				List.of(), Color.WHITE, 0, 8,
				combinedMapsNT);
		
		EvaluationUtils.combineImages(IMAGE_SIZE*4, 0,
				List.of(combinedMaps, new File(dir, "ramp_probability.png")),
				combinedRamp, true);		
		EvaluationUtils.combineImages(IMAGE_SIZE*4, 0,
				List.of(combinedMaps, new File(dir, "ramp_probability_NT.png")),
				combinedRampNT, true);
		
	}
	
	public static void setRenderSettings(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	}

}
