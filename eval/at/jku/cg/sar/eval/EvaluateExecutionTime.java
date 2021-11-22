package at.jku.cg.sar.eval;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import at.jku.cg.core.latex.LatexTableWriter;
import at.jku.cg.core.latex.LatexTableWriter.LatexTableRow;
import at.jku.cg.sar.main.Comparator;
import at.jku.cg.sar.main.Configuration;
import at.jku.cg.sar.metric.AUPCMetric;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;
import at.jku.cg.sar.world.World;

class EvaluateExecutionTime {
	
	
	

	public static void evalExecutionTime(World world, List<PathFinder> finders, TrajectoryPlanner planner,
			int startX, int startY,
			String caption, String label, File file) {
		Map<PathFinder, String> findersMap = new HashMap<>();
		for(PathFinder finder : finders) {
			findersMap.put(finder, finder.getName());
		}
		evalExecutionTime(world, findersMap, planner,
				startX, startY,
				caption, label, file);
	}
	
	public static void evalExecutionTime(World world, Map<PathFinder, String> finders, TrajectoryPlanner planner,
			int startX, int startY,
			String caption, String label, File file) {		
		
		String leftColumn = "Algorithm";
		
		LatexTableWriter writer = new LatexTableWriter();
		writer.setLeftColumn(leftColumn);
		if(caption != null && !caption.isBlank())	writer.setCaption(caption);
		if(label != null && !label.isBlank())		writer.setLabel(label);
		
		for(PathFinder finder : finders.keySet()) {
			Comparator comp = new Comparator(true);
			List<SimulationResult> results = comp.evaluate(new Configuration(List.of(finder), List.of(planner), EvaluationConfig.settings, new AUPCMetric(), startX, startY, world));
			SimulationResult result = results.get(0);
			
			
			List<Double> finderTimes = result.getExecutionTimesFinder();
			double finderMin = finderTimes.stream().mapToDouble(v -> v).min().orElseThrow();
			double finderAvg = finderTimes.stream().mapToDouble(v -> v).average().orElseThrow();
			double finderMax = finderTimes.stream().mapToDouble(v -> v).max().orElseThrow();
			double finderSum = finderTimes.stream().mapToDouble(v -> v).sum();
			
			
			LatexTableRow row = new LatexTableRow();
			row.addEntry(leftColumn, finders.get(finder));
			row.addEntry("min [ms]", String.format(Locale.ROOT, "%4.2f", finderMin));
			row.addEntry("avg [ms]", String.format(Locale.ROOT, "%4.2f", finderAvg));
			row.addEntry("max [ms]", String.format(Locale.ROOT, "%4.2f", finderMax));
			row.addEntry("sum [ms]", String.format(Locale.ROOT, "%4.2f", finderSum));			
			writer.addRow(row);
		}
		
		writer.export(file);

	}
}
