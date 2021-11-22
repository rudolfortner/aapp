package at.jku.cg.sar.pathfinder.vacuum;

import java.util.ArrayList;
import java.util.List;

import at.jku.cg.sar.core.splitcontainer.SplitContainer;
import at.jku.cg.sar.gui.GridViewer;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneContinous;
import at.jku.cg.sar.util.CourseUtil;
import at.jku.cg.sar.util.Vector;
import at.jku.cg.sar.world.WorldPoint;

public class VacuumCleanerSectioning extends PathFinder {

	public VacuumCleanerSectioning() {
		super(PathFinderType.CONTINOUS_ITERATIVE);
	}
	
	@Override
	public PathFinderResult nextContinous(DroneContinous drone) {
		
		double remainingProbs = drone.getProbabilities().getValue();
//		System.err.println("Remaining Probs: %3.3f".formatted(remainingProbs));
		if(drone.getStepCount() > 10000) new GridViewer(drone.getProbabilities());
		if(drone.getStepCount() > 10000) return null;
		if(remainingProbs <= 0.0) return null;
		
		return findNextHeading(drone, settings, 5.0, 10.0, 1000000.0);
	}
	
	public static PathFinderResult findNextHeading(DroneContinous drone, SimulatorSettings settings, double alpha, double theta, double radius) {		
		if(360.0 % alpha != 0.0) throw new IllegalArgumentException();
		
		double maxHeadingChange = CourseUtil.maxHeadingChange(settings.getAcceleration(), settings.getDeceleration(), settings.getSpeedScan(), 1.0);
		
		double heading	= 0.0;
		double maxValue	= 0.0;
		
		for(double h = 0.0; h < 360.0; h += alpha) {
			double headingDiff = CourseUtil.getDifferenceAbs(h, drone.getTrack());
			double t = 1.0 + headingDiff / maxHeadingChange;
			double value = evalRadial(h, theta, radius, drone) / t;
			
			if(value > maxValue) {
				maxValue = value;
				heading = h;
			}
		}
		
//		System.err.println("Select heading %f with value %f".formatted(heading, maxValue));
		return new PathFinderResult(heading);
	}

	public static double evalRadial(double radial, double theta, double radius, DroneContinous drone) {

		double leftHeading = CourseUtil.makeCourseCorrect(radial - theta / 2.0);
		double rightHeading = CourseUtil.makeCourseCorrect(radial + theta / 2.0);
		
		List<SplitContainer> partially = new ArrayList<>();
		for(SplitContainer c : drone.getProbabilities().getContainers()) {
			if(cellInArc(drone.getX(), drone.getY(), leftHeading, rightHeading, radius, c, true)) {
				partially.add(c);
			}
		}
		
		List<SplitContainer> fully = new ArrayList<>();
		for(SplitContainer c : partially) {
			if(cellInArc(drone.getX(), drone.getY(), leftHeading, rightHeading, radius, c, false)) {
				fully.add(c);
			}
		}		
		partially.removeAll(fully);
		
		double probs = 0.0;
		for(SplitContainer c : fully) probs += c.getValue();
		
		// TODO Add probs from partial fields
		for(SplitContainer c : partially) probs += c.getValue();
		
		
//		System.err.println("Radial %f has value %f".formatted(radial, probs));	
		return probs;
	}
	
	public static boolean cellInArc(double cx, double cy,
			double leftHeading, double rightHeading, double radius,
			SplitContainer c, boolean checkPartially) {
		boolean TL = pointInArc(cx, cy, leftHeading, rightHeading, radius, c.getCornerTopLeft());
		boolean TR = pointInArc(cx, cy, leftHeading, rightHeading, radius, c.getCornerTopRight());
		boolean BL = pointInArc(cx, cy, leftHeading, rightHeading, radius, c.getCornerBottomLeft());
		boolean BR = pointInArc(cx, cy, leftHeading, rightHeading, radius, c.getCornerBottomRight());
		return checkPartially ? (TL || TR || BL || BR) : (TL && TR && BL && BR);
	}
	
	
	public static boolean pointInArc(double cx, double cy,
			double leftHeading, double rightHeading, double radius,
			WorldPoint p) {
		return pointInArc(cx, cy, leftHeading, rightHeading, radius, p.getX(), p.getY());
	}
	
	public static boolean pointInArc(double cx, double cy,
			double leftHeading, double rightHeading, double radius,
			double x, double y) {
		
		double R = Math.sqrt(Math.pow(x - cx, 2.0) + Math.pow(y - cy, 2.0));
		if(R > radius) return false;
		
		double A = (new Vector(cx, cy, x, y)).getHeading();
		
		return CourseUtil.isBetween(A, leftHeading, rightHeading);
	}
	
	public static WorldPoint intersect(double x1, double y1, double x2, double y2,
			double x3, double y3, double x4, double y4) {
		
		double numeratorX = (x1*y2 - y1*x2)*(x3 - x4) - (x1 - x2)*(x3*y4 - y3*x4);
		double numeratorY = (x1*y2 - y1*x2)*(y3 - y4) - (y1 - y2)*(x3*y4 - y3*x4);
		double denominator = (x1 - x2)*(y3 - y4) - (y1 - y2)*(x3 - x4);
		
		if(denominator == 0.0) return null;
		
		return new WorldPoint(numeratorX / denominator, numeratorY / denominator);
	}
	
	
	@Override
	public String getName() {
		return "Vacuum Cleaner Sectioning";
	}

}
