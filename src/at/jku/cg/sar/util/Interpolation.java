package at.jku.cg.sar.util;

/**
 * This class holds multiple function for interpolating between given values
 * @author Rudolf Ortner
 * @since 0.00
 */
public class Interpolation {

	/**
	 * Performs linear interpolation between two given points
	 * @param x X coordinate where you want to interpolate between the given points
	 * @param x1 X coordinate of the first point
	 * @param y1 Y coordinate of the first point
	 * @param x2 X coordinate of the second point
	 * @param y2 Y coordinate of the second point
	 * @return Y value for the given X input
	 */
	public static double Linear(double x, double x1, double y1, double x2, double y2){
		if(x1 == x2) return y1;
		return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
	}

	/**
	 * Simplified linear interpolation.
	 * Works more like a mix slider between the two input parameters
	 * @param x Acts like a factor (0 returns Y1, 1 returns Y2)
	 * @param y1 First input value
	 * @param y2 Second input value
	 * @return Mix between the two input values
	 */
	public static double Lerp(double x, double y1, double y2){
		return Linear(x, 0.0, y1, 1.0, y2);
	}
	
	//TODO Interpolation parameters
	/**
	 * Performs bilinear interpolation
	 * Mostly used for interpolating in image data
	 * @see <a href="https://en.wikipedia.org/wiki/Bilinear_interpolation">Bilinear Interpolation</a>
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param f1
	 * @param f2
	 * @param f3
	 * @param f4
	 * @return
	 */
	public static double Bilinear(double x, double y, double x1, double y1, double x2, double y2, double f1, double f2, double f3, double f4) {
		
		double R1 = Linear(x, x1, f3, x2, f4);
		double R2 = Linear(x, x1, f1, x2, f2);
		
		double result = Linear(y, y1, R1, y2, R2);
		return result;
	}
	
	//TODO Interpolation parameters
	/**
	 * Performs trilinear interpolation
	 * Mostly used for interpolating in image data
	 * @see <a href="https://en.wikipedia.org/wiki/Trilinear_interpolation">Trilinear Interpolation</a>
	 * @param x
	 * @param y
	 * @param z
	 * @param x0
	 * @param x1
	 * @param y0
	 * @param y1
	 * @param z0
	 * @param z1
	 * @param c000
	 * @param c001
	 * @param c010
	 * @param c011
	 * @param c100
	 * @param c101
	 * @param c110
	 * @param c111
	 * @return
	 */
	public static double Trilinear(double x, double y, double z, double x0, double x1, double y0, double y1, double z0, double z1, double c000, double c001, double c010, double c011, double c100, double c101, double c110, double c111) {
		double c00 = Linear(x, x0, c000, x1, c100);
		double c01 = Linear(x, x0, c001, x1, c101);
		double c10 = Linear(x, x0, c010, x1, c110);
		double c11 = Linear(x, x0, c011, x1, c111);
		
		double c0 = Linear(y, y0, c00, y1, c01);
		double c1 = Linear(y, y0, c10, y1, c11);
		
		double c = Linear(z, z0, c0, z1, c1);
		
		return c;
	}
	
}
