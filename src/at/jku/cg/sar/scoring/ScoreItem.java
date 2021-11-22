package at.jku.cg.sar.scoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreItem {

	private final String name;
	private final Object pointer;

	private final Map<ScoreCriteria, Double> values;
	private final Map<ScoreCriteria, Double> subScores;
	
	private double score;
	
	public ScoreItem(String name) {
		this(name, null);
	}
	
	public ScoreItem(String name, Object pointer) {
		this.name = name;
		this.pointer = null;
		this.values = new HashMap<>();
		this.subScores = new HashMap<>();
	}
	
	void addSubScore(ScoreCriteria criteria, double value) {
		subScores.put(criteria, value);
	}

	public void addValue(ScoreCriteria criteria, double value) {
		values.put(criteria, value);
	}
	
	public boolean hasCriteria(ScoreCriteria criteria) {
		return values.keySet().contains(criteria);
	}
	
	public List<ScoreCriteria> getCriterias(){
		return new ArrayList<>(values.keySet());
	}
	
	public double getValue(ScoreCriteria criteria) {
		return values.get(criteria);
	}
	
	public double getSubScore(ScoreCriteria criteria) {
		return subScores.get(criteria);
	}

	public String getName() {
		return name;
	}

	public Object getPointer() {
		return pointer;
	}

	public double getScore() {
		return score;
	}

	void setScore(double score) {
		this.score = score;
	}
}
