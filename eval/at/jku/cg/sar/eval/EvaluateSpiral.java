package at.jku.cg.sar.eval;
import java.io.File;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.pathfinder.misc.GridFinder;
import at.jku.cg.sar.pathfinder.misc.SpiralFinder;
import at.jku.cg.sar.scoring.RankColoring;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.trajectory.SimpleTrajectory;
import at.jku.cg.sar.world.World;

class EvaluateSpiral {
	
	private static final File dir;
	
	static {
		dir = new File("out", "spiralFinder");
		dir.mkdirs();
	}
	
	@Test
	void evaluateMax() {
		EvaluationUtils.evaluateMax(dir, EvaluationConfig.findersDiscrete,
				"eval_spiral_max.txt", "Spiral Evaluation MAX", "tab:spiral_evaluation_max", RankColoring.FIRST,
				result -> result.getFinalPath().getDuration(), true);
	}
	
	@Test
	void evaluateAll() {
		EvaluationUtils.evaluateAll(dir, EvaluationConfig.findersDiscrete,
				"eval_spiral_all.txt", "Spiral Evaluation ALL", "tab:spiral_evaluation_all", RankColoring.FIRST,
				result -> result.getFinalPath().getDuration(), true);
	}
	
	@Test
	void findOthersBetter() {
		File dirComparison = new File(dir, "comparison");
		
		for(World world : EvaluationConfig.worlds) {
			EvaluationUtils.findOthersBetter(dirComparison, world,
					EvaluationConfig.findersDiscrete, EvaluationConfig.finderSpiral,
					result -> result.getMetricResult().getScore(), false, 3);
		}
	}
	
	@Test
	void createSpiralWinDistributionMap() {
		for(World world : EvaluationConfig.worlds) {
			String worldName = world.getName().toLowerCase().replace(" ", "_");
			System.err.println("Win Distribution for %s".formatted(world.getName()));

			File file = new File(dir, "eval_spiral_%s_win_distribution.png".formatted(worldName));
			
			EvaluationUtils.createWinDistributionMap(world,
					EvaluationConfig.finderSpiral, EvaluationConfig.findersDiscrete,
					new SimpleTrajectory(new SimulatorSettings()), 
					world.getName(), "Evaluated by Final Path Duration", false,
					file, res -> res.getFinalPath().getDuration(), true);
		}
	}
	
	@Test
	void createSpiralTimeDistributionMap() {
		File file = new File(dir, "eval_spiral_time_distribution.png");
		EvaluationUtils.createTimeDistributionMap(EvaluationConfig.worldSmoothedSpots, new SpiralFinder(), file, false);
	}
	
	@Test
	void createGridTimeDistributionMap() {
		File file = new File(dir, "eval_grid_time_distribution.png");
		EvaluationUtils.createTimeDistributionMap(EvaluationConfig.worldSmoothedSpots, new GridFinder(true, false), file, false);
	}
}
