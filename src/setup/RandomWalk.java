package setup;

public interface RandomWalk {
	boolean isRandomWalk = false;
//	double tick = 0.0005;
//	double p_f_mu = 0;				// fundamental price, geometric Brownian motion, percentage drift
//	double p_f_sigma = 0.001d;		// fundamental price, geometric Brownian motion, percentage volatility
//	double p_f_0 = 300d;			// fundamental price
	
	final long initialFundamentalPrice = (int) Math.pow(10, 6);
	public final double fundamentalBrownianMean = 0;
	public final double fundamentalBrownianVariance = 0.00001;
}
