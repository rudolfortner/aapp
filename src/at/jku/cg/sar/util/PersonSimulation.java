package at.jku.cg.sar.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.world.WorldPoint;

public class PersonSimulation {

	long seed;
	int width, height;
	double gridWidth;
	
	Grid<Double> terrain;
	Grid<Boolean> obstacles;
	
	public PersonSimulation() {
		seed = System.currentTimeMillis();
		width = height = 16;
		gridWidth = 30.0;
		
		terrain = new Grid<Double>(width, height, 0.0);
		obstacles = new Grid<Boolean>(width, height, false);
	}
	
	public PersonSimulation(int width, int height, double gridWidth) {
		this();
		this.width = width;
		this.height = height;
		this.gridWidth = gridWidth;
	}
	
	public PersonSimulation(Grid<Double> terrain, Grid<Boolean> obstacles, double gridWidth) {
		this();
		this.width = terrain.getWidth();
		this.height = terrain.getHeight();
		this.terrain = terrain.clone();
		this.obstacles = obstacles.clone();
		if(obstacles.getWidth() != width || obstacles.getHeight() != height) throw new IllegalArgumentException();
	}
	
	public Grid<Double> runSimple(double startX, double startY, int age) {
		Grid<Double> probs = new Grid<>(width, height, 0.0);
		int steps = 1000;
		double dt = 1.0 / steps;
		
		double speed = getPersonSpeed(age, 0.5);
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				double endX = (x + 0.5) * gridWidth;
				double endY = (y + 0.5) * gridWidth;
				
				double distance = Math.sqrt(Math.pow(endX-startX, 2.0) + Math.pow(endY-startY, 2.0));
				double distanceNeeded = 0.0;
				double previousAltitude = interpolate(terrain, startX/gridWidth-0.5, startY/gridWidth-0.5);
				
				for(double t = 0.0; t <= 1.0; t+=dt) {
					double cx = Interpolation.Lerp(t, startX, endX);
					double cy = Interpolation.Lerp(t, startY, endY);
					double cAltitude = interpolate(terrain, cx/gridWidth-0.5, cy/gridWidth-0.5);
					double partialDistance = distance * dt;
					
					double factorAltitude = Math.exp(Math.abs(cAltitude-previousAltitude) / 1000);
					double factorObstacle = obstacles.get((int) (cx / gridWidth - 0.5), (int) (cy / gridWidth - 0.5)) ? 2.0 : 1.0;
					
					
					distanceNeeded = partialDistance * factorAltitude * factorObstacle * Math.exp(t);
					
					previousAltitude = cAltitude;
				}
				
				double t = distanceNeeded / speed;
				probs.set(x, y, t);
			}
		}
		
		double max = probs.maxValue();
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				probs.set(x, y, 1.0 - probs.get(x, y) / max);
			}
		}
		
		return probs;
	}
	
	
	public Grid<Double> run(double startX, double startY, int age) {
		Grid<Integer> countMap = new Grid<Integer>(width, height, 0);
		
		List<Person> persons = new ArrayList<>();
		for(int i = 0; i < 1000; i++) {
			Person person = new Person(seed * i + i, startX, startY, age);
			persons.add(person);
		}
		
		double time = 0.0;
		double dt = 1.0;
		
		while(time < 3600.0 * 6) {			
			for(Person person : persons) {
				if(person.outside) continue;
				
				Random random = person.random;
				double walkDistance = dt * person.speed;
				
				double previousAltitude = interpolate(terrain, person.current.getX()/gridWidth-0.5, person.current.getY()/gridWidth-0.5);

				boolean valid = false;				
				double dx = 0, dy = 0;
				double nextX = 0, nextY = 0;
				int nx = 0, ny = 0;
				int tries = 0;
				while(!valid) {				
					// CHECK ENDLESS LOOP
					tries++;
					if(tries > 100) break;
					
					double walkDirection = 2.0 * Math.PI * random.nextDouble();					
					dx = walkDistance * Math.sin(walkDirection);
					dy = walkDistance * Math.cos(walkDirection);
					nextX = person.current.getX() + dx;
					nextY = person.current.getY() + dy;
					nx = (int) (nextX / gridWidth - 0.5);
					ny = (int) (nextY / gridWidth - 0.5);
//					System.err.println("%f %f ---> %f %f ---> %d %d".formatted(nextX, nextY,nextX / gridWidth - 0.5, nextY / gridWidth - 0.5, nx, ny));
					
					
					valid = true;
					// CHECK GRID BOUNDS
					if(nextX < 0 || nextX >= width*gridWidth || nextY < 0 || nextY >= height*gridWidth) {
						person.outside = true;
						person.walkTo(nextX, nextY);
						valid = false;
						break;
					}
					
					// CHECK OBSTACLES
					boolean obst = obstacles.get(nx, ny);
					if(obst) {
						valid = false;
						continue;
					}
					
//					// CHECK TERRAIN
					double currentAltitude = interpolate(terrain, nextX/gridWidth-0.5, nextY/gridWidth-0.5);
					double gradient = (currentAltitude-previousAltitude) / walkDistance;
					double gradientAbs = Math.abs(gradient);
					if(gradientAbs >= 3.0 && random.nextDouble() >= 0.1) {
						valid = false;
						continue;
					}
				}
				
				person.walk(dx, dy);
				if(!person.outside) countMap.set(nx, ny, countMap.get(nx, ny)+1);
			}
			time += dt;
		}
		
		int outsides = persons.stream().mapToInt(p -> p.outside ? 1 : 0).sum();
		System.err.println("%d persons outside".formatted(outsides));

		Grid<Double> probs = new Grid<Double>(width, height, 0.0);
		double max = countMap.maxValue();
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				probs.set(x, y, 1.0 * countMap.get(x, y) / max);
			}
		}
		return probs;
	}
	
	public final class Person {		
		Random random;
		WorldPoint current;
		double speed;
		boolean outside = false;
		
		public Person(long seed, double x, double y, int age) {
			this.random = new Random(seed);
			this.speed = getPersonSpeed(age, random.nextDouble());
			walkTo(x, y);
		}

		public void walkTo(double x, double y) {
			current = new WorldPoint(x, y);
		}
		
		public void walk(double dx, double dy) {
			walkTo(current.getX()+dx, current.getY()+dy);
		}
	}
	
	
	
	public static double getPersonSpeed(int age, double random) {
		if(20 <= age && age <= 29) {
			return Interpolation.Lerp(random, 1.34, 1.36);
		}else if(30 <= age && age <= 39) {
			return Interpolation.Lerp(random, 1.34, 1.43);
		}else if(40 <= age && age <= 49) {
			return Interpolation.Lerp(random, 1.39, 1.43);
		}else if(50 <= age && age <= 59) {
			return Interpolation.Lerp(random, 1.31, 1.43);
		}else if(60 <= age && age <= 69) {
			return Interpolation.Lerp(random, 1.24, 1.32);
		}else if(70 <= age && age <= 79) {
			return Interpolation.Lerp(random, 1.13, 1.26);
		}else if(80 <= age && age <= 89) {
			return Interpolation.Lerp(random, 0.94, 0.97);
		}
		throw new IllegalArgumentException("AGE NOT IN TABLE!");
	}
	
	public double interpolate(Grid<Double> grid, double x, double y) {
		int x0 = (int) Math.floor(x);
		int x1 = (int) Math.ceil(x);
		int y0 = (int) Math.floor(y);
		int y1 = (int) Math.ceil(y);

		x0 = Clamp.clamp(x0, 0, grid.getWidth()-1);
		y0 = Clamp.clamp(y0, 0, grid.getHeight()-1);
		x1 = Clamp.clamp(x1, 0, grid.getWidth()-1);
		y1 = Clamp.clamp(y1, 0, grid.getHeight()-1);
		
		double v00 = grid.get(x0, y0);
		double v01 = grid.get(x0, y1);
		double v10 = grid.get(x1, y0);
		double v11 = grid.get(x1, y1);
		
		return Interpolation.Bilinear(x, y, x0, y0, x1, y1, v00, v01, v10, v11);
	}
	
	public void setSeed(long seed) {
		this.seed = seed;
	}
}
