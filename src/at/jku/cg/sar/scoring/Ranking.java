package at.jku.cg.sar.scoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Ranking<RankData> implements Comparable<Ranking<RankData>> {

	private final List<RankData> raw;
	private final Function<RankData, Double> scoreFunction;
	private final boolean rankLowestFirst;
	
	private final List<RankData> sorted;
	private final List<List<RankData>> ranked;
	private final int lastRank;
	
	public Ranking(List<RankData> input, Function<RankData, Double> scoreFunction) {
		this(input, scoreFunction, false);
	}
	
	public Ranking(List<RankData> input, Function<RankData, Double> scoreFunction, boolean rankLowestFirst) {
		this.raw = Collections.unmodifiableList(input);
		this.scoreFunction = scoreFunction;
		this.rankLowestFirst = rankLowestFirst;
		
		List<RankData> sortedWIP = new ArrayList<>(input);
		sortedWIP.sort((d0, d1) -> {
			double score0 = scoreFunction.apply(d0);
			double score1 = scoreFunction.apply(d1);
			if(rankLowestFirst)	return Double.compare(score0, score1);
			else				return Double.compare(score1, score0);
		});
		this.sorted = Collections.unmodifiableList(sortedWIP);
		
		this.ranked = new ArrayList<>();
		
		Double currentScore = Double.NaN;
		int currentRank = 0;
		
		for(RankData d : sorted) {
			double score = scoreFunction.apply(d);
			
			if(score == currentScore) {
				ranked.get(currentRank-1).add(d);
			}else {
				currentRank++;
				currentScore = score;
				
				List<RankData> rank = new ArrayList<>();
				rank.add(d);
				ranked.add(rank);
			}
		}
		lastRank = currentRank;
	}
	
	public int getRank(RankData data) {
		for(int rank = 1; rank <= getLastRank(); rank++) {
			if(getForRank(rank).contains(data)) return rank;			
		}
		throw new IllegalStateException();
	}

	public boolean hasRank(int rank, RankData data) {
		return ranked.get(rank-1).contains(data);
	}
	
	public boolean isFirst(RankData data) {
		return hasRank(1, data);
	}
	
	public boolean isLast(RankData data) {
		return hasRank(lastRank, data);
	}
	
	public int getLastRank() {
		return lastRank;
	}
	
	public List<RankData> getForRank(int rank){
		return Collections.unmodifiableList(ranked.get(rank-1));
	}
	
	public List<RankData> getRaw(){
		return raw;
	}
	
	public List<RankData> getSorted(){
		return sorted;
	}
	
	public void print() {
		print(rd -> "N/A");
	}
	
	public void print(Function<RankData, String> label) {
		for(int r = 1; r <= lastRank; r++) {
			System.err.println("RANK %02d".formatted(r));
			for(RankData data : getForRank(r)) {
				System.err.println("\t%s:\t%s".formatted(label.apply(data), scoreFunction.apply(data)));
			}
		}
	}

	@Override
	public int compareTo(Ranking<RankData> o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
