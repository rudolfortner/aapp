package at.jku.cg.sar.gui.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.gui.DrawUtils;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.scoring.Ranking;
import at.jku.cg.sar.util.ColorRamp;
import at.jku.cg.sar.util.ListWrapper;
import at.jku.cg.sar.util.TextDrawer;
import at.jku.cg.sar.util.TextDrawer.HorizontalAlignment;
import at.jku.cg.sar.util.TextDrawer.VerticalAlignment;

public class DistributionMapRenderer {

	public static BufferedImage renderImage(Grid<Double> grid, Grid<ListWrapper<PathFinder>> distribution, PathFinder highlight,
			String title, String description, int height, boolean showValues, ColorRamp ramp) {		

		BufferedImage image = new BufferedImage(height * 3 / 2, height, BufferedImage.TYPE_INT_RGB);
				
		Graphics2D g = image.createGraphics();
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHints(rh);
		render(g, grid, distribution, highlight, title, description, height, showValues, ramp);
		g.dispose();
		
		return image;
	}
	
	
	public static void render(Graphics2D g, Grid<Double> grid, Grid<ListWrapper<PathFinder>> distribution, PathFinder highlight,
			String title, String description, int height, boolean showValues, ColorRamp ramp) {
		
		Set<PathFinder> finderSet = distribution.getValues().stream()
				.map(gv -> gv.getValue())
				.flatMap(List::stream)
				.collect(Collectors.toUnmodifiableSet());
		List<PathFinder> finders = new ArrayList<>(finderSet);
		Function<PathFinder, Double> scoreFunction = finder -> {
			int countWins = 0;
			for(int x = 0; x < distribution.getWidth(); x++) {
				for(int y = 0; y < distribution.getHeight(); y++) {
					countWins += distribution.get(x, y).contains(finder) ? 1 : 0;
				}
			}
			return 1.0 * countWins;
		};
		Ranking<PathFinder> rankFinders = new Ranking<>(finders, scoreFunction);
		finders = rankFinders.getSorted();
		Map<PathFinder, Character> numbering = new HashMap<>();
		for(int i = 0; i < finders.size(); i++) {
			numbering.put(finders.get(i), (char) ('A'+i));
		}
		
		// BACKGROUND
		g.setColor(Color.GRAY);
		g.fillRect(0, 0, height * 3 / 2, height);

		// RESULT
		int border = (int) (0.1 * height);
		int tileSize = (height - 2*border) / Math.max(grid.getWidth(), grid.getHeight());
		
		
		// RENDER PROB GRID
		GridCellRenderer<Double> overlayRenderer = new GridCellRenderer<>() {			
			@Override
			public void render(Graphics2D g, GridValue<Double> cell, int centerX, int centerY, int width, int height) {
				List<PathFinder> winners = distribution.get(cell.getX(), cell.getY());
				
				// BACKGROUND
				g.setColor(ramp.evaluate(cell.getValue()));
				g.fillRect(centerX - tileSize/2, centerY - tileSize/2, tileSize, tileSize);
				
				// VALUE
				if(showValues) {
					g.setFont(new Font("", Font.PLAIN, tileSize/4));
					g.setColor(Color.BLACK);
					TextDrawer.drawString(g, String.format(Locale.ROOT, "%.2f", cell.getValue()), centerX, centerY + height/2 - 4, HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
				}
				
				// RANK OF WINNERS
				int xOffset = width / (winners.size()+1);
				int radius = height / (3 + winners.size());
				for(int w = 0; w < winners.size(); w++) {
					PathFinder winner = winners.get(w);
					int x = centerX - width/2 + xOffset * (w+1);
					int y = centerY - height/5;
					if(winners.size() > 2 && w % 2 == 0) y += radius;
					DrawUtils.fillCircleBorderedLabelled(g, x, y, radius, ""+numbering.get(winner),
							winner.equals(highlight) ? Color.GREEN : Color.RED, Color.BLACK, Color.BLACK);
				}
				
				
				// BORDER
				g.drawRect(centerX - tileSize/2, centerY - tileSize/2, tileSize, tileSize);
			}
		};
		GridRenderer.render(g, grid, border, border, tileSize, ramp, overlayRenderer);
		
		
		
		// TITLE
		if(title != null && !title.isBlank()) {
			g.setColor(Color.BLACK);
			g.setFont(new Font("", Font.PLAIN, border/2));
			while(true) {
				FontMetrics m = g.getFontMetrics();
				if(m.stringWidth(title) <= (height - 2*border)) break;
				g.setFont(new Font("", Font.PLAIN, g.getFont().getSize()-1));
			}
			TextDrawer.drawString(g, title, height + height/4 - border/2, border/2, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		}
		
		
		// Description
		if(description != null && !description.isBlank()) {
			g.setColor(Color.BLACK);
			g.setFont(new Font("", Font.PLAIN, border/5));
			TextDrawer.drawString(g, description, border, height-border/4, HorizontalAlignment.LEFT);
		}
		

		int radius = 20;
		int x = height - border/2 + radius;
		int y = border + radius;
		// RENDER LIST
		for(PathFinder finder : finders) {			
			DrawUtils.fillCircleBorderedLabelled(g, x, y, radius, ""+rankFinders.getRank(finder),
					finder.equals(highlight) ? Color.GREEN : Color.RED, Color.BLACK, Color.BLACK);
			
			DrawUtils.fillCircleBorderedLabelled(g, x+radius*5/2, y, radius * 3/4, ""+numbering.get(finder),
					finder.equals(highlight) ? Color.GREEN : Color.RED, Color.BLACK, Color.BLACK);

			g.setColor(Color.BLACK);
			g.setFont(new Font("", Font.PLAIN, radius));
			TextDrawer.drawString(g, finder.getName(), x + radius * 5, y, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
			
			y += radius * 5 / 2;
		}
	}
	
}
