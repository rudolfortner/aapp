package at.jku.cg.sar.test;

import java.io.File;
import java.util.Random;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridIO;
import at.jku.cg.sar.gui.GridViewer;
import at.jku.cg.sar.util.Clamp;
import at.jku.cg.sar.util.PersonSimulation;

class PersonSimulatorTest {

	@Test
	void run() {
		int WIDTH = 16;
		double min = 0.05;
		
		Grid<Double> terrain = GridIO.fromImageFile(new File("in/terrain.png"), true, 0.0);
		for(int x = 0; x < WIDTH; x++) {
			for(int y = 0; y < WIDTH; y++) {
				double t = terrain.get(x, y);
				t = 2000 * t;
				terrain.set(x, y, t);
			}
		}
		Grid<Boolean> obstacles = new Grid<>(WIDTH, WIDTH, false);

		for(int x = 0; x < WIDTH; x++) {
			for(int y = 0; y < WIDTH; y++) {
				obstacles.set(x, y, (new Random()).nextDouble() < 0.2);
			}
		}
		PersonSimulation s = new PersonSimulation(terrain, obstacles, 30.0);
		s.setSeed(1000000);
		Grid<Double> simGrid = s.run(278, 345, 40);
		simGrid.modify(value -> {
			return Clamp.clamp(value + min, min, 1.0);
		});
		
		new GridViewer("Simulated World", simGrid);
		
		try {
			Thread.sleep(10000);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		GridIO.toImageFile8(new File("in/map_simulated.png"), simGrid);
	}

}
