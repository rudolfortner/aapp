package at.jku.cg.sar.eval;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

class EvaluateAlgoDiscrete {
	
	private static final File dir;
	private static final File dirComparison;
	private static final File dirExamples;
	private static final File dirExamplesAll;
	private static final File dirMetric;
	
	static {
		dir = new File("out", "algoDiscrete");
		dir.mkdirs();
		dirComparison = new File(dir, "comparison");
		dirComparison.mkdirs();
		dirExamples = new File(dir, "examples");
		dirExamples.mkdirs();
		dirExamplesAll = new File(dir, "examplesAll");
		dirExamplesAll.mkdirs();
		dirMetric = new File(dir, "metric");
		dirMetric.mkdirs();
	}
	
	@Test 
	void executionTimes(){
		String filenameExecutionTimes = "example_%02d_%02d_execution.txt".formatted(EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
		File fileExecutionTimes = new File(dirExamples, filenameExecutionTimes);
		
		EvaluateExecutionTime.evalExecutionTime(EvaluationConfig.worldSmoothedSpots, EvaluationConfig.findersDiscrete, EvaluationConfig.plannerSimple,
				EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
				"Discrete Execution Times", "tab:discrete_execution", fileExecutionTimes);
	}
	
	@Test
	void exampleMatch() {
		String filenameGraph	= "example_%02d_%02d_graph.png".formatted(EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
		String filenameGraphRaw	= "example_%02d_%02d_graph_raw.txt".formatted(EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
		String filenameTable	= "example_%02d_%02d_table.txt".formatted(EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
		File fileGraph		= new File(dirExamples, filenameGraph);
		File fileGraphRaw	= new File(dirExamples, filenameGraphRaw);
		File fileTable		= new File(dirExamples, filenameTable);
		

		
		EvaluationUtils.metricGraph(EvaluationConfig.worldSmoothedSpots,
				EvaluationConfig.findersDiscreteShortNames, EvaluationConfig.plannerSimple,
				EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
				fileGraph, fileGraphRaw, 512, 64);
		
		EvaluationUtils.exampleTable(EvaluationConfig.worldSmoothedSpots, EvaluationConfig.findersDiscreteShortNames, EvaluationConfig.plannerSimple,
				EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
				"Discrete Example Results", "tab:discrete_example_table", fileTable);
	}
	
	@Test
	void metricPlot() {
		List<File> files = new ArrayList<>();
		
		for(World world : EvaluationConfig.worlds) {
			String worldName = world.getName().toLowerCase().replace(" ", "_");
			String filenameMetric		= "%s_%02d_%02d_metric.png".formatted(worldName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameMetricRaw	= "%s_%02d_%02d_metric_raw.txt".formatted(worldName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			File fileMetric = new File(dirMetric, filenameMetric);
			File fileMetricRaw = new File(dirMetric, filenameMetricRaw);
			if(EvaluationConfig.importantWorlds.contains(world)) files.add(fileMetric);
			
			// Plot metric
			EvaluationUtils.metricGraph(world,
					EvaluationConfig.findersDiscreteShortNames, EvaluationConfig.plannerSimple,
					EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
					fileMetric, fileMetricRaw, 512, 64+16,
					0.6, 2.0/3.0);
			
			
			// Create table
			String filenameTable = "%s_%02d_%02d_table.txt".formatted(worldName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			File fileTable = new File(dirMetric, filenameTable);
			String tableCaption = "Results for " + world.getName();
			String tableLabel	= "tab:table_" + worldName;
			EvaluationUtils.exampleTable(world,
					EvaluationConfig.findersDiscreteShortNames, EvaluationConfig.plannerSimple,
					EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
					tableCaption, tableLabel, fileTable);
		}
		
		EvaluationUtils.combineImagesGrid(1024, 8, 2, 3, files,
				List.of("(a)", "(b)", "(c)", "(d)", "(e)", "(f)"), Color.BLACK, 32, 32,
				new File(dirMetric, "metric_combined.png"));
	}
	
	
	@Test
	void examples() {
		World world = EvaluationConfig.worldSmoothedSpots;
		
		for(PathFinder finder : EvaluationConfig.findersDiscrete) {
			String finderName = finder.getName().toLowerCase().replace(" ", "_");
			String filenamePath			= "example_%s_%02d_%02d_path.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameTrajectory	= "example_%s_%02d_%02d_trajectory.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameTrajectoryInf= "example_%s_%02d_%02d_trajectory_inf.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameCoverage		= "example_%s_%02d_%02d_coverage.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameCoverageInfo	= "example_%s_%02d_%02d_coverage.txt".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameCoverageRamp	= "example_%s_%02d_%02d_coverage_ramp.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameCoverageRampNT	= "example_%s_%02d_%02d_coverage_ramp_NT.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			String filenameOverview		= "example_%s_%02d_%02d_overview.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y); 
			String filenameOverviewNT	= "example_%s_%02d_%02d_overview_NT.png".formatted(finderName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y); 
			File filePath				= new File(dirExamples, filenamePath);
			File fileTrajectory			= new File(dirExamples, filenameTrajectory);
			File fileTrajectoryInf		= new File(dirExamples, filenameTrajectoryInf);
			File fileCoverage			= new File(dirExamples, filenameCoverage);
			File fileCoverageInfo		= new File(dirExamples, filenameCoverageInfo);
			File fileCoverageRamp		= new File(dirExamples, filenameCoverageRamp);
			File fileCoverageRampNT		= new File(dirExamples, filenameCoverageRampNT);
			File fileOverview			= new File(dirExamples, filenameOverview);
			File fileOverviewNT			= new File(dirExamples, filenameOverviewNT);
			
			EvaluationUtils.example(world, finder, EvaluationConfig.plannerSimple,
					EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
					true, false, false, filePath, EvaluationConfig.IMAGE_SIZE, 0);
			
			EvaluationUtils.example(world, finder, EvaluationConfig.plannerSimpleInfinit,
					EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
					false, true, false, fileTrajectoryInf, EvaluationConfig.IMAGE_SIZE, 0);
			
			EvaluationUtils.example(world, finder, EvaluationConfig.plannerSimple,
					EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
					false, true, false, fileTrajectory, EvaluationConfig.IMAGE_SIZE, 0);

			EvaluationCoverage.create(world, finder, EvaluationConfig.plannerSimple,
					EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
					256, EvaluationConfig.SCAN_RADIUS, true, fileCoverage, fileCoverageInfo, fileCoverageRamp, fileCoverageRampNT);
			
			
			EvaluationUtils.combineImages(EvaluationConfig.IMAGE_SIZE, 64, List.of(fileTrajectoryInf, fileTrajectory, fileCoverage, fileCoverageRamp),
					List.of("(a)", "(b)", "(c)"), Color.WHITE, EvaluationConfig.IMAGE_SIZE/32, 0,
					fileOverview, false);
			
			EvaluationUtils.combineImages(EvaluationConfig.IMAGE_SIZE, 64, List.of(fileTrajectoryInf, fileTrajectory, fileCoverage, fileCoverageRampNT),
					List.of(), Color.WHITE, 0, 0,
					fileOverviewNT, false);
			
		}
	}
	
	@Test
	void examplesAllWorlds() {
		for(World world : EvaluationConfig.worlds) {
			
			String worldName = world.getName().toLowerCase().replace(" ", "_");
			System.err.println("Exporting maps for map " + world.getName());
			for(PathFinder finder : EvaluationConfig.findersDiscrete) {
				
				String finderName = finder.getName().toLowerCase().replace(" ", "_");
				String filenameTrajectory		= "%s_%s_trajectory.png".formatted(worldName, finderName);
				String filenameTrajectoryInf	= "%s_%s_trajectory_inf.png".formatted(worldName, finderName);
				String filenameCoverage			= "%s_%s_coverage.png".formatted(worldName, finderName);
				String filenameCoverageInfo		= "%s_%s_coverage.txt".formatted(worldName, finderName);
				String filenameCoverageRamp		= "%s_%s_coverage_ramp.png".formatted(worldName, finderName);
				String filenameCoverageRampNT	= "%s_%s_coverage_ramp_NT.png".formatted(worldName, finderName);
				String filenameOverview			= "%s_%s_overview.png".formatted(worldName, finderName); 
				String filenameOverviewNT		= "%s_%s_overview_NT.png".formatted(worldName, finderName); 
				File fileTrajectory			= new File(dirExamplesAll, filenameTrajectory);
				File fileTrajectoryInf		= new File(dirExamplesAll, filenameTrajectoryInf);
				File fileCoverage			= new File(dirExamplesAll, filenameCoverage);
				File fileCoverageInfo		= new File(dirExamplesAll, filenameCoverageInfo);
				File fileCoverageRamp		= new File(dirExamplesAll, filenameCoverageRamp);
				File fileCoverageRampNT		= new File(dirExamplesAll, filenameCoverageRampNT);
				File fileOverview			= new File(dirExamplesAll, filenameOverview);
				File fileOverviewNT			= new File(dirExamplesAll, filenameOverviewNT);
				
				EvaluationUtils.example(world, finder, EvaluationConfig.plannerSimpleInfinit,
						EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
						false, true, false, fileTrajectoryInf, EvaluationConfig.IMAGE_SIZE, 0);
				
				EvaluationUtils.example(world, finder, EvaluationConfig.plannerSimple,
						EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
						false, true, false, fileTrajectory, EvaluationConfig.IMAGE_SIZE, 0);

				EvaluationCoverage.create(world, finder, EvaluationConfig.plannerSimple,
						EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y, 256, EvaluationConfig.SCAN_RADIUS, true, 0, 100,
						true, fileCoverage, fileCoverageInfo, fileCoverageRamp, fileCoverageRampNT);
				
				
				EvaluationUtils.combineImages(EvaluationConfig.IMAGE_SIZE, 64, List.of(fileTrajectoryInf, fileTrajectory, fileCoverage, fileCoverageRamp),
						List.of("(a)", "(b)", "(c)"), Color.WHITE, EvaluationConfig.IMAGE_SIZE/32, 0,
						fileOverview, false);
				
				EvaluationUtils.combineImages(EvaluationConfig.IMAGE_SIZE, 64, List.of(fileTrajectoryInf, fileTrajectory, fileCoverage, fileCoverageRampNT),
						List.of(), Color.WHITE, 0, 0,
						fileOverviewNT, false);
				
				// DELETE ALL AFTER COMBINED
				List.of(fileTrajectory, fileTrajectoryInf, fileCoverage, fileCoverageInfo, fileCoverageRamp, fileCoverageRampNT).stream().forEach(file -> file.delete());
			}
		}		
	}

	
	@Test
	void evaluateMax() {
		EvaluationUtils.evaluateMax(dir, EvaluationConfig.findersDiscrete,
				"eval_discrete_max.txt", "Discrete Comparison MAX", "tab:discrete_comparison_max", RankColoring.FIRST_SECOND_LAST,
				result -> result.getMetricResult().getScore(), false);
	}
	
	@Test
	void evaluateAll() {
		EvaluationUtils.evaluateAll(dir, EvaluationConfig.findersDiscrete,
				"eval_discrete_all.txt", "Discrete Comparison ALL", "tab:discrete_comparison_all", RankColoring.FIRST_LAST,
				result -> result.getMetricResult().getScore(), false);
	}
	
	@Test
	void createWinDistributionMap() {		
		for(World world : EvaluationConfig.worlds) {
			String worldName = world.getName().toLowerCase().replace(" ", "_");
			File file = new File(dir, "eval_discrete_%s_win_distribution.png".formatted(worldName));			
			
			EvaluationUtils.createWinDistributionMap(world,
					EvaluationConfig.finderRadialChecker, EvaluationConfig.findersDiscrete, new SimpleTrajectory(new SimulatorSettings()),
					world.getName(), "Evaluated by Metric Score", false,
					file, res -> res.getMetricResult().getScore(), false);
		}
	}
	
	@Test
	void findOthersBetter() {
		for(World world : EvaluationConfig.worlds) {
			EvaluationUtils.findOthersBetter(dirComparison, world,
					EvaluationConfig.findersDiscrete, EvaluationConfig.finderRadialChecker,
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
		Configuration config = new Configuration(EvaluationConfig.findersDiscrete, EvaluationConfig.planners, EvaluationConfig.settings, new AUPCMetric(), startX, startY, world);
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
