package at.jku.cg.sar.metric;

import java.util.List;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.scoring.ScoreType;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.sim.flightpath.FlightLeg;
import at.jku.cg.sar.util.Curve;

public class DiffusionMetric implements Metric {

	public DiffusionMetric() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void evaluate(List<SimulationResult> results) {

		for(SimulationResult simResult : results) {
			MetricResult result = new MetricResult(this);
			
			result.curve = evaluateFlightPathWeighted(simResult);
			result.score = result.curve.maxY();
			
			simResult.setMetricResult(result);

		}
	}
	
	private Curve evaluateFlightPathWeighted(SimulationResult result) {
		// TODO Multi-Layer support for adaptive Worlds
		Grid<Double> map = result.getConfiguration().getWorld().getProbabilities();
		
		Curve curve = new Curve();
		curve.addPoint(0.0, 0.0);

		double accumulatedProbs = 0.0;
		double accumulatedTime = 0.0;
		
		for(FlightLeg<?> leg : result.getFinalPath().getLegs()) {
			accumulatedTime += leg.getDuration();			
			if(leg.isScan()) {
				double prob = map.get(leg.getScanX(), leg.getScanY());
				map.set(leg.getScanX(), leg.getScanY(), 0.0);
				accumulatedProbs += prob * Math.pow(0.999, accumulatedTime);
			}
			
			curve.addPoint(accumulatedTime, accumulatedProbs);
		}
		
		return curve;
	}

	@Override
	public String getName() {
		return "Diffusion Metric";
	}

	@Override
	public ScoreType getScoreType() {
		return ScoreType.HIGHER_BETTER;
	}

}
