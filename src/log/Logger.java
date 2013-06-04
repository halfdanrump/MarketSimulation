package log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import setup.WorldObjectHandler;

import environment.TransactionReceipt;
import environment.World;

public class Logger implements Logging {
	public static int nLogs = 0;
	public static int nWarnings = 0;
	File file;
	FileWriter writer;
	BufferedWriter buffer;

	// public Logger(File file){
	// this.file = file;
	// }
	public Logger(String directory, String identifier) {
		this.createFolders(directory);
		String filepath = getFilePath(directory, identifier);
		this.file = new File(filepath);
		try {
			try {
				this.writer = new FileWriter(this.file);
			} catch (FileNotFoundException e) {

			}
			this.buffer = new BufferedWriter(this.writer);
			this.createEventLogHeader();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private File createFolders(String directory) {
		File file = new File(directory);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;

		// File file = new File("directory path");
		// if(file.exists()){
		// System.out.println("File Exists");
		// }else{
		// boolean wasDirecotyMade = file.mkdirs();
		// if(wasDirecotyMade)System.out.println("Direcoty Created");
		// else System.out.println("Sorry could not create directory");
		// }
	}

	public static String getFilePath(String directoryPath, String filename) {
		String time;
		if (appendDateToFiles) {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_kk_mm");
			time = sdf.format(cal.getTime());
		} else {
			time = "";
		}

		String filepath = directoryPath + filename + time;
		return filepath;
	}

	public void closeLog() {
		try {
			this.writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// public static void writeToFile(String line){
	//
	// System.out.println(line);
	// }

	protected void writeToConsole(String line) {
		System.out.println(line);
	}

	protected void writeToFile(String line) {
		// System.out.println(line);
		try {
			this.writer.write(line + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logError(String line) {
		Exception e = new Exception();
		writeToConsole(Logger.getNewEntry() + "ERROR!;\t" + line
				+ "\t Stack:\t ");
		e.printStackTrace();
		WorldObjectHandler.closeLogs();
		System.exit(1);
	}

	// public static void writeToFile(String line, File file){
	//
	// System.out.println(line);
	// }

	public static File getFile(String filepath) {
		return null;
	}

	private void createEventLogHeader() {
		String header = "nLogs\tRT\tRound\tEvent";
		this.writeToFile(header);
	}

	protected static String getNewEntry() {
		Logger.nLogs++;
		String line = String.valueOf(Logger.nLogs) + "\t"
				+ Logger.getRealTime() + "\t" + World.getCurrentRound() + "\t";
		return line;
	}

//	private void logOnelineEntry(String line) {
//		if (fileLogging) {
//			writeToFile(Logger.getNewEntry() + line);
//		}
//		if(consoleLogging) {
//			writeToConsole(line);
//		}
//	} 
	
//	public void logOnelineEvent(String line) {
//		if (fileLogging) {
//			writeToFile(Logger.getNewEntry() + line);
//		}
//		if(consoleLogging) {
//			writeToConsole(line);
//		}
//	}
//	
//	public void logOnelineWarning(String line) {
//		if (fileLogging) {
//			writeToFile(Logger.getNewEntry() + line);
//		}
//		
//	}

	// public void logWarning(String line){
	// if(logWarnings){
	// writeToConsole(Logger.getNewEntry() + line);
	// }
	// }

	public void writeLineToLog(String line) {
		this.writeToFile(line);
	}

	public void logShortSelling(TransactionReceipt receipt) {
		String line = String.format(
				"Agent %d tried to short sell %d units of stock %d", receipt
						.getOwner().getID(), receipt.getSignedVolume(), receipt
						.getStock().getID());
		writeToConsole(getNewEntry() + line);
	}

	public static String getFormattedStackTrace() {
		Exception e = new Exception();
		String formattedStack = "";
		for (int i = 2; i < e.getStackTrace().length; i++) {
			StackTraceElement element = e.getStackTrace()[i];
			// formattedStack += String.format("%s.java:%s\t",
			// element.getClassName(), element.getLineNumber());
			formattedStack += String.format("%s\t", element.toString());

		}
		return formattedStack;
	}

	// public static void logOrderbookEvent(String line, Order order){
	// Logger.nLogs++;
	// System.out.println(Logger.nLogs + Logger.getRealTime() + ", " + line +
	// ": , Order details: " + order.toString());
	// }

	// public static String getLogEntryHeader(){
	// String header = ;
	// return
	// }

	public static int getRealTime() {
		return (int) (System.currentTimeMillis() - World.creationTime);
	}

}
