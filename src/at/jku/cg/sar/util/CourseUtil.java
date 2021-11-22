package at.jku.cg.sar.util;

public class CourseUtil {
	
	/**
	 * Utility to check for a valid heading value
	 * @param heading
	 */
	public static void checkHeading(double heading) {
		if(Double.isNaN(heading)) throw new IllegalArgumentException("Heading " + heading + " is not valid");
		if(heading <   0.0) throw new IllegalArgumentException("Heading " + heading + " is not valid");
		if(heading > 360.0) throw new IllegalArgumentException("Heading " + heading + " is not valid");
	}
	
	public static double maxHeadingChange(double a, double b, double speed) {
		return maxHeadingChange(a, b, speed, 1.0);
	}
	
	public static double maxHeadingChange(double a, double b, double speed, double dt) {
		double heading = Math.atan2(a*dt, speed-b*dt);
		
		// Quick backwards calculation to check if speed stays the same
		double dx  = speed * Math.sin(heading);
		double dy2 = speed * Math.cos(heading);		
		double sum = Math.sqrt(dx*dx + dy2*dy2);
		
		if(Math.abs(sum - speed) > 10E-9) {
			System.err.println("speed  " + speed);
			System.err.println("sum  " + sum);
			throw new IllegalStateException("Original speed of %f results in sum %f".formatted(speed, sum));
		}
		
		if(Double.isNaN(heading)) throw new IllegalStateException();
		heading = Math.toDegrees(heading);
		return heading;
	}
	
	public static double changeHeading(double current, double next, double maxChange) {
		checkHeading(current);
		checkHeading(next);
		
		double diff = getDifference(current, next);
		if(Math.abs(diff) <= maxChange) return next;
		
		double heading = diff < 0.0 ? current-maxChange : current+maxChange;
		heading = makeCourseCorrect(heading);
		
		return heading;
	}
	
	public static double getDifference(double heading0, double heading1) {
		checkHeading(heading0);
		checkHeading(heading1);
	
		double left	 = heading0 - heading1;
		double right = heading1 - heading0;
		
		if(left  < 0.0) left += 360.0;
		if(right < 0.0) right += 360.0;
		
		return left < right ? -left : right;
	}
	
	public static double getDifferenceAbs(double heading0, double heading1) {
		return Math.abs(getDifference(heading0, heading1));
	}
	
	public static boolean isBetween(double heading, double heading0, double heading1) {
		double theta = getDifferenceAbs(heading0, heading1);
		double theta0 = getDifferenceAbs(heading, heading0);
		double theta1 = getDifferenceAbs(heading, heading1);
		
		return (theta0 <= theta && theta1 <= theta);
	}

	public static double makeCourseCorrect(double course) {
		return mod(course, 360.0);
	}
	
	private static double mod(double a, double b) {
		double ret = a % b;
		if(ret < 0) ret += b;
		return ret;
	}

}
