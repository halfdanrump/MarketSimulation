package agent;

import java.util.ArrayList;
import java.util.Random;

import setup.StylizedTraderBehavior;
import utilities.Utils;

import environment.Order;
import environment.Orderbook;
import environment.Stock;
import environment.World;

public class StylizedTrader implements StylizedTraderBehavior{

	
	public static Order submitRandomOrder(Order.Type type, Order.BuySell buysell, Orderbook orderbook){
		int arrivalTime = World.getCurrentRound();
		int dispatchTime = World.getCurrentRound();
		int volume = getStylizedTraderOrderVolume();
		int price = getStylizedTraderEstimatedPrice(orderbook.getStock());
		Order order = new Order(arrivalTime,dispatchTime, StylizedTraderBehavior.orderLength, volume, price, type, buysell, null, orderbook);
		return order;
	}
	
	public static int getStylizedTraderEstimatedPrice(Stock stock) {
		int currentFundamental = stock.getFundamentalPrice(World.getCurrentRound());
		int additivePriceNoise = Utils.getGaussianInteger(StylizedTraderBehavior.addivePriceNoiseMean, StylizedTraderBehavior.additivePriceNoiseStd); 
		int price = currentFundamental + additivePriceNoise;
		return price;
	}
	
	public static int getStylizedTraderOrderVolume() {
		int volume;
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
		int randomInt = random.nextInt(4);
		if(randomInt == 0){
			order = submitRandomLimitBuyOrder(orderbook);
		} else if(randomInt == 1){
			order = submitRandomLimitSellOrder(orderbook);
		} else if(randomInt == 2){
			order = submitRandomMarketBuyOrder(orderbook);
		} else if (randomInt == 3){
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
