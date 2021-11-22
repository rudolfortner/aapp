package at.jku.cg.sar.test.core;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.gui.GridViewer;

class GridTest {

	@Test
	void viewerUpdate() throws InterruptedException {
		Grid<Double> grid = new Grid<>(16, 16, 0.0);
		GridViewer viewer = new GridViewer(grid);
		
		
		Thread.sleep(2000);
		
		for(int x = 0; x < 16; x++) {
			for(int y = 0; y < 16; y++) {
				grid.set(x, y, 1.0);
				viewer.update();
				Thread.sleep(1000);
			}
		}
		
	}

}
