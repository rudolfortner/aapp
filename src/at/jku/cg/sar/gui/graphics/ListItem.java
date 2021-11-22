package at.jku.cg.sar.gui.graphics;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;

import at.jku.cg.sar.util.TextDrawer;
import at.jku.cg.sar.util.TextDrawer.HorizontalAlignment;
import at.jku.cg.sar.util.TextDrawer.VerticalAlignment;

public class ListItem {

	private final String name, value;

	public ListItem(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
	
	public static void renderList(Graphics2D g, List<ListItem> items, int x, int y) {
		renderList(g, items, x, y, 50);
	}

	public static void renderList(Graphics2D g, List<ListItem> items, int x, int y, int spacing) {
		int maxNameWidth = 0;
		int maxDataWidth = 0;
		int height = g.getFont().getSize();
		FontMetrics metric = g.getFontMetrics();
		
		for(ListItem item : items) {
			int nameWidth = metric.stringWidth(item.getName());
			int dataWidth = metric.stringWidth(item.getValue());

			maxNameWidth = Math.max(maxNameWidth, nameWidth);
			maxDataWidth = Math.max(maxDataWidth, dataWidth);
		}
		
		int dist = maxNameWidth + spacing + maxDataWidth;		
		for(int e = 0; e < items.size(); e++) {
			int currentY = y + e * height;
			ListItem item = items.get(e);

			TextDrawer.drawString(g, item.getName(), x, currentY, HorizontalAlignment.LEFT, VerticalAlignment.TOP);
			TextDrawer.drawString(g, item.getValue(), x + dist, currentY, HorizontalAlignment.RIGHT, VerticalAlignment.TOP);
		}		
	}

}
