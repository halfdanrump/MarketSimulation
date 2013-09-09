package agent;

import java.util.ArrayList;
import java.util.Random;

import Experiments.Experiment;

import environment.Message;
import environment.Order;
import environment.Orderbook;

public class NoisyFundamentalNoisyPriceTrader {
	private static Random random = new Random();
	public static void submitOrder(Experiment experiment, Orderbook orderbook) {
		int now = experiment.getWorld().getCurrentRound();
		int laggedTime = now - experiment.st_minimumDelay;
		long laggedFundamentalPrice = orderbook.getStock().getFundamentalPrice(laggedTime);
		long additiveNoiseToFundamentalEstimate = Math.round(random.nextGaussian() * experiment.st_fund_additivePriceNoiseStd);
		long estimatedFundamentalPrice = laggedFundamentalPrice + additiveNoiseToFundamentalEstimate;

		long laggedBestBuyPrice = orderbook.getLocalBestBuyPriceAtEndOfRound(laggedTime);
		long laggedBestSellPrice = orderbook.getLocalBestSellPriceAtEndOfRound(laggedTime);
		Order.BuySell buysell = null;
		long price = 0;
		
		if(estimatedFundamentalPrice < laggedBestSellPrice) {
			/*
			 * The agent believes that the stock is worth less than what other people are
			 * selling it for, so he wants to sell
			 */
			buysell = Order.BuySell.SELL;
			price = laggedBestSellPrice - Math.abs(Math.round(random.nextGaussian()));
		} else if(estimatedFundamentalPrice > laggedBestBuyPrice) {
			/*
			 * The agent believes that the stock is worth more than what people are
			 * buying for, so he wants to buy the stock
			 */
			buysell = Order.BuySell.BUY;
			price = laggedBestSellPrice + Math.abs(Math.round(random.nextGaussian()));
		} else {
			experiment.getWorld().errorLog.logError("Error in fundamentalist!", experiment);
			System.exit(1);
		}
		new Order(0, now,experiment.st_orderLength, experiment.st_fund_orderVolume, price, Order.Type.MARKET, buysell, null, orderbook, Message.TransmissionType.INSTANTANEOUS, experiment);
	}
	
	public static void sumbitOrderAtRandomOrderbook(Experiment experiment) {
		Random random = new Random();
		ArrayList<Orderbook> orderbooks = experiment.getWorld().getOrderbooks();
		Orderbook orderbook = orderbooks.get(random.nextInt(orderbooks.size()));
		submitOrder(experiment, orderbook);
	}
}
