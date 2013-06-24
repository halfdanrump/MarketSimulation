package agent;


import java.util.ArrayList;
import java.util.Random;

import setup.StylizedTraderBehavior;
import utilities.Utils;

import environment.Message;
import environment.Order;
import environment.Orderbook;
import environment.Stock;
import environment.World;

public class StylizedTrader implements StylizedTraderBehavior{

	
	public static Order submitRandomOrder(Order.Type type, Order.BuySell buysell, Orderbook orderbook){
		int dispatchTime = World.getCurrentRound();
		long volume = getStylizedTraderOrderVolume();
		long price = getStylizedTraderEstimatedPrice(orderbook.getStock());
		Order order = new Order(0,dispatchTime, StylizedTraderBehavior.orderLength, volume, price, type, buysell, null, orderbook, Message.TransmissionType.WITH_TRANSMISSION_DELAY);
		return order;
	}
	
	public static long getStylizedTraderEstimatedPrice(Stock stock) {
		long currentFundamental = stock.getFundamentalPrice(World.getCurrentRound());
		long additivePriceNoise = Utils.getGaussianInteger(StylizedTraderBehavior.addivePriceNoiseMean, StylizedTraderBehavior.additivePriceNoiseStd); 
		long price = currentFundamental + additivePriceNoise;
		if(price <= 0) {
			System.out.println(price);
		}
		return price;
	}
	
	public static long getStylizedTraderOrderVolume() {
		long volume;
		if(StylizedTraderBehavior.randomOrderVolume){
			volume = Utils.getNonNegativeGaussianInteger(StylizedTraderBehavior.volumeStd, StylizedTraderBehavior.volumeMean);
		} else{
			volume = StylizedTraderBehavior.constantVolume;
		}
		return volume;
	}
	
	
	public static Order submitRandomOrder(){
		/*
		 * Gets a random orderbook and submits and order to it
		 */
		Order order = null;
		Random random = new Random();
		ArrayList<Orderbook> orderbooks = World.getOrderbooks();
		Orderbook orderbook = orderbooks.get(random.nextInt(orderbooks.size()));
		long randomlong = random.nextInt(4);
		if(randomlong == 0){
			order = submitRandomLimitBuyOrder(orderbook);
		} else if(randomlong == 1){
			order = submitRandomLimitSellOrder(orderbook);
		} else if(randomlong == 2){
			order = submitRandomMarketBuyOrder(orderbook);
		} else if (randomlong == 3){
			order = submitRandomMarketSellOrder(orderbook);
		}
		return order;
	}
	
	public static Order submitRandomMarketBuyOrder(Orderbook orderbook){
		return submitRandomOrder(Order.Type.MARKET, Order.BuySell.BUY, orderbook);
	}
	
	public static Order submitRandomMarketSellOrder(Orderbook orderbook){
		return submitRandomOrder(Order.Type.MARKET, Order.BuySell.SELL, orderbook);
	}
	
	public static Order submitRandomLimitBuyOrder(Orderbook orderbook){
		return submitRandomOrder(Order.Type.LIMIT, Order.BuySell.BUY, orderbook);
	}
	
	public static Order submitRandomLimitSellOrder(Orderbook orderbook){
		return submitRandomOrder(Order.Type.LIMIT, Order.BuySell.SELL, orderbook);
	}
}
