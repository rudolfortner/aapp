package at.jku.cg.sar.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.main.Comparator;
import at.jku.cg.sar.main.Configuration;
import at.jku.cg.sar.metric.Metric;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;
import at.jku.cg.sar.world.World;

public class ConfigurationWindow {
	
	private final boolean presentMode;
	private final Map<PathFinder, String> rawFinders;
	private final Map<TrajectoryPlanner, String> rawPlanners;
	private final List<Metric> rawMetrics;
	private final List<World> rawWorlds;

	private final JFrame frame;

	private final Map<PathFinder, String> configPathFinders;
	private final Map<TrajectoryPlanner, String> configTrajectoryPlanners;
	private SimulatorSettings configSettings;
	private Metric configMetric;
	
	private int configStartX, configStartY;
	private World configWorld;
	
	private ActionListener actionRefresh;
	
	public ConfigurationWindow(List<PathFinder> finders, List<TrajectoryPlanner> planners, List<Metric> metrics, List<World> worlds, boolean presentMode) {
		this(finders.stream().collect(Collectors.toMap(f -> f, f -> f == null ? "None" : f.getName())),
				planners.stream().collect(Collectors.toMap(p -> p, p -> p == null ? "None" : p.getName())), metrics, worlds, presentMode);
	}
		
	public ConfigurationWindow(Map<PathFinder, String> finders, Map<TrajectoryPlanner, String> planners, List<Metric> metrics, List<World> worlds, boolean presentMode) {
		this.presentMode	= presentMode;
		this.rawFinders		= finders;
		this.rawPlanners	= planners;
		this.rawMetrics		= metrics;
		this.rawWorlds		= worlds;
		
		this.configPathFinders = new HashMap<>();
		this.configTrajectoryPlanners = new HashMap<>();
		this.configSettings = new SimulatorSettings();
		
		this.frame = new JFrame();
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setSize(1080, 720);
		this.frame.setTitle("Configuration");
		this.frame.setLayout(new GridLayout(1, 3));
		
		prepareWindow();
		
		this.frame.setVisible(true);
	}
	
	private void prepareWindow() {
		JPanel panelFinders = new JPanel();
		panelFinders.setBorder(BorderFactory.createTitledBorder("Path Finders"));
		panelFinders.setLayout(new GridBagLayout());
		List<JCheckBox> finderChecks = new ArrayList<>();		
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = c.gridy = 0;
		
		JPanel finderButtonPanel = new JPanel();
		JButton checkAllFinders = new JButton("ALL");
		checkAllFinders.addActionListener(a -> finderChecks.forEach(ch -> {if(!ch.isSelected()) ch.doClick();}));
		finderButtonPanel.add(checkAllFinders, c);
		
		JButton checkNoFinders = new JButton("NONE");
		checkNoFinders.addActionListener(a -> finderChecks.forEach(ch -> {if(ch.isSelected()) ch.doClick();}));
		finderButtonPanel.add(checkNoFinders, c);
		
		panelFinders.add(finderButtonPanel, c);
		c.gridy++;
		
		for(Entry<PathFinder, String> finder : rawFinders.entrySet()) {			
			JCheckBox checkFinder = new JCheckBox(finder.getValue());
			finderChecks.add(checkFinder);
			
			checkFinder.addActionListener(a -> {
				if(checkFinder.isSelected() && !configPathFinders.containsKey(finder)) {
					configPathFinders.put(finder.getKey(), finder.getValue());
				}else {
					configPathFinders.remove(finder.getKey());
				}
			});
			c.gridy++;
			panelFinders.add(checkFinder, c);
		}
		
		
		
		JPanel panelPlanners = new JPanel();
		panelPlanners.setBorder(BorderFactory.createTitledBorder("Trajectory Planners"));
		panelPlanners.setLayout(new GridBagLayout());
		List<JCheckBox> plannerChecks = new ArrayList<>();
		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = c.gridy = 0;


		JPanel plannerButtonPanel = new JPanel();
		JButton checkAllPlanners = new JButton("ALL");
		checkAllPlanners.addActionListener(a -> plannerChecks.forEach(ch -> {if(!ch.isSelected()) ch.doClick();}));
		plannerButtonPanel.add(checkAllPlanners, c);
		
		JButton checkNoPlanners = new JButton("NONE");
		checkNoPlanners.addActionListener(a -> plannerChecks.forEach(ch -> {if(ch.isSelected()) ch.doClick();}));
		plannerButtonPanel.add(checkNoPlanners, c);
		
		panelPlanners.add(plannerButtonPanel, c);
		c.gridy++;
		
		for(Entry<TrajectoryPlanner, String> planner : rawPlanners.entrySet()) {
			String name = planner.getKey() == null ? "None" : planner.getValue();
			JCheckBox checkFinder = new JCheckBox(name);
			plannerChecks.add(checkFinder);
			
			checkFinder.addActionListener(a -> {
				if(checkFinder.isSelected() && !configTrajectoryPlanners.containsKey(planner.getKey())) {
					configTrajectoryPlanners.put(planner.getKey(), planner.getValue());
				}else {
					configTrajectoryPlanners.remove(planner.getKey());
				}
			});
			c.gridy++;
			panelPlanners.add(checkFinder, c);
		}
		
		JPanel panelMetrics = new JPanel();
		panelMetrics.setBorder(BorderFactory.createTitledBorder("Metric"));
		panelMetrics.setLayout(new GridBagLayout());
		ButtonGroup group = new ButtonGroup();
		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = c.gridy = 0;
		
		boolean first = true;
		for(Metric metric : rawMetrics) {
			JRadioButton radioMetric = new JRadioButton(metric.getName());
			radioMetric.addActionListener(a -> configMetric = metric);
			if(first) {
				radioMetric.setSelected(true);
				configMetric = metric;
				first = false;
			}
			group.add(radioMetric);
			
			c.gridy++;
			panelMetrics.add(radioMetric, c);
		}
		
		
		JPanel panelOptions = new JPanel();
		panelOptions.setBorder(BorderFactory.createTitledBorder("Options"));
		panelOptions.setLayout(new GridLayout(0, 1));

		DefaultComboBoxModel<World> worldModel = new DefaultComboBoxModel<>();
		JComboBox<World> comboWorld = new JComboBox<>(worldModel);
		worldModel.addAll(rawWorlds);
		comboWorld.addActionListener(a -> {
			configWorld = (World) comboWorld.getSelectedItem();
			GridValue<Double> max = configWorld.getProbabilities().max();
			configStartX = max.getX();
			configStartY = max.getY();
			refresh();
		});
		comboWorld.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 7851533858766029493L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				World world = (World) value;
				c.setText(world == null ? "None" : world.getName());
				return c;
			}
		});
		panelOptions.add(comboWorld);
		
		JTextField fieldStartX = new JTextField();
		fieldStartX.addActionListener(a -> {
			configStartY = parseTextField(fieldStartX, 0);
		});
		panelOptions.add(pack(new JLabel("Start X:"), fieldStartX));
		
		JTextField fieldStartY = new JTextField();
		fieldStartY.addActionListener(a -> {
			configStartY = parseTextField(fieldStartY, 0);
		});
		panelOptions.add(pack(new JLabel("Start Y:"), fieldStartY));
		
		panelOptions.add(new JSeparator());
		
		JTextField fieldSpeedScan = new JTextField();
		fieldSpeedScan.addActionListener(a -> {
			double speedScan = parseTextField(fieldSpeedScan, 0.0);
			configSettings.setSpeedScan(speedScan);
		});
		panelOptions.add(pack(new JLabel("Speed Scan:"), fieldSpeedScan));
		
		JTextField fieldSpeedFast = new JTextField();
		fieldSpeedFast.addActionListener(a -> {
			double speedFast = parseTextField(fieldSpeedFast, 0.0);
			configSettings.setSpeedFast(speedFast);
		});
		panelOptions.add(pack(new JLabel("Speed Fast:"), fieldSpeedFast));
		
		JTextField fieldAcceleration = new JTextField();
		fieldAcceleration.addActionListener(a -> {
			double acceleration = parseTextField(fieldAcceleration, 0.0);
			configSettings.setAcceleration(acceleration);
		});
		panelOptions.add(pack(new JLabel("Acceleration:"), fieldAcceleration));
		
		JTextField fieldDeceleration = new JTextField();
		fieldDeceleration.addActionListener(a -> {
			double deceleration = parseTextField(fieldDeceleration, 0.0);
			configSettings.setDeceleration(deceleration);
		});
		panelOptions.add(pack(new JLabel("Deceleration:"), fieldDeceleration));
		
		
		JCheckBox checkForceFastFlight = new JCheckBox("Force Fast Flight", false);
		if(!presentMode) panelOptions.add(checkForceFastFlight);
		
		JButton buttonRun = new JButton("RUN");
		buttonRun.addActionListener(a -> {
			Thread thread = new Thread(() -> {
				buttonRun.setEnabled(false);
				
				// UPDATE CONFIG FROM FIELDS
				configWorld = (World) comboWorld.getSelectedItem();
				configStartX = parseTextField(fieldStartX, 0);
				configStartY = parseTextField(fieldStartY, 0);

				configSettings.setSpeedScan(parseTextField(fieldSpeedScan, 0.0));
				configSettings.setSpeedFast(parseTextField(fieldSpeedFast, 0.0));
				configSettings.setAcceleration(parseTextField(fieldAcceleration, 0.0));
				configSettings.setDeceleration(parseTextField(fieldDeceleration, 0.0));

				configSettings.setForceFastFlight(checkForceFastFlight.isSelected());
				
				// Create new finder instances (or copies) to set settings inside planner
				Map<PathFinder, String> configPathFindersNew = new HashMap<>();
				configPathFinders.entrySet().forEach(e -> configPathFindersNew.put(e.getKey().newInstance(configSettings), e.getValue()));

				// Create new planner instances to set settings inside planner
				Map<TrajectoryPlanner, String> configTrajectoryPlannersNew = new HashMap<>();
				configTrajectoryPlanners.entrySet().forEach(e -> configTrajectoryPlannersNew.put(e.getKey().newInstance(configSettings), e.getValue()));
								
				Configuration config = new Configuration(configPathFindersNew, configTrajectoryPlannersNew,
						configSettings, configMetric, configStartX, configStartY, configWorld);				
				
				Comparator comp = new Comparator(false);
				comp.evaluateShow(config);
				buttonRun.setEnabled(true);
			});
			thread.start();
		});
		panelOptions.add(buttonRun);

		this.frame.add(panelFinders);
		this.frame.add(panelPlanners);
		this.frame.add(panelMetrics);
		this.frame.add(panelOptions);		
		
		this.frame.setVisible(true);
		
		actionRefresh = a -> {
			fieldStartX.setText(configStartX+"");
			fieldStartY.setText(configStartY+"");

			fieldSpeedScan.setText(configSettings.getSpeedScan()+"");
			fieldSpeedFast.setText(configSettings.getSpeedFast()+"");
			
			fieldAcceleration.setText(configSettings.getAcceleration()+"");
			fieldDeceleration.setText(configSettings.getDeceleration()+"");
		};
		refresh();
	}
	
	private void refresh() {
		actionRefresh.actionPerformed(null);
	}

	
	private JComponent pack(JComponent c1, JComponent c2) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 2));
		panel.add(c1);
		panel.add(c2);
		return panel;
	}
	
	private int parseTextField(JTextField field, int defaultValue) {
		int value;
		try {
			value = Integer.parseInt(field.getText());
		} catch (Exception e) {
			value = defaultValue;
			field.setText(value+"");
		}
		return value;
	}
	
	private double parseTextField(JTextField field, double defaultValue) {
		double value;
		try {
			value = Double.parseDouble(field.getText());
		} catch (Exception e) {
			value = defaultValue;
			field.setText(value+"");
		}
		return value;
	}
}
