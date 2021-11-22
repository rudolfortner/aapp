package at.jku.cg.sar.main;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import at.jku.cg.sar.metric.Metric;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;
import at.jku.cg.sar.world.World;

public class Configuration {

	private final Map<PathFinder, String> finders;
	private final Map<TrajectoryPlanner, String> planners;
	
	private final SimulatorSettings settings;
	private final Metric metric;
	
	private final int startX, startY;
	private final World world;
	
	public Configuration(List<PathFinder> finders, List<TrajectoryPlanner> planners, SimulatorSettings settings, Metric metric,
			int startX, int startY, World world) {
		this(finders.stream().collect(Collectors.toMap(f -> f, f -> f == null ? "None" : f.getName())),
				planners.stream().collect(Collectors.toMap(p -> p, p -> p == null ? "None" : p.getName())), settings, metric, startX, startY, world);

	}
	
	public Configuration(Map<PathFinder, String> finders, Map<TrajectoryPlanner, String> planners, SimulatorSettings settings, Metric metric,
			int startX, int startY, World world) {
		this.finders = finders;
		this.planners = planners;
			
		this.settings = settings;
		this.metric = metric;
		
		this.startX = startX;
		this.startY = startY;
		this.world = world;
	}

	public Map<PathFinder, String> getFinders() {
		return finders;
	}

	public Map<TrajectoryPlanner, String> getPlanners() {
		return planners;
	}

	public Metric getMetric() {
		return metric;
	}

	public SimulatorSettings getSettings() {
		return settings;
	}

	public int getStartX() {
		return startX;
	}

	public int getStartY() {
		return startY;
	}

	public World getWorld() {
		return world;
	}
	
}
