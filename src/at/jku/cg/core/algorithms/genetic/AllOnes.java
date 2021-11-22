package at.jku.cg.core.algorithms.genetic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import at.jku.cg.sar.gui.LineGraphViewer;
import at.jku.cg.sar.gui.graphics.LineGraphDataSet;

public class AllOnes {
	
	private final int length;
		
	public AllOnes(int length) {
		this.length = length;
	}

	public void run() {
		List<LineGraphDataSet> graph = new ArrayList<>();
		
		GeneticAlgorithm ga = new GeneticAlgorithm(10000, 1000, 0.01);
		BitChromosome result = ga.run(new BitPopulation(), graph);
		
		for(int i = 0; i < length; i++) {
			System.err.print(result.data[i]);
		}
		System.err.print("\n");
		

		new LineGraphViewer(graph);
	}
	
	
	private final class BitPopulation extends Population<BitChromosome> {

		@Override
		public boolean finished() {
			BitChromosome c = getFittest();
			
			for(int i = 0; i < length; i++) {
				if(c.data[i] != 1) return false;
			}
			
			return true;
		}

		@Override
		public BitChromosome createChromosome(Random random) {
			BitChromosome c = new BitChromosome(length);
			for(int i = 0; i < length; i++) {
				c.data[i] = random.nextBoolean() ? 1 : 0;
			}
			return c;
		}

		@Override
		public BitChromosome crossover(Random random, BitChromosome mum, BitChromosome dad) {
			BitChromosome child = new BitChromosome(length);
			int splitPoint = random.nextInt(length);
			
			for(int s = 0; s < splitPoint; s++) {
				child.data[s] = dad.data[s];
			}
			for(int s = splitPoint; s < length; s++) {
				child.data[s] = mum.data[s];
			}
			return child;
		}
		
	}
	
	private final class BitChromosome extends Chromosome {

		int[] data;
		
		public BitChromosome(int len) {
			this.data = new int[len];
		}
		
		@Override
		public void calculateFitness() {
			fitness = 0;
			for(int i = 0; i < length; i++) {
				if(data[i] == 1) fitness++;
			}
		}

		@Override
		public void mutate(Random random) {
			int pos = random.nextInt(length);
			data[pos] = data[pos] == 1 ? 0 : 1;
		}		
	}
	
}
