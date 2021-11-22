package at.jku.cg.sar.scoring;

public class ScoreCriteria {

	final String name;
	final double weight;
	final ScoreType type;
	
	public ScoreCriteria(String name) {
		this(name, ScoreType.HIGHER_BETTER);
	}
	
	public ScoreCriteria(String name, ScoreType type) {
		this(name, 1.0, type);
	}
	
	public ScoreCriteria(String name, double weight, ScoreType type) {
		this.name = name;
		this.weight = weight;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public double getWeight() {
		return weight;
	}

	public ScoreType getType() {
		return type;
	}	
}
