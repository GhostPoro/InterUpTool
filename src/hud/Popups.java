package hud;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import holders.Configuration;
import holders.Text;
import holders.ToolOptions;
import tools.Logger;
import tools.TextProcessor;
import tools.Utils;

public class Popups {
	
	// holders for temp elements
	private static JTextArea CURRENT_runtimeErrorLogsPopup_TextArea = null;
	private static JScrollPane CURRENT_runtimeErrorLogsPopup_ScrollPane = null;
	
	public static ToolOptions configWarning(ToolOptions opts, final String[] args) {
		Object[] options = { Text.STARTING_POPUP_TEXTS[1], Text.STARTING_POPUP_TEXTS[2], Text.STARTING_POPUP_TEXTS[3], Text.STARTING_POPUP_TEXTS[4] };
		int userConfigAnswer = -1;
		
		boolean requestAgain = true;
		
		String ffmpeg_path_replacement = "NOT_INITIALIZED";
  
		do {
		   
		   	ffmpeg_path_replacement = ((opts.FFMPEG_ENCODER_APP_PATH != null) ? opts.FFMPEG_ENCODER_APP_PATH : "NULL");
		   
		   	/** direct insertion of 'opts.FFMPEG_ENCODER_APP_PATH' inside 'replace' cause to crash if 'opts.FFMPEG_ENCODER_APP_PATH' == null, if no config file found, so premature initialization is required */
		   	userConfigAnswer = JOptionPane.showOptionDialog(null, Text.STARTING_POPUP_TEXTS[5].replace("$FFMPEG_ENCODER_APP_PATH$", ffmpeg_path_replacement), Text.STARTING_POPUP_TEXTS[0], JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);
			if(userConfigAnswer == 0) { // Read config again
				opts = ToolOptions.load(args);
			}
			else if(userConfigAnswer > 2 || userConfigAnswer < 0) { // Cancel -> Exit from program
				System.err.println("User did not provide access to FFMPEG executable via config file. Exiting...");
				System.exit(-1);
			}
			else if(userConfigAnswer == 1) { // try open config file in system explorer
				Utils.openFileInSystem(Configuration.CORE_CONFIG_FILE_PATH);
			}
			else if(userConfigAnswer == 2) {
				if(!opts.canProcess()) {
					String configText = TextProcessor.restoreVarsValues(TextProcessor.overrideVARs(Text.DEFAULT_CONFIGURATION_FILE_STRING, Configuration.VARS_TO_OVERRIDE_IN_CONFIG));
					Utils.writeLinesToFile(Configuration.CORE_CONFIG_FILE_PATH, configText);
					System.out.println("Configuration file fixed. (A)");
				}
				else if(!opts.allValid()) {
					String configText = TextProcessor.restoreVarsValues(Utils.readFileToLine(Configuration.CORE_CONFIG_FILE_PATH));
					Utils.writeLinesToFile(Configuration.CORE_CONFIG_FILE_PATH, configText);
					System.out.println("Configuration file fixed. (B)");
				}
				Utils.openFileInSystem(Configuration.CORE_CONFIG_FILE_PATH);
			}
			
			if(opts.canProcess()) {
				requestAgain = false;
			}
			
		} while (requestAgain);
		return opts;
	}
	
	public static boolean showRuntimeErrorsLog() {
		if(Logger.tempLog.length() < 1) {
			// do nothing
			return false;
		}
		else {
			if(CURRENT_runtimeErrorLogsPopup_TextArea == null) {
		        // Create the pop-up window
		        JDialog dialog = new JDialog() {
					private static final long serialVersionUID = 3878456927574837938L;

					@Override
		            public void dispose() {
						// store text, if user in some way want reopen closed errors log window
						//Logger.previusTempLog.setLength(0);
						Logger.previusTempLog = CURRENT_runtimeErrorLogsPopup_TextArea.getText();
						
						// dispose popup's elements
						CURRENT_runtimeErrorLogsPopup_TextArea   = null;
						CURRENT_runtimeErrorLogsPopup_ScrollPane = null;
						
						System.out.println("Dialog disposed");

		                // don't forget to call the dispose method of the super class
		                // to ensure that the dialog is properly disposed
		                super.dispose();
		            }
		        };
		        dialog.setTitle(Text.ERRORS_POPUP_TEXTS[0]);
		        //dialog.setModal(true); // will block main GUI
		        dialog.setResizable(true);
		        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

				// Create the text area and set it to be disabled
				JTextArea textArea = new JTextArea(0,0);
				textArea.setEditable(false);
				
				// Create the scroll pane and add the text area to it
			    JScrollPane scrollPane = new JScrollPane(textArea);
			    CURRENT_runtimeErrorLogsPopup_ScrollPane = scrollPane;
			    
			    // Set the scroll pane's policy to display scroll bars as needed
			    //scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			    //scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

				// Create the button
				JButton button = new JButton("OK");
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dialog.dispose();
					}
				});
				
				// add button
		        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		        buttonPanel.add(button);
		        
		        // Add the text field and button to the pop-up window
		        JPanel panel = new JPanel(new BorderLayout());
		        
		        // add elements to panel
		        panel.add(scrollPane, BorderLayout.CENTER);
		        panel.add(buttonPanel, BorderLayout.SOUTH);

				// Add the panel to the frame
		        dialog.add(panel);
				
				// set the global variable for runtime errors showing
		        CURRENT_runtimeErrorLogsPopup_TextArea = textArea;
		        
				// set currently logged text
		        updateTextArea(Logger.tempLog.toString());
		        
		        // Show the pop-up window
		        dialog.pack();
		        dialog.setSize(500,400);
		        dialog.setLocationRelativeTo(null);
		        dialog.setVisible(true);
		        
				return true;
			}
			else {
				String currentErrorsLine = CURRENT_runtimeErrorLogsPopup_TextArea.getText();
				String futureText = currentErrorsLine + "\n" + Logger.tempLog.toString();
				updateTextArea(futureText);
				return true;
			}
		}
	}
	
	private static void updateTextArea(String text) {
		//int size = ((text == null) ? 0 : text.split("\n").length);
		// set previously and currently logged text
		CURRENT_runtimeErrorLogsPopup_TextArea.setText(text);
		// TODO: Fix scroll calculation
		CURRENT_runtimeErrorLogsPopup_TextArea.setPreferredSize(new Dimension(8192, 16000)); // otherwise scroll did not appear
		CURRENT_runtimeErrorLogsPopup_ScrollPane.revalidate();
		CURRENT_runtimeErrorLogsPopup_ScrollPane.repaint();
		// reset text storage
		Logger.tempLog.setLength(0);
	}

}
