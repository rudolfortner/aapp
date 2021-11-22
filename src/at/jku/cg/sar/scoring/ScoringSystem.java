package at.jku.cg.sar.scoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScoringSystem {

	private final NormalizationType type;
	
	public ScoringSystem() {
		this(NormalizationType.DIVIDE_LARGEST);
	}
	
	public ScoringSystem(NormalizationType type) {
		this.type = type;
	}
	
	public List<ScoreItem> process(List<ScoreItem> itemsRaw) {
		Set<ScoreCriteria> criterias = new HashSet<>();
		for(ScoreItem item : itemsRaw) {
			criterias.addAll(item.getCriterias());
		}
		
		List<ScoreItem> items = new ArrayList<>();
		// Check for items that do not have all criteria
		for(ScoreItem item : itemsRaw) {
			boolean valid = true;
			for(ScoreCriteria c : criterias) {
				if(!item.hasCriteria(c)) {
					valid = false;
					break;
				}
			}
			if(valid) items.add(item);
		}
		
		// Evaluate all criteria
		for(ScoreCriteria criteria : criterias) {			
			evaluateCriteria(criteria, items);
		}
		
		double totalWeight = (new ArrayList<>(criterias)).stream().mapToDouble(c -> c.weight).sum();
		
		for(ScoreItem item : items) {
			double score = 0;
			
			for(ScoreCriteria criteria : criterias) {
				score += criteria.weight * item.getSubScore(criteria);				
			}
			score = score / totalWeight;
			
			item.setScore(score);
		}
		
		return items;
	}
	
	public void evaluateCriteria(ScoreCriteria criteria, List<ScoreItem> items) {
		switch (type) {
			case NONE:
				evaluateCriteriaNone(criteria, items);
				break;
			case DIVIDE_LARGEST:
				evaluateCriteriaDivideLargest(criteria, items);
				break;
			case RANGE:
				evaluateCriteriaRange(criteria, items);
				break;
			case Z_SCORE:
				evaluateCriteriaZScore(criteria, items);
				break;
			case DIVIDE_TOTAL:
				evaluateCriteriaDivideTotal(criteria, items);
				break;
			default:
				throw new IllegalStateException("Unknown normalization type");
		}		
	}

	private void evaluateCriteriaNone(ScoreCriteria criteria, List<ScoreItem> items) {
		for(ScoreItem item : items) {
			double score = 0.0;
			score = item.getValue(criteria);
			item.addSubScore(criteria, score);
		}
	}
	
	private void evaluateCriteriaDivideLargest(ScoreCriteria criteria, List<ScoreItem> items) {
		double max = items.stream().mapToDouble(item -> item.getValue(criteria)).max().orElseThrow();
		
		for(ScoreItem item : items) {
			double score = 0.0;
			
			if(criteria.type == ScoreType.HIGHER_BETTER) {
				score = item.getValue(criteria) / max;
			}else if(criteria.type == ScoreType.LOWER__BETTER){
				score = 1.0 - item.getValue(criteria) / max;
			}
			
			item.addSubScore(criteria, score);
		}
	}
	
	private void evaluateCriteriaRange(ScoreCriteria criteria, List<ScoreItem> items) {
		double min = items.stream().mapToDouble(item -> item.getValue(criteria)).min().orElseThrow();
		double max = items.stream().mapToDouble(item -> item.getValue(criteria)).max().orElseThrow();
		
		for(ScoreItem item : items) {
			double score = 0.0;
			
			if(criteria.type == ScoreType.HIGHER_BETTER) {
				score = (item.getValue(criteria) - min) / (max - min);
			}else if(criteria.type == ScoreType.LOWER__BETTER){
				score = (max - item.getValue(criteria)) / (max - min);
			}
			
			item.addSubScore(criteria, score);
		}
	}
	
	private void evaluateCriteriaZScore(ScoreCriteria criteria, List<ScoreItem> items) {
		List<Double> values = items.stream().map(item -> item.getValue(criteria)).collect(Collectors.toUnmodifiableList());
		
		double mean = values.stream().mapToDouble(v -> v).average().orElseThrow();
		double variance = 0.0;
		for(Double value : values) {
			variance += Math.pow(value - mean, 2.0);
		}
		variance = variance / values.size();
		double sigma = Math.sqrt(variance);
		
		
		for(ScoreItem item : items) {
			double score = (item.getValue(criteria) - mean) / sigma;
			item.addSubScore(criteria, score);
		}
	}

	private void evaluateCriteriaDivideTotal(ScoreCriteria criteria, List<ScoreItem> items) {
		double total = items.stream().mapToDouble(item -> item.getValue(criteria)).sum();
		
		for(ScoreItem item : items) {
			double score = 0.0;
			
			if(criteria.type == ScoreType.HIGHER_BETTER) {
				score = item.getValue(criteria) / total;
			}else if(criteria.type == ScoreType.LOWER__BETTER){
				score = 1.0 - item.getValue(criteria) / total;
			}

			item.addSubScore(criteria, score);
		}
	}
}
