package at.jku.cg.sar.eval;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import at.jku.cg.core.latex.LatexTableWriter;
import at.jku.cg.core.latex.LatexTableWriter.LatexTableRow;
import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridPoint;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.gui.DrawUtils;
import at.jku.cg.sar.gui.SimulationResultViewer;
import at.jku.cg.sar.gui.graphics.FlightPathRenderer;
import at.jku.cg.sar.gui.graphics.GridCellRenderer;
import at.jku.cg.sar.gui.graphics.GridRenderer;
import at.jku.cg.sar.gui.graphics.LineGraphDataSet;
import at.jku.cg.sar.gui.graphics.LineGraphRenderer;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.pathfinder.PathFinderResult;
import at.jku.cg.sar.pathfinder.PathFinderType;
import at.jku.cg.sar.pathfinder.misc.RadialChecker;
import at.jku.cg.sar.scoring.RankColoring;
import at.jku.cg.sar.scoring.Ranking;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.sim.drone.DroneDiscrete;
import at.jku.cg.sar.sim.flightpath.FlightPath;
import at.jku.cg.sar.sim.flightpath.GridFlightLeg;
import at.jku.cg.sar.sim.flightpath.WorldFlightLeg;
import at.jku.cg.sar.trajectory.SimpleTrajectory;
import at.jku.cg.sar.util.Vector;
import at.jku.cg.sar.world.World;

class EvaluateRadialCheckers {

	private static final File dir;
	private static final File dirUniform;
	
	private static final List<PathFinder> finders;
	private static final PathFinder finderFull, finderIterative;
	private static final Grid<Integer> visited = new Grid<>(EvaluationConfig.WIDTH, EvaluationConfig.WIDTH, 0);
	
	static {
		dir = new File("out", "radialChecker");
		dir.mkdirs();
		dirUniform = new File(dir, "uniform");
		dirUniform.mkdirs();
		
		finderFull = new RadialChecker(false);
		finderIterative = new RadialChecker(true);
		
		finders = new ArrayList<>();
		finders.add(finderFull);
		finders.add(finderIterative);
		
		{
			int cx = 2;
			int cy = 12;
			// VERTICAL
			visited.set(cx, cy-1, 1);
			visited.set(cx, cy-2, 1);
			visited.set(cx, cy-3, 1);
			
			visited.set(cx, cy-6, 1);

			visited.set(cx, cy-9, 1);
			visited.set(cx, cy-10, 1);
			
			visited.set(cx, cy-11, 1);
			visited.set(cx, cy-12, 1);
		}
		// DIA
		visited.set(5, 10, 1);
		visited.set(11, 6, 1);
	}
	
	@Test
	void evaluateAll() {
		EvaluationUtils.evaluateAll(dir, finders,
				"eval_radialcheckers_all.txt", "Radial Checker Comparison", "tab:radial_checker_comparison", RankColoring.FIRST,
				result -> result.getMetricResult().getScore(), false);
	}
	
	@Test
	void findOthersBetter() {
		File dirComparison = new File(dir, "comparison");
		
		for(World world : EvaluationConfig.worlds) {
			EvaluationUtils.findOthersBetter(dirComparison, world,
					finders, finderIterative,
					result -> result.getMetricResult().getScore(), false, 3);
		}
	}
	
	@Test
	void uniform() {
		for(int x = 0; x < EvaluationConfig.WIDTH; x++) {
			for(int y = 0; y < EvaluationConfig.WIDTH; y++) {
				System.err.print("RUNNING EVALUATION AT %02d/%02d\r".formatted(x, y));
				String filenamePath			= "example_%02d_%02d_path.png".formatted(x, y);
				File filePath				= new File(dirUniform, filenamePath);
				EvaluationUtils.example(EvaluationConfig.worldUniform, EvaluationConfig.finderRadialChecker, EvaluationConfig.plannerSimple,
						x, y,
						true, false, false, filePath, EvaluationConfig.IMAGE_SIZE, 0);
				
			}
		}
	}
	
	@Test
	void createWinDistributionMap() {
		
		for(World world : EvaluationConfig.worlds) {
			String worldName = world.getName().toLowerCase().replace(" ", "_");
			System.err.println("Win Distribution for %s".formatted(world.getName()));

			File file = new File(dir, "eval_radialcheckers_%s_win_distribution.png".formatted(worldName));
			
			EvaluationUtils.createWinDistributionMap(world,
					finderIterative, finders, new SimpleTrajectory(new SimulatorSettings()),
					world.getName(), "Evaluated by Metric Score", false,
					file, res -> res.getMetricResult().getScore(), false);
			
		}
	}
	
	@Test
	void evaluateRadii() {
		
		// Create RadialCheckers with different radii settings
		List<PathFinder> finders = new ArrayList<>();		
		for(int r1 = 1; r1 <= 16; r1++) {
			finders.add(new RadialChecker(true, 0.0, r1));
		}
		
		finders.add(new RadialChecker(true));
		Map<PathFinder, Integer> winMap = new HashMap<>();
		finders.forEach(f -> winMap.put(f, 0));
		
		int total = 0;
		for(World world : List.of(EvaluationConfig.worldSmoothedSpots)) {
			List<Future<Ranking<SimulationResult>>> futures = new ArrayList<>();
			System.err.println("Running on world " + world.getName());
			for(int startX = 0; startX < 16; startX++) {
				for(int startY = 0; startY < 16; startY++) {
					Future<Ranking<SimulationResult>> rankScore = EvaluationUtils.rankSingleFuture(world, startX, startY,
							finders, EvaluationConfig.planners, result -> result.getMetricResult().getScore(), false);
					futures.add(rankScore);							
				}
			}
			for(Future<Ranking<SimulationResult>> future : futures) {
				Ranking<SimulationResult> ranking = EvaluationUtils.rankLater(future);
				if(ranking == null) continue;

				for(SimulationResult winner : ranking.getForRank(1)) {
					winMap.put(winner.getFinder(), winMap.get(winner.getFinder())+1);
				}
				total++;
			}			
		}
		
		Ranking<PathFinder> rankFinders = new Ranking<>(finders, f -> 1.0 * winMap.get(f));
				
		LatexTableWriter writer = new LatexTableWriter();
		writer.setCaption("Radial Checker Radii");
		writer.setLabel("tab:radial_checker_radii");
		
		for(PathFinder f : rankFinders.getSorted()) {
			RadialChecker r = (RadialChecker) f;
			LatexTableRow row = new LatexTableRow();

			row.addEntry("r0", String.format(Locale.ROOT, "%2.2f", r.getR0()));
			row.addEntry("r1", String.format(Locale.ROOT, "%2.2f", r.getR1()));

			int wins = winMap.get(f);
			double percentage = wins * 100.0 / total;
			row.addEntry("wins", String.format(Locale.ROOT, "%d (%2.1f %%)", winMap.get(f), percentage));			
						
			writer.addRow(row);
		}
		
		writer.export(new File(dir, "evaluate_radii.txt"));
	}
	
	
	@Test
	void generateExplanation() {
		int cx = 2;
		int cy = 12;
		double r0 = 0.0;
		double r1 = Double.POSITIVE_INFINITY;
		
		
		generateExplanation(EvaluationConfig.worldSmoothedSpots, visited, cx, cy, r0, r1);
	}
	
	
	private static void generateExplanation(World world, Grid<Integer> visited, int cx, int cy, double r0, double r1) {
				
		// GATHER ALL POSSIBLE RADIALS AND RENDER
		Set<Double> radials = new HashSet<>();
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				if((x == cx && y == cy)) continue;
				double heading = (new GridFlightLeg(cx, cy, x, y, 0)).getHeading();
				radials.add(heading);
			}
		}		
		renderRadials(new File(dir, "explanation_01_radials.png"), world, radials, cx, cy, 1024, 0);
		
		
		
		// EVALUATE RADIALS
		DroneDiscrete drone = new DroneDiscrete(world.getProbabilities(), visited, cx, cy);
//		drone.visit(cx, cy);
		List<FlightPath<WorldFlightLeg>> results = new ArrayList<>();
		for(double radial : new ArrayList<>(radials)) {
			results.add(evaluateRadial(drone.clone(), radial, 0.0, Double.POSITIVE_INFINITY, new LineGraphDataSet("", null)));
		}
		renderRadialsEvaluated(new File(dir, "explanation_02_eval.png"), world, results, cx, cy, 1024, 0);
				
		
		// TEST
		double dia = (new GridFlightLeg(cx, cy, cx+2+1, cy-1-1, 30.0)).getHeading();
		RadialData diaData = new RadialData(dia, 4.0, 12.0);
		diaData = new RadialData(dia, 0.0, Double.POSITIVE_INFINITY);	// For variant 2
		
		LineGraphDataSet gradientHorizontal = new LineGraphDataSet("Horizontal", Color.RED);
		FlightPath<WorldFlightLeg> pathHorizontal = evaluateRadial(drone.clone(), 90.0, r0, r1, null);
		LineGraphDataSet gradientVertical = new LineGraphDataSet("Vertical", Color.GREEN);
		FlightPath<WorldFlightLeg> pathVertical = evaluateRadial(drone.clone(), 0.0, r0, r1, null);
		LineGraphDataSet gradientDiagonal = new LineGraphDataSet("Diagonal", Color.BLUE);
		FlightPath<WorldFlightLeg> pathDiagonal = evaluateRadial(drone.clone(), diaData.radial, diaData.r0, diaData.r1, null);

		pathHorizontal = modifyPath(pathHorizontal, cx, cy, 0);
		pathVertical = modifyPath(pathVertical, cx, cy, 1);
		pathDiagonal = modifyPath(pathDiagonal, cx, cy, 2);

		gradientHorizontal	= evaluatePath(pathHorizontal, world.clone(), gradientHorizontal);
		gradientVertical	= evaluatePath(pathVertical, world.clone(), gradientVertical);
		gradientDiagonal	= evaluatePath(pathDiagonal, world.clone(), gradientDiagonal);
		
		

		File filePath = new File(dir, "explanation_00_path.png");
		File filePathRaw = new File(dir, "explanation_00_path_raw.txt");
		File filePlot = new File(dir, "explanation_00_plot.png");
		File fileComb = new File(dir, "explanation_00_comb.png");
		File fileCombNT = new File(dir, "explanation_00_comb_NT.png");

		int border = EvaluationConfig.IMAGE_SIZE / 32;
		renderSingle(filePath, world, visited, List.of(pathHorizontal, pathVertical, pathDiagonal), List.of(),
				cx, cy, r0, r1, EvaluationConfig.IMAGE_SIZE, border);
		
		plotGraph(List.of(gradientHorizontal, gradientVertical, gradientDiagonal), filePlot, 512, 64);
		LineGraphRenderer.export(List.of(gradientHorizontal, gradientVertical, gradientDiagonal), "Time [s]", "Accumulated Probability", filePathRaw);
		
		
		EvaluationUtils.combineImages(EvaluationConfig.IMAGE_SIZE, 64, List.of(filePath, filePlot),
				List.of("(a)", "(b)"), List.of(Color.WHITE, Color.BLACK), EvaluationConfig.IMAGE_SIZE/32, 0,
				fileComb, false);		
		EvaluationUtils.combineImages(EvaluationConfig.IMAGE_SIZE, 64, List.of(filePath, filePlot),
				List.of(), List.of(Color.WHITE, Color.BLACK), 0, 0,
				fileCombNT, false);
	}
	
	private static FlightPath<WorldFlightLeg> modifyPath(FlightPath<WorldFlightLeg> path, int x, int y, int num){
		List<WorldFlightLeg> legs = new ArrayList<>(path.getLegs());
		legs.remove(0);
		legs.remove(0);
		
		// Leg from center
		double cx = EvaluationConfig.settings.getCellSize() * (x + 0.5);
		double cy = EvaluationConfig.settings.getCellSize() * (y + 0.5);
		
		if(num == 0) {
			// HORIZONTAL
			WorldFlightLeg first = legs.get(0);
			WorldFlightLeg newFirst = new WorldFlightLeg(cx, cy, 0.0, first.getFromX(), first.getFromY(), first.getFromSpeed(), first.getGridUnit());
			legs.add(0, newFirst);
		}else if(num == 1) {
			// VERTICAL
			for(int i = 0; i < 3; i++) legs.remove(0);
			WorldFlightLeg first = legs.get(0);
			WorldFlightLeg dummyOrigin = new WorldFlightLeg(cx, cy, cx, cy, first.getGridUnit());
			SimulatorSettings set = EvaluationConfig.settings;
			legs.addAll(0, SimpleTrajectory.connectDirect(dummyOrigin, legs.get(0), set.getSpeedFast(), set.getAcceleration(), -set.getDeceleration()));
		}else if(num == 2) {
			// DIAGONAL
			for(int i = 0; i < 4; i++) legs.remove(0);
			WorldFlightLeg first = legs.get(0);
			WorldFlightLeg dummyOrigin = new WorldFlightLeg(cx, cy, cx, cy, first.getGridUnit());
			SimulatorSettings set = EvaluationConfig.settings;
			legs.addAll(0, SimpleTrajectory.connectDirect(dummyOrigin, legs.get(0), set.getSpeedFast(), set.getAcceleration(), -set.getDeceleration()));
		}else throw new IllegalStateException();
		
		
		
		FlightPath<WorldFlightLeg> newPath = new FlightPath<>();
		newPath.appendLegs(legs);
		
		return newPath;
	}
	
	private static LineGraphDataSet evaluatePath(FlightPath<WorldFlightLeg> path, World world, LineGraphDataSet set) {
		double accumProb = 0.0;
		double accumTime = 0.0;
		set.addPoint(accumTime, accumProb);		
		
		for(WorldFlightLeg leg : path.getLegs()) {
			accumTime += leg.getDuration();
			if(leg.isScan()) {
				accumProb += world.getProbability(leg.getScanX(), leg.getScanY());
				set.addPoint(accumTime, accumProb);				
			}
		}
		
		return set;
	}

	private static final class RadialData {
		public final double radial, r0, r1;
		
		public RadialData(double radial, double r0, double r1) {
			this.radial = radial;
			this.r0 = r0;
			this.r1 = r1;
		}
	}
	
	private static void renderRadials(File file, World world, Set<Double> radials, int cx, int cy, int size, int border) {
		
		int tileSize = (size - 2*border) / Math.max(world.getWidth(), world.getHeight());
		int borderX = (size - tileSize*world.getWidth()) / 2;
		int borderY = (size - tileSize*world.getHeight()) / 2;
		
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		g.setBackground(new Color(255, 255, 255, 0));
		g.clearRect(0, 0, size, size);
		
		GridCellRenderer<Double> renderer = new GridCellRenderer<>() {			
			@Override
			public void render(Graphics2D g, GridValue<Double> cell, int centerX, int centerY, int width, int height) {
				Color col = EvaluationConfig.rampProbability.evaluate(cell.getValue());				
				g.setColor(col);
				g.fillRect(centerX - tileSize/2, centerY - tileSize/2, tileSize, tileSize);			
				
				g.setFont(new Font("", Font.PLAIN, tileSize/4));
				g.setColor(Color.BLACK);
				//if(showValue) TextDrawer.drawString(g, "%.2f".formatted(cell.getValue()), centerX, centerY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
				g.drawRect(centerX - tileSize/2, centerY - tileSize/2, tileSize, tileSize);
			}
		};
		
		// GRID
		GridRenderer.render(g, world.getProbabilities(), borderX, borderY, tileSize, renderer);
		
		// RADIALS
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
		for(double radial : radials) {
			Vector vec = Vector.fromHeading(radial);
			vec.scale(100);

			int x0 = (int) (borderX + tileSize * (cx + 0.5));
			int y0 = (int) (borderY + tileSize * (cy + 0.5));
			int x1 = (int) (borderX + tileSize * (cx + 0.5 + vec.x));
			int y1 = (int) (borderY + tileSize * (cy + 0.5 + vec.y));			
			g.drawLine(x0, y0, x1, y1);			
		}
		g.setStroke(oldStroke);
		
		g.dispose();
				
		
		try {
			ImageIO.write(image, "PNG", file);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void renderRadialsEvaluated(File file, World world, List<FlightPath<WorldFlightLeg>> results, int cx, int cy, int size, int border) {
		
		int tileSize = (size - 2*border) / Math.max(world.getWidth(), world.getHeight());
		int borderX = (size - tileSize*world.getWidth()) / 2;
		int borderY = (size - tileSize*world.getHeight()) / 2;
		
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		g.setBackground(new Color(255, 255, 255, 0));
		g.clearRect(0, 0, size, size);
		
		GridCellRenderer<Double> renderer = new GridCellRenderer<>() {			
			@Override
			public void render(Graphics2D g, GridValue<Double> cell, int centerX, int centerY, int width, int height) {
				Color col = EvaluationConfig.rampProbability.evaluate(cell.getValue());				
				g.setColor(col);
				g.fillRect(centerX - tileSize/2, centerY - tileSize/2, tileSize, tileSize);			
				
				g.setFont(new Font("", Font.PLAIN, tileSize/4));
				g.setColor(Color.BLACK);
				//if(showValue) TextDrawer.drawString(g, "%.2f".formatted(cell.getValue()), centerX, centerY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
				g.drawRect(centerX - tileSize/2, centerY - tileSize/2, tileSize, tileSize);
			}
		};
		
		// GRID
		GridRenderer.render(g, world.getProbabilities(), borderX, borderY, tileSize, renderer);
		
		// RADIALS
		double factor = tileSize / (new SimulatorSettings()).getCellSize();
		for(FlightPath<WorldFlightLeg> path : results) {			
			FlightPathRenderer.render(g, path, borderX, borderY, factor, EvaluationConfig.rampSpeed);
			
		}
		
		g.dispose();
				
		
		try {
			ImageIO.write(image, "PNG", file);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static FlightPath<WorldFlightLeg> evaluateRadial(DroneDiscrete drone, double radial, double r0, double r1, LineGraphDataSet set) {
		
		SimpleTrajectory planner = new SimpleTrajectory(new SimulatorSettings());
		if(drone.getStepCount() > 0) planner.next(new PathFinderResult(drone.getPreviousX(), drone.getPreviousY(), true));
		planner.next(new PathFinderResult(drone.getX(), drone.getY(), true));
		
		FlightPath<WorldFlightLeg> trajectory = new FlightPath<>();
		
		List<PathFinderResult> path = new ArrayList<>();
//		LineGraphDataSet set = new LineGraphDataSet("probs", Color.RED);
		if(set != null) set.addPoint(0, 0);
		
		List<GridValue<Double>> onRadial = RadialChecker.onRadial(drone, radial, false);
		GridValue<Double> currentMax = null;
		double maxGradient = 0.0;
		double accumProb = 0.0;
		double accumTime = 0.0;
		for(GridValue<Double> g : onRadial) {
			int x = g.getX(), y = g.getY();
			if(drone.isVisited(x, y)) {										// Jump over visited cells
				continue;								
			}
			double distance = g.distance(drone.getX(), drone.getY());		// Calculate distance from current position
			if(distance <= r0) {											// All cells closer than r0 are overflown fast										
				continue;													// Overfly
			}
			if(distance > r1) break;										// All cells beyond r1 are not considered
			
			FlightPath<WorldFlightLeg> flightPath = new FlightPath<>();		// FlightPath object from current pos to next cell or from previous cell to next cell
			PathFinderResult next = new PathFinderResult(x, y, true);	// PathFinder next position
			List<WorldFlightLeg> legs = planner.next(next);
			flightPath.appendLegs(legs);									// TrajectoryPlanner plans for next position
			trajectory.appendLegs(legs);
			path.add(next);													// Add next to intermediate result
			
			accumProb += g.getValue();										// Accumulate probabilities
			accumTime += flightPath.getDuration();							// Accumulate time
			double gradient = accumProb / accumTime;						// Calculate gradient up to now
			if(set != null) set.addPoint(accumTime, accumProb);
			
			if(gradient >= maxGradient) {
				currentMax = g;
				maxGradient = gradient;
			}
		}
		trajectory.appendLegs(planner.next(null));
		
		// Final Path only contains cells up to the point with the maximum gradient
		List<PathFinderResult> finalPath = new ArrayList<>();
		for(PathFinderResult r : path) {
			finalPath.add(r);
			if(r.getPosX() == currentMax.getX() && r.getPosY() == currentMax.getY()) break;
		}
		return trajectory;
	}
	
	
	private static void renderSingle(File file, World world, Grid<Integer> visited, List<FlightPath<WorldFlightLeg>> paths,
			List<RadialData> radials,
			int cx, int cy, double r0, double r1, int size, int border) {
		
		int tileSize = (size - 2*border) / Math.max(world.getWidth(), world.getHeight());
		int borderX = (size - tileSize*world.getWidth()) / 2;
		int borderY = (size - tileSize*world.getHeight()) / 2;
		
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		g.setBackground(new Color(255, 255, 255, 0));
		g.clearRect(0, 0, size, size);
		
		GridCellRenderer<Double> renderer = new GridCellRenderer<>() {			
			@Override
			public void render(Graphics2D g, GridValue<Double> cell, int centerX, int centerY, int width, int height) {
				Color col = EvaluationConfig.rampProbability.evaluate(cell.getValue());				
				g.setColor(visited.get(cell.getX(), cell.getY()) > 0 ? Color.BLACK : col);
				g.fillRect(centerX - tileSize/2, centerY - tileSize/2, tileSize, tileSize);			
				
				float strokeWidth = tileSize / 64.0f;
				int strokeHalf = (int) (strokeWidth / 2.0 - 0.5);
				g.setStroke(new BasicStroke(strokeWidth));
				g.setFont(new Font("", Font.PLAIN, tileSize/4));
				g.setColor(Color.BLACK);
				//if(showValue) TextDrawer.drawString(g, "%.2f".formatted(cell.getValue()), centerX, centerY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
				g.drawRect(centerX - tileSize/2 + strokeHalf, centerY - tileSize/2 + strokeHalf, tileSize-2*strokeHalf, tileSize-2*strokeHalf);
			}
		};
		
		// GRID
		GridRenderer.render(g, world.getProbabilities(), borderX, borderY, tileSize, renderer);
		
		// FLIGHTPATH
		double factor = tileSize / (new SimulatorSettings()).getCellSize();
		for(FlightPath<WorldFlightLeg> path : paths) {
			FlightPathRenderer.render(g, path, borderX, borderY, factor, EvaluationConfig.rampSpeed, 3.f*size/512.0);
		}
		
		// CIRCLES
		int x0 = (int) (borderX + tileSize * (cx + 0.5));
		int y0 = (int) (borderY + tileSize * (cy + 0.5));
		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(2.5f));
		if(r0 > 0.0) {
			int screenRadius0 = (int) (tileSize * r0);
			DrawUtils.drawCircle(g, x0, y0, screenRadius0);
		}
		if(r1 > 0.0) {
			int screenRadius1 = (int) (tileSize * r1);
			DrawUtils.drawCircle(g, x0, y0, screenRadius1);
		}
		g.drawLine(x0-5, y0, x0+5, y0);
		g.drawLine(x0, y0-5, x0, y0+5);
		
		
		float strokeSize = size/256.0f;
		Stroke stroke = new BasicStroke(strokeSize,
				BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_MITER,
				10.0f,
				new float[] {0.0f, 2.0f*strokeSize},
				0.0f);
		g.setStroke(stroke);
		for(RadialData data : radials) {
			Vector location = new Vector(cx, cy);
			Vector direction = Vector.fromHeading(data.radial);
			direction.normalize();
			System.err.println("Rendering radial " + data.radial);

			Vector r0Start = location.clone();
			r0Start.add(direction.Scale(r0));
			Vector r1Start = location.clone();
			r1Start.add(direction.Scale(r1));

			double r0x = (cx + direction.x * data.r0);
			double r0y = (cy + direction.y * data.r0);
			double r1x = (cx + direction.x * data.r1);
			double r1y = (cy + direction.y * data.r1);
			double r2x = (cx + direction.x * 100.0);
			double r2y = (cy + direction.y * 100.0);
			

			int x1 = (int) (borderX + tileSize * (r0x + 0.5));
			int y1 = (int) (borderY + tileSize * (r0y + 0.5));

			int x2 = (int) (borderX + tileSize * (r1x + 0.5));
			int y2 = (int) (borderY + tileSize * (r1y + 0.5));
			
			int x3 = (int) (borderX + tileSize * (r2x + 0.5));
			int y3 = (int) (borderY + tileSize * (r2y + 0.5));
			
			
			g.setColor(Color.WHITE);
			g.drawLine(x0, y0, x1, y1);
			
			g.setColor(Color.BLACK);
			g.drawLine(x1, y1, x2, y2);
			
			g.setColor(Color.WHITE);
			g.drawLine(x2, y2, x3, y3);
		}
		
		g.dispose();
				
		
		try {
			ImageIO.write(image, "PNG", file);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void plotGraph(List<LineGraphDataSet> dataSets,
			File file, int size, int border) {

		int width = size * 16 / 9;
		int height = size;
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		// Draw Background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		
		int canvasWidth = width-2*border;
		int canvasHeight = height - 2*border;
		
		// canvas background
//		g.setColor(Color.WHITE);
//		g.fillRect(border, border, canvasWidth, canvasHeight);
//		g.setColor(Color.BLACK);
//		g.drawRect(border, border, canvasWidth, canvasHeight);

		
		LineGraphRenderer.drawLineGraph(g, dataSets,
				border, border, canvasWidth, canvasHeight,
				"Time [s]", "Accumulated Probability",
				false, false, 0.0, false,
				false, 0, 0, 0, 0);
		
		LineGraphRenderer.drawLegend(g, dataSets,
				border + canvasWidth/6, border + canvasHeight/6,
				canvasWidth*1/3, canvasHeight, false);
		
		
		g.dispose();
		
		try {
			ImageIO.write(image, "PNG", file);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void checkCellsOnRadial() {
		DroneDiscrete drone = new DroneDiscrete(EvaluationConfig.worldSmoothedSpots.getProbabilities().clone(), EvaluationConfig.EXAMPLE_START_X, EvaluationConfig.EXAMPLE_START_Y);
		
		Set<Double> radials = new HashSet<>();
		for(int x = 0; x < drone.getWidth(); x++) {
			for(int y = 0; y < drone.getHeight(); y++) {
				if(drone.isVisited(x, y)) continue;	
				if((x == drone.getX() && y == drone.getY())) continue;
				double heading = (new GridFlightLeg(drone.getX(), drone.getY(), x, y, 0)).getHeading();
				radials.add(heading);
			}
		}
		
		Map<Integer, Integer> counts = new HashMap<>();
		
		for(Double radial : radials) {
			List<GridValue<Double>> onRadial = RadialChecker.onRadial(drone, radial, false);
			
			int cellCount = onRadial.size();			
			int cellCountCount = counts.getOrDefault(cellCount, 0);
			counts.put(cellCount, cellCountCount+1);			
		}
		
		List<Entry<Integer, Integer>> list = counts.entrySet().stream().sorted((e0, e1) -> Integer.compare(e0.getKey(), e1.getKey())).collect(Collectors.toList());

		int radialCount = counts.entrySet().stream().mapToInt(e -> e.getValue()).sum();
		System.err.println("TOTAL: " + radialCount);

		System.err.println("-----------");
		System.err.printf("Cell Count\tHow often\n");
		for(Entry<Integer, Integer> entry : list) {			
			System.err.printf("%2d:\t%4d\n", entry.getKey(), entry.getValue());
		}
		System.err.println("-----------");
	}
	
	@Test
	void testFullRadial() {
		testFullRadial(2, 12, 65.0);
		generateFullRadialComparison(EvaluationConfig.worldSmoothedSpots, visited, 2, 12, 0.0, Double.POSITIVE_INFINITY);
	}
	
	private static FlightPath<WorldFlightLeg> testFullRadial(int cx, int cy, double radial) {
		List<GridPoint> points = new ArrayList<>();
		
		
		
		double delta = 0.05;
		for(double t = delta; t <= EvaluationConfig.WIDTH*2; t+=delta) {
			System.err.println("Checking for t = " + t);
			Vector deltaVec = Vector.fromHeading(radial).Scale(t);;

			int x = (int) (cx + deltaVec.x);
			int y = (int) (cy + deltaVec.y);
			if(x >= EvaluationConfig.WIDTH || y >= EvaluationConfig.WIDTH) break;
			
			GridPoint p = new GridPoint(x, y);
			if(points.contains(p)) continue;
			points.add(p);
		}
		
		
		Grid<Double> probs = EvaluationConfig.worldSmoothedSpots.getProbabilities().clone();
		for(GridPoint p : points) probs.set(p.getX(), p.getY(), 0.0);
		
		final class TestFinder extends PathFinder {

			public TestFinder() {
				super(PathFinderType.DISCRETE_FULL);
			}
			
			@Override
			public List<PathFinderResult> solveFull(DroneDiscrete drone) {
				return points.stream().map(p -> new PathFinderResult(p.getX(), p.getY(), true)).collect(Collectors.toList());
			}

			@Override
			public String getName() {
				return "TEST";
			}
			
		}
		
		SimulationResult result = EvaluationUtils.rankSingleImmediate(EvaluationConfig.worldSmoothedSpots, cx, cy,
				List.of(new TestFinder()), List.of(EvaluationConfig.plannerSimple), val -> val.getMetricResult().getScore(), false)
				.getRaw().get(0);
		
		new SimulationResultViewer(result);
		System.err.println("Duration: " + result.getFinalPath().getDuration());
		
		return result.getTrajectoryPath();
	}
	
	private static void generateFullRadialComparison(World world, Grid<Integer> visited, int cx, int cy, double r0, double r1) {
		
		// GATHER ALL POSSIBLE RADIALS AND RENDER
		Set<Double> radials = new HashSet<>();
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				if((x == cx && y == cy)) continue;
				double heading = (new GridFlightLeg(cx, cy, x, y, 0)).getHeading();
				radials.add(heading);
			}
		}		
		
		
		// EVALUATE RADIALS
		DroneDiscrete drone = new DroneDiscrete(world.getProbabilities(), visited, cx, cy);
//		drone.visit(cx, cy);
		List<FlightPath<WorldFlightLeg>> results = new ArrayList<>();
		for(double radial : new ArrayList<>(radials)) {
			results.add(evaluateRadial(drone.clone(), radial, 0.0, Double.POSITIVE_INFINITY, new LineGraphDataSet("", null)));
		}
				
		// TEST		
		LineGraphDataSet gradientHorizontal = new LineGraphDataSet("Horizontal", Color.RED);
		FlightPath<WorldFlightLeg> pathHorizontal = evaluateRadial(drone.clone(), 90.0, r0, r1, null);
		
		LineGraphDataSet gradientVertical = new LineGraphDataSet("Vertical", Color.GREEN);
		FlightPath<WorldFlightLeg> pathVertical = evaluateRadial(drone.clone(), 0.0, r0, r1, null);
		
		double dia = (new GridFlightLeg(cx, cy, cx+2+1, cy-1-1, 30.0)).getHeading();
		LineGraphDataSet gradientDiagonal = new LineGraphDataSet("Diagonal", Color.BLUE);		
		FlightPath<WorldFlightLeg> pathDiagonal = testFullRadial(cx, cy, dia);

		pathHorizontal = modifyPath(pathHorizontal, cx, cy, 0);
		pathVertical = modifyPath(pathVertical, cx, cy, 1);
		pathDiagonal = modifyPath(pathDiagonal, cx, cy, 2);

		gradientHorizontal	= evaluatePath(pathHorizontal, world.clone(), gradientHorizontal);
		gradientVertical	= evaluatePath(pathVertical, world.clone(), gradientVertical);
		gradientDiagonal	= evaluatePath(pathDiagonal, world.clone(), gradientDiagonal);
		
		
		File filePath = new File(dir, "radial_full_00_path.png");
		File filePathRaw = new File(dir, "radial_full_00_path_raw.txt");
		File filePlot = new File(dir, "radial_full_00_plot.png");
		File fileComb = new File(dir, "radial_full_00_comb.png");

		int border = EvaluationConfig.IMAGE_SIZE / 32;
		renderSingle(filePath, world, visited, List.of(pathHorizontal, pathVertical, pathDiagonal), List.of(),
				cx, cy, r0, r1, EvaluationConfig.IMAGE_SIZE, border);
		
		plotGraph(List.of(gradientHorizontal, gradientVertical, gradientDiagonal), filePlot, 512, 64);
		LineGraphRenderer.export(List.of(gradientHorizontal, gradientVertical, gradientDiagonal), "Time [s]", "Accumulated Probability", filePathRaw);
				
		EvaluationUtils.combineImages(EvaluationConfig.IMAGE_SIZE, 64, List.of(filePath, filePlot),
				List.of("(a)", "(b)"), List.of(Color.WHITE, Color.BLACK), EvaluationConfig.IMAGE_SIZE/32, 64,
				fileComb, false);
	}
	
}
