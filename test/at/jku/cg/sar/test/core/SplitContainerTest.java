package at.jku.cg.sar.test.core;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.splitcontainer.SplitContainer;
import at.jku.cg.sar.core.splitcontainer.SplitGrid;
import at.jku.cg.sar.gui.GridViewer;
import at.jku.cg.sar.world.WorldGenerator;

class SplitContainerTest {
	
	public static final double EPSILON = 10E-6;
	public static final int WIDTH = 16;
	public static final double GRID_WIDTH = 30.0;

	private Grid<Double> grid;
	private SplitGrid container;
	private Random random;
	
	@BeforeEach
	void init() {
		WorldGenerator generator = new WorldGenerator(0);
		grid = generator.SmoothedSpotsNormalized(WIDTH, WIDTH, 0.2, 0.05, 1.0);
		container = new SplitGrid(grid, GRID_WIDTH);
		random = new Random();
	}
	
	@Test
	void gridInit() {
		for(int x = 0; x < grid.getWidth(); x++) {
			for(int y = 0; y < grid.getHeight(); y++) {
				assertEquals(grid.get(x, y), container.getCell(x, y).getValue());
				assertEquals(GRID_WIDTH*GRID_WIDTH, container.getCell(x, y).getArea());
			}
		}
	}
	
	@Test
	void collectNormal() {		
		SplitContainerTester test = new SplitContainerTester();
		
		for(int i = 0; i < 100; i++) {
			double left = GRID_WIDTH * WIDTH * random.nextDouble();
			double top = GRID_WIDTH * WIDTH * random.nextDouble();
	
			double right = left + 2.0 * GRID_WIDTH * random.nextDouble();
			double bottom = top + 2.0 * GRID_WIDTH * random.nextDouble();
			
			test.addTest(grid -> grid.collectRectangle(left, top, right, bottom));
		}
		
		test.run(container);		
	}
	
	@Test
	void collectNormalCollapse() {		
		SplitContainerTester test = new SplitContainerTester();
		
		for(int i = 0; i < 100; i++) {
			double left = GRID_WIDTH * WIDTH * random.nextDouble();
			double top = GRID_WIDTH * WIDTH * random.nextDouble();
	
			double right = left + 2.0 * GRID_WIDTH * random.nextDouble();
			double bottom = top + 2.0 * GRID_WIDTH * random.nextDouble();
			
			test.addTest(grid -> {
				double value = grid.collectRectangle(left, top, right, bottom);
				grid.collapse();
				return value;
			});
		}
		
		test.run(container);		
	}
	
	@Test
	void collectSmall() {		
		SplitContainerTester test = new SplitContainerTester();
		
		for(int x = 0; x < GRID_WIDTH; x++) {
			for(int y = 0; y < GRID_WIDTH; y++) {
				double cx = (x + 0.5) * GRID_WIDTH;
				double cy = (y + 0.5) * GRID_WIDTH;
				
				double left	= cx - GRID_WIDTH / 8.0;
				double top	= cy - GRID_WIDTH / 8.0;
		
				double right = cx + GRID_WIDTH / 8.0;
				double bottom = cy + GRID_WIDTH / 8.0;
				
				test.addTest(grid -> grid.collectRectangle(left, top, right, bottom));
			}
		}
		
		test.run(container);
		new GridViewer(container);
		try {
			Thread.sleep(12000);
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	void collectTwice() {		
		SplitContainerTester test = new SplitContainerTester();
		
		double left = GRID_WIDTH * WIDTH * random.nextDouble();
		double top = GRID_WIDTH * WIDTH * random.nextDouble();
		double right = left + 2.0 * GRID_WIDTH * random.nextDouble();
		double bottom = top + 2.0 * GRID_WIDTH * random.nextDouble();

		test.addTest(grid -> grid.collectRectangle(left, top, right, bottom));
		test.addTest(grid -> grid.collectRectangle(left, top, right, bottom));
		
		test.run(container);		
	}
	
	@Test
	void collectNormalScanLines() {		
		SplitContainerTester test = new SplitContainerTester();
		
		double stepWidth = grid.getHeight() * GRID_WIDTH;
		for(int i = 0; i < 100; i++) {
			double left = 0.0;
			double right = GRID_WIDTH * WIDTH;
			
			double top = i * stepWidth;	
			double bottom = top + stepWidth;
			
			test.addTest(grid -> grid.collectRectangle(left, top, right, bottom));
		}
		
		test.run(container);
	}
	
	@Test
	void collectNormalScanLinesCollapse() {		
		SplitContainerTester test = new SplitContainerTester();
		
		double stepWidth = grid.getHeight() * GRID_WIDTH;
		for(int i = 0; i < 100; i++) {
			double left = 0.0;
			double right = GRID_WIDTH * WIDTH;
			
			double top = i * stepWidth;	
			double bottom = top + stepWidth;
			
			test.addTest(grid -> {
				double value = grid.collectRectangle(left, top, right, bottom);
				grid.collapse();
				return value;
			});
		}
		
		test.run(container);
	}
	
	@Test
	void collectCircleSingle() {		
		SplitContainerTester test = new SplitContainerTester();
		
		test.addTest(grid -> grid.collectCircle(151.0, 150.0, 64.0, 8));
		
		test.run(container);
	}
	
	@Test
	void collectCircleSmall() {		
		SplitContainerTester test = new SplitContainerTester();
		
		test.addTest(grid -> grid.collectCircle(13.7, 15.9, 9.9, 8));
		
		test.run(container);
	}
	
	@Test
	void collectCircles() {		
		SplitContainerTester test = new SplitContainerTester();
		
		for(int i = 0; i < 20; i++) {			
			double centerX = GRID_WIDTH * WIDTH * random.nextDouble();
			double centerY = GRID_WIDTH * WIDTH * random.nextDouble();	
			double radius = 2.0 * GRID_WIDTH * random.nextDouble();
			
			test.addTest(grid -> grid.collectCircle(centerX, centerY, radius, 8));
		}
		
		test.run(container);
	}
	
	@Test
	void collectCirclesMaxStepHeight() {		
		SplitContainerTester test = new SplitContainerTester();
		
		for(int i = 0; i < 20; i++) {			
			double centerX = GRID_WIDTH * WIDTH * random.nextDouble();
			double centerY = GRID_WIDTH * WIDTH * random.nextDouble();	
			double radius = 2.0 * GRID_WIDTH * random.nextDouble();
			
			test.addTest(grid -> grid.collectCircle(centerX, centerY, radius, 10.0));
		}
		
		test.run(container);
	}
	
	@Test
	void min() {
		double minGrid = grid.minValue();
		double minContainer = container.min();
		assertEquals(minGrid, minContainer);		
	}
	
	@Test
	void max() {
		double maxGrid = grid.maxValue();
		double maxContainer = container.max();
		assertEquals(maxGrid, maxContainer);		
	}
	
	@Test
	void modify() {
		Function<Double, Double> func = value -> Math.sqrt(value);
		
		grid.modify(func);
		container.modify(func);
		
		for(int x = 0; x < WIDTH; x++) {
			for(int y = 0; y < WIDTH; y++) {
				assertEquals(grid.get(x, y), container.getCell(x, y).getValue());
			}
		}
	}
	
	@Test
	void putTest() {		
		
		double left = GRID_WIDTH * WIDTH * random.nextDouble();
		double top = GRID_WIDTH * WIDTH * random.nextDouble();
		double right = left + 4.0 * GRID_WIDTH * random.nextDouble();
		double bottom = top + 4.0 * GRID_WIDTH * random.nextDouble();
		
		container.putRectangle(left, top, right, bottom, 10.0);
		
		new GridViewer(grid);
		new GridViewer(container);
		try {
			Thread.sleep(12000);
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	private final class SplitContainerTester {		
		private final List<Function<SplitContainer, Double>> tests;
		
		public SplitContainerTester() {
			this.tests = new ArrayList<>();
		}
		
		public void addTest(Function<SplitContainer, Double> test) {
			this.tests.add(test);
		}
		
		public void run(SplitContainer container) {
			printHeader();
			double probsBefore	= container.getContainers().stream().mapToDouble(c -> c.getValue()).sum();
			double areaBefore	= container.getContainers().stream().mapToDouble(c -> c.getArea()).sum();
			
			double probsCollected = 0.0;
			for(Function<SplitContainer, Double> test : tests) {
				probsCollected += test.apply(container);
			}

			double probsAfter	= container.getContainers().stream().mapToDouble(gv -> gv.getValue()).sum();
			double areaAfter	= container.getContainers().stream().mapToDouble(c -> c.getArea()).sum();
			double probsTotal	= probsAfter + probsCollected;

			print("Probs Before", probsBefore);
			print("Probs After", probsAfter);
			print("Probs Collected", probsCollected);
			print("Probs Total", probsTotal);
			assertTrue(probsAfter <= probsBefore);
			assertTrue(Math.abs(probsBefore - probsTotal) < EPSILON);


			print("Area Before", areaBefore);
			print("Area After", areaAfter);
			assertTrue(Math.abs(areaBefore - areaAfter) < EPSILON);
			
			// COLLAPSE TEST
			int containersBefore = container.getContainers().size();
			print("Containers Before", containersBefore);
			container.collapse();
			int containersAfter = container.getContainers().size();
			print("Containers After", containersAfter);
			assertTrue(containersAfter <= containersBefore);

			printHeader();
		}		
	}
	
	public void printHeader() {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < 45; i++) builder.append("-");
		System.err.println(builder.toString());		
	}
	
	public void print(String title, double value) {
		System.err.println("%-25s%20.9f".formatted(title+":", value));
	}
	
	public void print(String title, int value) {
		System.err.println("%-25s%10d".formatted(title+":", value));
		
	}
}
