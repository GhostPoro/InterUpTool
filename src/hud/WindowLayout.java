package hud;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import holders.Configuration;
import holders.CurrentSessionFilesProcessingSettings;
import holders.ToolOptions;
import tools.FilesProcessor;
import tools.Logger;
import tools.Utils;
import holders.RowData;
import holders.Text;

public class WindowLayout {

	private final UpdatableTableModel updTableModel;

	public WindowLayout(UpdatableTableModel inUTModel, final String[] args) {
		this.updTableModel = inUTModel;
		
		// check if User provide access to FFMPEG executable
		if(!Configuration.OPTIONS.canProcess()) {
			Configuration.OPTIONS = Popups.configWarning(Configuration.OPTIONS, args);
		}
		
		final ToolOptions dummyOpts = Configuration.OPTIONS;
		
		Configuration.SETTINGS = new CurrentSessionFilesProcessingSettings();

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {

				// Make sure we have nice window decorations.
				JFrame.setDefaultLookAndFeelDecorated(true);

				// UpdatableTableModel updTableModel = new UpdatableTableModel();

				JTable table = new JTable();
				table.setModel(updTableModel);

				// Assign a progress cell renderer to column 4.
				// Create a renderer for displaying progress cells.
				table.getColumn("Progress").setCellRenderer(new AdvProgressCellRenderer());
				// table.getColumn("Status").setCellRenderer(new ProgressCellRender());

				// Create an object from an anonymous subclass of
				// TableModelListener. That object's anonymous subclass overrides
				// tableChanged() to test any changes made to hoursWorked, in the
				// table model, by calls to the table model's
				// setValueAt(Object value, int rowIndex, int colIndex) method.
				// The idea is to validate user entry. (User should enter only
				// values ranging from 0 through 40 (inclusive).

				TableModelListener tml;
				tml = new TableModelListener() {

					public void tableChanged(TableModelEvent e) {
						// Only updates to the table model are to be
						// considered. (Actually, it is not necessary to
						// test against UPDATE because there is no way a
						// table row will be inserted or deleted in this
						// program as it currently stands.
						// However, in the event that you wish to change the
						// program to allow for dynamic inserts and
						// removals, you might want to leave the following
						// if test.)

						if (e.getType() == TableModelEvent.UPDATE) {
							// Obtain the current column index.

							int column = e.getColumn();

							// Respond to updates that affect the middle
							// column only. (Actually, because only column 1
							// can be updated, the following if test is not
							// necessary. But you might decide to add
							// additional columns in
							// the future that are editable. Or, you might
							// decide
							// to make the leftmost names column editable.
							// In either situation, the if test is
							// necessary.)

							if (column == 1) {
								// Identify the row containing the cell
								// whose value changed.
								int row = e.getFirstRow();
							}
						}
					}
				};

				// Register the table model listener with the table's data model.
				updTableModel.addTableModelListener(tml);

				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
				frame.setTitle(Text.TOOL_NAME_SHORT + " " + Text.TOOL_VERSION);
				
				frame.setLayout(new BorderLayout());

				// Create and set up the menu bar and content pane.
				frame.setJMenuBar(createMenuBar(frame, table));

				frame.add(new AdvDnDPane(Configuration.windowInitHeight, Configuration.windowInitWidth, table, updTableModel));

				// delleteing by pressing 'DEL' key
				Action deleteRows = new AbstractAction() {
					private static final long serialVersionUID = 3847170562969694589L;
					public void actionPerformed(ActionEvent e) {
						int[] selectedRowsArray = table.getSelectedRows();
						int srasize = selectedRowsArray.length;
						for (int i = (srasize - 1); i > -1; i--) {
							updTableModel.removeFile(selectedRowsArray[i]);
						}
						table.clearSelection();
					}
				};
				table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteRowsActionName");
				table.getActionMap().put("deleteRowsActionName", deleteRows);

				// Display the window.
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

	// Create an Edit menu to support cut/copy/paste.
	public JMenuBar createMenuBar(JFrame frame, JTable table) {
		JMenuItem menuItem = null;
		
		JMenu mainMenu = new JMenu("File");
		
		menuItem = new JMenuItem("Open Config File");
		mainMenu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Utils.openFileInSystem(Configuration.CORE_CONFIG_FILE_PATH);
			}
		});
		
		
		menuItem = new JMenuItem("Show Last Errors");
		mainMenu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String prevLog = Logger.previusTempLog;
				Logger.tempLog.append(prevLog);
				Popups.showRuntimeErrorsLog();
			}
		});
		

		
		
		JButton btn_SessionSettings = new JButton("Settings");
		btn_SessionSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SessionSettingsGUI();
			}
		});
		
		JButton btn_StartProcess = new JButton("Process >>");
		btn_StartProcess.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(Configuration.PROCESSING) {
					System.out.println("Cancelling files processing...");
					btn_StartProcess.setText("Process >>");;
					Configuration.PROCESSING = false;
					//List<RowData> rows = updTableModel.getRowsData();
					// reset state
//					for (RowData rowData : rows) {
//						float curSatus = 0;
//						rowData.setStatus(curSatus);
//						updTableModel.updateProgressBar(rowData.getCheckSum(), curSatus);
//					}
					table.setEnabled(true);
					btn_SessionSettings.setEnabled(true);
					//System.out.println("Done A!");
				}
				else {
					
					List<RowData> rows = updTableModel.getRowsData();
					
					if(rows.size() > 0) {
						
						Thread filesProcessingThread = new Thread() {
							public void run() {
								
								table.setEnabled(false);
								btn_SessionSettings.setEnabled(false);
								table.clearSelection();
								btn_StartProcess.setText("Cancel");
								Configuration.PROCESSING = true;
								
								System.out.println("Files enchancing process Started!");
						
								if(FilesProcessor.process(updTableModel, rows)) {
									System.out.println("Files enchancing process successfully Finished!"); //  
								}
								else {
									System.err.println("Files enchancing process finished with errors or was interrupted!");
								}
								
								btn_StartProcess.setText("Process >>");;
								Configuration.PROCESSING = false;
								table.setEnabled(true);
								btn_SessionSettings.setEnabled(true);
							}
						};
						filesProcessingThread.start();
						
					}
					else {
						JOptionPane.showMessageDialog(frame, Text.POPUP_NOTHING_TO_PROCESS_TEXT, Text.POPUP_NOTHING_TO_PROCESS_NAME, JOptionPane.INFORMATION_MESSAGE);
					}
					
	
				}
			}
		});

		JMenuBar menuBar = new JMenuBar();
		menuBar.setLayout(new GridLayout(1,8));
		menuBar.add(mainMenu);
		menuBar.add(new JLabel(""));
		menuBar.add(new JLabel(""));
		//menuBar.add(new JLabel(""));
		menuBar.add(Configuration.INFOLABELIMAGE);
		menuBar.add(Configuration.INFOLABELANIMS);
		menuBar.add(Configuration.INFOLABELTOOLS);
		menuBar.add(btn_SessionSettings);
		menuBar.add(btn_StartProcess, BorderLayout.EAST);

		return menuBar;
	}

}
