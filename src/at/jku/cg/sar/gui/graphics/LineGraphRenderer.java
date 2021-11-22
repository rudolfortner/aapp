package at.jku.cg.sar.gui.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import at.jku.cg.sar.gui.DrawUtils;
import at.jku.cg.sar.util.Curve.CurvePoint;
import at.jku.cg.sar.util.Interpolation;
import at.jku.cg.sar.util.TextDrawer;
import at.jku.cg.sar.util.TextDrawer.HorizontalAlignment;
import at.jku.cg.sar.util.TextDrawer.VerticalAlignment;

public class LineGraphRenderer {
	
	public static void drawLineGraph(Graphics2D g, List<LineGraphDataSet> data,
			int x, int y, int width, int height,
			boolean useCustomRect, double customLeft, double customTop, double customRight, double customBottom) {
		drawLineGraph(g, data,
				x, y, width, height,
				null, null,
				false, false, 0.0, false,
				useCustomRect, customLeft, customTop, customRight, customBottom);
	}
	
	public static void drawLineGraph(Graphics2D g, List<LineGraphDataSet> data,
			int x, int y, int width, int height,
			String labelX, String labelY,
			boolean useCustomRect, double customLeft, double customTop, double customRight, double customBottom) {
		drawLineGraph(g, data,
				x, y, width, height,
				labelX, labelY,
				false, false, 0.0, false,
				useCustomRect, customLeft, customTop, customRight, customBottom);
	}
	
	public static void drawLineGraph(Graphics2D g, List<LineGraphDataSet> data,
			int x, int y, int width, int height,
			String labelX, String labelY,
			boolean fillCurve, boolean useFillStop, double fillStopValue, boolean showFillStop,
			boolean useCustomRect, double customLeft, double customTop, double customRight, double customBottom) {
		
		if(data == null) data = new ArrayList<>();
		List<Path> paths = new ArrayList<>();
		List<Path> polygons = new ArrayList<>();
		
		/* ----- DATA PREPARATION ----- */
		
		// Store value ranges of dataset
		double minX, maxX;
		double minY, maxY;
		
		if(!useCustomRect) {
			minX = Double.POSITIVE_INFINITY;
			maxX = Double.NEGATIVE_INFINITY;
			
			minY = Double.POSITIVE_INFINITY;
			maxY = Double.NEGATIVE_INFINITY;
			
			for(LineGraphDataSet dataSet : data) {
				minX = Math.min(minX, dataSet.minX());
				maxX = Math.max(maxX, dataSet.maxX());
				
				minY = Math.min(minY, dataSet.minY());
				maxY = Math.max(maxY, dataSet.maxY());
			}
		}else {
			minX = customLeft;
			maxX = customRight;
			
			minY = customTop;
			maxY = customBottom;
		}

		
		/* ----- DATA GENERATION ----- */		
		
		// GENERATE PATHS
		for(LineGraphDataSet dataSet : data) {
			Path path = new Path(dataSet.getColor());
			for(CurvePoint point : dataSet.getPoints()) {
				int px = (int) Interpolation.Linear(point.getX(), minX, x, maxX, x+width);
				int py = (int) Interpolation.Linear(point.getY(), minY, y + height, maxY, y);				
				path.addPoint(px, py);
			}
			paths.add(path);
		}
		
		// GENERATE POLYGONS
		if(fillCurve) {
			for(LineGraphDataSet dataSet : data) {
				Color polyColor = new Color(dataSet.getColor().getRed(), dataSet.getColor().getGreen(), dataSet.getColor().getBlue(), 64);
				
				Path polygon = new Path(polyColor);
				// Add origin of graph
				int ox = (int) Interpolation.Linear(minX, minX, x, maxX, x+width);
				int oy = (int) Interpolation.Linear(minY, minY, y + height, maxY, y);
				polygon.addPoint(ox, oy);
				
				double lastX = 0.0, lastY = 0.0;
				for(int p = 0; p < dataSet.getPoints().size(); p++) {
					CurvePoint point = dataSet.getPoints().get(p);
					
					if(useFillStop && point.getX() > fillStopValue) {
						CurvePoint pointBefore = dataSet.getPoints().get(p-1);
						
						lastX = fillStopValue;
						lastY = Interpolation.Linear(fillStopValue, point.getX(), point.getY(), pointBefore.getX(), pointBefore.getY());
						
					}else {
						lastX = point.getX();
						lastY = point.getY();
					}
					int px = (int) Interpolation.Linear(lastX, minX, x, maxX, x+width);
					int py = (int) Interpolation.Linear(lastY, minY, y + height, maxY, y);		
					polygon.addPoint(px, py);
				}
				
				// Add final point
				int px = (int) Interpolation.Linear(lastX, minX, x, maxX, x+width);
				int py = (int) Interpolation.Linear(minY, minY, y + height, maxY, y);				
				polygon.addPoint(px, py);
				
				polygons.add(polygon);
			}
		}
		
		

		/* ----- RENDERING ----- */
		
		// Arrows (x y)
		g.setColor(Color.BLACK);
		DrawUtils.drawArrow(g, x, y+height, x+width, y+height, 3);
		DrawUtils.drawArrow(g, x, y+height, x, y, 3);
		
		// DRAW POLYGONS (area below graphs)
		if(fillCurve) {
			for(Path poly : polygons) poly.fillPolygon(g);
		}
		
		// DRAW FILLSTOP
		if(fillCurve && useFillStop && showFillStop) {
			double max = data.stream().mapToDouble(curve -> curve.getValue(fillStopValue)).max().orElse(0.0);

			int px = (int) Interpolation.Linear(fillStopValue, minX, x, maxX, x+width);
			int py0 = (int) Interpolation.Linear(0.0, minY, y + height, maxY, y);
			int py1 = (int) Interpolation.Linear(max, minY, y + height, maxY, y);
			
			g.setColor(Color.BLACK);
			g.drawLine(px, py0, px, py1);
		}

		// DRAW LINES (overpainted by borders)
		for(Path path : paths) path.draw(g);
		
		
		/* ----- FOREGROUND ----- */
		
		if(data == null || data.size() == 0) {
			g.setColor(Color.BLACK);
			TextDrawer.drawString(g, "NO DATA",  + width/2, y + height/2, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
			return;
		}
		
		// DRAW DOTS (may overpaint borders)
		for(Path path : paths) {
			path.drawDots(g, 4, x, y, x+width, y+height);
		}
		
		g.setColor(Color.BLACK);
		g.setFont(new Font("", Font.BOLD, 16));
		// X-Axis
		TextDrawer.drawString(g, String.format(Locale.ROOT, "%.2f", minX), x, y+height+10, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
		TextDrawer.drawString(g, String.format(Locale.ROOT, "%.2f", maxX), x+width, y+height+10, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
		if(labelX != null && !labelX.isBlank()) TextDrawer.drawString(g, labelX, x+width/2, y+height+10, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
		
		// Y-Axis
		TextDrawer.drawString(g, String.format(Locale.ROOT, "%.2f", minY), x-10, y+height, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
		TextDrawer.drawString(g, String.format(Locale.ROOT, "%.2f", maxY), x-10, y, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
		if(labelY != null && !labelY.isBlank()) TextDrawer.drawString(g, labelY, x-10, y+height/2, -90.0, HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
		
	}
	
	public static void drawLegend(Graphics2D g, List<LineGraphDataSet> dataSets,
			int x, int y, int width, int height,
			boolean center) {
		int colorWidth = 16;
		
		int nameWidth = 0;
		int nameHeight = 0;
		for(LineGraphDataSet dataSet : dataSets) {
			FontMetrics metrics = g.getFontMetrics();
			int setNameWidth = metrics.stringWidth(dataSet.getName());
			int setNameHeight = metrics.getHeight();

			nameWidth = Math.max(nameWidth, setNameWidth);
			nameHeight = Math.max(nameHeight, setNameHeight);
		}
		
		int boxWidth = 4 + colorWidth + 8 + nameWidth + 4;
		int playerHeight = (int) Math.max(nameHeight * 1.25, colorWidth+4);
		int boxHeight = (dataSets.size() * playerHeight);
		
		if(center) {
			int centerX = x + width/2;
			int centerY = y + height/2;
			int newX = centerX - boxWidth/2;
			int newY = centerY - boxHeight/2;
			
			x = Math.max(x, newX);
			y = Math.max(y, newY);
		}

		g.setColor(Color.WHITE);
		g.fillRect(x, y, boxWidth, boxHeight);
		g.setColor(Color.BLACK);
		g.drawRect(x, y, boxWidth, boxHeight);
		
		for(int ds = 0; ds < dataSets.size(); ds++) {
			LineGraphDataSet dataSet = dataSets.get(ds);
			int playerY = (int) (y + playerHeight * (0.5 + ds)) + 2;

			g.setColor(dataSet.getColor());
			g.fillRect(x + 4, playerY - playerHeight/2, colorWidth, colorWidth);
			g.setColor(Color.BLACK);
			g.setFont(new Font("", Font.BOLD, 16));
			g.drawRect(x + 4, playerY - playerHeight/2, colorWidth, colorWidth);
			TextDrawer.drawString(g, dataSet.getName(), x + colorWidth + 12, playerY-4, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
		}
	}
	
	public static void export(List<LineGraphDataSet> dataSets, String labelX, String labelY, File file) {
		
		try {
			FileWriter writer = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(writer);
			
			Set<Double> allXvaluesSet = new HashSet<>();
			for(LineGraphDataSet set : dataSets) {
				for(CurvePoint p : set.getPoints()) {
					allXvaluesSet.add(p.getX());
				}
			}
			
			List<Double> allXvalues = new ArrayList<>(allXvaluesSet);
			allXvalues.sort((v0, v1) -> Double.compare(v0, v1));
			
			bw.write("Time [s]");
			for(LineGraphDataSet set : dataSets) {
				bw.write("," + set.getName());
			}
			bw.write(System.lineSeparator());
			
			String format = "%10.20f";
			for(double x : allXvalues) {
				String XvalueString = String.format(Locale.ROOT, format, x);
				bw.write(XvalueString);
				for(LineGraphDataSet set : dataSets) {
					bw.write(",");
					if(set.maxX() < x) continue;
					double y = set.getValue(x);
					String YvalueString = String.format(Locale.ROOT, format, y);
					bw.write(YvalueString);
				}
				bw.write(System.lineSeparator());
			}
			bw.close();
			writer.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	
		
		
		
	}

}
