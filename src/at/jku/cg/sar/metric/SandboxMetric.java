package at.jku.cg.sar.metric;

import java.awt.Color;
import java.util.List;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.gui.LineGraphViewer;
import at.jku.cg.sar.gui.graphics.LineGraphDataSet;
import at.jku.cg.sar.scoring.ScoreType;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.sim.flightpath.FlightLeg;
import at.jku.cg.sar.util.Curve;
import at.jku.cg.sar.util.Interpolation;

public class SandboxMetric implements Metric {
	
	
	public SandboxMetric() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void evaluate(List<SimulationResult> results) {
		
		// TODO Maybe filter out time=0.0
		double shortest = results.stream()
				.filter(res -> res.getFinalPath().getLegCount() > 0)
				.mapToDouble(res -> res.getFinalPath().getDuration()).min().orElse(0.0);
		
		print("Name", "k", "weightedGradient");
		
		for(SimulationResult simResult : results) {
			MetricResult result = new MetricResult(this);
			
			
//			result.score = integrate(result.curve, 0.0, shortest);
			
			// DIFFUSION TESTING
			result.curve = evaluateFlightPathDiffusion(simResult);
			result.score = result.curve.maxY();
			simResult.setMetricResult(result);
			
			
			double k = (result.curve.maxY() - result.curve.minY()) / (result.curve.maxX() - result.curve.minX());
			double weightedGradient = weightedGradient(result.curve);
			
			print(simResult.getFinder().getName(), k, weightedGradient);

		}
	}
	
	private void print(Object...objs) {
		StringBuilder builder = new StringBuilder();
		for(Object obj : objs) {
			builder.append("%-30s".formatted(obj.toString()));
			builder.append("\t");
		}		
		System.err.println(builder.toString());
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
	
	private Curve evaluateFlightPathDiffusion(SimulationResult result) {
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
				double factor = 1.0;
//				factor = Math.pow(0.99999, accumulatedTime);	// EXP
				double maxTime = 816;
				factor = Interpolation.Linear(accumulatedTime, 0.0, 1.0, maxTime, 0.0);
				if(factor < 0) factor = 0.0;
				if(factor > 1) factor = 1.0;
				accumulatedProbs += prob * factor;
			}
			
			curve.addPoint(accumulatedTime, accumulatedProbs);
		}
		
		return curve;
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
				accumulatedProbs += prob * Math.pow(0.99, accumulatedTime);
			}
			
			curve.addPoint(accumulatedTime, accumulatedProbs);
		}
		
		
		LineGraphDataSet setAccumProb = new LineGraphDataSet("accum prob", Color.RED, curve);
		LineGraphDataSet setGradient = new LineGraphDataSet("gradient", Color.BLUE);
		LineGraphDataSet setMetric = new LineGraphDataSet("Metric", Color.GREEN);

		double metric = 0.0;
		for(int i = 0; i < 1000; i++) {
			double t = Interpolation.Linear(i, 0, curve.minX(), 1000-1, curve.maxX());
			double prob = curve.evaluate(t, false);
			double grad = curve.evaluateGradient(t, false);
			
			metric += prob * Math.pow(0.99, t);
			
			setGradient.addPoint(t, 100.0 * grad);
			setMetric.addPoint(t, metric / 100.0);
		}
		
		new LineGraphViewer(result.getFinder().getName(), List.of(setAccumProb, setGradient, setMetric));
		
		return curve;
	}
	
	
	public double integrate(Curve curve, double a, double b) {
		double integral = 0.0;
				
		int steps = 10000;
		double dx = Math.abs(b-a) / steps;
		
		double lastX = a;
		double lastY = curve.evaluate(a, false);
		
		for(int i = 0; i < steps; i++) {
			double x0 = lastX;
			double y0 = lastY;
			
			double x1 = x0 + dx;
			double y1 = curve.evaluate(x1, false);
			
			lastX = x1;
			lastY = y1;
			
			double t = Interpolation.Linear(i, 0, 0.0, steps-1, 1.0);
			t = Math.exp(-t);
//			t = Math.exp(-x0 / 3600.0);
//			t = Math.exp(-x0 / 60.0);
//			t = Math.exp(-Interpolation.Linear(i, 0, 0.0, steps-1, 1.0));
			
			double area = (y0 + y1) * dx / 2.0;
			integral += area * t;
		}
		return integral;
	}

	
	public double weightedGradient(Curve curve) {
		double a = curve.minX();
		double b = curve.maxX();
		
		double integral = 0.0;
				
		int steps = 100000;
		double dx = Math.abs(b-a) / steps;
		
		double lastX = a;
		
		for(int i = 0; i < steps; i++) {
			double x0 = lastX;
			double x1 = x0 + dx;
			double x = (x0 + x1);
			
			lastX = x1;
			
			double t = Interpolation.Linear(i, 0, 1.0, steps-1, 0.0);
//			t=1.0;
			
			double gradient = curve.evaluateGradient(x, false);
			
			integral += gradient*t;
		}
		return integral / steps;
	}


	@Override
	public String getName() {
		return "Sandbox";
	}

	@Override
	public ScoreType getScoreType() {
		return ScoreType.HIGHER_BETTER;
	}

}
