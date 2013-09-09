package agent;


import java.util.ArrayList;
import java.util.Random;

import Experiments.Experiment;

import utilities.Utils;

import environment.Message;
import environment.Order;
import environment.Orderbook;
import environment.Stock;

public class StylizedTrader {

	
	public static Order submitRandomOrder(Order.Type type, Order.BuySell buysell, Orderbook orderbook, Experiment experiment){
		int dispatchTime = experiment.getWorld().getCurrentRound();
		long volume = getStylizedTraderOrderVolume(experiment);
		long price = getStylizedTraderEstimatedPrice(orderbook.getStock(), experiment);
		Order order = new Order(0,dispatchTime, experiment.st_orderLength, volume, price, type, buysell, null, orderbook, Message.TransmissionType.WITH_TRANSMISSION_DELAY, experiment);
		return order;
	}
	
	public static long getStylizedTraderEstimatedPrice(Stock stock, Experiment experiment) {
		long currentFundamental = stock.getFundamentalPrice(experiment.getWorld().getCurrentRound());
		long additivePriceNoise = 0;//Utils.getGaussianInteger(experiment.addivePriceNoiseMean, experiment.additivePriceNoiseStd);
		experiment.getWorld().errorLog.logError("This function is deprecated and should not be called!", experiment);
		long price = currentFundamental + additivePriceNoise;
		return price;
	}
	
	public static long getStylizedTraderOrderVolume(Experiment experiment) {
		long volume;
		if(experiment.st_randomOrderVolume){
			volume = Utils.getNonNegativeGaussianInteger(experiment.st_volumeNoiseStd, experiment.st_minimumVolume);
		} else{
			volume = experiment.st_minimumVolume;
		}
		return volume;
	}
	
	
	public static Order submitRandomOrder(Experiment experiment){
		/*
		 * Gets a random orderbook and submits and order to it
		 */
		Order order = null;
		Random random = new Random();
		ArrayList<Orderbook> orderbooks = experiment.getWorld().getOrderbooks();
		Orderbook orderbook = orderbooks.get(random.nextInt(orderbooks.size()));
		long randomlong = random.nextInt(4);
		if(randomlong == 0){
			order = submitRandomLimitBuyOrder(orderbook, experiment);
		} else if(randomlong == 1){
			order = submitRandomLimitSellOrder(orderbook, experiment);
		} else if(randomlong == 2){
			order = submitRandomMarketBuyOrder(orderbook, experiment);
		} else if (randomlong == 3){
			order = submitRandomMarketSellOrder(orderbook, experiment);
		}
		return order;
	}
	
	public static Order submitRandomMarketBuyOrder(Orderbook orderbook, Experiment experiment){
		return submitRandomOrder(Order.Type.MARKET, Order.BuySell.BUY, orderbook, experiment);
	}
	
	public static Order submitRandomMarketSellOrder(Orderbook orderbook, Experiment experiment){
		return submitRandomOrder(Order.Type.MARKET, Order.BuySell.SELL, orderbook, experiment);
	}
	
	public static Order submitRandomLimitBuyOrder(Orderbook orderbook, Experiment experiment){
		return submitRandomOrder(Order.Type.LIMIT, Order.BuySell.BUY, orderbook, experiment);
	}
	
	public static Order submitRandomLimitSellOrder(Orderbook orderbook, Experiment experiment){
		return submitRandomOrder(Order.Type.LIMIT, Order.BuySell.SELL, orderbook, experiment);
	}
}
