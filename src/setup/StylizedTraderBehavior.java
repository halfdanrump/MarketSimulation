package setup;

public interface StylizedTraderBehavior {
	public int orderLength = 1000;
	
	public boolean randomOrderVolume = false;
	public long constantVolume = 100;
	public double volumeMean = 100d;
	public double volumeStd = 10d;
	
	public double addivePriceNoiseMean = 0d;
	public double additivePriceNoiseStd = 50d;
	
	
	public long fundamentalistWeightMean = 0;
	public long fundamentalistWeightStd = 1;
//	public long fundamentalMeanReversion = 
	
	public long chartistWeightMean = 0;
	public long chartistWeightStd = 0;
	
	public long noiseWeightMean = 0;
	public long noiseWeightStd = 1;
	
	//SlowTraderMeanReversionTimeInterval = 2000;
//	double g_1_mu = 0d;				// expected value of fundamentalist weight, g_i_1
//	double g_1_sigma = 5d;			// variance of fundamentalist weight, g_i_1 
//	double g_2_mu = 0d;				// expected value of chartist weight, g_2_1	
//	double g_2_sigma = 20d;			// variance of chartist weight, g_i_2
//	double g_3_mu = 0d;				// expected value of noise weight, g_i_3
//	double g_3_sigma = 1d;			// variance of noise weight, g_i_3
	
//	public static long decideVolume();
//	public long decidePrice();
//	public Order getOrder();
}

