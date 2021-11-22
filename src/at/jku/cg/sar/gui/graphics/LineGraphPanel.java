package at.jku.cg.sar.gui.graphics;

import java.util.List;

public class LineGraphPanel extends AbstractLineGraphPanel {

	private static final long serialVersionUID = 3888711754582814301L;
	
	private final List<LineGraphDataSet> dataSets;
	
	public LineGraphPanel(String title, List<LineGraphDataSet> dataSets) {
		super(title);
		this.dataSets = dataSets;
	}
	
	public LineGraphPanel(List<LineGraphDataSet> dataSets) {
		super();
		this.dataSets = dataSets;
	}

	@Override
	public List<LineGraphDataSet> getDataSets() {
		return dataSets;
	}
	
}
