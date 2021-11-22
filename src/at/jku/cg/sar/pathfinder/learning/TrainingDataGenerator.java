package at.jku.cg.sar.pathfinder.learning;

import java.io.File;
import java.util.Random;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridIO;
import at.jku.cg.sar.gui.ProgressBarWindow;
import at.jku.cg.sar.world.WorldGenerator;

public class TrainingDataGenerator {

	private final File outputDirectory;	
	private final int mapWidth, mapHeight;
	private final double minProb, maxProb;

	public TrainingDataGenerator(File outputDirectory, int mapWidth, int mapHeight) {
		this(outputDirectory, mapWidth, mapHeight, 0.0, 1.0);
	}
	
	public TrainingDataGenerator(File outputDirectory, int mapWidth, int mapHeight, double minProb, double maxProb) {
		this.outputDirectory = outputDirectory;
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.minProb = minProb;
		this.maxProb = maxProb;
	}

	
	public void generateBatch(int count) {
		ProgressBarWindow bar = new ProgressBarWindow("Generating Training Data");
		for(int i = 1; i <= count; i++) {
			int percent = (i-1) * 100 / count;
			bar.setProgress(percent);
			System.err.println("Generating training data with seed %04d".formatted(i));
			
			generate(i, i * 1997 + System.nanoTime());
		}
		bar.close();
	}
	
	
	private void generate(int index, long seed) {
		Random random = new Random(seed);
		WorldGenerator generator = new WorldGenerator(seed);
		
		double prob = WorldGenerator.randRange(random, minProb, maxProb);
		Grid<Double> gridUniform = generator.Uniform(mapWidth, mapHeight, prob);
		export("uniform", index, gridUniform);
		
		
		Grid<Double> gridSmoothedSpots = generator.SmoothedSpots(mapWidth, mapHeight, random.nextDouble(), minProb, maxProb);
		export("smoothed_spots", index, gridSmoothedSpots);
		
		
		Grid<Double> gridPatches = generator.Patches(mapWidth, mapHeight,
				random.nextInt(5)+1, random.nextInt(4)+1, minProb, maxProb, true);
		export("patches", index, gridPatches);
		
		
		Grid<Double> gridGaussian = generator.GaussianPositioned(mapWidth, mapHeight,
				random.nextDouble() * mapWidth, random.nextDouble() * mapHeight,
				random.nextDouble() * 4.0 + 1.0, minProb, maxProb);
		export("gaussian", index, gridGaussian);
		
		
		double startX = random.nextDouble() * mapWidth * 30.0;
		double startY = random.nextDouble() * mapHeight * 30.0;
		Grid<Double> gridSimulated = generator.Simulated(mapWidth, mapHeight, 30.0,
				startX, startY, random.nextDouble(), random.nextDouble(),
				20 + random.nextInt(70), minProb, maxProb);
		export("simulated", index, gridSimulated);
		
	}
	
	
	private void export(String name, int index, Grid<Double> grid) {
		String filename = "%s_%04d.png".formatted(name, index);
		File file = new File(outputDirectory, filename);		
		GridIO.toImageFile16(file, grid);
	}
	
	
	
}
