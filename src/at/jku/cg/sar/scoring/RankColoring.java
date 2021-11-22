package at.jku.cg.sar.scoring;

import java.awt.Color;
import java.util.List;

public enum RankColoring {
	NONE,
	FIRST,
	LAST,
	FIRST_LAST,
	FIRST_SECOND_LAST;
	
	
	
	
	public <T> Color getColor(Ranking<T> ranking, T data) {
		if(ranking.isFirst(data) && List.of(FIRST, FIRST_LAST, FIRST_SECOND_LAST).contains(this)) {
			return Color.GREEN;
		}else if(ranking.hasRank(2, data) && List.of(FIRST_SECOND_LAST).contains(this)) {
			return Color.ORANGE;
		}else if(ranking.isLast(data) && List.of(LAST, FIRST_LAST, FIRST_SECOND_LAST).contains(this)) {
			return Color.RED;
		}
		return null;
	}
	
}
