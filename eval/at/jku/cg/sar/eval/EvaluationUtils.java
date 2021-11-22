package at.jku.cg.sar.eval;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import at.jku.cg.core.latex.LatexTableWriter;
import at.jku.cg.core.latex.LatexTableWriter.LatexTableEntry;
import at.jku.cg.core.latex.LatexTableWriter.LatexTableRow;
import at.jku.cg.sar.core.grid.Grid;
import at.jku.cg.sar.core.grid.GridValue;
import at.jku.cg.sar.gui.graphics.ColorRampRenderer;
import at.jku.cg.sar.gui.graphics.DistributionMapRenderer;
import at.jku.cg.sar.gui.graphics.GridRenderer;
import at.jku.cg.sar.gui.graphics.LineGraphDataSet;
import at.jku.cg.sar.gui.graphics.LineGraphRenderer;
import at.jku.cg.sar.gui.graphics.SimulationResultRenderer;
import at.jku.cg.sar.main.Comparator;
import at.jku.cg.sar.main.Configuration;
import at.jku.cg.sar.metric.AUPCMetric;
import at.jku.cg.sar.pathfinder.PathFinder;
import at.jku.cg.sar.scoring.RankColoring;
import at.jku.cg.sar.scoring.Ranking;
import at.jku.cg.sar.sim.SimulationResult;
import at.jku.cg.sar.sim.SimulatorSettings;
import at.jku.cg.sar.trajectory.SimpleTrajectory;
import at.jku.cg.sar.trajectory.TrajectoryPlanner;
import at.jku.cg.sar.util.ColorRamp;
import at.jku.cg.sar.util.Interpolation;
import at.jku.cg.sar.util.ListWrapper;
import at.jku.cg.sar.util.TextDrawer;
import at.jku.cg.sar.util.TextDrawer.HorizontalAlignment;
import at.jku.cg.sar.util.TextDrawer.VerticalAlignment;
import at.jku.cg.sar.world.World;

public class EvaluationUtils {
	
	public static void example(World world, PathFinder finder, TrajectoryPlanner planner,
			int startX, int startY, boolean showPlannerPath, boolean showTrajectoryPath, boolean showGridValues,
			File file, int size, int border) {
		Comparator comp = new Comparator(true);
		List<SimulationResult> results = comp.evaluate(new Configuration(List.of(finder), List.of(planner), EvaluationConfig.settings,
				new AUPCMetric(), startX, startY, world));
		SimulationResult result = results.get(0);
		
		int tileSize = (size - 2*border) / Math.max(result.getConfiguration().getWorld().getWidth(), result.getConfiguration().getWorld().getHeight());
		int borderX = (size - tileSize*result.getConfiguration().getWorld().getWidth()) / 2;
		int borderY = (size - tileSize*result.getConfiguration().getWorld().getHeight()) / 2;
		
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		g.setBackground(new Color(255, 255, 255, 0));
		g.clearRect(0, 0, size, size);
		SimulationResultRenderer.renderRaw(g, result, borderX, borderY, tileSize,
				showPlannerPath, showTrajectoryPath, 3.0*size/512.0,
				false, false, 0.0, false,
				EvaluationConfig.rampProbability, EvaluationConfig.rampSpeed);
		g.dispose();
		
		try {
			ImageIO.write(image, "PNG", file);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void exampleTable(World world, Map<PathFinder, String> finders, TrajectoryPlanner planner, int startX, int startY,
			String tableCaption, String tableLabel,
			File file) {
		Ranking<SimulationResult> ranking = rankSingleImmediate(world, startX, startY, new ArrayList<>(finders.keySet()), List.of(planner), res -> res.getMetricResult().getScore(), false);
		double tmin = ranking.getRaw().stream().mapToDouble(result -> result.getFinalPath().getDuration()).min().orElseThrow();
		
		String leftColumn = "Algorithm";
		
		LatexTableWriter writer = new LatexTableWriter();
		writer.setCaption(tableCaption);
		writer.setLabel(tableLabel);
		writer.setLeftColumn(leftColumn);
		writer.setSortColumns(false);
		
		for(SimulationResult result : ranking.getSorted()) {
			
			LatexTableRow row = new LatexTableRow();
			
			String format = "%4.2f";
			row.addEntry(leftColumn, finders.get(result.getFinder()));
			row.addEntry("Time [s]", String.format(Locale.ROOT, format, result.getFinalPath().getDuration()));
			row.addEntry("Distance [m]", String.format(Locale.ROOT, format, result.getFinalPath().getDistance()));
			row.addEntry("Distance to tmin [m]", String.format(Locale.ROOT, format, result.getFinalPath().getTravelledDistance(tmin)));
			row.addEntry("Score", String.format(Locale.ROOT, format, result.getMetricResult().getScore()));
			
			writer.addRow(row);
		}
		
		writer.export(file);
	}
	
	public static void createTimeDistributionMap(World world, PathFinder pathFinder, File file, boolean showValues) {
		SimulatorSettings settings = new SimulatorSettings();
		
		Grid<Double> timeDistribution = new Grid<Double>(world.getWidth(), world.getHeight(), 0.0);
		
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				System.err.print("RUNNING EVALUATION AT %02d/%02d\r".formatted(x, y));
				
				Comparator comp = new Comparator(true);
				Configuration config = new Configuration(List.of(pathFinder), List.of(new SimpleTrajectory(settings, true)), EvaluationConfig.settings,
						new AUPCMetric(), x, y, world);
				List<SimulationResult> results = comp.evaluate(config);

				double time = results.get(0).getFinalPath().getDuration();
				timeDistribution.set(x, y, time);				
			}
		}

		double minTime = timeDistribution.minValue();
		double maxTime = timeDistribution.maxValue();
		
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				double orig = timeDistribution.get(x, y);
				double normalized = Interpolation.Linear(orig, minTime, 0.0, maxTime, 1.0);
				timeDistribution.set(x, y, normalized);
			}
		}
		
		// Store Grid
		BufferedImage gridImage = GridRenderer.renderImage(timeDistribution, 1024, EvaluationConfig.rampProbability, showValues, null);
		try {
			ImageIO.write(gridImage, "PNG", file);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void createWinDistributionMap(World world, PathFinder highlight, List<PathFinder> finders, TrajectoryPlanner planner,
			String title, String description, boolean showValues,
			File file, Function<SimulationResult, Double> winCriteria, boolean lowestWins) {
		

		System.err.println("Evaluating Win Distribution for Map %s".formatted(world.getName()));	
		Grid<ListWrapper<PathFinder>> winMap = new Grid<ListWrapper<PathFinder>>(world.getWidth(), world.getHeight(), null);
		
		List<Future<Ranking<SimulationResult>>> futures = new ArrayList<>();
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				Future<Ranking<SimulationResult>> future = rankSingleFuture(world, x, y,
						finders, List.of(planner), winCriteria, lowestWins);
				futures.add(future);
			}
		}
		
		for(Future<Ranking<SimulationResult>> future : futures) {
			Ranking<SimulationResult> ranking = rankLater(future);
			
			List<PathFinder> winners = ranking.getForRank(1).stream()
					.map(sr -> sr.getFinder())
					.collect(Collectors.toUnmodifiableList());
			winMap.set(ranking.getSorted().get(0).getConfiguration().getStartX(), ranking.getSorted().get(0).getConfiguration().getStartY(), new ListWrapper<PathFinder>(winners));
		}		
		
		// Store Grid
		BufferedImage gridImage = DistributionMapRenderer.renderImage(world.getProbabilities(), winMap, highlight,
				title, description, 1024, showValues, EvaluationConfig.rampProbability);
		try {
			ImageIO.write(gridImage, "PNG", file);
		}catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	public static Future<Ranking<SimulationResult>> rankSingleFuture(World world, int startX, int startY,
			List<PathFinder> finders, List<TrajectoryPlanner> planners,
			Function<SimulationResult, Double> winCriteria, boolean lowestWins) {
		
		Callable<Ranking<SimulationResult>> callable = () -> {
			System.err.print("RUNNING EVALUATION AT %02d/%02d\r".formatted(startX, startY));
			Comparator comp = new Comparator(true);
			List<SimulationResult> results = comp.evaluate(new Configuration(finders, planners, EvaluationConfig.settings,
					new AUPCMetric(), startX, startY, world));
			return new Ranking<>(results, winCriteria, lowestWins);
		};
		return EvaluationConfig.service.submit(callable);
	}
	
	public static Ranking<SimulationResult> rankLater(Future<Ranking<SimulationResult>> future){
		try {
			return future.get();
		}catch (InterruptedException e) {
			e.printStackTrace();
		}catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Ranking<SimulationResult> rankSingleImmediate(World world, int startX, int startY,
			List<PathFinder> finders, List<TrajectoryPlanner> planners,
			Function<SimulationResult, Double> winCriteria, boolean lowestWins) {
		Future<Ranking<SimulationResult>> future = rankSingleFuture(world, startX, startY, finders, planners, winCriteria, lowestWins);
		return rankLater(future);
	}
	
	public static void evaluateMax(File dir, List<PathFinder> finders,
			String filename, String caption, String label, RankColoring coloring,
			Function<SimulationResult, Double> winCriteria, boolean lowestWins) {

		LatexTableWriter writer = new LatexTableWriter();
		final String leftColumn = "Map";
		writer.setLeftColumn(leftColumn);
		if(caption != null && !caption.isBlank())	writer.setCaption(caption);
		if(label != null && !label.isBlank())		writer.setLabel(label);

		for(World world : EvaluationConfig.worlds) {
			System.err.println("RUNNING EVALUATION ON %s".formatted(world.getName()));
			// Start at Point with highest propability
			GridValue<Double> max = world.getProbabilities().max();
			System.err.println("MAX %f is at %d/%d".formatted(max.getValue(), max.getX(), max.getY()));
			int startX = max.getX();
			int startY = max.getY();

			Ranking<SimulationResult> ranking = rankSingleImmediate(world, startX, startY,
					finders, EvaluationConfig.planners,
					winCriteria, lowestWins);
						
			LatexTableRow row = new LatexTableRow();
			row.addEntry(leftColumn, world.getName());
			
			for(SimulationResult result : ranking.getSorted()) {
				Color color = coloring.getColor(ranking, result);
				String name = result.getFinder().getName();
				String data = String.format(Locale.ROOT, "%.2f", winCriteria.apply(result));			

				row.addEntry(name, new LatexTableEntry(data, color));

				System.err.println("%02d\t%s:\t%s".formatted(ranking.getRank(result), name, data));
			}
			
			writer.addRow(row);
		}
		

		writer.export(new File(dir, filename));
	}
	
	public static void evaluateAll(File dir, List<PathFinder> finders,
			String filename, String caption, String label, RankColoring coloring,
			Function<SimulationResult, Double> winCriteria, boolean lowestWins) {
		
		LatexTableWriter writer = new LatexTableWriter();
		final String leftColumn = "Map";
		writer.setLeftColumn(leftColumn);
		if(caption != null && !caption.isBlank())	writer.setCaption(caption);
		if(label != null && !label.isBlank())		writer.setLabel(label);
		
		for(World world : EvaluationConfig.worlds) {
			System.err.println("RUNNING EVALUATION ON %s".formatted(world.getName()));

			LatexTableRow row = new LatexTableRow();
			row.addEntry(leftColumn, world.getName());
			
			Map<String, Integer> wins = new HashMap<>();
			int winCount = 0;
			
			List<Future<Ranking<SimulationResult>>> futures = new ArrayList<>();
			for(int x = 0; x < world.getWidth(); x++) {
				for(int y = 0; y < world.getHeight(); y++) {
					Future<Ranking<SimulationResult>> future = rankSingleFuture(world, x, y,
							finders, EvaluationConfig.planners, winCriteria, lowestWins);
					futures.add(future);
				}
			}
			
			for(Future<Ranking<SimulationResult>> future : futures) {
				Ranking<SimulationResult> ranking = rankLater(future);
				
				for(SimulationResult result : ranking.getForRank(1)) {
					wins.put(result.getFinder().getName(), wins.getOrDefault(result.getFinder().getName(), 0)+1);
				}
				winCount++;			
			}
			
			Ranking<Entry<String, Integer>> ranking = new Ranking<>(new ArrayList<>(wins.entrySet()), e -> 1.0 * e.getValue());			
			for(Entry<String, Integer> e : wins.entrySet()) {
				Color color = coloring.getColor(ranking, e);				
				
				double percentage = e.getValue() * 100.0 / winCount;
				
				String data = String.format(Locale.ROOT, "%d (%2.1f %%)", e.getValue(), percentage);
				row.addEntry(e.getKey(), new LatexTableEntry(data, color));

				System.err.println("%02d\t%s:\t%s".formatted(ranking.getRank(e), e.getKey(), data));
			}
			
			writer.addRow(row);
		}
		
		writer.export(new File(dir, filename));
	}
	
	public static void findOthersBetter(File dirComparison, World world, List<PathFinder> finders, PathFinder highlight,
			Function<SimulationResult, Double> winCriteria, boolean lowestWins, int count) {
		if(dirComparison == null || world == null || highlight == null) throw new NullPointerException();
		
		System.err.println("Finding better algorithms than %s on map %s".formatted(highlight.getName(), world.getName()));
		
		
		Map<Double, List<SimulationResult>> resultsByScoreDifference = new HashMap<>();
		
		List<Future<Ranking<SimulationResult>>> futures = new ArrayList<>();
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				Future<Ranking<SimulationResult>> future = rankSingleFuture(world, x, y,
						finders, EvaluationConfig.planners, winCriteria, lowestWins);
				futures.add(future);
			}
		}
		
		for(Future<Ranking<SimulationResult>> future : futures) {
			Ranking<SimulationResult> ranking = rankLater(future);
			
			if(!ranking.getForRank(1).stream().map(res -> res.getFinder()).collect(Collectors.toList()).contains(highlight)) {
				// CASE WHERE SPIRAL FINDER IS NOT A WINNER
				
				SimulationResult winnerResult = ranking.getForRank(1).get(0);
				SimulationResult highlightResult = ranking.getRaw().stream()
						.filter(res -> res.getFinder().equals(highlight))
						.findAny().get();
				
				double duration0 = winnerResult.getFinalPath().getDuration();
				double duration1 = highlightResult.getFinalPath().getDuration();
				
				double diff = duration1 - duration0;
				
				resultsByScoreDifference.put(diff, ranking.getSorted());					
			}
		}		
		
		List<Double> differences = new ArrayList<>(resultsByScoreDifference.keySet());
		differences.sort((d0, d1) -> Double.compare(d1, d0));

		String worldName = world.getName().toLowerCase().replace(" ", "_");
		for(int i = 0; i < Math.min(differences.size(), count); i++) {
			Double diff = differences.get(i);
			List<SimulationResult> results = resultsByScoreDifference.get(diff);
			String filename = "%s_%02d_%02d.png".formatted(worldName, results.get(0).getConfiguration().getStartX(), results.get(0).getConfiguration().getStartY());
			
			BufferedImage image = SimulationResultRenderer.renderComparison(resultsByScoreDifference.get(diff),
					1024, true, false, EvaluationConfig.rampProbability, EvaluationConfig.rampSpeed);
			
			try {
				File file = new File(dirComparison, filename);
				file.mkdirs();
				ImageIO.write(image, "PNG", file);
			}catch (IOException e) {
				e.printStackTrace();
			}			
		}	
	}
	
	// START
	public static void fitImage(File inputFile, File outputFile, int imageSize, int border) {
		fitImage(inputFile, outputFile, imageSize, border, border);
	}
	
	public static void fitImage(File inputFile, File outputFile, int imageSize, int borderX, int borderY) {
		try {
			BufferedImage image = ImageIO.read(inputFile);
			fitImage(image, outputFile, imageSize, borderX, borderY);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static BufferedImage fitImage(BufferedImage input, int imageSize, int border) {
		return fitImage(input, imageSize, border, border);
	}

	public static BufferedImage fitImage(BufferedImage input, int imageSize, int borderX, int borderY) {
		BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = image.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		g.setBackground(new Color(255, 255, 255, 0));
		g.clearRect(0, 0, imageSize, imageSize);
		g.drawImage(input, borderX, borderY, imageSize-2*borderX, imageSize-2*borderY, null);
		g.dispose();

		return image;
	}
	
	public static void fitImage(BufferedImage input, File outputFile, int imageSize, int border) {
		fitImage(input, outputFile, imageSize, border, border);
	}
	
	public static void fitImage(BufferedImage input, File outputFile, int imageSize, int borderX, int borderY) {
		
		BufferedImage image = fitImage(input, imageSize, borderX, borderY);	
		
		try {
			ImageIO.write(image, "PNG", outputFile);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// ADD BORDER
	public static void addBorder(File inputFile, File outputFile, int border) {
		addBorder(inputFile, outputFile, border, border);
	}
	
	public static void addBorder(File inputFile, File outputFile, int borderX, int borderY) {
		try {
			BufferedImage image = ImageIO.read(inputFile);
			addBorder(image, outputFile, borderX, borderY);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static BufferedImage addBorder(BufferedImage input, int border) {
		return addBorder(input, border, border);
	}

	public static BufferedImage addBorder(BufferedImage input, int borderX, int borderY) {
		int width	= input.getWidth()	+ 2*borderX;
		int height	= input.getHeight()	+ 2*borderY;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = image.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		g.setBackground(new Color(255, 255, 255, 0));
		g.clearRect(0, 0, width, height);
		g.drawImage(input, borderX, borderY, input.getWidth(), input.getHeight(), null);
		g.dispose();

		return image;
	}
	
	public static void addBorder(BufferedImage input, File outputFile, int border) {
		fitImage(input, outputFile, border, border);
	}
	
	public static void addBorder(BufferedImage input, File outputFile, int borderX, int borderY) {
		
		BufferedImage image = addBorder(input, borderX, borderY);	
		
		try {
			ImageIO.write(image, "PNG", outputFile);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void exportColorRamp(ColorRamp ramp, int size, boolean horizontal,
			String title, boolean showDecimals,
			File file) {
		exportColorRamp(ramp, size, horizontal, title, showDecimals, val -> val, false, file);
	}
	
	public static void exportColorRamp(ColorRamp ramp, int size, boolean horizontal,
			String title, boolean showDecimals, Function<Double, Double> remapLabels,
			boolean disableText, File file) {

		int border		= size / 64;
		int barWidth	= size / 16;
		int fontSize	= size / 64;
		
		int additionalSpace	= border;
		if(horizontal) {
			additionalSpace += 2*fontSize;
		}else {
			additionalSpace += 2*fontSize;
			if(showDecimals) additionalSpace += 2*fontSize;
		}
		
		
		int canvasWidth = border + barWidth + additionalSpace + border;
		int width		= horizontal ? size			: canvasWidth;
		int height		= horizontal ? canvasWidth	: size;
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = image.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		g.setBackground(new Color(255, 255, 255, 0));
		g.clearRect(0, 0, image.getWidth(), image.getHeight());
		ColorRampRenderer.render(g, ramp,
				2*border, border, horizontal ? width-2*border : border+barWidth, horizontal ? border+barWidth : height-2*border, horizontal,
				new Font("", Font.BOLD, disableText ? 0 : fontSize), showDecimals, 1, remapLabels, title);
		g.dispose();		
		
		try {
			ImageIO.write(image, "PNG", file);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static Color[] colors = new Color[]{Color.RED, Color.BLUE, Color.GREEN, Color.BLACK, Color.ORANGE, Color.CYAN, Color.PINK, Color.MAGENTA, Color.YELLOW};
	
	public static void metricGraph(World world, Map<PathFinder, String> finders, TrajectoryPlanner planner,
			int startX, int startY,
			File fileGraph, File fileCSV, int size, int border) {
		metricGraph(world, finders, planner, startX, startY, fileGraph, fileCSV,size, border, 2.0/3.0, 2.0/3.0);
	}
	
	public static void metricGraph(World world, Map<PathFinder, String> finders, TrajectoryPlanner planner,
			int startX, int startY,
			File fileGraph, File fileCSV, int size, int border,
			double legendX, double legendY) {
		
		Ranking<SimulationResult> ranking = rankSingleImmediate(world, startX, startY,
				new ArrayList<>(finders.keySet()), EvaluationConfig.planners,
				val -> 0.0, false);
		
		
		List<LineGraphDataSet> data = new ArrayList<>();
		int col = 0;
		for(SimulationResult result : ranking.getRaw()) {			
			LineGraphDataSet set = new LineGraphDataSet(finders.get(result.getFinder()), colors[col % colors.length], result.getMetricResult().getCurve());
			data.add(set);
			col++;
		}
		
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

		
		double t_min = data.stream().mapToDouble(graph -> graph.maxX()).min().orElse(0.0);
		LineGraphRenderer.drawLineGraph(g, data,
				border, border, canvasWidth, canvasHeight,
				"Time [s]", "Accumulated Probability",
				true, true, t_min, true,
				false, 0, 0, 0, 0);
		
		LineGraphRenderer.drawLegend(g, data,
				(int) (2*size-border-canvasWidth*legendX), border, (int) (canvasWidth*legendY), canvasHeight, true);
		
		
		g.dispose();
		
		try {
			ImageIO.write(image, "PNG", fileGraph);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		if(fileCSV != null) LineGraphRenderer.export(data, "Time [s]", "Accumulated Probability", fileCSV);
	}
	

	public static void combineImages(int height, int border, List<File> files, File out, boolean vertical) {
		combineImages(height, border, files,
				List.of(), Color.WHITE, 16, 0,
				out, vertical);
	}
	
	public static void combineImages(int height, int border, List<File> files,
			List<String> labels, Color labelColor, int labelSize, int labelInset,
			File out, boolean vertical) {
		List<Color> labelColors = files.stream().map(file -> labelColor).collect(Collectors.toList());
		combineImages(height, border, files, labels, labelColors, labelSize, labelInset, out, vertical);
	}
	
	public static void combineImages(int size, int border, List<File> files,
			List<String> labels, List<Color> labelColors, int labelSize, int labelInset,
			File out, boolean vertical) {
		
		// LOAD IMAGES
		List<BufferedImage> images = new ArrayList<>(files.size());
		try {
			for(File file : files) {
				BufferedImage image = ImageIO.read(file);
				images.add(image);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int imgSize = size - 2*border;
		int finalSize = border;	// EITHER WIDTH OR HEIGHT

		Map<BufferedImage, Integer> imageSizes = new HashMap<>(files.size());
		for(BufferedImage image : images) {
			if(vertical) {
				double aspect = 1.0 * imgSize / image.getWidth();
				int newHeight = (int) Math.ceil(image.getHeight() * aspect);
				imageSizes.put(image, newHeight);
				
				finalSize += newHeight + border;
			}else {
				double aspect = 1.0 * imgSize / image.getHeight();
				int newWidth = (int) Math.ceil(image.getWidth() * aspect);
				imageSizes.put(image, newWidth);
				
				finalSize += newWidth + border;
			}			
		}
		
		int width = vertical ? size : finalSize;
		int height = vertical ? finalSize : size;
		
		BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = combined.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		
		g.setFont(g.getFont().deriveFont(0, labelSize));
		
		int x = border;
		int y = border;		
		for(int i = 0; i < images.size(); i++) {
			BufferedImage image = images.get(i);
			String label = i < labels.size() ? labels.get(i) : new String();
			Color color = i < labelColors.size() ? labelColors.get(i) : Color.PINK;
			
			int imgWidth	= vertical ? imgSize : imageSizes.get(image);
			int imgHeight	= vertical ? imageSizes.get(image) : imgSize;

			g.drawImage(image, x, y, imgWidth, imgHeight, null);
			g.setColor(color);
			TextDrawer.drawString(g, label, x+5+labelInset, y+5+labelInset, HorizontalAlignment.LEFT, VerticalAlignment.TOP);
			
			if(vertical) {
				y += imgHeight + border;
			}else {
				x += imgWidth + border;
			}
		}
		
		g.dispose();
		
		try {
			ImageIO.write(combined, "PNG", out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void combineImagesGrid(int height, int border, int rows, int columns, List<File> files, File out) {
		combineImagesGrid(height, border, rows, columns, files,
				List.of(), Color.WHITE, 16, 0,
				out);
	}
	
	public static void combineImagesGrid(int height, int border, int rows, int columns, List<File> files,
			List<String> labels, Color labelColor, int labelSize, int labelInset,
			File out) {
		
		// LOAD IMAGES
		final List<BufferedImage> images = new ArrayList<>(files.size());
		try {
			for(File file : files) {
				BufferedImage image = ImageIO.read(file);
				images.add(image);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		final int imgHeight = height - 2*border;
		
		Map<BufferedImage, Integer> imageWidths = new HashMap<>(files.size());
		for(BufferedImage image : images) {
			double aspect = 1.0 * imgHeight / image.getHeight();
			int newWidth = (int) Math.ceil(image.getWidth() * aspect);
			imageWidths.put(image, newWidth);
		}
		final int maxWidth = imageWidths.values().stream().mapToInt(value -> value).max().orElse(imgHeight);
		final int finalWidth = border + (border + maxWidth) * columns;
		final int finalHeight = border + (border + imgHeight) * rows;
		
		BufferedImage combined = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = combined.createGraphics();
		EvaluationConfig.setRenderSettings(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, finalWidth, finalHeight);
		
		g.setColor(labelColor);
		g.setFont(g.getFont().deriveFont(0, labelSize));
		
		int x = 0;
		int y = 0;		
		for(int i = 0; i < images.size(); i++) {
			final int imageX = border + (border + maxWidth) * x;
			final int imageY = border + (border + imgHeight) * y;
			
			BufferedImage image = images.get(i);
			String label = i < labels.size() ? labels.get(i) : new String();
			
			int width = imageWidths.get(image);
			g.drawImage(image, imageX, imageY, width, imgHeight, null);
			TextDrawer.drawString(g, label, imageX+5+labelInset, imageY+5+labelInset, HorizontalAlignment.LEFT, VerticalAlignment.TOP);
			
			x++;
			if(x >= columns) {
				x = 0;
				y++;
			}
		}
		
		g.dispose();
		
		try {
			ImageIO.write(combined, "PNG", out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
