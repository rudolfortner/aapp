package at.jku.cg.sar.util;

import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Class that holds function to draw Text to a Graphics Object in an aligned way
 * @author Rudolf Ortner
 * @since 0.3.1
 */
public class TextDrawer {

	public enum HorizontalAlignment {
		LEFT, CENTER, RIGHT,
	}; 
	public enum VerticalAlignment {
		TOP, CENTER, BOTTOM;
	}; 
	
	/**
	 * Draws a String in a formatted way
	 * @param g Graphics Object you want to draw to
	 * @param text String you want to draw
	 * @param x Location for drawing
	 * @param y Location for drawing
	 * @param hAlignment Horizontal alignment of the text
	 */
	public static void drawString(Graphics2D g, String text, int x, int y, HorizontalAlignment hAlignment) {
		drawString(g, text, x, y, hAlignment, VerticalAlignment.BOTTOM);
	}
	
	/**
	 * Draws a String in a formatted way
	 * @param g Graphics Object you want to draw to
	 * @param text String you want to draw
	 * @param x Location for drawing
	 * @param y Location for drawing
	 * @param hAlignment Horizontal alignment of the text
	 * @param vAlignment Vertical alignment of the text
	 */
	public static void drawString(Graphics2D g, String text, int x, int y, HorizontalAlignment hAlignment, VerticalAlignment vAlignment) {
		FontMetrics metrics = g.getFontMetrics();
		int width = metrics.stringWidth(text);
		int height = metrics.getHeight();
		
		int renderX = 0;
		int renderY = 0;
		
		if(hAlignment == HorizontalAlignment.LEFT) {
			renderX = x;
		}else if(hAlignment == HorizontalAlignment.RIGHT) {
			renderX = x - width;
		}else if(hAlignment == HorizontalAlignment.CENTER) {
			renderX = x - width / 2;
		}
		
		if(vAlignment == VerticalAlignment.TOP) {
			renderY = y + height;
		}else if(vAlignment == VerticalAlignment.BOTTOM) {
			renderY = y;
		}else if(vAlignment == VerticalAlignment.CENTER) {
			renderY = y + height / 2;
		}
		
		g.drawString(text, renderX, renderY);
	}
	
	/**
	 * Draws a String in a formatted way
	 * @param g Graphics Object you want to draw to
	 * @param text String you want to draw
	 * @param x Location for drawing
	 * @param y Location for drawing
	 * @param rotation Rotation of the String
	 * @param hAlignment Horizontal alignment of the text, defines the rotation origin
	 * @param vAlignment Vertical alignment of the text, defines the rotation origin
	 */
	public static void drawString(Graphics2D g, String text, int x, int y, double rotation, HorizontalAlignment hAlignment, VerticalAlignment vAlignment) {
		FontMetrics metrics = g.getFontMetrics();
		int width = metrics.stringWidth(text);
		int height = metrics.getAscent();
		
		int renderX = 0;
		int renderY = 0;
		
		if(hAlignment == HorizontalAlignment.LEFT) {
			renderX = x;
		}else if(hAlignment == HorizontalAlignment.RIGHT) {
			renderX = x - width;
		}else if(hAlignment == HorizontalAlignment.CENTER) {
			renderX = x - width / 2;
		}
		
		if(vAlignment == VerticalAlignment.TOP) {
			renderY = y + height;
		}else if(vAlignment == VerticalAlignment.BOTTOM) {
			renderY = y;
		}else if(vAlignment == VerticalAlignment.CENTER) {
			renderY = y + height / 2;
		}
		
		g.rotate(Math.toRadians(rotation), x, y);
		g.drawString(text, renderX, renderY);
		g.rotate(-Math.toRadians(rotation), x, y);
	}

}
