package at.jku.cg.sar.trajectory;

public enum ScanPattern {
	T_B(180.0), B_T(  0.0),
	L_R( 90.0), R_L(270.0),
	LT_RB(135.0), RB_LT(315.0),
	RT_LB(225.0), LB_RT( 45.0);
	
	
	
	
	
	private final double heading;
	
	private ScanPattern(double heading) {
		this.heading = heading;
	}
	
	
	public static double difference(double heading0, double heading1) {
		double diff = (heading1 - heading0) % 360.0;
		if(heading0 > heading1) diff += 360.0;
		if(diff >= 180.0) diff = -(diff - 360.0);
		return diff;
	}
	
	public static ScanPattern fromHeading4(double heading) {
		ScanPattern pattern = null;
		double difference = 360.0;
		
		for(ScanPattern p : new ScanPattern[] {T_B, B_T, L_R, R_L}) {
			double diff = difference(heading, p.heading);
			if(diff < difference) {
				pattern = p;
				difference = diff;
			}
		}
		
		return pattern;
	}
	
	public static ScanPattern fromHeading(double heading) {
		ScanPattern pattern = null;
		double difference = 360.0;
		
		for(ScanPattern p : values()) {
			double diff = difference(heading, p.heading);
			if(diff < difference) {
				pattern = p;
				difference = diff;
			}
		}
		
		return pattern;
	}
}
