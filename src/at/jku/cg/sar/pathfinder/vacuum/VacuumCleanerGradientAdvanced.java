package at.jku.cg.sar.pathfinder.vacuum;

import at.jku.cg.sar.core.splitcontainer.SplitContainer;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneContinous;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.util.CourseUtil;
import at.jku.cg.sar.util.Vector;

public class VacuumCleanerGradientAdvanced extends PathFinder {

	public VacuumCleanerGradientAdvanced() {
		super(PathFinderType.CONTINOUS_ITERATIVE);
	}
	
	@Override
	public PathFinderResult nextContinous(DroneContinous drone) {
		
		double remainingProbs = drone.getProbabilities().getValue();
//		System.err.println("Remaining Probs: %3.3f".formatted(remainingProbs));
		if(remainingProbs <= 0.0) return null;
		
		return findMaxGradient(drone, settings);
	}
	
	public static PathFinderResult findMaxGradient(DroneContinous drone, SimulatorSettings settings) {
		SplitContainer max = null;
		double maxGradient = 0;
		
		for(SplitContainer c : drone.getProbabilities().getContainers()) {
			
			double duration = estimateDuration(drone.getX(), drone.getY(), drone.getTrack(),
					c.getCenterX(), c.getCenterY(), settings.getSpeedScan(), settings);
			double prob = c.getValue();
			double gradient = prob / duration;
			
			if(gradient > maxGradient) {
				max = c;
				maxGradient = gradient;
			}			
		}
		
		double heading = (new WorldFlightLeg(drone.getX(), drone.getY(), max.getCenterX(), max.getCenterY(), 0.0)).getHeading();
				
		return new PathFinderResult(heading);
	}
	
	public static double estimateDuration(double startX, double startY, double startHeading,
			double endX, double endY, double speed,
			SimulatorSettings settings) {

		double dt = 1.0;
		double x = startX, y = startY, heading = startHeading;
		double time = 0.0;
		
		while(true) {
			
			Vector direct = new Vector(x, y, endX, endY);
			double distanceToEnd = direct.getLength();
			double headingToEnd = direct.getHeading();
			
			if(distanceToEnd <= 30.0) {
				time += distanceToEnd / speed;
				return time;
			}

			double distance = dt * speed;
			if(distanceToEnd < distance) {
				distance = distanceToEnd;
				dt = distance / speed;
			}
			time += dt;
			
			double maxHeadingChange = CourseUtil.maxHeadingChange(settings.getAcceleration(), settings.getDeceleration(), speed, dt);
			heading = CourseUtil.changeHeading(heading, headingToEnd, maxHeadingChange);
			double direction = Math.toRadians(heading - 90.0);
			x += distance * Math.cos(direction);
			y += distance * Math.sin(direction);
//			
//			System.err.println("%f %f -> %f %f".formatted(x, y, endX, endY));
		}
	}

	@Override
	public String getName() {
		return "Vacuum Cleaner Gradient Advanced";
	}

}
