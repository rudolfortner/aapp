package at.jku.cg.sar.metric;

import at.jku.cg.sar.util.Curve;

public class MetricResult {
	
	private final Metric metric;
	
	// General
	protected double score;
	protected Curve curve;

	
	public MetricResult(Metric metric) {
		this.metric = metric;
	}

	
	public Metric getMetric() {
		return metric;
	}
		
	public double getScore() {
		return score;
	}

	public Curve getCurve() {
		return curve;
	}

}
