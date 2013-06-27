package utilities;
import environment.*;

import setup.SimulationSetup;
import setup.WorldObjectHandler;

public class Main {

	
	
	
	public static void main(String[] args){
		
		
		Main.testWorld();
		System.out.println(String.format("Finished simulation in %s seconds", ((double) World.runTime)/1000f));
	}
	
	public static void testWorld(){
		World.setupEnvironment();
//		World.executeInitalRounds(10);
		World.executeNRounds(SimulationSetup.nRounds);
		WorldObjectHandler.closeLogs();
	}
	
}