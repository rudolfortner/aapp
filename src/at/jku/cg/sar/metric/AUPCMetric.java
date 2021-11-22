package at.jku.cg.sar.metric;

import java.util.List;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.scoring.ScoreType;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.sim.flightpath.FlightLeg;
import at.jku.cg.sar.util.Curve;

public class AUPCMetric implements Metric {
	
	
	public AUPCMetric() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void evaluate(List<SimulationResult> results) {
		
		// TODO Maybe filter out time=0.0
		double shortest = results.stream()
				.filter(res -> res.getFinalPath().getLegCount() > 0)
				.mapToDouble(res -> res.getFinalPath().getDuration()).min().orElse(0.0);
		
		for(SimulationResult simResult : results) {
			MetricResult result = new MetricResult(this);
			
			result.curve = evaluateFlightPath(simResult);
			result.score = result.curve.integrate(0.0, shortest, false);
						
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
			if(leg.isVacuum()) {
				accumulatedProbs += leg.getVacuumProb();
			}
			curve.addPoint(accumulatedTime, accumulatedProbs);
		}
		
		return curve;
	}

	@Override
	public String getName() {
		return "AUPC Metric (original)";
	}

	@Override
	public ScoreType getScoreType() {
		return ScoreType.HIGHER_BETTER;
	}

}
