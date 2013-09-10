package utilities;

import java.util.ArrayList;
import java.util.Random;

public class Utils {
	private static Random random = new Random();
	
	public static int getRandomUniformInteger(int minimum, int maximum){
		int number = (int) random.nextInt(maximum-minimum+1)+minimum;
		return number;
	}
	
	public static long getRandomUniformLong(long minimum, long maximum){
		long number = Math.round(random.nextDouble()*(maximum - minimum) + minimum);
//		long number = random.nextInt(maximum-minimum+1)+minimum;
		return number;
	}
	
	
	public static long getGaussianInteger(double mean, double std){
		double noise = random.nextGaussian() * std + mean;
		return (int) Math.round(noise);
	}
	
	
	public static void initializeStringArrayWithEmptyStrings(ArrayList<String> arrayList, int capacity, String initialValue) {
		for(int i = 0; i<capacity; i++) {
			arrayList.add(initialValue);
		}
	}
	
	public static String convertArrayListToString(@SuppressWarnings("rawtypes") ArrayList arrayList) {
		return arrayList.toString().replace("[", "").replace("]", "").replace(" ", "");
	}
	

}
