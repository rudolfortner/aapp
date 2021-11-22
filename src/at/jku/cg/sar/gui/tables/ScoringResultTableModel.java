package at.jku.cg.sar.gui.tables;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

import at.jku.cg.sar.scoring.ScoreCriteria;
import at.jku.cg.sar.scoring.ScoreItem;

public class ScoringResultTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -4885467593178366474L;
	
	private final List<ScoreItem> items;
	private final List<ScoreCriteria> criterias;
	
	public ScoringResultTableModel(List<ScoreItem> items) {
		this.items = items.stream().sorted((i0, i1) -> Double.compare(i1.getScore(), i0.getScore())).collect(Collectors.toUnmodifiableList());
		Set<ScoreCriteria> cs = items.stream().map(i -> i.getCriterias()).flatMap(c -> c.stream()).collect(Collectors.toSet());
		this.criterias = cs.stream().sorted((c0, c1) -> c0.getName().compareTo(c1.getName())).collect(Collectors.toUnmodifiableList());
	}
	
	public ScoreItem get(int index) {
		return items.get(index);
	}

	@Override
	public int getRowCount() {
		return items.size();
	}

	@Override
	public int getColumnCount() {
		return 1 + criterias.size() + 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ScoreItem item = items.get(rowIndex);
		
		if(columnIndex == 0) {
			return item.getName();
		}else if(columnIndex == 1+ criterias.size()) {
			return item.getScore();
		}else {
			// CRITERIAS
			ScoreCriteria criteria = criterias.get(columnIndex-1);
			String valueString = "%5.4f".formatted(item.getValue(criteria));
			String scoreString = "%5.4f".formatted(item.getSubScore(criteria));
			return prepareString(valueString, scoreString);
		}
	}
	
	

	@Override
	public String getColumnName(int column) {
		if(column == 0) {
			return "Path Finder";
		}else if(column == 1+ criterias.size()) {
			return "Score";
		}else {
			// CRITERIAS
			return splitMinus(criterias.get(column-1).getName());
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if(columnIndex == 0) {
			return String.class;
		}else if(columnIndex == 1+ criterias.size()) {
			return Double.class;
		}else {
			// CRITERIAS
			return String.class;
		}
	}
	
	private String splitMinus(String input) {
		return prepareString(input.split(" - "));
	}
	
	private String prepareString(String...lines) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html>");
		builder.append("<center>");
		
		for(String s : lines) {
			builder.append(s);
			builder.append("<br />");
		}

		builder.append("</center>");
		builder.append("</html>");
		return builder.toString();
	}	
}
