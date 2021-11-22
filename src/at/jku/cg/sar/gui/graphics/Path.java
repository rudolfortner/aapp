package at.jku.cg.sar.gui.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

public class Path {

	private Color color;
	private final List<PathPoint> points;
	
	public Path() {
		this(Color.BLACK);
	}
	
	public Path(Color color) {
		this.color = color;
		this.points = new ArrayList<>();
	}
	
	public void addPoint(int x, int y) {
		points.add(new PathPoint(x, y));
	}
		
	public void draw(Graphics g) {
		g.setColor(color);
		for(int p = 0; p < points.size()-1; p++) {
			PathPoint p0 = points.get(p);
			PathPoint p1 = points.get(p+1);
			
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}	
	}
	
	public void fillPolygon(Graphics g) {
		Polygon polygon = new Polygon();
		
		for(PathPoint point : points) {
			polygon.addPoint(point.x, point.y);
		}
		
		g.setColor(color);
		g.fillPolygon(polygon);
	}
	
	
	public void drawDots(Graphics g, int radius) {
		g.setColor(color);
		for(PathPoint p : points) {
			g.fillOval(p.x-radius, p.y-radius, 2*radius, 2*radius);
		}
	}
	
	public void drawDots(Graphics g, int radius, int minX, int minY, int maxX, int maxY) {
		g.setColor(color);
		for(PathPoint p : points) {
			if(p.x < minX) continue;
			if(p.x > maxX) continue;
			if(p.y < minY) continue;
			if(p.y > maxY) continue;
			g.fillOval(p.x-radius, p.y-radius, 2*radius, 2*radius);
		}
	}
	
	
	private class PathPoint {
		int x, y;
		public PathPoint(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

}
