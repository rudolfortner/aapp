package at.jku.cg.sar.eval;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.scoring.RankColoring;

class EvaluateAlgoContinuousVsDiscrete {

	private static File dir;
	private static File dirExamples;

	private static PathFinder algo1 = EvaluationConfig.finderRadialChecker;
	private static PathFinder algo2 = EvaluationConfig.finderVacuumCleanerGradient;
	static {
		dir = new File("out", "algoContinuousVsDiscrete");
		dir.mkdirs();
//		dirComparison = new File(dir, "comparison");
//		dirComparison.mkdirs();
		dirExamples = new File(dir, "examples");
		dirExamples.mkdirs();
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
		shortNames.put(EvaluationConfig.finderRadialChecker, "Radial");
		shortNames.put(EvaluationConfig.finderVacuumCleanerGradient, "Continuous Gradient");
		
		EvaluationUtils.metricGraph(EvaluationConfig.worldSmoothedSpots,
				shortNames, EvaluationConfig.plannerSimple,
				EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
				fileGraph, fileGraphRaw,
				512, 64,
				0.9, 2.0/3.0);
		
		EvaluationUtils.exampleTable(EvaluationConfig.worldSmoothedSpots, shortNames, EvaluationConfig.plannerSimple,
				EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
				"Continuous Vs. Discrete Example Results", "tab:continuous_vs_discrete_example_table", fileTable);
		
		
		// Combine VCG and metric of comparison
		File file0 = new File("out/algoContinuous/examples/example_vacuum_cleaner_gradient_04_08_trajectory.png");
		File file1 = new File("out/algoContinuous/examples/example_vacuum_cleaner_gradient_04_08_coverage.png");
		File file2 = new File("out/algoContinuous/examples/example_vacuum_cleaner_gradient_04_08_coverage_ramp.png");
		File file2_NT = new File("out/algoContinuous/examples/example_vacuum_cleaner_gradient_04_08_coverage_ramp_NT.png");
		File combined = new File(dir, "combined.png");
		File combinedNT = new File(dir, "combined_NT.png");
		
		EvaluationUtils.combineImages(EvaluationConfig.IMAGE_SIZE, EvaluationConfig.IMAGE_SIZE / 64, List.of(file0, file1, file2, fileGraph),
				List.of("(a)", "(b)", "", "(c)"), Color.BLACK, 32, 0,
				combined, false);
		EvaluationUtils.combineImages(EvaluationConfig.IMAGE_SIZE, EvaluationConfig.IMAGE_SIZE / 64, List.of(file0, file1, file2_NT, fileGraph),
				List.of(), Color.BLACK, 0, 0,
				combinedNT, false);
	}
	
	@Test
	void evaluateMax() {
		EvaluationUtils.evaluateMax(dir, List.of(algo1, algo2),
				"eval_max.txt", "Comparison MAX", "tab:comparison_max", RankColoring.FIRST_LAST,
				result -> result.getMetricResult().getScore(), false);
	}
	
	@Test
	void evaluateAll() {
		EvaluationUtils.evaluateAll(dir, List.of(algo1, algo2),
				"eval_all.txt", "Comparison ALL", "tab:comparison_all", RankColoring.FIRST_LAST,
				result -> result.getMetricResult().getScore(), false);
	}

}
