package agent;

import java.util.ArrayList;
import java.util.Random;


import environment.Message;
import environment.Order;
import environment.Orderbook;
import experiments.Experiment;

public class NoisyFundamentalTrader {
	public static Random random = new Random();
	
	
	public static void submitOrder(Experiment experiment, Orderbook orderbook) {
		int now = experiment.getWorld().getCurrentRound();
		int laggedTime = getLaggedTime(experiment, now); 
		long laggedFundamentalPrice = orderbook.getStock().getFundamentalPrice(laggedTime);
		
		long additiveNoiseToFundamentalEstimate = Math.round(random.nextGaussian() * experiment.st_fund_fundamentalNoiseStd);
		long estimatedFundamentalPrice = laggedFundamentalPrice + additiveNoiseToFundamentalEstimate;

		long laggedBestBuyPrice = orderbook.getLocalBestBuyPriceAtEndOfRound(laggedTime);
		long laggedBestSellPrice = orderbook.getLocalBestSellPriceAtEndOfRound(laggedTime);
		Order.BuySell buysell = null;
		long price = 0;
		/*
		 * If the fundamental price is within the spread, then select to buy or sell with equal probability
		 */
		if(estimatedFundamentalPrice < laggedBestSellPrice & estimatedFundamentalPrice > laggedBestBuyPrice) {
			int type = random.nextInt(2);
			if(type==0) {
				buysell = Order.BuySell.BUY;
				price = laggedBestBuyPrice + experiment.st_fund_tickChange;
			} else {
				buysell = Order.BuySell.SELL;
				price = laggedBestSellPrice - experiment.st_fund_tickChange;
			}
		} else if(estimatedFundamentalPrice < laggedBestSellPrice) {
			/*
			 * The agent believes that the stock is worth less than what other people are
			 * selling it for, so he wants to sell
			 */
			buysell = Order.BuySell.SELL;
			
			price = laggedBestSellPrice - experiment.st_fund_tickChange;
		} else if(estimatedFundamentalPrice > laggedBestBuyPrice) {
			/*
			 * The agent believes that the stock is worth more than what people are
			 * buying for, so he wants to buy the stock
			 */
			buysell = Order.BuySell.BUY;
			price = laggedBestBuyPrice + experiment.st_fund_tickChange;
		} else {
			experiment.getWorld().errorLog.logError("Error in fundamentalist!", experiment);
		}
		price += Math.round(random.nextGaussian()*experiment.st_fund_priceNoiseStd);
		long volume;
		if(experiment.st_randomOrderVolume) {
			volume = experiment.st_minimumVolume;
		} else {
			volume = experiment.st_minimumVolume + Math.round(Math.abs(random.nextGaussian() * experiment.st_volumeNoiseStd));
		}
		new Order(0, now,experiment.st_orderLength, volume, price, Order.Type.MARKET, buysell, null, orderbook, Message.TransmissionType.INSTANTANEOUS, experiment);
	}
	
	public static void sumbitOrderAtRandomOrderbook(Experiment experiment) {
		Random random = new Random();
		ArrayList<Orderbook> orderbooks = experiment.getWorld().getOrderbooks();
		Orderbook orderbook = orderbooks.get(random.nextInt(orderbooks.size()));
		submitOrder(experiment, orderbook);
	}
	
	public static int getLaggedTime(Experiment experiment, int now) {
		return now - experiment.st_minimumDelay - (int) Math.round(Math.abs(random.nextGaussian()*experiment.st_delayStd));
	}
}
