package tools;

import holders.Configuration;
import holders.ToolOptions;

/**
 * Class to handle all logging capabilities of program.
 */
public class Logger {
	
	/**
	Log for all life time of program execution.
	Will be saved, if some kind of error/exception will appear.
	*/
	public static final StringBuilder runtimeLog = new StringBuilder();
	
	/** Fast phase temporally log collection, before activate user's pop-ups */
	public static final StringBuilder tempLog = new StringBuilder();
	
	/** Fast phase temporally log collection, before activate user's pop-ups */
	public static String previusTempLog = "";
	
	public static String log(String line) {
		runtimeLog.append(line + "\n");
		return line;
	}
	
	public static boolean logLevelAbove(int level) {
		ToolOptions opts = Configuration.OPTIONS;
		if(opts != null) {
			if(opts.getLogLevel() >= level) {
				return true;
			}
		}
		return false;
	}

}
