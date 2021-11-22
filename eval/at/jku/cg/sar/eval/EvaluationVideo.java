package at.jku.cg.sar.eval;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.gui.graphics.SimulationResultRenderer;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.scoring.Ranking;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;
import at.jku.cg.sar.world.World;

class EvaluationVideo {
	
	private static File dir;
	
	static {
		dir = new File("out", "videos");
		dir.mkdirs();
	}
	
	@Test
	void renderAll() {
		
		int size = 1024;
		int border = 64;
		
		for(PathFinder finder : Stream.concat(EvaluationConfig.findersDiscrete.stream(), EvaluationConfig.findersContinuous.stream()).collect(Collectors.toList())) {
			System.err.println("Rendering video for %s".formatted(finder.getName()));
			renderExample(finder, EvaluationConfig.plannerSimple, size, border);
		}
		
		
	}
	
	public static void renderExample(PathFinder finder, TrajectoryPlanner planner,
			int size, int border) {
		render(EvaluationConfig.worldSmoothedSpots, finder, planner,
				EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y, size, border);
	}
	
	public static void render(World world, PathFinder finder, TrajectoryPlanner planner,
			int startX, int startY, int size, int border) {	
		
		String dirName = finder.getName().replace(" ", "_").toLowerCase();
		
		Ranking<SimulationResult> results = EvaluationUtils.rankSingleImmediate(world, startX, startY,
				List.of(finder), List.of(planner), r -> 1.0, false);
		
		SimulationResultRenderer.renderAnimation(results.getSorted().get(0), new File(dir, dirName), 1024, 64, true, true, true, true);
		
	}

}
