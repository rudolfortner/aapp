package at.jku.cg.sar.gui.graphics;

import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import at.jku.cg.sar.util.Interpolation;

public abstract class AbstractLineGraphPanel extends GraphicsPanel {

	private static final long serialVersionUID = 3012138846364448104L;

	private final GraphMouseListener mouse;
	
	public AbstractLineGraphPanel() {
		this(new String());
	}
	
	public AbstractLineGraphPanel(String title) {
		super(title, 0.05, 0.3, 0.1, 0.1);
	
		mouse = new GraphMouseListener();
		this.addMouseListener(mouse);
		this.addMouseMotionListener(mouse);
	}
	
	public abstract List<LineGraphDataSet> getDataSets();
	

	// Private variables to retrieve data only once
	private List<LineGraphDataSet> dataSets;
	
	private double minX, maxX;
	private double minY, maxY;

	private boolean useCustomRect = false;
	private double customLeft, customRight;
	private double customTop, customBottom;
	
	@Override
	protected void drawBackground(Graphics2D g) {
		
	}

	@Override
	protected void drawForeground(Graphics2D g) {
		dataSets = getDataSets();
		if(dataSets == null) dataSets = new ArrayList<>();
		
		
		if(!useCustomRect) {
			minX = Double.POSITIVE_INFINITY;
			maxX = Double.NEGATIVE_INFINITY;
			
			minY = Double.POSITIVE_INFINITY;
			maxY = Double.NEGATIVE_INFINITY;
			
			for(LineGraphDataSet dataSet : dataSets) {
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
		
		LineGraphRenderer.drawLineGraph(g, dataSets,
				bord_x, bord_y, canvas_width, canvas_height,
				useCustomRect, customLeft, customTop, customRight, customBottom);
		
		LineGraphRenderer.drawLegend(g, dataSets, 
				bord_x+canvas_width+20 , 0, this.getWidth()-bord_x-canvas_width-20, this.getHeight(), true);
		
		if(mouse.isDragged) {
			int x = Math.min(mouse.startX, mouse.mouseX);
			int y = Math.min(mouse.startY, mouse.mouseY);
			int w = Math.abs(mouse.mouseX - mouse.startX);
			int h = Math.abs(mouse.mouseY - mouse.startY);
			g.drawRect(x, y, w, h);
		}
	}

	
	private class GraphMouseListener extends MouseAdapter {

		boolean isDragged = false;
		int startX, startY;
		int mouseX, mouseY;
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON3) {
				useCustomRect = false;
			}
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(!isDragged) {
				startX = e.getX();
				startY = e.getY();
				isDragged = true;
			}
			mouseX = e.getX();
			mouseY = e.getY();
			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(isDragged) {
				isDragged = false;
				
				int x0 = Math.min(mouse.startX, mouse.mouseX);
				int y0 = Math.min(mouse.startY, mouse.mouseY);
				int x1 = x0 + Math.abs(mouse.mouseX - mouse.startX);
				int y1 = y0 + Math.abs(mouse.mouseY - mouse.startY);

				customLeft = Interpolation.Linear(x0, bord_x, minX, bord_x+canvas_width, maxX);
				customRight = Interpolation.Linear(x1, bord_x, minX, bord_x+canvas_width, maxX);
				
				customBottom = Interpolation.Linear(y0, bord_y+canvas_height, minY, bord_y, maxY);
				customTop = Interpolation.Linear(y1, bord_y+canvas_height, minY, bord_y, maxY);
				
				useCustomRect = true;
			}
			repaint();
		}		
	}
	
}
