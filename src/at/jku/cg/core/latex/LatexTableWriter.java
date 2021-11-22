package at.jku.cg.core.latex;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class LatexTableWriter {

	private final List<LatexTableRow> rows;
	
	private String caption = null;
	private String label   = null;
	
	private String leftCol = null;
	private boolean sortColumns = true;
	
	public LatexTableWriter() {
		this.rows = new ArrayList<>();
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setLeftColumn(String left) {
		this.leftCol = left;
	}
	
	public void addRow(LatexTableRow row) {
		this.rows.add(row);
	}
	
	public void setSortColumns(boolean sortColumns) {
		this.sortColumns = sortColumns;
	}
	
	
	/*
	 * \definecolor{maroon}{rgb}{0.0, 1.0, 0.5}
	 * 
	 * \label{tab:commands}
		\begin{tabular}{|c|c|l|}
			\hline
			Command &A Number & Comments\\
			\hline
			asas & \cellcolor{blue} 100& Author \\
			qdwqdq  & 300 & For tables\\
			qwe qwe& 400& For wider tables\\
			\hline
		\end{tabular}
	 */
	
	public List<String> getColumnNames(){
		Set<String> columnSet = new HashSet<>();
		for(LatexTableRow row : rows) {
			columnSet.addAll(row.rowEntries.keySet());
		}
		List<String> columnNames = new ArrayList<>(columnSet);
		
		columnNames.sort((s0, s1) -> {
			if(leftCol != null) {
				if(s0.equals(leftCol)) return -1;
				if(s1.equals(leftCol)) return  1;				
			}
			if(sortColumns) return s0.compareTo(s1);
			return 0;
		});
		
		return columnNames;
	}
	
	public List<Color> getColors(){
		Set<Color> colors = new HashSet<>();
		
		for(LatexTableRow row : rows) {
			for(LatexTableEntry entry : row.rowEntries.values()) {
				if(entry.color != null) colors.add(entry.color);
			}
		}
		
		return new ArrayList<>(colors);
	}
	
	public void export(File file) {

		List<Color> colors = getColors();
		Map<Color, String> colorMapping = new HashMap<>();
		List<String> columnNames = getColumnNames();
		int colCount = columnNames.size();
		
		StringBuilder builder = new StringBuilder();
		
		// COLOR DEFINITIONS
		for(Color color : colors) {
			// COLOR NAME
			String hex = String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
			String colName = "col" + hex;
			
			// COLOR CONVERSION to 0.0 - 1.0 range
			double r = color.getRed() / 255.0;
			double g = color.getGreen() / 255.0;
			double b = color.getBlue() / 255.0;
			
			// COLOR DEFINITION
			// \definecolor{maroon}{rgb}{0.0, 1.0, 0.5}
			builder.append("\\definecolor{").append(colName).append("}{rgb}");
			builder.append("{").append(String.format(Locale.ROOT, "%f, %f, %f", r, g, b)).append("}").append(System.lineSeparator());
			
			// COLOR MAPPING
			colorMapping.put(color, colName);
		}
		
		// LABEL AND CAPTION
		if(caption != null) builder.append("\\caption{").append(caption).append("}").append(System.lineSeparator());
		if(label   != null) builder.append("\\label{").append(label).append("}").append(System.lineSeparator());
		
		// BEGIN TABULAR
		builder.append("\\begin{tabularx}{\\linewidth}{|");
		for(int c = 0; c < colCount; c++) builder.append("X|");
		builder.append("}").append(System.lineSeparator());
		
		// HEADER
		builder.append("\t").append("\\hline").append(System.lineSeparator());
		builder.append("\t");
		for(int c = 0; c < colCount; c++) {
			String columnName = makeValid(columnNames.get(c));
			builder.append(columnName);
			if(c < colCount-1) {
				builder.append(" & ");
			}else {
				builder.append(" \\\\");
			}
		}
		builder.append(System.lineSeparator());
		builder.append("\t").append("\\hline").append(System.lineSeparator());
		
		// ROWS
		for(LatexTableRow row : rows) {
			builder.append("\t");
			
			for(int c = 0; c < colCount; c++) {
				LatexTableEntry entry = row.rowEntries.getOrDefault(columnNames.get(c), new LatexTableEntry());
				
				// CELL ALIGN RIGHT
				if(leftCol != null && c > 0) builder.append("\\hfill ");
				
				// CELL COLOR
				if(entry.color != null) {
					String colorName = colorMapping.get(entry.color);
					builder.append("\\cellcolor{").append(colorName).append("} ");
				}
				// CELL VALUE
				String value = makeValid(entry.value);
				builder.append(value);
				if(c < colCount-1) {
					builder.append(" & ");
				}else {
					builder.append(" \\\\");
				}
			}

			builder.append(System.lineSeparator());
		}

		builder.append("\t").append("\\hline").append(System.lineSeparator());
		
		builder.append("\\end{tabularx}");
		
		
		try {
			file.getParentFile().mkdirs();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(builder.toString());
			writer.flush();
			writer.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static String makeValid(String string) {
		String result = string.replace("%", "\\%");
		return result;
	}
	
	public static class LatexTableRow {
		
		private Map<String, LatexTableEntry> rowEntries;

		public LatexTableRow() {
			super();
			this.rowEntries = new HashMap<>();
		}
		
		public void addEntry(String column, String value) {
			this.rowEntries.put(column, new LatexTableEntry(value));
		}
		
		public void addEntry(String column, LatexTableEntry entry) {
			this.rowEntries.put(column, entry);
		}
	}
	
	public static class LatexTableEntry {
		
		private final String value;
		private final Color color;
		
		public LatexTableEntry() {
			this(new String());
		}
		
		public LatexTableEntry(String value) {
			this(value, null);
		}
		
		public LatexTableEntry(String value, Color color) {
			super();
			if(value == null) throw new NullPointerException();
			this.value = value;
			this.color = color;
		}
	}
	
}
