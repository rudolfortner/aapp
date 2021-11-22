package at.jku.cg.sar.sim;

public class SimulatorSettings {
	
	// WORLD SETTINGS
	private double cellSize;
		
	// PLANNER SETTINGS
	private boolean forceFastFlight;
	
	// TIMEOUTS
	private boolean useSimulationTimeout;
	private boolean usePathFinderTimeout;
	private boolean useTrajectoryPlannerTimeout;
	
	private long simulationTimeout;
	private long pathFinderTimeout;
	private long trajectoryPlannerTimeout;
		
	// DRONE SETTINGS
	// In m/s
	private double speedSlow;
	private double speedScan;
	private double speedFast;
	
	private double scanRadius;
	
	// In m/s²
	private double acceleration;
	private double deceleration;
	
	
	public SimulatorSettings() {
		// DEFAULT WORLD SETTINGS
		cellSize = 30.0;
		
		// DEFAULT PLANNER SETTINGS
		forceFastFlight = false;
		
		// TIMEOUTS
		useSimulationTimeout = false;
		usePathFinderTimeout = false;
		useTrajectoryPlannerTimeout = false;
		
		simulationTimeout = 360_000;
		pathFinderTimeout = 1_500;
		trajectoryPlannerTimeout = 500;
		
		// DEFAULT DRONE SETTINGS
		speedSlow =  0.0;
		speedScan =  5.0;
		speedFast = 10.0;
		
		scanRadius = 30.0 / 2.0;
		
		acceleration = 1.4;
		deceleration = 1.4;
	}
	
	
	public double getCellSize() {
		return cellSize;
	}

	public void setCellSize(double gridWidth) {
		this.cellSize = gridWidth;
	}
	
	public boolean isForceFastFlight() {
		return forceFastFlight;
	}

	public void setForceFastFlight(boolean forceFastFlight) {
		this.forceFastFlight = forceFastFlight;
	}


	public boolean isUseSimulationTimeout() {
		return useSimulationTimeout;
	}


	public void setUseSimulationTimeout(boolean useSimulationTimeout) {
		this.useSimulationTimeout = useSimulationTimeout;
	}


	public boolean isUsePathFinderTimeout() {
		return usePathFinderTimeout;
	}


	public void setUsePathFinderTimeout(boolean usePathFinderTimeout) {
		this.usePathFinderTimeout = usePathFinderTimeout;
	}


	public boolean isUseTrajectoryPlannerTimeout() {
		return useTrajectoryPlannerTimeout;
	}


	public void setUseTrajectoryPlannerTimeout(boolean useTrajectoryPlannerTimeout) {
		this.useTrajectoryPlannerTimeout = useTrajectoryPlannerTimeout;
	}


	public long getSimulationTimeout() {
		return simulationTimeout;
	}

	public void setSimulationTimeout(long timeout) {
		this.simulationTimeout = timeout;
	}

	public long getPathFinderTimeout() {
		return pathFinderTimeout;
	}

	public void setPathFinderTimeout(long pathFinderTimeout) {
		this.pathFinderTimeout = pathFinderTimeout;
	}

	public long getTrajectoryPlannerTimeout() {
		return trajectoryPlannerTimeout;
	}

	public void setTrajectoryPlannerTimeout(long trajectoryPlannerTimeout) {
		this.trajectoryPlannerTimeout = trajectoryPlannerTimeout;
	}

	
	public double getSpeedSlow() {
		return speedSlow;
	}

	public void setSpeedSlow(double speedSlow) {
		this.speedSlow = speedSlow;
	}


	public double getSpeedScan() {
		return speedScan;
	}

	public void setSpeedScan(double speedScan) {
		this.speedScan = speedScan;
	}


	public double getSpeedFast() {
		return speedFast;
	}

	public void setSpeedFast(double speedFast) {
		this.speedFast = speedFast;
	}

	public double getScanRadius() {
		return scanRadius;
	}

	public void setScanRadius(double scanRadius) {
		this.scanRadius = scanRadius;
	}


	public double getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}


	public double getDeceleration() {
		return deceleration;
	}

	public void setDeceleration(double deceleration) {
		this.deceleration = deceleration;
	}

}
