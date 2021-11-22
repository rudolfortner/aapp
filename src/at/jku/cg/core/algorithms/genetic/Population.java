package at.jku.cg.core.algorithms.genetic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Population<ChromosomeType extends Chromosome> {
	
	protected int populationSize;
	protected List<ChromosomeType> chromosomes;
	
	public Population() {
		this.chromosomes = new ArrayList<>();
	}	
	
	public abstract ChromosomeType createChromosome(Random random);
	public abstract ChromosomeType crossover(Random random, ChromosomeType mum, ChromosomeType dad);
	
	/**
	 * @return true if some end condition is met
	 */
	public boolean finished() {
		return false;
	}
	
	public final void initialize(Random random, int populationSize) {
		this.populationSize = populationSize;
		this.chromosomes = new ArrayList<>(populationSize);
		for(int i = 0; i < populationSize; i++) {
			chromosomes.add(createChromosome(random));
		}
	}
	
	public final void crossover(Random random, SelectionType selectionType) {
		List<ChromosomeType> nextGeneration = new ArrayList<>(populationSize);
		
		for(int i = 0; i < populationSize; i++) {
			
			ChromosomeType mum = select(random, selectionType);
			ChromosomeType dad = select(random, selectionType);
			
			ChromosomeType child = crossover(random, mum, dad);
			
			nextGeneration.add(child);
		}
		
		this.chromosomes = nextGeneration;
	}
	
	public final void mutate(Random random, double mutationProbability) {
		for(ChromosomeType c : chromosomes) {
			double prob = random.nextDouble();
			if(prob <= mutationProbability) c.mutate(random);
		}
	}	
	
	public final ChromosomeType select(Random random, SelectionType type) {
		switch (type) {
			case ROULETTE:	return selectRoulette(random);
			default:		throw new IllegalArgumentException("Unexpected value: " + type);
		}
	}
	
	public final ChromosomeType selectRoulette(Random random) {
		double sumFitness = chromosomes.stream().mapToDouble(c -> c.getFitness()).sum();
		double threshold = sumFitness * random.nextDouble();
		
		double sum = 0.0;
		for(ChromosomeType c : chromosomes) {
			sum += c.getFitness();
			if(sum >= threshold) return c;
		}
		throw new IllegalStateException();
	}
	
	public final void calculateFitness() {
		for(ChromosomeType c : chromosomes) c.calculateFitness();
	}
	
	public final double getAverageFitness() {
		return chromosomes.stream()
				.mapToDouble(c -> c.getFitness())
				.average()
				.orElse(0.0);
	}
	
	public final ChromosomeType getFittest() {
		return chromosomes.stream()
				.min((c0, c1) -> Double.compare(c1.getFitness(), c0.getFitness()))
				.get();
	}

}
