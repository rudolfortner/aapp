package at.jku.cg.sar.sim.flightpath;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.jku.cg.sar.gui.graphics.LineGraphDataSet;
import at.jku.cg.sar.world.WorldPoint;

public class FlightPath<Leg extends FlightLeg<?>>{

	private final List<Leg> legs;
	
	public FlightPath() {
		legs = new ArrayList<>();
	}
	
	public FlightPath(FlightPath<Leg> path) {
		this();
		legs.addAll(path.getLegs());
	}
	
	public List<LineGraphDataSet> createGraph() {		
		LineGraphDataSet dataSpeed = new LineGraphDataSet("Speed", Color.BLUE);
		LineGraphDataSet dataAcceleration = new LineGraphDataSet("Acceleration", Color.RED);
		LineGraphDataSet dataDistance = new LineGraphDataSet("Distance", Color.GREEN);		
		
		double time = 0.0;
		double distance = 0.0;		
		
		for(Leg leg : legs) {
			// START OF THE LEG
			dataSpeed.addPoint(time, leg.getFromSpeed());
			dataAcceleration.addPoint(time, leg.getAcceleration());
			dataDistance.addPoint(time, distance);
			
			// ADVANCE VARIABLES
			time += leg.getDuration();
			distance += leg.getDistance();
			
			// END OF THE LEG
			dataSpeed.addPoint(time, leg.getToSpeed());
			dataAcceleration.addPoint(time, leg.getAcceleration());
			dataDistance.addPoint(time, distance);			
		}

		List<LineGraphDataSet> data = new ArrayList<>();
		data.add(dataSpeed);
		data.add(dataAcceleration);
		data.add(dataDistance);
		
		return data;
	}
	
	public double getTravelledDistance(double t) {
		double distance = 0.0;
		double currentTime = 0.0;
		
		for(Leg leg : legs) {
			double duration = leg.getDuration();
			
			if(currentTime + duration < t) {
				currentTime += duration;
				distance += leg.getDistance();
			}else {
				double delta = t - currentTime;
				distance += leg.getTravelledDistance(delta);
				break;
			}
		}
		
		return distance;
	}
	
	public WorldPoint getLocationAfterDistance(double distance) {
		double currentDistance = 0.0;
		
		for(Leg leg : legs) {
			double legDistance = leg.getDistance();
			
			if(currentDistance + legDistance < distance) {
				currentDistance += legDistance;
			}else {
				double delta = distance - currentDistance;
				return leg.getLocationAfterDistance(delta);
			}
		}
		if(Math.abs(currentDistance - getDistance()) <= 10.0E-6) {
			double x = legs.get(getLegCount()-1).getToX().doubleValue();
			double y = legs.get(getLegCount()-1).getToY().doubleValue();
			return new WorldPoint(x, y);
		}
		throw new IllegalStateException("Distance %s of %s   (Total %s)".formatted(""+currentDistance, ""+distance, ""+getDistance()));
	}
	
	public WorldPoint getLocationAfterTime(double t) {
		double currentTime = 0.0;
		
		for(Leg leg : legs) {
			double legTime = leg.getDuration();
			
			if(currentTime + legTime < t) {
				currentTime += legTime;
			}else {
				double delta = t - currentTime;
				return leg.getLocationAfterTime(delta);
			}
		}
		throw new IllegalStateException();
	}
	
	public FlightPath<Leg> createSubPath(double time) {
		FlightPath<Leg> path = new FlightPath<>();
		
		double currentTime = 0.0;		
		for(Leg leg : legs) {
			double duration = leg.getDuration();
			
			if(currentTime + duration < time) {
				currentTime += duration;
				path.appendLeg(leg);
			}else {
				double delta = time - currentTime;
				path.appendLeg((Leg) leg.createSubLeg(delta));
				return path;
			}
		}
		throw new IllegalStateException();
	}
	
	
	public void appendLeg(Leg leg) {
		if(leg == null) throw new NullPointerException();
		this.legs.add(leg);
	}
	
	public void appendLegs(List<Leg> legs) {
		if(legs == null) throw new NullPointerException();
		for(Leg leg : legs) {
			if(leg == null) throw new NullPointerException();
			this.legs.add(leg);
		}
	}
	
	public Leg getStart() {
		if(legs.size() == 0) return null;
		return legs.get(0);
	}
	
	public Leg getEnd() {
		if(legs.size() == 0) return null;
		return legs.get(legs.size()-1);
	}
	
	public List<Leg> getLegs() {
		return Collections.unmodifiableList(legs);
	}
	
	public int getLegCount() {
		return legs.size();
	}

	public double getAverageSpeed() {
		return legs.stream().mapToDouble(leg -> leg.getAverageSpeed()).average().orElse(0.0);
	}
	
	public double getDistance() {
		return legs.stream().mapToDouble(leg -> leg.getDistance()).sum();
	}
	
	public double getDistanceNormed() {
		return legs.stream().mapToDouble(leg -> leg.getGridDistance()).sum();
	}
	
	public double getDuration() {
		return legs.stream().mapToDouble(leg -> leg.getDuration()).sum();
	}
	
	
	
}
