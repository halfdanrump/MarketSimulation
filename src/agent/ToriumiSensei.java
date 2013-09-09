package agent;

import java.util.ArrayList;
import java.util.Random;

import utilities.Utils;

import Experiments.Experiment;
import environment.Message;
import environment.NoOrdersException;
import environment.Order;
import environment.Stock;
import environment.Order.BuySell;
import environment.Orderbook;

public class ToriumiSensei {
	private static Random random = new Random();
	
	public static Order submitRandomOrder(Experiment experiment){
		/*
		 * Gets a random orderbook and submits and order to it
		 */
		Order order = null;
		Random random = new Random();
		ArrayList<Orderbook> orderbooks = experiment.getWorld().getOrderbooks();
		Orderbook orderbook = orderbooks.get(random.nextInt(orderbooks.size()));
		long i = random.nextInt(4);
		if(i == 0){
			order = submitRandomLimitBuyOrder(orderbook, experiment);
		} else if(i == 1){
			order = submitRandomLimitSellOrder(orderbook, experiment);
		} else if(i == 2){
			order = submitRandomMarketBuyOrder(orderbook, experiment);
		} else if (i == 3){
			order = submitRandomMarketSellOrder(orderbook, experiment);
		}
//		System.out.println(String.format("side: %s, price: %s", order.getBuySell(), order.getPrice()));
		return order;
	}
	
	public static Order submitRandomMarketBuyOrder(Orderbook orderbook, Experiment experiment){
		return createOrder(Order.Type.MARKET, Order.BuySell.BUY, orderbook, experiment);
	}
	
	public static Order submitRandomMarketSellOrder(Orderbook orderbook, Experiment experiment){
		return createOrder(Order.Type.MARKET, Order.BuySell.SELL, orderbook, experiment);
	}
	
	public static Order submitRandomLimitBuyOrder(Orderbook orderbook, Experiment experiment){
		return createOrder(Order.Type.LIMIT, Order.BuySell.BUY, orderbook, experiment);
	}
	
	public static Order submitRandomLimitSellOrder(Orderbook orderbook, Experiment experiment){
		return createOrder(Order.Type.LIMIT, Order.BuySell.SELL, orderbook, experiment);
	}
	
	public static Order createOrder(Order.Type type, Order.BuySell buysell, Orderbook orderbook, Experiment experiment){
		int dispatchTime = experiment.getWorld().getCurrentRound();
		long volume = getStylizedTraderOrderVolume(experiment);
		long price = getToriumiTraderEstimatedPrice(orderbook, experiment, buysell);
		Order order = new Order(0,dispatchTime, experiment.st_orderLength, volume, price, type, buysell, null, orderbook, Message.TransmissionType.WITH_TRANSMISSION_DELAY, experiment);
		order.printOrderDetails();
		return order;
	}
	
	public static long getToriumiTraderEstimatedPrice(Orderbook ob, Experiment experiment, BuySell buysell) {
		/*
		 * 
		 */
//		double w1 = random.nextDouble();
//		double w2 = random.nextDouble();
//		double w3 = random.nextDouble();
//		double wt = w1 + w2 + w3;
//		double wFundamental = w1 / wt;
//		double wPrice = w2 / wt;
//		double wNoise = w3 / wt;
		double wFundamental = 1;
		double wPrice = 0;
		double wNoise = 0;
		
		int currentRound = experiment.getWorld().getCurrentRound();
		int laggedRound = currentRound - experiment.st_minimumDelay;
		if(laggedRound < 0) {
			laggedRound = 0;
		} 
		long price = 0;
		long laggedFundamental = ob.getStock().getFundamentalPrice(laggedRound);
		long laggedBestBuy = ob.getLocalBestBuyPriceAtEndOfRound(laggedRound);
		long laggedBestSell = ob.getLocalBestSellPriceAtEndOfRound(laggedRound);
		if(buysell == Order.BuySell.BUY) {
			double noise = (double) laggedBestBuy + random.nextGaussian() * experiment.st_noiseStd;
			price = Math.round(Math.min(laggedBestSell, wFundamental * laggedFundamental + wPrice * laggedBestSell + wNoise * noise));
			//price = (long) Math.min(laggedBestSell, wFundamental * laggedFundamental + wPrice * laggedBestSell + wNoise * noise);
		} else if(buysell == BuySell.SELL){
			double noise = (double) laggedBestSell + random.nextGaussian() * experiment.st_noiseStd;
			price = Math.round(Math.max(laggedBestBuy, wFundamental * laggedFundamental + wPrice * laggedBestBuy + wNoise * noise));
		}

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
	
}
