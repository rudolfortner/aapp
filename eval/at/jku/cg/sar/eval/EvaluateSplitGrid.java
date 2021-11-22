package at.jku.cg.sar.eval;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.splitcontainer.SplitGrid;
import at.jku.cg.sar.gui.graphics.GridRenderer;

class EvaluateSplitGrid {

	@Test
	void test() {
		double border = 0.2;
		run(1, border);
		run(8, border);
		
	}
	
	private void run(int gridSize, double border) {
		double cellSize = 30.0;
		File dir = new File("out", "splitgrid");
		dir.mkdirs();
		File fileBefore = new File(dir, "splitgrid_%02d_before.png".formatted(gridSize));
		File fileAfter  = new File(dir, "splitgrid_%02d_after.png".formatted(gridSize));
		File fileComb	= new File(dir, "splitgrid_%02d_combined.png".formatted(gridSize));
		
		Grid<Double> grid = new Grid<Double>(gridSize, gridSize, 1.0 / 2.0);
		SplitGrid splitGrid = new SplitGrid(grid, cellSize);

		BufferedImage imageBefore = GridRenderer.renderImage(splitGrid, 1024, EvaluationConfig.rampProbability, false);		

		double left = border * gridSize * cellSize;
		double top = border * gridSize * cellSize;
		double right = (1.0 - border) * gridSize * cellSize;
		double bottom = (1.0 - border) * gridSize * cellSize;
		double prob = splitGrid.collectRectangle(left, top, right, bottom);

		BufferedImage imageAfter = GridRenderer.renderImage(splitGrid, 1024, EvaluationConfig.rampProbability, false);
		
		try {
			ImageIO.write(imageBefore, "PNG", fileBefore);
			ImageIO.write(imageAfter, "PNG", fileAfter);
			
			EvaluationUtils.combineImages(1024, 32, List.of(fileBefore, fileAfter), fileComb, false);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
