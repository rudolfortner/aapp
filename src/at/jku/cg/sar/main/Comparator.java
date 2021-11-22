package at.jku.cg.sar.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import at.jku.cg.sar.gui.ComparisonViewer;
import at.jku.cg.sar.gui.ProgressBarWindow;
import at.jku.cg.sar.gui.SimulationResultOverviewWindow;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.sim.SimpleSimulator;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.sim.Simulator;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;

public class Comparator {
	
	private final ExecutorService service = Executors.newFixedThreadPool(1);
	
	private final boolean exitOnClose;
	
	public Comparator(boolean exitOnClose) {
		this.exitOnClose = exitOnClose;
	}
	
	public List<SimulationResult> evaluate(Configuration config) {
		return evaluate(config, false);
	}
	
	public List<SimulationResult> evaluate(Configuration config, boolean showProgress) {
		ProgressBarWindow bar = null;
		if(showProgress) bar = new ProgressBarWindow("Waiting for SimulationResult...");

		
		List<FutureTask<SimulationResult>> futures = new ArrayList<>();

		for(PathFinder finder : config.getFinders().keySet()) {
			for(TrajectoryPlanner planner : config.getPlanners().keySet()) {
				PathFinder pFinder = finder == null ? null : finder.newInstance();
				TrajectoryPlanner tPlaner = planner == null ? null : planner.newInstance();
				
				Simulator sim = new SimpleSimulator(config);
				FutureTask<SimulationResult> future = new FutureTask<>(() -> sim.run(config.getWorld(), pFinder, tPlaner));
				
				service.submit(future);
				futures.add(future);
			}			
		}
		
		List<SimulationResult> results = new ArrayList<>(futures.size());
		int done = 0;
		for(FutureTask<SimulationResult> future : futures) {
			SimulationResult result = null;
			done++;
			
				try {
					if(config.getSettings().isUseSimulationTimeout()) {
						result = future.get(config.getSettings().getSimulationTimeout(), TimeUnit.MILLISECONDS);						
					}else {
						result = future.get();
					}
				}catch (InterruptedException | TimeoutException e) {
					e.printStackTrace();
					future.cancel(true);
					System.err.println("An algorithm was not able to finish properly");
				}catch(Exception e) {
					e.printStackTrace();
				}
			
			if(result != null) results.add(result);
			
			int progress = done * 100 / futures.size();
			if(showProgress) bar.setProgress(progress);
		}
		
		config.getMetric().evaluate(results);		

		if(showProgress) bar.close();
		return results;
	}
	
	public List<SimulationResult> evaluateShow(Configuration config) {
		List<SimulationResult> results = evaluate(config, true);
		new ComparisonViewer(config, results, exitOnClose);
		new SimulationResultOverviewWindow(config, results, exitOnClose);
		return results;
	}
	

	/*
	public List<ScoreItem> evaluate(Configuration config, List<World> worlds) {
		
		// Evaluate all worlds
		Map<World, List<SimulationResult>> results = new HashMap<>();		
		for(World world : worlds) {
			results.put(world, evaluate(config, world));
		}
		
		ScoringSystem system = new ScoringSystem();
		
		Set<PathFinder> algorithms = results.values().stream().flatMap(s -> s.stream()).map(s -> s.getFinder()).collect(Collectors.toSet());
		
		Map<PathFinder, ScoreItem> items = new HashMap<>();
		for(PathFinder finder : algorithms) {
			items.put(finder, new ScoreItem(finder.getName(), finder));
		}
		
		for(World world : worlds) {
			ScoreCriteria criteriaMetric = new ScoreCriteria("%s - Metric".formatted(world.getName()), ScoreType.HIGHER_BETTER);
			ScoreCriteria criteriaTime = new ScoreCriteria("%s - Time".formatted(world.getName()), 0.1, ScoreType.LOWER__BETTER);
			
			for(SimulationResult result : results.get(world)) {
				ScoreItem item = items.get(result.getFinder());

				item.addValue(criteriaMetric, result.getMetricResult().getScore());	
				item.addValue(criteriaTime, result.getFinalPath().getDuration());				
			}
		}
		
		system.process(new ArrayList<>(items.values()));
		
		return new ArrayList<>(items.values());
	}

	public List<ScoreItem> evaluateShow(Configuration config, List<World> worlds) {
		List<ScoreItem> items = evaluate(config, worlds);
		new ScoringResultWindow(items, exitOnClose);
		return items;
	}
	*/
}
