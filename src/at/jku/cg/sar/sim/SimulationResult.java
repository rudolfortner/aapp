package at.jku.cg.sar.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.jku.cg.sar.main.Configuration;
import at.jku.cg.sar.metric.MetricResult;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.GridFlightLeg;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;

public class SimulationResult {

	// Simulation Configuration
	private final Configuration config;
	private final PathFinder finder;
	private final TrajectoryPlanner planner;
	
	// Simulation Results
	private double simulationTime;
	List<Double> executionTimesFinder, executionTimesPlanner;
	
	// Result of PathFinder
	//PathResult pathResult;
	FlightPath<GridFlightLeg> finderPath;
	
	// Result of TrajectoryPlanner
	FlightPath<WorldFlightLeg> trajectoryPath;
	
	// Results of Metric
	MetricResult metricResult;
	
	public SimulationResult(Configuration config, PathFinder finder, TrajectoryPlanner planner) {
		this.config = config;
		this.finder = finder;
		this.planner = planner;
		
		executionTimesFinder = new ArrayList<>();
		executionTimesPlanner = new ArrayList<>();
	}

	public Configuration getConfiguration() {
		return config;
	}
	
	public PathFinder getFinder() {
		return finder;
	}
	
	public TrajectoryPlanner getPlanner() {
		return planner;
	}
	
	public double getSimulationTime() {
		return simulationTime;
	}

	void setSimulationTime(double simulationTime) {
		this.simulationTime = simulationTime;
	}

	void setFinderPath(FlightPath<GridFlightLeg> finderPath) {
		if(finderPath == null) throw new IllegalStateException("PathResult already set!");
		this.finderPath = finderPath;
	}
	
	void setTrajectoryPath(FlightPath<WorldFlightLeg> trajectoryPath) {
		if(trajectoryPath == null) throw new IllegalStateException("TrajectoryResult already set!");
		this.trajectoryPath = trajectoryPath;
	}
	
	public void setMetricResult(MetricResult metricResult) {
		if(metricResult == null) throw new IllegalStateException("MetricResult already set!");
		this.metricResult = metricResult;
	}
	
	void pushExecutionTimeFinder(double executionTime) {
		this.executionTimesFinder.add(executionTime);
	}
	
	void pushExecutionTimeFinderNanos(long executionTimeNanos) {
		this.executionTimesFinder.add(executionTimeNanos / 1_000_000.0);
	}
	
	void pushExecutionTimePlanner(double executionTime) {
		this.executionTimesPlanner.add(executionTime);
	}
	
	void pushExecutionTimePlannerNanos(double executionTimeNanos) {
		this.executionTimesPlanner.add(executionTimeNanos / 1_000_000.0);
	}
	
	public FlightPath<GridFlightLeg> getPlannerPath() {
		return this.finderPath;
	}
	
	public FlightPath<WorldFlightLeg> getTrajectoryPath() {
		return this.trajectoryPath;
	}
	
	public FlightPath<?> getFinalPath(){
		return trajectoryPath == null ? finderPath : trajectoryPath;
	}
	
	public MetricResult getMetricResult() {
		return this.metricResult;
	}
	
	public List<Double> getExecutionTimesFinder(){
		return Collections.unmodifiableList(executionTimesFinder);
	}

	public List<Double> getExecutionTimesPlanner(){
		return Collections.unmodifiableList(executionTimesPlanner);
	}

}
