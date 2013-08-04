package log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import Experiments.Experiment;

import setup.Logging;

import environment.TransactionReceipt;
import environment.World;

public class Logger implements Logging {
	public static long nLogs = 0;
	public static long nWarnings = 0;
	File file;
	FileWriter writer;
	BufferedWriter buffer;

	public boolean logToFile;
	public boolean logToConsole;
	public boolean createLogString;
	
	public Experiment experiment;
	
	public enum Type {
		TXT, CSV
	}

	// public Logger(File file){
	// this.file = file;
	// }
	public Logger(String rootDirectory, String filename, Type type, Boolean logToFile, Boolean logToConsole, Experiment experiment) {
		this.logToFile = logToFile;
		this.logToConsole = logToConsole;
		this.experiment = experiment;
		this.createLogString = this.logToConsole | this.logToFile;
		if(logToFile){
			String directory = this.createFolders(rootDirectory);
			
			if (type == Type.TXT) {
				filename += ".txt";
			} else if (type == Type.CSV) {
				filename += ".csv";
			}
			String filepath = directory + filename;
			this.file = new File(filepath);
			try {
				try {
					this.writer = new FileWriter(this.file);
					System.out.println(String.format("Creating file %s", filepath));
//					this.writeToConsole(String.format("Creating file %s", filepath));
				} catch (FileNotFoundException e) {
					System.out.println("Could not create file: " + filepath);
					System.exit(1);
				}
				this.buffer = new BufferedWriter(this.writer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private String createFolders(String directory) {
		if (createTimeSpecificLogFolders) {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_kk_mm_ss");
			String time = sdf.format(cal.getTime()) + "/";
			directory += time;
		} 
		File file = new File(directory);
		if (!file.exists()) {
			file.mkdirs();
		}
		return directory;
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

	public void writeToConsole(String line) {
		if(this.logToConsole){
			System.out.println(line);
		}
	}

	public void writeToFile(String line) {
		// System.out.println(line);
		if (this.logToFile) {
			try {
				this.writer.write(line + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void logError(String line, Experiment experiment) {
		Exception e = new Exception();
		writeToConsole(this.getNewEntry() + "ERROR!;\t" + line + "\t Stack:\t ");
		e.printStackTrace();
		experiment.closeLogs();
//		WorldObjectHandler.closeLogs();
		System.exit(1);
	}

	@SuppressWarnings("unused")
	private void createEventLogHeader() {
		String header = "nLogs\tRT\tRound\tEvent";
		this.writeToFile(header);
	}

	protected String getNewEntry() {
		Logger.nLogs++;
		String line = String.valueOf(Logger.nLogs) + "\t" + this.getRealTime() + "\t" + this.experiment.getWorld().getCurrentRound() + "\t";
		return line;
	}

	public void writeLineToLog(String line) {
		this.writeToFile(line);
	}

	public void logShortSelling(TransactionReceipt receipt) {
		String line = String.format("Agent %d tried to short sell %d units of stock %d", receipt.getOwnerOfFilledStandingOrder().getID(), receipt.getUnsignedVolume(), receipt.getStock().getID());
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

	public long getRealTime() {
		return (int) (System.currentTimeMillis() - this.experiment.getWorld().creationTime);
	}

}
