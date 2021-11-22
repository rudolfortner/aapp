package at.jku.cg.sar.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.jku.cg.sar.core.grid.GridIO;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.gui.ConfigurationWindow;
import at.jku.cg.sar.metric.AOPCMetric;
import at.jku.cg.sar.metric.AUPCMetric;
import at.jku.cg.sar.metric.DiffusionMetric;
import at.jku.cg.sar.metric.Metric;
import at.jku.cg.sar.metric.SandboxMetric;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.apf.AttractionApproach;
import at.jku.cg.sar.pathfinder.apf.AttractionApproachGradient;
import at.jku.cg.sar.pathfinder.apf.AttractionApproachHeading;
import at.jku.cg.sar.pathfinder.apf.AttractionApproachSandbox;
import at.jku.cg.sar.pathfinder.apf.SimplePotentialField;
import at.jku.cg.sar.pathfinder.astar.AStarAttracted;
import at.jku.cg.sar.pathfinder.astar.AStarGlobalAdvanced;
import at.jku.cg.sar.pathfinder.astar.AStarGlobalAdvancedProb;
import at.jku.cg.sar.pathfinder.astar.AStarGlobalProb;
import at.jku.cg.sar.pathfinder.astar.AStarGlobalSimple;
import at.jku.cg.sar.pathfinder.astar.AStarGlobalTest;
import at.jku.cg.sar.pathfinder.astar.AStarLocalSimple;
import at.jku.cg.sar.pathfinder.astar.AStarPython;
import at.jku.cg.sar.pathfinder.genetic.GeneticPathFinder;
import at.jku.cg.sar.pathfinder.misc.GradientFinder;
import at.jku.cg.sar.pathfinder.misc.GridFinder;
import at.jku.cg.sar.pathfinder.misc.MaxCatcher;
import at.jku.cg.sar.pathfinder.misc.RadialChecker;
import at.jku.cg.sar.pathfinder.misc.RandomFinder;
import at.jku.cg.sar.pathfinder.misc.SimulatedAnnealing;
import at.jku.cg.sar.pathfinder.misc.SpiralFinder;
import at.jku.cg.sar.pathfinder.vacuum.VacuumCleaner;
import at.jku.cg.sar.pathfinder.vacuum.VacuumCleanerGradient;
import at.jku.cg.sar.pathfinder.vacuum.VacuumCleanerGradientAdvanced;
import at.jku.cg.sar.pathfinder.vacuum.VacuumCleanerRadialChecker;
import at.jku.cg.sar.pathfinder.vacuum.VacuumCleanerRadialRange;
import at.jku.cg.sar.pathfinder.vacuum.VacuumCleanerRaycast;
import at.jku.cg.sar.pathfinder.vacuum.VacuumCleanerSectioning;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.trajectory.SimpleTrajectory;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;
import at.jku.cg.sar.trajectory.TrajectorySandbox;
import at.jku.cg.sar.world.World;
import at.jku.cg.sar.world.WorldGenerator;

public class Main {
	
	public static final boolean PRESENT = true;
	public static final boolean USE_GUI = true;

	public static void main(String[] args) {
		
		int WIDTH = 16;
		double min = 0.05;
		
		WorldGenerator generator = new WorldGenerator(0);

		// LIST OF PREDEFINED WORLDS
		List<World> worlds = new ArrayList<>();
		World worldSpots			= new World("Spots", generator.Spots(WIDTH, WIDTH, 0.2, min, 1.0));
		World worldSmoothedSpots	= new World("Smoothed Spots", generator.SmoothedSpots(WIDTH, WIDTH, 0.2, min, 1.0));
		World worldSmoothedSpotsNorm= new World("Smoothed Spots Normalized", generator.SmoothedSpotsNormalized(WIDTH, WIDTH, 0.2, min, 1.0));

		World worldGaussian			= new World("Gaussian", generator.Gaussian(WIDTH, WIDTH, WIDTH/5, min, 1.0));
		World worldPatches			= new World("Patches", generator.Patches(WIDTH, WIDTH, WIDTH/6, 3, min, 1.0));
		World worldNoise			= new World("Noise", generator.Noise(WIDTH, WIDTH));
		World worldUniform			= new World("Uniform", generator.Uniform(WIDTH, WIDTH, 0.05));
		
		World worldManual			= new World("Manual World", GridIO.fromImageFile(new File("in/map_smooth.png"), true, min));
		World worldManualRandomized	= new World("Manual World Randomized", GridIO.fromImageFile(new File("in/map_randomized.png"), true, min));

		World worldSimulated		= new World("Simulated World", GridIO.fromImageFile(new File("in/map_simulated.png"), true, min));

		
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
		
		// SELECTED WORLD
		World world = worldSmoothedSpots;
			
		// LIST OF PATH FINDING ALGORTIHMS
		List<PathFinder> finders = new ArrayList<>();
		
		// Simple Variants
		finders.add(new RandomFinder());
		finders.add(new MaxCatcher());
		finders.add(new SpiralFinder());
		finders.add(new SpiralFinder(true));
		finders.add(new RadialChecker(true));
		finders.add(new RadialChecker(false));
		finders.add(new GradientFinder());
		
		// Grid Finder Variants
		finders.add(new GridFinder(false, false));
		finders.add(new GridFinder(true, false));
		finders.add(new GridFinder(false, true));
		finders.add(new GridFinder(true, true));
		
		// Artificial Potential Field Variants
		finders.add(new SimplePotentialField());
		finders.add(new AttractionApproach());
		finders.add(new AttractionApproachGradient());
		finders.add(new AttractionApproachHeading());
		finders.add(new AttractionApproachSandbox());
		
		// A* Variants
		finders.add(new AStarAttracted());
		finders.add(new AStarPython());
		finders.add(new AStarLocalSimple());
		finders.add(new AStarGlobalSimple(10));
		finders.add(new AStarGlobalAdvanced(10));
		finders.add(new AStarGlobalAdvancedProb(10));
		finders.add(new AStarGlobalTest(10));
		finders.add(new AStarGlobalProb(10));
		
		// Vacuum Cleaners
		finders.add(new VacuumCleaner());
		finders.add(new VacuumCleanerGradient());
		finders.add(new VacuumCleanerGradientAdvanced());
		finders.add(new VacuumCleanerRadialRange(5.0, 10.0));
		finders.add(new VacuumCleanerRaycast());
		finders.add(new VacuumCleanerRadialChecker());
		finders.add(new VacuumCleanerSectioning());
		
		// Other
		finders.add(new SimulatedAnnealing());
		finders.add(new GeneticPathFinder());
		
		Map<PathFinder, String> findersNamed = new HashMap<>();
		findersNamed.put(new GridFinder(true, false), "Grid");
		findersNamed.put(new SpiralFinder(), "Spiral");
		findersNamed.put(new AttractionApproachGradient(), "Gradient");
		findersNamed.put(new RadialChecker(true), "Radial");
		findersNamed.put(new VacuumCleanerGradient(), "Continuous Gradient");
		

		// LIST OF TRAJECTORY PLANNING ALGORTIHMS
		List<TrajectoryPlanner> planners = new ArrayList<>();
		planners.add(null);
		planners.add(new SimpleTrajectory(null, false));
		planners.add(new SimpleTrajectory(null, true));
		planners.add(new TrajectorySandbox(null));
		
		Map<TrajectoryPlanner, String> plannersNamed = new HashMap<>();
		plannersNamed.put(new SimpleTrajectory(null, true), "Trajectory Planner");
		
		
		// LIST OF METRICS
		List<Metric> metrics = new ArrayList<>();
		metrics.add(new AUPCMetric());
		if(!PRESENT) {
			metrics.add(new AOPCMetric());
			metrics.add(new DiffusionMetric());
			metrics.add(new SandboxMetric());
		}

		// Start at Point with highest propability
		GridValue<Double> max = world.getProbabilities().max();
		System.err.println("MAX %f is at %d/%d".formatted(max.getValue(), max.getX(), max.getY()));
		int startX = max.getX();
		int startY = max.getY();

		
		if(USE_GUI) {
			if(PRESENT) {
				new ConfigurationWindow(findersNamed, plannersNamed, metrics, worlds, PRESENT); 
			}else {
				new ConfigurationWindow(finders, planners, metrics, worlds, PRESENT); 	
			}			
		}else {
			Comparator comp = new Comparator(true);
			SimulatorSettings settings = new SimulatorSettings();
			comp.evaluate(new Configuration(finders, planners, settings, new AUPCMetric(), startX, startY, world));
		}
	}

}
