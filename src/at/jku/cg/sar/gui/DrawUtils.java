package at.jku.cg.sar.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Stroke;

import at.jku.cg.sar.util.TextDrawer;
import at.jku.cg.sar.util.TextDrawer.HorizontalAlignment;
import at.jku.cg.sar.util.TextDrawer.VerticalAlignment;

public class DrawUtils {

	
	public static void drawCircle(Graphics g, int x, int y, int radius) {
		g.drawOval(x-radius, y-radius, 2*radius, 2*radius);
	}
	
	public static void fillCircle(Graphics g, int x, int y, int radius) {
		g.fillOval(x-radius, y-radius, 2*radius, 2*radius);
	}
	
	public static void fillCircleBordered(Graphics g, int x, int y, int radius, Color fillColor, Color borderColor) {
		g.setColor(fillColor);
		fillCircle(g, x, y, radius);
		g.setColor(borderColor);
		drawCircle(g, x, y, radius);
	}
	
	public static void drawDashedRect(Graphics2D g, int centerX, int centerY, int width, int height,
			double stroke, double dash, Color color) {
		Stroke oldStroke = g.getStroke();
		Color oldColor = g.getColor();
		
		Stroke newStroke = new BasicStroke((float) stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[] {(float) dash}, 0);
		g.setStroke(newStroke);
		g.setColor(color);
		
		g.drawRect(centerX-width/2, centerY-height/2, width, height);
		
		g.setColor(oldColor);
		g.setStroke(oldStroke);
	}
	
	public static void fillCircleBorderedLabelled(Graphics g, int x, int y, int radius, String label, Color fillColor, Color borderColor, Color labelColor) {
		fillCircleBordered(g, x, y, radius, fillColor, borderColor);
		
		Font oldFont = g.getFont();
		
		g.setFont(new Font("", Font.PLAIN, 2*radius));
		while(true) {
			FontMetrics m = g.getFontMetrics();
			int width = m.stringWidth(label);
			int height = m.getHeight();
			if(Math.sqrt(width*width/4 + height*height/4) <= radius*0.9) break;
			g.setFont(new Font("", Font.PLAIN, g.getFont().getSize()-1));
		}
		
		g.setColor(labelColor);
		TextDrawer.drawString((Graphics2D) g, label, x, y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		
		g.setFont(oldFont);
	}
	
	public static void drawLine(Graphics2D g, int x0, int y0, int x1, int y1) {
		g.drawLine(x0, y0, x1, y1);
	}
	
	public static void drawLine(Graphics2D g, int x0, int y0, int x1, int y1, Color start, Color end) {
		// Save old Paint
		Paint paint = g.getPaint();
				
		g.setPaint(new GradientPaint(new Point(x0, y0), start, new Point(x1, y1), end));
		g.drawLine(x0, y0, x1, y1);
		
		// Restore old Paint
		g.setPaint(paint);
	}
	
	public static void drawArrow(Graphics2D g, int x0, int y0, int x1, int y1, int arrowSize) {
		g.drawLine(x0, y0, x1, y1);
		
		double angle = Math.atan2(y1-y0, x1-x0);
		g.translate(x1, y1);
		g.rotate(angle);

		g.drawLine(0, 0, -3*arrowSize, +arrowSize);
		g.drawLine(0, 0, -3*arrowSize, -arrowSize);
		
		g.rotate(-angle);
		g.translate(-x1, -y1);
	}
	
	public static void drawArrow(Graphics2D g, int x0, int y0, int x1, int y1, int arrowSize, Color start, Color end) {
		// Save old Paint
		Paint paint = g.getPaint();
		
		// Draw the arrow body
		g.setPaint(new GradientPaint(new Point(x0, y0), start, new Point(x1, y1), end));
		g.drawLine(x0, y0, x1, y1);
		
		
		// Rotate canvas
		double angle = Math.atan2(y1-y0, x1-x0);
		g.translate(x1, y1);
		g.rotate(angle);
		
		// Draw arrows with proper gradient
		g.setPaint(new GradientPaint(new Point(-3*arrowSize, 0), start, new Point(0, 0), end));
		g.drawLine(0, 0, -3*arrowSize, +arrowSize);
		g.drawLine(0, 0, -3*arrowSize, -arrowSize);

		// Rotate canvas BACK
		g.rotate(-angle);
		g.translate(-x1, -y1);
		
		// Restore old Paint
		g.setPaint(paint);
	}
}
