package tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import holders.Configuration;
import holders.Configuration.OS;

public class ProcessHandler {
	
	public static boolean DEBUG = false;
	
	public static String[] run(final String cmd, final String killLine, final boolean waitForFinish, final boolean singleRequeest) {
		final String[] outputs = new String[2];
		List<String> appOutputNormal = new ArrayList<String>();
		List<String> appOutputErrors = new ArrayList<String>();
		String[] result = run(cmd, killLine, waitForFinish, singleRequeest, appOutputNormal, appOutputErrors, outputs);
		appOutputNormal.clear();
		appOutputErrors.clear();
		appOutputNormal = null;
		appOutputErrors = null;
		return result;
	}
	
	public static String[] run(final String cmd, final String killLine, final boolean waitForFinish, final boolean singleRequeest, final List<String> appOutputNormal, final List<String> appOutputErrors, final String[] outputs) {
		Process process = null;
		
		if(Logger.logLevelAbove(2)) {
			System.out.println("ProcessHandler.run EXEC: " + cmd);
		}
		
		try {
			
			if(Configuration.PROCESSING || singleRequeest) { // no matter what, create process only in program in 'PROCESSING' stage 
				if(Configuration.os == OS.WIN) {
					process = Runtime.getRuntime().exec(cmd);
				}
				else {
					String[] commands = { "bash", "-c", cmd };
					process = Runtime.getRuntime().exec(commands);
				}
				storeShellOutput(process.getInputStream(), outputs, 0, appOutputNormal);
				storeShellOutput(process.getErrorStream(), outputs, 1, appOutputErrors);
			}
			
			if(process != null) {
				if(killLine == null) {
					if(waitForFinish) {
						
						// aka process.waitFor(), but with check of current main program status
						while(process.isAlive() && (Configuration.PROCESSING || singleRequeest)) {
							Thread.sleep(100);
						}
						
						// can be alive if 'Configuration.PROCESSING' changed to false;
						if(process.isAlive()) {
							killProcess(process);
						}
						
						int counter = 0;
						while((outputs[0] == null || outputs[1] == null) && counter < 1000) {
							Thread.sleep(10);
							counter++;
						}
					}
				}
				else {
					
					int inputArrayLookupIdx = 0;
					int errorArrayLookupIdx = 0;
					
					boolean markedToBeKilled = false;
					
					while(process.isAlive() && (Configuration.PROCESSING || singleRequeest)) {
						if(markedToBeKilled) {
							killProcess(process);
						}
						else {
							int[] inputSearchResult = lookFor(appOutputNormal, killLine, inputArrayLookupIdx);
							inputArrayLookupIdx = inputSearchResult[2];
							
							int[] errorSearchResult = lookFor(appOutputErrors, killLine, errorArrayLookupIdx);
							errorArrayLookupIdx = errorSearchResult[2];
							
							// if in some way found what we looking for -> kill process
							if((inputSearchResult[0] > 0) || (errorSearchResult[0] > 0)) {
								markedToBeKilled = true;
							}
							else {
								// wait before another loop
								Thread.sleep(100);
							}
						}
						
					}
					
					// can be alive if 'Configuration.PROCESSING' changed to false;
					if(process.isAlive()) {
						killProcess(process);
					}
				}
			}
			else {
				return null;
			}
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
		catch (InterruptedException ite) { // needed for 'process.waitFor()'
			ite.printStackTrace();
			return null;
		}
		return outputs;
	}
	
	private static boolean killProcess(Process process) {
		process.destroy();
		try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
		if(process.isAlive()) {
			process.destroyForcibly();
		}
		try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
		return process.isAlive();
	}
	
	/**
	 * 
	 * @param where
	 * @param what
	 * @param idx
	 * @return 0/1 (false/true), current size of supplied array, current index 
	 */
	private static int[] lookFor(List<String> where, String what, int idx) {
		
		if(where != null && what != null) {
			int size = where.size();
			if(idx < size) {
				return new int[] { (where.get(idx).contains(what) ? 1 : 0), size, idx++ };
			}
		}
		
		return new int[] { 0, -1, idx };
	}
	
	private static boolean storeShellOutput(InputStream stream, final String[] holder, int idx, List<String> storage) {
		if(stream != null) {
			Thread readerThread = new Thread() {
			    public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
					StringBuilder strBuilder = new StringBuilder();
					
					boolean showProcessOutput = (Logger.logLevelAbove(3) || DEBUG);
					
					try {
						String output = reader.readLine();
						while (output != null) {
							String trimmedOutput = output.trim();
							storage.add(trimmedOutput);
							if(showProcessOutput) { System.out.println(trimmedOutput); }
							strBuilder.append(trimmedOutput + "\n");
							output = reader.readLine();
						}
						
						// when done
						if((holder != null) && (idx > -1) && (idx < holder.length)) {
							holder[idx] = strBuilder.toString();
						}
						
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
			    }
			};
			readerThread.start();
			return true;
		}
		return false;
	}

}
