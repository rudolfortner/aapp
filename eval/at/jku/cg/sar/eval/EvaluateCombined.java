package at.jku.cg.sar.eval;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.world.World;

class EvaluateCombined {

	private static final File dir, dirMetric, dirExecution;
	
	static {
		dir = new File("out", "combined");
		dir.mkdirs();
		dirExecution = new File(dir, "execution");
		dirExecution.mkdirs();
		dirMetric = new File(dir, "metric");
		dirMetric.mkdirs();
	}
	
	@Test 
	void executionTimes(){
		String filenameExecutionTimes = "example_%02d_%02d_execution.txt".formatted(EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
		File fileExecutionTimes = new File(dirExecution, filenameExecutionTimes);
		
		EvaluateExecutionTime.evalExecutionTime(EvaluationConfig.worldSmoothedSpots, EvaluationConfig.findersCombinedShortNames, EvaluationConfig.plannerSimple,
				EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
				"Combined Execution Times", "tab:combined_execution", fileExecutionTimes);
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
					EvaluationConfig.findersCombinedShortNames, EvaluationConfig.plannerSimple,
					EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
					fileMetric, fileMetricRaw, 512, 64+16,
					0.6, 2.0/3.0);
			
			
			// Create table
			String filenameTable = "%s_%02d_%02d_table.txt".formatted(worldName, EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
			File fileTable = new File(dirMetric, filenameTable);
			String tableCaption = "Results for " + world.getName();
			String tableLabel	= "tab:table_" + worldName;
			EvaluationUtils.exampleTable(world,
					EvaluationConfig.findersCombinedShortNames, EvaluationConfig.plannerSimple,
					EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y,
					tableCaption, tableLabel, fileTable);
		}
		
		EvaluationUtils.combineImagesGrid(1024, 8, 2, 3, files,
				List.of("(a)", "(b)", "(c)", "(d)", "(e)", "(f)"), Color.BLACK, 32, 32,
				new File(dirMetric, "metric_combined.png"));
	}

}
