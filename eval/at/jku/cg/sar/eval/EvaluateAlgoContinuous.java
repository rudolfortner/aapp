package at.jku.cg.sar.eval;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.gui.ComparisonViewer;
import at.jku.cg.sar.gui.SimulationResultOverviewWindow;
import at.jku.cg.sar.gui.graphics.SimulationResultRenderer;
import at.jku.cg.sar.main.Comparator;
import at.jku.cg.sar.main.Configuration;
import at.jku.cg.sar.metric.AUPCMetric;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.scoring.RankColoring;
import at.jku.cg.sar.scoring.Ranking;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.trajectory.SimpleTrajectory;
import at.jku.cg.sar.world.World;

class EvaluateAlgoContinuous {
	
	private static final File dir;
	private static final File dirComparison;
	private static final File dirExamples;
	
	static {
		dir = new File("out", "algoContinuous");
		dir.mkdirs();
		dirComparison = new File(dir, "comparison");
		dirComparison.mkdirs();
		dirExamples = new File(dir, "examples");
		dirExamples.mkdirs();
	}
	
	@Test 
	void executionTimes(){
		String filenameExecutionTimes = "example_%02d_%02d_execution.txt".formatted(EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
		File fileExecutionTimes = new File(dirExamples, filenameExecutionTimes);
		
		EvaluateExecutionTime.evalExecutionTime(EvaluationConfig.worldSmoothedSpots, EvaluationConfig.findersContinuous, EvaluationConfig.plannerSimple,
				EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
				"Continuous Execution Times", "tab:continuous_execution", fileExecutionTimes);
	}
	
	@Test
	void exampleMatch() {
		String filenameGraph	= "example_%02d_%02d_graph.png".formatted(EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
		String filenameGraphRaw	= "example_%02d_%02d_graph_raw.txt".formatted(EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
		String filenameTable	= "example_%02d_%02d_table.txt".formatted(EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
		File fileGraph		= new File(dirExamples, filenameGraph);
		File fileGraphRaw	= new File(dirExamples, filenameGraphRaw);
		File fileTable		= new File(dirExamples, filenameTable);
		
		Map<PathFinder, String> shortNames = new HashMap<>();
		shortNames.put(EvaluationConfig.finderVacuumCleaner, "Vacuum Cleaner");
		shortNames.put(EvaluationConfig.finderVacuumCleanerGradient, "Vacuum Cleaner Gradient");
		
		EvaluationUtils.metricGraph(EvaluationConfig.worldSmoothedSpots,
				shortNames, EvaluationConfig.plannerSimple,
				EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
				fileGraph, fileGraphRaw, 512, 64);
		
		EvaluationUtils.exampleTable(EvaluationConfig.worldSmoothedSpots, shortNames, EvaluationConfig.plannerSimple,
				EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
				"Continuous Example Results", "tab:continuous_example_table", fileTable);
	}
	
	@Test
	void examples() {
		for(PathFinder finder : EvaluationConfig.findersContinuous) {
			System.err.println("Generating example for " + finder.getName());
			
			String finderName = finder.getName().toLowerCase().replace(" ", "_");
			String filename = "example_%s_%02d_%02d_trajectory.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameCoverage = "example_%s_%02d_%02d_coverage.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameCoverageInfo = "example_%s_%02d_%02d_coverage.txt".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameCoverageRamp = "example_%s_%02d_%02d_coverage_ramp.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameCoverageRampNT = "example_%s_%02d_%02d_coverage_ramp_NT.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameOverview = "example_%s_%02d_%02d_overview.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameOverviewNT = "example_%s_%02d_%02d_overview_NT.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			File fileTrajectory = new File(dirExamples, filename);
			File fileCoverage = new File(dirExamples, filenameCoverage);
			File fileCoverageInfo = new File(dirExamples, filenameCoverageInfo);
			File fileCoverageRamp = new File(dirExamples, filenameCoverageRamp);
			File fileCoverageRampNT = new File(dirExamples, filenameCoverageRampNT);
			File fileOverview = new File(dirExamples, filenameOverview);
			File fileOverviewNT = new File(dirExamples, filenameOverviewNT);
			
			int border = EvaluationConfig.IMAGE_SIZE / 32;
			EvaluationUtils.example(EvaluationConfig.worldSmoothedSpots, finder, EvaluationConfig.plannerSimple,
					EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
					false, true, false, fileTrajectory,
					EvaluationConfig.IMAGE_SIZE, border);
			
			EvaluationCoverage.create(EvaluationConfig.worldSmoothedSpots, finder, EvaluationConfig.plannerSimple,
					EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
					256, EvaluationConfig.SCAN_RADIUS, true, fileCoverage, fileCoverageInfo, fileCoverageRamp, fileCoverageRampNT);
			
			EvaluationUtils.fitImage(fileCoverage, fileCoverage, EvaluationConfig.IMAGE_SIZE, border);
			EvaluationUtils.addBorder(fileCoverageRamp, fileCoverageRamp, 0, border);
			EvaluationUtils.addBorder(fileCoverageRampNT, fileCoverageRampNT, 0, border);
			
			EvaluationUtils.combineImages(EvaluationConfig.IMAGE_SIZE, 16, List.of(fileTrajectory, fileCoverage, fileCoverageRamp),
					List.of("(a)", "(b)", ""), Color.WHITE, border, border,
					fileOverview, false);
			EvaluationUtils.combineImages(EvaluationConfig.IMAGE_SIZE, 16, List.of(fileTrajectory, fileCoverage, fileCoverageRampNT),
					List.of(), Color.WHITE, 0, border,
					fileOverviewNT, false);
		}
	}
	
	@Test
	void evaluateMax() {
		EvaluationUtils.evaluateMax(dir, EvaluationConfig.findersContinuous,
				"eval_continuous_max.txt", "Continuous Comparison MAX", "tab:continuous_comparison_max", RankColoring.FIRST_SECOND_LAST,
				result -> result.getMetricResult().getScore(), false);
	}
	
	@Test
	void evaluateAll() {
		EvaluationUtils.evaluateAll(dir, EvaluationConfig.findersContinuous,
				"eval_continuous_all.txt", "Continuous Comparison ALL", "tab:continuous_comparison_all", RankColoring.FIRST_LAST,
				result -> result.getMetricResult().getScore(), false);
	}
	
	@Test
	void createWinDistributionMap() {		
		for(World world : EvaluationConfig.worlds) {
			String worldName = world.getName().toLowerCase().replace(" ", "_");
			File file = new File(dir, "eval_continuous_%s_win_distribution.png".formatted(worldName));			
			
			EvaluationUtils.createWinDistributionMap(world,
					EvaluationConfig.finderRadialChecker, EvaluationConfig.findersContinuous, new SimpleTrajectory(new SimulatorSettings()),
					world.getName(), "Evaluated by Metric Score", false,
					file, res -> res.getMetricResult().getScore(), false);
		}
	}
	
	@Test
	void findOthersBetter() {
		for(World world : EvaluationConfig.worlds) {
			EvaluationUtils.findOthersBetter(dirComparison, world,
					EvaluationConfig.findersContinuous, EvaluationConfig.finderVacuumCleanerGradient,
					result -> result.getMetricResult().getScore(), false, 3);
		}		
	}
	
	void investigation() {
		
		int startX = 2;
		int startY = 0;
		World world = EvaluationConfig.worldUniform;
		String worldName = world.getName().toLowerCase().replace(" ", "_");
		String filename = "%s_%02d_%02d.png".formatted(worldName, startX, startY);
		
		Comparator comp = new Comparator(true);
		Configuration config = new Configuration(EvaluationConfig.findersContinuous, EvaluationConfig.planners, EvaluationConfig.settings, new AUPCMetric(), startX, startY, world);
		List<SimulationResult> results = comp.evaluate(config);

		Ranking<SimulationResult> ranking = new Ranking<>(results, r -> r.getMetricResult().getScore());
		
		
		// OUTPUT
		BufferedImage image = SimulationResultRenderer.renderComparison(ranking.getSorted(),
				1024, true, false, EvaluationConfig.rampProbability, EvaluationConfig.rampSpeed);
		
		try {
			File file = new File(dirComparison, filename);
			file.mkdirs();
			ImageIO.write(image, "PNG", file);
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		if(true) {
			new ComparisonViewer(config, results, true);
			new SimulationResultOverviewWindow(config, results, true);
			try {
				Thread.sleep(1000000);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
