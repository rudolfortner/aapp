package at.jku.cg.core.algorithms.genetic;

import java.util.Random;

public abstract class Chromosome {

	protected double fitness;
	
	public Chromosome() {
		// TODO Auto-generated constructor stub
	}
	
	public abstract void calculateFitness();
	public abstract void mutate(Random random);
	
	
	
	
	public double getFitness() {
		return this.fitness;
	}
	
}
