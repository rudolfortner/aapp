package at.jku.cg.sar.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.splitcontainer.SplitContainer;
import at.jku.cg.sar.core.splitcontainer.SplitGrid;
import at.jku.cg.sar.eval.EvaluationConfig;
import at.jku.cg.sar.gui.DrawUtils;
import at.jku.cg.sar.gui.GridViewer;
import at.jku.cg.sar.gui.LineGraphViewer;
import at.jku.cg.sar.gui.graphics.LineGraphDataSet;
import at.jku.cg.sar.scoring.ScoreCriteria;
import at.jku.cg.sar.scoring.ScoreItem;
import at.jku.cg.sar.scoring.ScoreType;
import at.jku.cg.sar.scoring.ScoringSystem;
import at.jku.cg.sar.util.Bezier;
import at.jku.cg.sar.world.WorldGenerator;
import at.jku.cg.sar.world.WorldPoint;

class Testing {

	@Test
	void BezierTest() {
		// CURVE TEST
		WorldPoint p0 = new WorldPoint(0.0, 0.0);
		WorldPoint p1 = new WorldPoint(0.0, 1024.0);
		WorldPoint p2 = new WorldPoint(1024.0, 0.0);
		WorldPoint p3 = new WorldPoint(1024.0, 1024.0);
		List<WorldPoint> curve = Bezier.createCurve(List.of(p0, p1, p2, p3), 64.0);
		
		BufferedImage curveImg = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = curveImg.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		g.setColor(Color.RED);
		DrawUtils.drawCircle(g, 0, 0, 20);
		DrawUtils.drawCircle(g, 0, 1024, 20);
		DrawUtils.drawCircle(g, 1024, 1024, 20);
		
		for(int p = 0; p < curve.size()-1; p++) {
			WorldPoint cp0 = curve.get(p);
			WorldPoint cp1 = curve.get(p+1);
			g.setColor(Color.ORANGE);
			DrawUtils.drawCircle(g, (int) cp0.getX(), (int) cp0.getY(), 10);	
			DrawUtils.drawCircle(g, (int) cp1.getX(), (int) cp1.getY(), 10);
			
			g.setColor(Color.GREEN);
			g.drawLine((int) cp0.getX(), (int) cp0.getY(), (int) cp1.getX(), (int) cp1.getY());
		}
		
		g.dispose();
		try {
			ImageIO.write(curveImg, "PNG", new File("out/curve.png"));
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void DistanceCalculation() {
		// TEST DISTANCE CALCULATION OF GRID/WORLD
		double dist0 = 30.0 * Math.sqrt(1.0*1.0 + 2.0*2.0);
		double dist1 = Math.sqrt(30.0*30.0 + 60.0*60.0);		
		System.err.println(dist0 + "  " + dist1 + " " + (dist0-dist1));
	}
	
	@Test
	void ScoringTest() {
		ScoreCriteria c1 = new ScoreCriteria("Grades", ScoreType.LOWER__BETTER);
		ScoreCriteria c2 = new ScoreCriteria("Body Size", ScoreType.HIGHER_BETTER);

		ScoreItem item1 = new ScoreItem("Person 1");
		ScoreItem item2 = new ScoreItem("Person 2");
		ScoreItem item3 = new ScoreItem("Person 3");
		item1.addValue(c1, 1);
		item2.addValue(c1, 2);
		item3.addValue(c1, 5);
		
		item1.addValue(c2, 172);
		item2.addValue(c2, 173);
		item3.addValue(c2, 150);
		List<ScoreItem> items = List.of(item1, item2, item3);

		System.err.println("BEFORE");
		for(ScoreItem item : items) {
			System.err.println("%s\t%f".formatted(item.getName(), item.getScore()));
		}
		
		ScoringSystem ss = new ScoringSystem();
		List<ScoreItem> res = ss.process(items);
		
		System.err.println("AFTER " + res.size());
		for(ScoreItem item : res) {
			System.err.println("%s\t%f".formatted(item.getName(), item.getScore()));
		}
	}
	
	@Test
	void SplitContainerCircleTest() {
		
		WorldGenerator generator = new WorldGenerator(0);
		Grid<Double> grid = generator.SmoothedSpotsNormalized(16, 16, 0.2, 0.05, 1.0);
		double sum = grid.getValues().stream().mapToDouble(gv -> gv.getValue()).sum();
		
		SplitGrid sGrid = new SplitGrid(grid, 30.0);
		
		double collected = sGrid.collectCircle(150, 150, 64);

		double sumAfter = sGrid.getContainers().stream().mapToDouble(gv -> gv.getValue()).sum();
		double total = sumAfter + collected;
		
		assertEquals(sum, total);

		System.err.println("Sum Before: %f \t Sum After: %f".formatted(sum, sumAfter));
		System.err.println("Collected %f".formatted(collected));
		System.err.println("Total %f".formatted(total));
		
		System.err.println(sGrid.getContainers().size());
		sGrid.collapse();
		System.err.println(sGrid.getContainers().size());
		
		System.err.println("AREA: " + sGrid.getCell(4, 4).getContainers().stream().mapToDouble(c -> c.getArea()).sum());
		for(SplitContainer c : sGrid.getCell(4, 4).getContainers()) {
			System.err.println("VAL " + c.getValue());
		}
		
		new GridViewer(grid);
		new GridViewer(sGrid);
		while(true) {
			try {
				Thread.sleep(10000);
			}catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Test
	void AccelerationCalculationTest() {
		
		Function<Double, Double> aFunc = t -> {
			if(t < 0.0) return 0.0;
			if(t > 4.0) return 0.0;
			return 0 + Math.pow(t/8.0 - 0.5, 2.0);
		};
		
		double start = 0.0;		// Seconds
		double end = 8;		// Seconds
		int steps = 100;
		double dt = (end - start) / steps;
		
		LineGraphDataSet setAcceleration = new LineGraphDataSet("Acceleration", Color.RED);
		LineGraphDataSet setSpeed = new LineGraphDataSet("Speed", Color.GREEN);
		LineGraphDataSet setDistance = new LineGraphDataSet("Distance", Color.BLUE);
		
		double speed = 0.0;
		double dist = 0.0;
		
		for(int i = 0; i < steps; i++) {
			double t0 = start + dt * i;
			double t1 = t0 + dt;
			
			double a = aFunc.apply(t0);
			speed += a * dt;
			dist  += speed * dt;
			
			setAcceleration.addPoint(t0, a);
			setSpeed.addPoint(t0, speed);
			setDistance.addPoint(t0, dist);
			
		}
		
		
		new LineGraphViewer("Test", List.of(setAcceleration, setDistance, setSpeed));
		
		
		while(true) {
			try {
				Thread.sleep(1000);
			}catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
