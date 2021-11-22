package at.jku.cg.sar.gui.tables;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

import at.jku.cg.sar.main.Configuration;
import at.jku.cg.sar.scoring.ScoreType;
import at.jku.cg.sar.sim.SimulationResult;

public class SimulationResultTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -3102705189271527956L;
	
	private final Configuration config;
	private final List<SimulationResult> results;
	
	public SimulationResultTableModel(Configuration config, List<SimulationResult> results) {
		this.config = config;
		this.results = results.stream().sorted(new SimulationResultComparator(config.getMetric().getScoreType())).collect(Collectors.toUnmodifiableList());
	}
	
	public SimulationResult get(int index) {
		return results.get(index);
	}

	@Override
	public int getRowCount() {
		return results.size();
	}

	@Override
	public int getColumnCount() {
		return 8;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		SimulationResult result = results.get(rowIndex);
		
		switch (columnIndex) {
			// GENERAL INFO
			case 0:		return config.getFinders().get(result.getFinder());
			case 1:		return result.getFinder().getType().toString();
			case 2:		return config.getPlanners().get(result.getPlanner());
			
			// FINAL TRAJECTORY INFO
			case 3:		return result.getFinalPath() == null ? 0 : result.getFinalPath().getDistance();
			case 4:		return result.getFinalPath() == null ? 0 : result.getFinalPath().getDuration();
			case 5:		return result.getFinalPath() == null ? 0 : result.getFinalPath().getLegCount();
			
			// SIMULATION
			case 6:		return result.getSimulationTime();
			
			// METRIC RESULTS
			case 7:		return result.getMetricResult() == null ? 0 : result.getMetricResult().getScore();
			
			default:	return "N/A";
		}
	}
	
	

	@Override
	public String getColumnName(int column) {
		switch (column) {
			// GENERAL INFO
			case 0:		return "Path Finder";
			case 1:		return "Path Finder Type";
			case 2:		return "Trajectory Planner";
			
			// FINAL TRAJECTORY INFO
			case 3:		return "Trajectory Distance";
			case 4:		return "Trajectory Time";
			case 5:		return "Trajectory Leg Count";
			
			// SIMULATION
			case 6:		return "Simulation Time";
						
			// METRIC RESULTS
			case 7:		return prepareString("Metric Score", config.getMetric().getScoreType().toString());
			
			default:	return "N/A";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 0:
			case 1:
			case 2:
				return String.class;
				
			case 3:
			case 4:
				return Double.class;
				
			case 5:
				return Integer.class;
			
			case 6:
			case 7:
				return Double.class;
			default:
				throw new IllegalArgumentException("Unexpected value: " + columnIndex);
		}
	}
	
	private String prepareString(String...lines) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html>");
		builder.append("<center>");
		
		for(String s : lines) {
			builder.append(s);
			builder.append("<br />");
		}

		builder.append("</center>");
		builder.append("</html>");
		return builder.toString();
	}
	
	
	private final class SimulationResultComparator implements Comparator<SimulationResult> {

		private final ScoreType type;
		
		public SimulationResultComparator(ScoreType type) {
			this.type = type;
		}
		
		@Override
		public int compare(SimulationResult o1, SimulationResult o2) {
			if(o1.getMetricResult() == null || o2.getMetricResult() == null) return 0;
						
			if(type == ScoreType.HIGHER_BETTER) {
				return Double.compare(o2.getMetricResult().getScore(), o1.getMetricResult().getScore());
			}else if(type == ScoreType.LOWER__BETTER) {
				return Double.compare(o1.getMetricResult().getScore(), o2.getMetricResult().getScore());
			}
			throw new IllegalStateException();
		}
	}
	
}
