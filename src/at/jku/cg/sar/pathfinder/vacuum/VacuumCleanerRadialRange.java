package at.jku.cg.sar.pathfinder.vacuum;

import java.util.List;
import java.util.stream.Collectors;

import at.jku.cg.sar.core.splitcontainer.SplitContainer;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneContinous;
import at.jku.cg.sar.util.CourseUtil;
import at.jku.cg.sar.util.Vector;

public class VacuumCleanerRadialRange extends PathFinder {

	private final double alpha;	// Angle between radials
	private final double theta;	// Angle difference between radial and cell-centers
	
	public VacuumCleanerRadialRange() {
		this(5.0, 5.0);
	}
	
	public VacuumCleanerRadialRange(double alpha, double theta) {
		super(PathFinderType.CONTINOUS_ITERATIVE);
		this.alpha = alpha;
		this.theta = theta;
	}
	
	@Override
	public PathFinderResult nextContinous(DroneContinous drone) {
		
		double remainingProbs = drone.getProbabilities().getValue();
//		System.err.println("Remaining Probs: %3.3f".formatted(remainingProbs));
		if(remainingProbs <= 0.0) return null;
	
		double heading = nextRadial(drone, settings, alpha, theta);
		return new PathFinderResult(heading);
	}

	
	public static double nextRadial(DroneContinous drone, SimulatorSettings settings, double alpha, double theta) {
		if(alpha <= 0.0) throw new IllegalArgumentException();
		if(360.0 % alpha != 0.0) throw new IllegalArgumentException();
		if(theta < 0.0) throw new IllegalArgumentException();
		
		double maxGradient	= 0.0;
		double maxRadial	= 0.0;
		
		for(double h = 90.0; h < 360.0; h += alpha) {
			double gradient = evaluateRadial(drone, settings, h, theta);
//			System.err.println("Radial %f has gradient %f".formatted(h, gradient));
			if(gradient > maxGradient) {
				maxGradient = gradient;
				maxRadial = h;
			}
		}
		return maxRadial;
	}
	
	public static double evaluateRadial(DroneContinous drone, SimulatorSettings settings, double radial, double theta) {
		
		List<SplitContainer> onRadial = onRadial(drone, radial, theta);
		if(onRadial.isEmpty()) return 0.0;

		double accumTime = 0.0;
		double accumProb = 0.0;
		double maxGradient = 0.0;
		double x = drone.getX(), y = drone.getY();
		
		for(SplitContainer c : onRadial) {
			double dist = (new Vector(x, y, c.getCenterX(), c.getCenterY())).getLength();
			double time = dist / settings.getSpeedScan();
			accumTime += time;
			accumProb += c.getValue();
			
			double gradient = accumProb / accumTime;
			maxGradient = Math.max(maxGradient, gradient);
			if(Double.isInfinite(gradient)) {
				System.err.println("Drone %f/%f".formatted(drone.getX(), drone.getY()));
				System.err.println("Cell %f/%f".formatted(c.getCenterX(), c.getCenterY()));
				System.err.println("dist " + dist);
				System.err.println("Time " + time);
				System.err.println("accumTime " + accumTime);
				System.err.println("accumProb " + accumProb);
				throw new IllegalStateException();
			}

			x = c.getCenterX();
			y = c.getCenterY();
		}
		
//		return onRadial.stream().mapToDouble(c -> c.getValue()).sum();
		return maxGradient;
	}
	
	
	public static List<SplitContainer> onRadial(DroneContinous drone, double radial, double theta) {
		return drone.getProbabilities().getContainers().stream()
				.filter(c -> {
					// Filter out all zero values
					return c.getValue() > 0.0;
				})
				.filter(c -> {
					// Filter by heading
					double heading = (new Vector(drone.getX(), drone.getY(), c.getCenterX(), c.getCenterY())).getHeading();
					double diff = CourseUtil.getDifferenceAbs(radial, heading);
					return diff <= theta;
				})
				.sorted((c0, c1) -> {
					// Sort by distance from drone
					double dist0 = (new Vector(drone.getX(), drone.getY(), c0.getCenterX(), c0.getCenterY())).getLength();
					double dist1 = (new Vector(drone.getX(), drone.getY(), c1.getCenterX(), c1.getCenterY())).getLength();
					return Double.compare(dist0, dist1);
				})
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public String getName() {
		return "Vacuum Cleaner Radial Range (%f / %f)".formatted(alpha, theta);
	}
}
