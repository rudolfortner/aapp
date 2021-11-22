package at.jku.cg.core.algorithms.genetic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import at.jku.cg.sar.gui.graphics.LineGraphDataSet;

public class GeneticAlgorithm {

	private final int epochs, populationSize;
	private final double mutationRate;
	private final SelectionType selectionType = SelectionType.ROULETTE;

	private final Random random;
	
	public GeneticAlgorithm() {
		this(1000, 100, 0.01);
	}

	public GeneticAlgorithm(int epochs, int populationSize, double mutationRate) {
		super();
		this.epochs = epochs;
		this.populationSize = populationSize;
		this.mutationRate = mutationRate;
		
		this.random = new Random(1997);
	}
	

	public <ChromosomeType extends Chromosome> ChromosomeType run(Population<ChromosomeType> population) {
		return run(population, new ArrayList<>());
	}

	public <ChromosomeType extends Chromosome> ChromosomeType run(Population<ChromosomeType> population, List<LineGraphDataSet> data) {
		// Graphing
		LineGraphDataSet dataAverageFitness = new LineGraphDataSet("Average Fitness", Color.ORANGE);
		LineGraphDataSet dataBestFitness = new LineGraphDataSet("Best Fitness", Color.RED);
		data.add(dataAverageFitness);
		data.add(dataBestFitness);
		
		// 1. Random Solutions
		population.initialize(random, populationSize);
		
		// Fitness calculation of intial population
		population.calculateFitness();
		
		for(int epoch = 1; epoch <= epochs; epoch++) {
			if(Thread.interrupted()) return population.getFittest();
	
			
			// Crossover
			population.crossover(random, selectionType);
			
			// Mutation
			population.mutate(random, mutationRate);			
			
			// Fitness calculation of new population
			population.calculateFitness();
			
			System.out.println("Genetic Algorithm -- Epoch %04d -- Average Fitness %f -- Best Fitness %f".formatted(epoch, population.getAverageFitness(), population.getFittest().getFitness()));
			dataAverageFitness.addPoint(epoch, population.getAverageFitness());
			dataBestFitness.addPoint(epoch, population.getFittest().getFitness());
			
			if(population.finished()) break;
		}
		
		return population.getFittest();
	}

}
