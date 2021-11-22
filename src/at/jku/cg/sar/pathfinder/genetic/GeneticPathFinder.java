package at.jku.cg.sar.pathfinder.genetic;

import java.nio.channels.IllegalSelectorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import at.jku.cg.core.algorithms.genetic.Chromosome;
import at.jku.cg.core.algorithms.genetic.GeneticAlgorithm;
import at.jku.cg.core.algorithms.genetic.Population;
import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.gui.LineGraphViewer;
import at.jku.cg.sar.gui.graphics.LineGraphDataSet;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.FlightLeg;
import at.jku.cg.sar.trajectory.SimpleTrajectory;

public class GeneticPathFinder extends PathFinder {

	enum CrossOverType {
		ORDER_CROSSOVER;
	}
	
	private List<PathGene> unvisited;
	private Grid<Double> map;
	private final CrossOverType crossOverType = CrossOverType.ORDER_CROSSOVER;
	
	public GeneticPathFinder() {
		super(PathFinderType.DISCRETE_FULL);
	}
	
	@Override
	public List<PathFinderResult> solveFull(DroneDiscrete drone) {
		map = drone.getProbabilities().clone();
		unvisited = drone.getProbabilities()
				.getValues()
				.stream()
				.filter(gv -> !drone.isVisited(gv.getX(), gv.getY()))
				.map(gv -> new PathGene(gv.getX(), gv.getY(), gv.getValue()))
				.collect(Collectors.toList());

		GeneticAlgorithm ga = new GeneticAlgorithm(1000, 1000, 0.01);
		
		PathPopulation population = new PathPopulation();
		List<LineGraphDataSet> graph = new ArrayList<>();
		PathChromosome result = ga.run(population, graph);
		new LineGraphViewer(graph);
		
		List<PathFinderResult> path = new ArrayList<>();
		
		for(PathGene gene : result.genes) {
			path.add(new PathFinderResult(gene.x, gene.y, false));
		}
		
		return path;
	}

	@Override
	public String getName() {
		return "Genetic Path Finder";
	}
	
	private final class PathPopulation extends Population<PathChromosome> {

		@Override
		public PathChromosome createChromosome(Random random) {
			Collections.shuffle(unvisited);
			
			PathChromosome c = new PathChromosome();
			c.genes = new ArrayList<>(unvisited);
			return c;
		}

		@Override
		public PathChromosome crossover(Random random, PathChromosome mum, PathChromosome dad) {
			switch (crossOverType) {
				case ORDER_CROSSOVER:	return order(random, mum, dad);
				default:				throw new IllegalSelectorException();
			}
		}
		
		public PathChromosome order(Random random, PathChromosome mum, PathChromosome dad) {
			PathChromosome child = new PathChromosome();
			
			int length = mum.genes.size();
			int left   = random.nextInt(length);
			int right  = random.nextInt(length);
			while(left >= right) {
				left   = random.nextInt(length);
				right  = random.nextInt(length);				
			}
		
			List<PathGene> genes = new ArrayList<>(length);
			for(int i = 0; i < length; i++) genes.add(null);
			
			for(int i = left; i <= right; i++) {
				genes.set(i, mum.genes.get(i));
			}
			
			
			int insertPosition = right + 1;
			for(int i = 0; i < length; i++) {
				int lookUpPosition = right + i + 1;
				
				PathGene gene = dad.genes.get(lookUpPosition % length);
				if(genes.contains(gene)) continue;
				
				genes.set(insertPosition++ % length, gene);				
			}
			
			if(genes.contains(null)) throw new IllegalSelectorException();
			child.genes = genes;
			return child;
		}
	}
	
	private final class PathChromosome extends Chromosome {

		List<PathGene> genes = new ArrayList<>();
		
		@Override
		public void calculateFitness() {
			SimpleTrajectory planner = new SimpleTrajectory(settings);
			double accumProbs = 0.0;
			double accumTime  = 0.0;
			double accumArea  = 0.0;
			
			for(PathGene next : genes) {
				for(FlightLeg<?> leg : planner.next(new PathFinderResult(next.x, next.y, false))) {
					double dt = leg.getDuration();
					double oldProbs = accumProbs;
					
					accumTime += dt;
					if(leg.isScan()) {
						accumProbs += map.get(leg.getScanX(), leg.getScanY());
					}
					accumArea += (oldProbs + accumProbs) * dt / 2.0;
				}
			}
			fitness = 1.0 / (accumProbs*accumTime - accumArea);
			fitness = 10000.0 / accumTime;
		}

		@Override
		public void mutate(Random random) {
			int swap0 = random.nextInt(genes.size());
			int swap1 = random.nextInt(genes.size());
			Collections.swap(genes, swap0, swap1);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			
			for(PathGene gene : genes) {
				builder.append("(%d/%d)".formatted(gene.x, gene.y));
			}
				
			return builder.toString();
		}
		
		
	}
	
	private final class PathGene {
		final int x, y;
		@SuppressWarnings("unused")
		final double prob;
		
		public PathGene(int x, int y, double prob) {
			this.x = x;
			this.y = y;
			this.prob = prob;
		}
	}

}
