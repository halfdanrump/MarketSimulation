package utilities;
import environment.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import setup.SimulationSetup;
import setup.WorldObjectHandler;

public class Main {

	
	
	
	public static void main(String[] args){
		
		
		//		Test t = new Test();
//		Test.testOrderbook();
//		Test.testPriorityQueue();
//		Test.testOBExecution3();
//		Test.testWorldOrderQueue();
//		Test.testMiscWorld();
//		Test.testCreateAgent();
//		Test.testBrownianMotion();
//		Test.testRandomOrders();
		Main.testWorld();
		System.out.println("hej");
	}
	
	public static void testWorld(){
		World.setupEnvironment();
//		World.executeInitalRounds(10);
		World.executeNRounds(SimulationSetup.nRounds);
		WorldObjectHandler.closeLogs();
	}
	
	public static void testBrownianMotion(){
		
//		Stock stock = new Stock(1, 0.001, 0.01);
//		for(long i=0; i<Global.nRounds; i++){
//			stock.updateFundamentalPrice();
//		}
//		System.out.println(stock.getFundamentalPrice().toString());
	}
	
//	public static void testRandomOrders(){
//		Market market1 = new Market();
//		Stock stock1 = new Stock(100, 0, 1);
//		Orderbook orderbook = new Orderbook(stock1, market1);
//		StylizedTrader.submitRandomLimitBuyOrder(orderbook);
//		StylizedTrader.submitRandomMarketSellOrder(orderbook);
//		World.dispatchArrivingOrders();
//	}
	
//	public static void testCreateAgent(){
//		
//		/*
//		 * Create the stock to be traded
//		 */
//		long stockId, initialFundamental;
//		Stock stock0 = new Stock(stockId = 0, initialFundamental = 100);
//		
//		/*
//		 * Create the markets
//		 */
//		Market market0 = new Market(0);
//		Market market1 = new Market(1);
//		World.createOrderbooks();
//		
//		/*
//		 * Specify parameters for an agent ad create it
//		 */
//		long wealth, id, minimumSpread;
//		int[] stocks = {0};
//		int[] ownedStocks = {100};
//		int[] markets = {0,1};
//		int[] latencies = {2,5};
//		
//		
//		HFT agent1 = new SingleStockMarketMaker(id = 1, wealth = 100000, stocks, ownedStocks, markets, latencies, minimumSpread = 100);
//		System.out.println("hej");
//	}
	
	public void testTime(){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_kk_mm");
		System.out.println(sdf.format(cal.getTime()));
		cal.getTime();
		
		
		long t = cal.getTimeInMillis();
		t = System.currentTimeMillis();
		System.out.println(t);
		
	}
	
	
	public static void testOBExecution2(){
		/*
		 * Make agent with random latencies to markets
		 */
//		Order buy1, buy2, sell1, sell2;
//		Orderbook book = new Orderbook();
////		order = new Order(100,100,Order.Type.MARKET,Order.BuySell.BUY);
////		sell1 = new Order(120,50,Order.Type.MARKET,Order.BuySell.SELL);
////		sell2 = new Order(121,50,Order.Type.MARKET,Order.BuySell.SELL);
//////(100, 200, 10, 30, Order.Type.MARKET, Order.BuySell.BUY);
////		buy2 = new Order(122,55,Order.Type.MARKET,Order.BuySell.BUY);
////		book.receiveOrder(order);
////		book.processAllNewOrders();
////		book.receiveOrder(sell1);
////		book.processAllNewOrders();
////		book.receiveOrder(sell2);
////		book.processAllNewOrders();
////		book.receiveOrder(buy2);
//		book.processAllNewOrders();
//		book.printBook();
		
//		for(long i = 0; i<100; i++){
//			order = Utils.getRandomOrder(100, 200, 10, 30, Order.Type.MARKET, Order.BuySell.BUY);
//			book.receiveOrder(order);
//			order2 = Utils.getRandomOrder(100, 200, 10, 30, Order.Type.MARKET, Order.BuySell.SELL);
//			book.receiveOrder(order2);
//			book.processAllNewOrders();
//		}
	}
	
	public static void testOBExecution1(){
		/*
		 * Test case that shows how transaction prices differ as the orders are processed in random order,
		 * which happens when they arrive to the market in the same round.
		 */
//		Order buy1, buy2, sell1, sell2;
//		Orderbook book = new Orderbook();
////		sell1 = new Order(120,50,Order.Type.MARKET,Order.BuySell.SELL);
////		sell2 = new Order(121,50,Order.Type.MARKET,Order.BuySell.SELL);
////		buy2 = new Order(122,55,Order.Type.MARKET,Order.BuySell.BUY);
////		book.receiveOrder(sell1);
////		book.receiveOrder(sell2);
////		book.receiveOrder(buy2);
//		book.processAllNewOrders();
//		book.printBook();
	}
	
	public static void testOBExecution3(){
		/*
		 * Test case that shows how transaction prices differ as the orders are processed in random order,
		 * which happens when they arrive to the market in the same round.
		 */
//		Order buy1, buy2, sell1, sell2;
//		Orderbook book = new Orderbook();
//		sell2 = new Order(119,55,Order.Type.MARKET,Order.BuySell.SELL);
//		buy1 = new Order(120,50,Order.Type.MARKET,Order.BuySell.BUY);
//		buy2 = new Order(121,50,Order.Type.MARKET,Order.BuySell.BUY);
//		book.receiveOrder(sell2);
//		book.receiveOrder(buy1);
//		book.receiveOrder(buy2);
//		book.processAllNewOrders();
//		book.printBook();
	}
	
	public static void testWorldOrderQueue(){
//		World world = new World();
//		long volume, price;
//		long arrive, dispatch;
//		Orderbook orderbook = new Orderbook();
//		
////		Order order1 = new Order(price = 80, volume = 100,Order.Type.MARKET,Order.BuySell.BUY,arrive = 2, dispatch = 0, orderbook);
////		Order order2 = new Order(price = 90, volume = 100,Order.Type.MARKET,Order.BuySell.BUY,arrive = 4, dispatch = 0, orderbook);
////		Order order3 = new Order(price = 90, volume = 50,Order.Type.MARKET,Order.BuySell.SELL,arrive = 5, dispatch = 0, orderbook);
////		Order order4 = new Order(price = 91, volume = 50,Order.Type.LIMIT,Order.BuySell.SELL,arrive = 6, dispatch = 0, orderbook);
////		Order order5 = new Order(price = 91, volume = 50,Order.Type.MARKET,Order.BuySell.SELL,arrive = 8, dispatch = 0, orderbook);
////		Order order6 = new Order(price = 89, volume = 50,Order.Type.LIMIT,Order.BuySell.SELL,arrive = 9, dispatch = 0, orderbook);
////		
//		World.executeNRounds(10);
		
	}
	
//	public static void testOrderbook(){
//		HighFrequencyTrader t1 = new MarketMaker();
//		Orderbook ob = new Orderbook();
//		long arrivalTime, dispatchTime, volume, price, nRounds;
//		Order os1 = new Order(arrivalTime = 10, dispatchTime = 0, nRounds = 100, volume = 10, price = 35, Order.Type.MARKET, Order.BuySell.SELL);
//		Order os2 = new Order(arrivalTime = 10, dispatchTime = 0, volume = 10, price = 30, Order.Type.MARKET, Order.BuySell.SELL);
//		Order os3 = new Order(arrivalTime = 10, dispatchTime = 0, volume = 15, price = 20, Order.Type.MARKET, Order.BuySell.SELL);
//		Order ob1 = new Order(arrivalTime = 10, dispatchTime = 0, volume = 10, price = 23, Order.Type.MARKET, Order.BuySell.BUY);
//		Order ob2 = new Order(arrivalTime = 10, dispatchTime = 0, volume = 10, price = 21, Order.Type.MARKET, Order.BuySell.BUY);
//		Order ob3 = new Order(arrivalTime = 10, dispatchTime = 0, volume = 15, price = 10, Order.Type.MARKET, Order.BuySell.BUY);
//		ob.receiveOrder(os1);
//		ob.receiveOrder(os2);
//		ob.receiveOrder(os3);
//		ob.receiveOrder(ob1);
//		ob.receiveOrder(ob2);
//		ob.receiveOrder(ob3);
//		ob.processOrders();
//		
//	}
//	
//	public static void testPriorityQueue(){
//		Orderbook ob = new Orderbook();;
//		Order o1 = new Order(10, 0, 10, 20, Order.Type.MARKET, Order.BuySell.SELL);
//		Order o2 = new Order(15, 0, 10, 22, Order.Type.MARKET, Order.BuySell.SELL);
//
//		Comparator<Message> comp = new ArrivalTimeComparator();
//		PriorityQueue<Order> queue = new PriorityQueue<Order>(10, comp);
//		queue.add(o1);
//		queue.add(o2);
//		while(queue.size()!=0){
//			System.out.println(queue.poll().getArrivalTime());
//		}	
//	}

}
