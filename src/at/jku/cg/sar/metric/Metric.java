package at.jku.cg.sar.metric;

import java.util.List;

import at.jku.cg.sar.scoring.ScoreType;
import at.jku.cg.sar.sim.SimulationResult;

public interface Metric {
	public void evaluate(List<SimulationResult> results);
	public String getName();
	public ScoreType getScoreType();
}
