package at.jku.cg.sar.gui.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import at.jku.cg.sar.gui.DrawUtils;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.GridFlightLeg;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.sim.flightpath.WorldFlightLegBezier;
import at.jku.cg.sar.util.ColorRamp;
import at.jku.cg.sar.util.Vector;
import at.jku.cg.sar.world.WorldPoint;

public class FlightPathRenderer {
	
	public static void render(Graphics2D g, FlightPath<GridFlightLeg> flightPath, int originX, int originY,
			int tileSize, ColorRamp rampSpeed) {
		render(g, flightPath, originX, originY, tileSize, rampSpeed, null, 2.0);
	}
	
	public static void render(Graphics2D g, FlightPath<GridFlightLeg> flightPath, int originX, int originY,
			int tileSize, ColorRamp rampSpeed, double strokeWidth) {
		render(g, flightPath, originX, originY, tileSize, rampSpeed, null, strokeWidth);
	}

	public static void render(Graphics2D g, FlightPath<GridFlightLeg> flightPath, int originX, int originY,
			int tileSize, ColorRamp rampSpeed, Color overrideLineColor, double strokeWidth) {
		if(flightPath == null) return;
		
		double zoom = tileSize / 32.0;
		
		// Path
		Stroke stroke = g.getStroke();
		g.setStroke(new BasicStroke((float) strokeWidth));
		for(GridFlightLeg leg : flightPath.getLegs()) {
			if(leg.getDistance() == 0.0) continue;
			
			int x0 = (int) (originX + (leg.getFromX()+0.5) * tileSize);
			int y0 = (int) (originY + (leg.getFromY()+0.5) * tileSize);
			int x1 = (int) (originX + (leg.getToX()+0.5) * tileSize);
			int y1 = (int) (originY + (leg.getToY()+0.5) * tileSize);
			
			if((new Vector(x0, y0, x1, y1)).getLength() <= 2.0) continue;
			
			g.setColor(rampSpeed.evaluate(leg.getToSpeed()));
			if(overrideLineColor != null) g.setColor(overrideLineColor);
			if(leg.hasAcceleration()) {
				DrawUtils.drawArrow(g, x0, y0, x1, y1, (int) (2 * zoom), rampSpeed.evaluate(leg.getFromSpeed()), rampSpeed.evaluate(leg.getToSpeed()));
			}else {
				DrawUtils.drawArrow(g, x0, y0, x1, y1, (int) (2 * zoom));
			}
			DrawUtils.fillCircle(g, x1, y1, 3);	
		}
		g.setStroke(stroke);
		
		// Draw Start and End
		if(flightPath.getStart() != null) {
			int startX = (int) (originX + (flightPath.getStart().getFromX()+0.5) * tileSize);
			int startY = (int) (originY + (flightPath.getStart().getFromY()+0.5) * tileSize);
			DrawUtils.fillCircleBordered(g, startX, startY, (int) (8 * zoom), Color.GREEN, Color.BLACK);
		}		

		if(flightPath.getEnd() != null) {
			int endX = (int) (originX + (flightPath.getEnd().getToX()+0.5) * tileSize);
			int endY = (int) (originY + (flightPath.getEnd().getToY()+0.5) * tileSize);
			g.setColor(Color.RED);
			DrawUtils.fillCircleBordered(g, endX, endY, (int) (6 * zoom), Color.RED, Color.BLACK);
		}		
	}
	
	public static void renderWorldFlightLeg(Graphics2D g, WorldFlightLeg leg, int originX, int originY,
			double factor, ColorRamp rampSpeed, boolean renderArrow) {
		renderWorldFlightLeg(g, leg, originX, originY, factor, rampSpeed, renderArrow, null);
	}
	
	public static void renderWorldFlightLeg(Graphics2D g, WorldFlightLeg leg, int originX, int originY,
			double factor, ColorRamp rampSpeed, boolean renderArrow, Color overrideLineColor) {

		
		int x0 = (int) (originX + leg.getFromX() * factor);
		int y0 = (int) (originY + leg.getFromY() * factor);
		int x1 = (int) (originX + leg.getToX()   * factor);
		int y1 = (int) (originY + leg.getToY()   * factor);
		
		if((new Vector(x0, y0, x1, y1)).getLength() <= 1.0) return;
		
		Color fromColor	= rampSpeed.evaluate(leg.getFromSpeed());
		Color toColor	= rampSpeed.evaluate(leg.getToSpeed());
		if(overrideLineColor != null) fromColor = toColor = overrideLineColor;
		g.setColor(fromColor);
		
		if(renderArrow) {
			if(leg.hasAcceleration()) {
				DrawUtils.drawArrow(g, x0, y0, x1, y1, (int) (2 * factor), fromColor, toColor);
			}else {
				DrawUtils.drawArrow(g, x0, y0, x1, y1, (int) (2 * factor));
			}
		}else {
			if(leg.hasAcceleration()) {
				DrawUtils.drawLine(g, x0, y0, x1, y1, fromColor, toColor);
			}else {
				DrawUtils.drawLine(g, x0, y0, x1, y1);
			}
		}
		
		DrawUtils.fillCircle(g, x1, y1, 2);		
	}
	
	
	public static void renderWorldFlightLegBezier(Graphics2D g, WorldFlightLegBezier leg, int originX, int originY,
			double factor, ColorRamp rampSpeed) {
		renderWorldFlightLegBezier(g, leg, originX, originY, factor, rampSpeed, null);
	}
	
	public static void renderWorldFlightLegBezier(Graphics2D g, WorldFlightLegBezier leg, int originX, int originY,
			double factor, ColorRamp rampSpeed, Color overrideLineColor) {
		
		int x0, y0, x1 = 0, y1 = 0;
		for(int p = 0; p < leg.getCurve().size()-1; p++) {
			WorldPoint from = leg.getCurve().get(p);
			WorldPoint to = leg.getCurve().get(p+1);
			
			x0 = (int) (originX + from.getX() * factor);
			y0 = (int) (originY + from.getY() * factor);
			x1 = (int) (originX + to.getX()   * factor);
			y1 = (int) (originY + to.getY()   * factor);
			
			Color color = rampSpeed.evaluate(leg.getFromSpeed());
			g.setColor(color);
			if(overrideLineColor != null) g.setColor(overrideLineColor);
			
			if(p == leg.getCurve().size()-2) {
				// DRAW ARROW
				if(leg.hasAcceleration()) {
					DrawUtils.drawArrow(g, x0, y0, x1, y1, (int) (2 * factor), rampSpeed.evaluate(leg.getFromSpeed()), rampSpeed.evaluate(leg.getToSpeed()));
				}else {
					DrawUtils.drawArrow(g, x0, y0, x1, y1, (int) (2 * factor));
				}
			}else {
				// DRAW LINE ONLY
				if(leg.hasAcceleration()) {
					DrawUtils.drawLine(g, x0, y0, x1, y1, rampSpeed.evaluate(leg.getFromSpeed()), rampSpeed.evaluate(leg.getToSpeed()));
				}else {
					DrawUtils.drawLine(g, x0, y0, x1, y1);
				}
			}			
		}

		DrawUtils.fillCircle(g, x1, y1, 2);
	}
	
	public static void render(Graphics2D g, FlightPath<WorldFlightLeg> flightPath, int originX, int originY,
			double factor, ColorRamp rampSpeed) {
		render(g, flightPath, originX, originY, factor, rampSpeed, null, 2.0);
	}
	
	public static void render(Graphics2D g, FlightPath<WorldFlightLeg> flightPath, int originX, int originY,
			double factor, ColorRamp rampSpeed, double strokeWidth) {
		render(g, flightPath, originX, originY, factor, rampSpeed, null, strokeWidth);
	}
	
	public static void render(Graphics2D g, FlightPath<WorldFlightLeg> flightPath, int originX, int originY,
			double factor, ColorRamp rampSpeed, Color overrideLineColor, double strokeWidth) {
		if(flightPath == null) return;
				
		// Path
		Stroke stroke = g.getStroke();
		g.setStroke(new BasicStroke((float) strokeWidth));
		for(WorldFlightLeg leg : flightPath.getLegs()) {
			if(leg instanceof WorldFlightLegBezier) {
				renderWorldFlightLegBezier(g, (WorldFlightLegBezier) leg, originX, originY, factor, rampSpeed, overrideLineColor);
			}else{
				renderWorldFlightLeg(g, leg, originX, originY, factor, rampSpeed, true, overrideLineColor);
			}			
		}
		g.setStroke(stroke);
		
		// Draw Start and End
		if(flightPath.getStart() != null) {
			int startX = (int) (originX + flightPath.getStart().getFromX() * factor);
			int startY = (int) (originY + flightPath.getStart().getFromY() * factor);
			DrawUtils.fillCircleBordered(g, startX, startY, (int) (8 * factor), Color.GREEN, Color.BLACK);
		}		
		
		if(flightPath.getEnd() != null) {
			int endX = (int) (originX + flightPath.getEnd().getToX() * factor);
			int endY = (int) (originY + flightPath.getEnd().getToY() * factor);
			g.setColor(Color.RED);
			DrawUtils.fillCircleBordered(g, endX, endY, (int) (6 * factor), Color.RED, Color.BLACK);
		}		
	}
	
}
