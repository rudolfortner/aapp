package at.jku.cg.sar.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;

import at.jku.cg.sar.gui.tables.ScoringResultTableModel;
import at.jku.cg.sar.scoring.ScoreItem;

public class ScoringResultWindow {

	private final JFrame frame;
	
	private final ScoringResultTableModel tableModel;
	private final JTable table;
	
	public ScoringResultWindow(List<ScoreItem> items, boolean exitOnClose) {
		
		frame = new JFrame();
		frame.setTitle("Overview");
		frame.setSize(1200, 600);
		frame.setDefaultCloseOperation(exitOnClose ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		tableModel = new ScoringResultTableModel( items);
		table = new JTable(tableModel);
		table.getTableHeader().setFont(new Font("", 0, 10));
		table.getTableHeader().setPreferredSize(new Dimension(10, 30));
		
		table.setRowHeight(50);
		table.setFont(new Font("", 0, 16));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		table.setBorder(new LineBorder(Color.BLACK));
		table.setAutoCreateRowSorter(true);
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
//					int row = table.convertRowIndexToModel(table.getSelectedRow());
//					new SimulationResultViewer(tableModel.get(row));
				}				
			}
		});
		

		frame.add(new JScrollPane(table), BorderLayout.CENTER);
		
		frame.setVisible(true);
	}

}
