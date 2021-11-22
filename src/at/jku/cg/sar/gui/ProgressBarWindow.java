package at.jku.cg.sar.gui;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class ProgressBarWindow {

	private final JFrame frame;
	private final JProgressBar progressBar;
	

	public ProgressBarWindow() {
		this("");
	}
	
	public ProgressBarWindow(String title) {
		this.frame = new JFrame(title);
		this.frame.setSize(400, 100);
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.progressBar = new JProgressBar();
		this.progressBar.setStringPainted(true);
		this.progressBar.setValue(0);
		
		this.frame.add(progressBar);
				
		this.frame.setVisible(true);
		
		this.frame.setLocationRelativeTo(null);
		this.frame.toFront();
	}

	public void setProgress(int progress) {
		if(progress < this.progressBar.getMinimum() || progress > this.progressBar.getMaximum() || progressBar == null) return;
//		SwingUtilities.invokeLater(() -> {
			this.progressBar.setValue(progress);
			this.frame.repaint();
//		});
	}
	
	public void setTitle(String title) {
		this.frame.setTitle(title);
	}
	
	public void close() {
		this.frame.dispose();
	}
	
}
