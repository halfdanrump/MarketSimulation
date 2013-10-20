package utilities;
import utilities.P;

public class Parameter {
	
	public static String getPar(String parName) {
		String parVal = "";
		parVal = System.getProperty(parName);
		P.p(String.format("Received parameter %s: %s", parName, parVal));
		if(parVal == null) {
			
			P.p(String.format("Could not get parameter: %s", parName));
			/*
			 * Print to file
			 */
			System.exit(1);
		} 
		return parVal;
		
	}
	
	public static int getAsInt(String parName) {
		String parVal =  getPar(parName);
		return Integer.valueOf(parVal);
	}
	
	public static long getAsLong(String parName) {
		String parVal =  getPar(parName);
		return Long.valueOf(parVal);
	}
	
	public static float getAsFloat(String parName) {
		String parVal =  getPar(parName);
		return Float.valueOf(parVal);
	}
	
	public static String getAsString(String parName) {
		return getPar(parName);
	}
	
	

}
