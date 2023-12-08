package hud;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JScrollPane;

import holders.Configuration;
import tools.FileFinderWorker;

public class AdvDnDPane extends JScrollPane implements DropTargetListener {
	
	private static final long serialVersionUID = 6059507217660177832L;

	public enum DragState { WAITING, ACCEPT, REJECT }

	private DragState state = DragState.WAITING;
	
	private final UpdatableTableModel updTableModel;
	
	private final Dimension windims;

	public AdvDnDPane(int wHeight, int wWidth, Component view, UpdatableTableModel inTModel) {
		super(view);
		this.updTableModel = inTModel;
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this, true);
		this.windims = new Dimension(wHeight, wWidth);
	}

	@Override
	public Dimension getPreferredSize() {
		return windims;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		if(!Configuration.PROCESSING) {
			state = DragState.REJECT;
			Transferable t = dtde.getTransferable();
			List<File> processList = updTableModel.getProcessingFileList();
			if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				
				Object dropData = null;
				
				try {
					dropData = t.getTransferData(DataFlavor.javaFileListFlavor);
				}
				catch (UnsupportedFlavorException ufe) {
					ufe.printStackTrace();
				}
				catch (IOException ioe) {
					if(ioe.getMessage().contains("Owner")) {
						int atempts = 0;
						while(atempts < 3) {
							try {
								dropData = t.getTransferData(DataFlavor.javaFileListFlavor);
							}
							catch (UnsupportedFlavorException ufe) {
								ufe.printStackTrace();
							}
							catch (IOException e) {}
							try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
							atempts++;
						}
					}
					else {
						ioe.printStackTrace();
					}
				}
				
				if (dropData != null && (dropData instanceof List)) {
					
					state = DragState.ACCEPT;
					
					List<Object> dropDataList = ((List<Object>) dropData);
					
					for (Object data : dropDataList) {
						if (data instanceof File) {
							if(addToProcessList(((File) data))) {
								this.updTableModel.setListChanged(true);
							}
						}
					}
				}
			}
			
			if(processList.size() == 0) {
				state = DragState.REJECT;
			}
			
			if (state == DragState.ACCEPT) {
				dtde.acceptDrag(DnDConstants.ACTION_COPY);
			} else {
				dtde.rejectDrag();
			}
			
			repaint();
		}
	}
	
	private boolean addToProcessList(File insertingFile) {
		List<File> processList = updTableModel.getProcessingFileList();
		int size = processList.size();
		for (int i = 0; i < size; i++) {
			File fileInList = processList.get(i);
			if(fileInList.getAbsolutePath().equals(insertingFile.getAbsolutePath())) {
				return false;
			}
		}
		
		size = Configuration.VALID_EXTENSIONS_APP.length;
		for (int i = 0; i < size; i++) {
			String checkExtension = Configuration.VALID_EXTENSIONS_APP[i];
			if(insertingFile.getName().toLowerCase().endsWith(checkExtension)) {
				processList.add(insertingFile);
				return true;
			}
		}

		return false;
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		// executes every second while dragged something on window
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		//System.out.println("dropActionChanged");
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		state = DragState.WAITING;
		//System.out.println("finish_drag_drop_DENY");
		repaint();
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		state = DragState.WAITING;
		//System.out.println("finish_drag_drop_success"); // Success
		
		if(updTableModel.isListChanged()) {
	        FileFinderWorker worker = new FileFinderWorker(updTableModel.setListChanged(false));
	        worker.execute();
		}
		
		repaint();
	}
}
