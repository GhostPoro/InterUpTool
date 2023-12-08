package core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import holders.Configuration;
import holders.ToolOptions;
import holders.Text;
import hud.UpdatableTableModel;
import hud.WindowLayout;

public class Main {

	private static List<File> fileList;
	
	private static boolean consoleMode = false;
	
	private static boolean showHelp = false;

	public static void main(String[] args) {
		
		/*
		 * TODO:
		 * Java up-scaling
		 * Add to console support of:
		 * 		command generation -> in console out or to file
		 * 		setting input-output files:
		 * 			for logs -log -l
		 * 			session settings -s -session -settings
		 * 			global settings  -c -conf -config
		 * 			console mode -console -consolemode -console_mode
		 * Dark theme (and theme modification thru *.theme files, aka dark.theme / light.theme etc...)
		 * Force 'Settings' configurations in-app applying
		 * GIF/APNG/WEBP Animated Images Up-scaling
		 * Add proper output logging
		 * Transfer 'WindowLayout' and 'SessionSettingsGUI' text strings to 'Text' class
		 * Make support (with list in settings) for localization files to override text strings from Text class (ge.loc, ua.loc etc...)
		 */
		
		/* Load simple core configuration. */
		Configuration.init();
		
		// test room
		//Configuration.PROCESSING = true;
		//args = new String[] { "-vvv" };
		//System.exit(0);
		
		/* Process basic supplied arguments. */
		if(args != null) {
			int argssize = args.length;
			for (int i = 0; i < argssize; i++) {
				String param = args[i].trim();
				switch (param) {
					case "-c" : consoleMode = true; break;
					case "-h" : showHelp    = true; break;
					default   :                     break;
				}
			}
		}
		
		/* Print help and exit. */
		if(showHelp) {
			System.out.println(Text.HELP_STRING);
			System.exit(0);
		}
		
		/* Load main program options from console and config file. */
		ToolOptions options = ToolOptions.load(args);
		if(options != null) {
			Configuration.OPTIONS = options;
			fileList = new ArrayList<File>();
			new WindowLayout(new UpdatableTableModel(fileList), args);
		}
		else {
			if(consoleMode) {
				System.err.println(Text.PANIC_EXIT_EXPLAIN); 
			}
			else {
				JOptionPane.showMessageDialog(null, Text.PANIC_EXIT_EXPLAIN);
			}
			System.exit(-1);
		}
	}
}
