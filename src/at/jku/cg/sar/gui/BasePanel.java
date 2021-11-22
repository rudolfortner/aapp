package at.jku.cg.sar.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public abstract class BasePanel extends JPanel {

	private static final long serialVersionUID = 2025359876812216664L;
	

	public static final boolean ANTI_ALIASING = false;
	
	protected double zoom = 1.0;
	protected int originX, originY;
	private final List<BasePanelListener> listeners;
	
	public BasePanel() {
		this.listeners = new ArrayList<>();
		
		BasePanelKeyListener keyListener = new BasePanelKeyListener();
		this.addKeyListener(keyListener);
		
		BasePanelMouseListener mouseListener = new BasePanelMouseListener();
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseListener);
		this.addMouseWheelListener(mouseListener);
		
	}
	
	@Override
	public void paint(Graphics gg) {
		super.paint(gg);
		Graphics2D g = (Graphics2D) gg;
		
		//Enable Anti-Aliasing
		if(ANTI_ALIASING) {
			RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHints(rh);
		}
		
		drawPanel(g);
	}
	
	public abstract void drawPanel(Graphics2D g);
	public abstract void frameAll(int left, int top, int right, int bottom);
	
	public void frameAll() {
		frameAll(0);
	}
	
	public void frameAll(int border) {
		frameAllIntern(border, border, border, border);
	}

	private void frameAllIntern(int left, int top, int right, int bottom) {
		frameAll(left, top, right, bottom);
		fireViewChanged(false);
	}
	
	
	
	public void setView(int originX, int originY, double zoom) {
		this.originX = originX;
		this.originY = originY;
		this.zoom = zoom;
		this.repaint();
		this.fireViewChanged(false);
	}
	

	public void fireViewChanged(boolean byUser) {
		for(BasePanelListener listener : listeners) {
			listener.viewChanged(originX, originY, zoom, byUser);
		}
	}
	
	public void addListener(BasePanelListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(BasePanelListener listener) {
		this.listeners.remove(listener);
	}	
	
	private class BasePanelKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_SPACE) {
				frameAll();
				fireViewChanged(true);
			}
		}		
	}
	
	private class BasePanelMouseListener extends MouseAdapter {

		private int mouseX, mouseY;
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			zoom -= e.getPreciseWheelRotation() / 10.0;
			repaint();
			fireViewChanged(true);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int dx = e.getX() - mouseX;
			int dy = e.getY() - mouseY;
			mouseX = e.getX();
			mouseY = e.getY();
			
			originX += dx;
			originY += dy;
			
			repaint();
			fireViewChanged(true);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mouseX = e.getX();
			mouseY = e.getY();
			requestFocus();
		}
	}
	
}
