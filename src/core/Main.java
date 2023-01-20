package core;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import holders.Configuration;
import holders.ToolOptions;
import holders.Text;
import hud.UpdatableTableModel;
import hud.WindowLayout;
import tools.ImageProcessor;
import tools.ProcessHandler;
import tools.Utils;

public class Main {

	private static List<File> fileList;
	
	private static boolean consoleMode = false;
	
	private static boolean showHelp = false;

	public static void main(String[] args) {
		
		/* Load simple core configuration. */
		Configuration.init();
		
		// test room
		//Configuration.PROCESSING = true;
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
//			System.exit(0);
			fileList = new ArrayList<File>();
			new WindowLayout(new UpdatableTableModel(fileList));
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
