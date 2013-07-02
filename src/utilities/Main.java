package utilities;
import java.util.ArrayList;

import environment.*;

import setup.SimulationSetup;
import setup.WorldObjectHandler;

public class Main {

	
	
	
	public static void main(String[] args){
		
		
		Main.testWorld();
//		Main.test();
		System.out.println(String.format("Finished simulation in %s seconds", ((double) World.runTime)/1000f));
	}
	
	public static void testWorld(){
		World.setupEnvironment();
//		World.executeInitalRounds(10);
		World.executeNRounds(SimulationSetup.nRounds-1);
		WorldObjectHandler.closeLogs();
	}
	
	
	public static void test() {
		long a = Long.MAX_VALUE;
		long b = 5l;
		long c = a*b;
		System.out.println(String.valueOf(a));
		System.out.println(String.valueOf(b));
		System.out.println(String.valueOf(a/b));
		System.out.println(String.valueOf(a*b));
		System.out.println(String.valueOf(c));
	}
}