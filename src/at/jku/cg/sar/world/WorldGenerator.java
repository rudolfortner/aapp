package at.jku.cg.sar.world;

import java.util.Random;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.util.Interpolation;
import at.jku.cg.sar.util.PersonSimulation;

public class WorldGenerator {

	// Seed used for random values
	private final long seed;
	
	public WorldGenerator(long seed) {
		this.seed = seed;
	}
	
	public Grid<Double> Empty(int width, int height) {
		return new Grid<Double>(width, height, 0.0);
	}
	
	public Grid<Double> Uniform(int width, int height, double value) {
		Grid<Double> map = new Grid<>(width, height, 0.0);
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				map.set(x, y, value);
			}
		}
		
		return map;
	}
	

	public Grid<Double> Spots(int width, int height, double spots) {
		return Spots(width, height, spots, 0.0, 1.0);
	}
	
	public Grid<Double> Spots(int width, int height, double spots, double min, double max) {
		if(spots < 0.0 || spots > 1.0) throw new IllegalArgumentException();
		Random random = new Random(seed);
		
		Grid<Double> map = new Grid<>(width, height, min);
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				double prob = random.nextDouble();
				if(prob <= spots) map.set(x, y, randRange(random, min, max));
			}
		}
		
		return map;
	}
	

	public Grid<Double> SmoothedSpots(int width, int height, double spots) {
		return SmoothedSpots(width, height, spots, 0.0, 1.0);
	}
	
	public Grid<Double> SmoothedSpots(int width, int height, double spots, double min, double max) {
		
		Grid<Double> dots = Spots(width, height, spots, min, max);
		
		double[][] smoothed = new double[width][height];
		
		int[][] filter = new int[][]{{1, 2, 1},
									 {2, 4, 2},
									 {1, 2, 1}};
		int filterSum = 16;
										
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				
				double sum = 0.0;
				for(int dx = -1; dx <= 1; dx++) {
					for(int dy = -1; dy <= 1; dy++) {
						int sampleX = x + dx;
						int sampleY = y + dy;
						if(sampleX < 0 || sampleX >= width || sampleY < 0 || sampleY >= height) continue;
						
						double prob = dots.get(sampleX, sampleY);
						sum += prob * filter[dx+1][dy+1];
					}
				}
				smoothed[x][y] = sum / filterSum;
			}
		}	
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				dots.set(x, y, smoothed[x][y]);
			}
		}
		
		return dots;
	}
	
	public Grid<Double> SmoothedSpotsNormalized(int width, int height, double spots, double min, double max) {
		
		Grid<Double> grid = SmoothedSpots(width, height, spots);
		
		double maxVal = grid.maxValue();
		for(int x = 0; x < grid.getWidth(); x++) {
			for(int y = 0; y < grid.getHeight(); y++) {
				double norm = grid.get(x, y) / maxVal;
				double prob = norm * (max - min) + min;
				grid.set(x, y, prob);
			}
		}
		
		return grid;
	}
	

	public Grid<Double> Patches(int width, int height, int radius, int count, double min, double max){
		return Patches(width, height, radius, count, min, max, false);
	}
	
	public Grid<Double> Patches(int width, int height, int radius, int count, double min, double max, boolean randomSize){
		Random random = new Random(seed);
		
		Grid<Double> map = new Grid<>(width, height, min);
		
		for(int i = 0; i < count; i++) {
			int centerX = random.nextInt(width);
			int centerY = random.nextInt(height);
			
			int rad = radius;
			if(randomSize) rad = 1 + random.nextInt(radius);
			
			for(int dx = -rad; dx <= rad; dx++) {
				for(int dy = -rad; dy <= rad; dy++) {
					int x = centerX + dx;
					int y = centerY + dy;
					if(x < 0 || x >= width)		continue;
					if(y < 0 || y >= height)	continue;
					if(Math.sqrt(Math.pow(centerX - x, 2.0) + Math.pow(centerY - y, 2.0)) + randRange(random, -0.5, 0.5) > radius) continue;
					
					double prob = randRange(random, min, max);
					map.set(x, y, prob);
				}
			}
		}
		
		return map;
	}
	
	public Grid<Double> Gaussian(int width, int height, double sigma, double min, double max){
		// TODO cx and cy as double ?
		double cx = width / 2.0 - 0.5;
		double cy = height / 2.0 - 0.5;
		return GaussianPositioned(width, height, cx, cy, sigma, min, max);
	}
	
	public Grid<Double> GaussianPositioned(int width, int height, double cx, double cy, double sigma, double min, double max){
		// TODO cx and cy as double ?
		Grid<Double> map = new Grid<>(width, height, min);

		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				double power = -(Math.pow(x - cx, 2.0) / (2 * sigma*sigma) + Math.pow(y - cy, 2.0) / (2 * sigma*sigma));
				double gauss = Math.exp(power);
				double value = gauss * (max - min) + min;
				map.set(x, y, value);
			}
		}
		
		return map;
	}

	public Grid<Double> Noise(int width, int height) {
		return Noise(width, height, 0.0, 1.0);
	}
	
	public Grid<Double> Noise(int width, int height, double min, double max) {
		Random random = new Random(seed);
		
		Grid<Double> map = new Grid<>(width, height);
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				map.set(x, y, randRange(random, min, max));
			}
		}
		
		return map;
	}
	
	public Grid<Double> Simulated(int width, int height, double gridWidth,
			double startX, double startY, double obstacles, double hills,
			int age, double min, double max) {
		// Generate random obstacle map
		Grid<Boolean> obstacleMap = new Grid<>(width, height, false);
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				obstacleMap.set(x, y, (new Random()).nextDouble() < obstacles);
			}
		}
		
		// Generate random terrain map
		Grid<Double> terrainMap = SmoothedSpots(width, height, hills);
		terrainMap.modify(value -> 100.0 * value);
		
		// Run Simulation
		PersonSimulation s = new PersonSimulation(terrainMap, obstacleMap, gridWidth);
		s.setSeed(seed);
		Grid<Double> simGrid = s.run(startX, startY, age);
		
		// Scale values to fit between min and max
		double minValue = simGrid.minValue();
		double maxValue = simGrid.maxValue();		
		simGrid.modify(value -> Interpolation.Linear(value, minValue, min, maxValue, max));
				
		return simGrid;
	}

	public World Mixed(int width, int height) {
		throw new UnsupportedOperationException("NOT IMPLEMENTED YET!");
	}
	
	public static double randRange(Random random, double min, double max) {
		if(random == null) throw new NullPointerException();
		return random.nextDouble() * (max - min) + min;
	}

	public static Grid<Double> makeCorrect(Grid<Double> input){
		double sum = input.getValues().stream().mapToDouble(g -> g.getValue()).sum();
		
		Grid<Double> grid = new Grid<Double>(input.getWidth(), input.getHeight(), 0.0);
		for(int x = 0; x < grid.getWidth(); x++) {
			for(int y = 0; y < grid.getHeight(); y++) {
				double value = input.get(x, y) / sum;
				grid.set(x, y, value);
			}
		}
		return grid;
	}
}
