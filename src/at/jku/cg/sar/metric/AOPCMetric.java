package at.jku.cg.sar.metric;

import java.util.List;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.scoring.ScoreType;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.sim.flightpath.FlightLeg;
import at.jku.cg.sar.util.Curve;

public class AOPCMetric implements Metric {
	
	
	public AOPCMetric() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void evaluate(List<SimulationResult> results) {
		
		for(SimulationResult simResult : results) {
			MetricResult result = new MetricResult(this);
			
			result.curve = evaluateFlightPath(simResult);
			double time = result.curve.maxX();
			double prob = result.curve.maxY();
			result.score = time*prob - result.curve.integrate(0.0, time, false);
						
			simResult.setMetricResult(result);
		}
	}
	
	private Curve evaluateFlightPath(SimulationResult result) {
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
				accumulatedProbs += prob;
			}
			
			curve.addPoint(accumulatedTime, accumulatedProbs);
		}
		
		return curve;
	}

	@Override
	public String getName() {
		return "AOPC Metric";
	}
	
	@Override
	public ScoreType getScoreType() {
		return ScoreType.LOWER__BETTER;
	}

}
