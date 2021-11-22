package at.jku.cg.sar.gui.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import at.jku.cg.sar.util.TextDrawer;
import at.jku.cg.sar.util.TextDrawer.HorizontalAlignment;
import at.jku.cg.sar.util.TextDrawer.VerticalAlignment;

public abstract class GraphicsPanel extends JPanel {

	private static final long serialVersionUID = 4915296774293551831L;

	private static final Font DEFAULT_FONT_TITLE = new Font("", Font.BOLD, 26);

	protected String title;

	protected Font font = DEFAULT_FONT_TITLE;
	protected int header_font_style = Font.BOLD;
	protected int header_font_size = 26;

	protected int width, height;
	protected int bord_x, bord_y;
	protected int canvas_width, canvas_height;

	protected double margin_left, margin_right;
	protected double margin_top, margin_bottom;

	protected boolean fix_left, fix_right;
	protected boolean fix_top, fix_bottom;

	public GraphicsPanel() {
		this(null);
	}

	public GraphicsPanel(String title) {
		this(title, 0.1, 0.1, 0.1, 0.1);
	}

	public GraphicsPanel(String title, double margin_left, double margin_right, double margin_top,
			double margin_bottom) {
		super();
		this.title = title;
		this.margin_left = margin_left;
		this.margin_right = margin_right;
		this.margin_top = margin_top;
		this.margin_bottom = margin_bottom;
	}

	public void paint(Graphics gg) {
		Graphics2D g = (Graphics2D) gg;

		width = this.getWidth();
		height = this.getHeight();

		if(fix_left)
			bord_x = (int) margin_left;
		else
			bord_x = (int) (width * margin_left);

		if(fix_right)
			canvas_width = (int) (width - bord_x - margin_right);
		else
			canvas_width = (int) (width - (width * margin_right) - bord_x);

		if(fix_top)
			bord_y = (int) margin_top;
		else
			bord_y = (int) (height * margin_top);

		if(fix_bottom)
			canvas_height = (int) (height - bord_y - margin_bottom);
		else
			canvas_height = (int) (height - (height * margin_bottom) - bord_y);

		// Clear canvas
		g.clearRect(0, 0, width, height);

		// Enable Anti-Aliasing
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHints(rh);

		// Draw Background
		g.setColor(getBackground());
		g.fillRect(0, 0, width, height);
		drawBackground(g);

		// Draw borders
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, bord_x, height);
		g.fillRect(0, 0, width, bord_y);
		g.fillRect(bord_x+canvas_width, 0, width-bord_x-canvas_width, height);
		g.fillRect(0, bord_y+canvas_height, width, height-bord_y-canvas_height);

		g.setColor(Color.BLACK);
		g.drawRect(bord_x, bord_y, canvas_width, canvas_height);
		

		// Draw title if given
		if(title != null) {
			g.setFont(font.deriveFont(header_font_style, header_font_size));
			TextDrawer.drawString((Graphics2D) g, title, bord_x + canvas_width / 2, bord_y / 2,
					HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
			g.setFont(font.deriveFont(Font.PLAIN, 12));
		}

		// Draw Foreground
		drawForeground(g);
	}

	protected abstract void drawBackground(Graphics2D g);

	protected abstract void drawForeground(Graphics2D g);

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		this.repaint();
	}

	public void setMargins(double margin_left, double margin_right, double margin_top, double margin_bottom) {
		setMarginLeft(margin_left, false);
		setMarginRight(margin_right, false);
		setMarginTop(margin_top, false);
		setMarginBottom(margin_bottom, false);
		this.repaint();
	}

	public void setMarginsFixed(double margin_left, double margin_right, double margin_top, double margin_bottom) {
		setMarginLeft(margin_left, true);
		setMarginRight(margin_right, true);
		setMarginTop(margin_top, true);
		setMarginBottom(margin_bottom, true);
		this.repaint();
	}

	public void setMarginLeft(double margin, boolean fixed) {
		this.margin_left = margin;
		this.fix_left = fixed;
	}

	public void setMarginRight(double margin, boolean fixed) {
		this.margin_right = margin;
		this.fix_right = fixed;
	}

	public void setMarginTop(double margin, boolean fixed) {
		this.margin_top = margin;
		this.fix_top = fixed;
	}

	public void setMarginBottom(double margin, boolean fixed) {
		this.margin_bottom = margin;
		this.fix_bottom = fixed;
	}

	public void setFont(Font font) {
		this.font = font;
		this.repaint();
	}
}
