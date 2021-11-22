package at.jku.cg.sar.core.grid;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import javax.imageio.ImageIO;

public class GridIO {
	
	public static Grid<Double> fromImageFile(File file, boolean normalize, double min){
		try {
			BufferedImage image = ImageIO.read(file);
			return fromBufferedImage(image, normalize, min);
		}catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Grid<Double> fromBufferedImage(BufferedImage image, boolean normalize, double min){
		Grid<Double> grid = new Grid<>(image.getWidth(), image.getHeight(), 0.0);
		
		for(int x = 0; x < grid.getWidth(); x++) {
			for(int y = 0; y < grid.getHeight(); y++) {
				Color col = new Color(image.getRGB(x, y));
				double bw = (col.getRed() + col.getGreen() + col.getGreen()) / 3.0;
				
				double value = normalize ? bw / 255.0 : bw;
				value = Math.max(value, min);
				grid.set(x, y, value);
			}
		}		
		
		return grid;
	}
	
	public static void toImageFile8(File file, Grid<Double> grid){
		try {
			BufferedImage image = toBufferedImage8(grid);
			ImageIO.write(image, "PNG", file);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static BufferedImage toBufferedImage8(Grid<Double> grid) {
		BufferedImage image = new BufferedImage(grid.getWidth(), grid.getHeight(), BufferedImage.TYPE_BYTE_GRAY);		
		
		for(int x = 0; x < grid.getWidth(); x++) {
			for(int y = 0; y < grid.getHeight(); y++) {
				int value = (int) Math.round(255 * grid.get(x, y));
				
				Color col = new Color(value, value, value);
				image.setRGB(x, y, col.getRGB());
			}
		}
		
		return image;
	}
	
	
	public static void toImageFile16(File file, Grid<Double> grid){
		try {
			BufferedImage image = toBufferedImage16(grid);
			ImageIO.write(image, "PNG", file);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static BufferedImage toBufferedImage16(Grid<Double> grid) {
		return toBufferedImage16(grid, value -> value);
	}
	
	public static BufferedImage toBufferedImage16(Grid<Double> grid, Function<Double, Double> mapping) {
		BufferedImage image = new BufferedImage(grid.getWidth(), grid.getHeight(), BufferedImage.TYPE_USHORT_GRAY);		
		WritableRaster raster = image.getRaster();
		
		for(int x = 0; x < grid.getWidth(); x++) {
			for(int y = 0; y < grid.getHeight(); y++) {
				// Map grid value into 0.0 to 1.0 range
				double mapped = mapping.apply(grid.get(x, y));
				raster.setSample(x, y, 0, 2.0 * Short.MAX_VALUE * mapped);
			}
		}
		return image;
	}
	
}
