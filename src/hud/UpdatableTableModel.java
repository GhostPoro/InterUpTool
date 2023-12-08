package hud;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import holders.Configuration;
import holders.ToolOptions;
import holders.RowData;
import holders.Text;
import tools.FilesAnalyzer;
import tools.Hash;
import tools.Logger;

public class UpdatableTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 467996132848313011L;
	private final List<RowData> rows;
	//private final Map<String, RowData> mapLookup;
	private final List<File> fileList;
	private boolean listChanged;

	public UpdatableTableModel(List<File> inFileList) {
		//this.mapLookup = new HashMap<String, RowData>(25);
		this.rows = new ArrayList<RowData>(25);
		this.fileList = inFileList;
		this.listChanged = false;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public String getColumnName(int columnNum) {
		switch (columnNum) {
			case 0 : return "File Name";
			case 1 : return "File Path";
			case 2 : return "File Properties";
			case 3 : return "Current / End / Stage";
			case 4 : return "Progress";
		}
		return "UNKNOWN_COLUMN_NAME";
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		RowData rowData = rows.get(rowIndex);
		switch (columnIndex) {
			case 0 : return rowData.getFile().getName();
			case 1 : return rowData.getFile().getAbsolutePath();
			case 2 : return rowData.getFileProperties();
			case 3 : return rowData.getFileProcessStage();
			case 4 : return rowData.getStatus();
		}
		return null;
	}

	public void addFile(File file) {
		int currentRowsSize = getRowCount();
		if(file.length() > 0) {
			String fileFullPath = file.getAbsolutePath();
			if(fileNotInList(fileFullPath)) {
				rows.add(new RowData(file, currentRowsSize, fileFullPath));
				
				int rowsSize = getRowCount();
				int rowsSizeMinusOne = (rowsSize - 1);
				
				fireTableRowsInserted(rowsSizeMinusOne, rowsSizeMinusOne);
				
				// add file to the queue, for media info extraction
				FilesAnalyzer.addTask(this, file);
				
				// show user, what something is happening
				SimpleAnimator.run(this, rows);
			}
			else {
				//System.err.println("Can't add same file!");
			}
		}
		else {
			String filePath = file.getAbsolutePath();
			String errorLine = Text.ERROR_ZERO_FILE_SIZE.replace("$FILE_PATH$", filePath);
			System.err.print(errorLine);
			Logger.tempLog.append(errorLine);
			Logger.log(errorLine);
		}
	}
	
	public void removeFile(int fileIndex) {
		rows.remove(fileIndex);
		
		fileList.clear();
		
		int rowsSize = getRowCount();
		int rowsSizeMinusOne = (rowsSize - 1);
		
		for (int ri = 0; ri < rowsSize; ri++) {
			fileList.add(rows.get(ri).getFile());
		}
		
		// this one expects first row and last, why its only last one dono 
		fireTableRowsInserted(rowsSizeMinusOne, rowsSizeMinusOne);
	}

	/* Progress  here is float from 0.00f to 1.00f as 0-100% */
	public void updateProgressBar(long rowID, float progress) {
		RowData rowData = getRowByID(rowID);
		if (rowData != null) {
			int rowIndex = rowData.getRowIndex();
			fireTableCellUpdated(rowIndex, 4);
		}
	}
	
	public void updateFileProgressStageColumn(long rowID) {
		RowData rowData = getRowByID(rowID);
		if (rowData != null) {
			fireTableCellUpdated(rowData.getRowIndex(), 3);
		}
	}
	
	public void setFileProgressStage(long rowID, String text) {
		RowData rowData = getRowByID(rowID);
		if (rowData != null) {
			rowData.setFileProgressStage(text);
			fireTableCellUpdated(rowData.getRowIndex(), 3);
		}
	}
	
	public void setFileProperties(long rowID, String text, boolean valid) {
		RowData rowData = getRowByID(rowID);
		if (rowData != null) {
			rowData.setFileProperties(text, valid);
			fireTableCellUpdated(rowData.getRowIndex(), 2);
		}
	}

	public List<File> getProcessingFileList() {
		return fileList;
	}
	
	public List<RowData> getRowsData() {
		return rows;
	}

	public boolean isListChanged() {
		return listChanged;
	}

	public UpdatableTableModel setListChanged(boolean flag) {
		this.listChanged = flag;
		return this;
	}
	
	private RowData getRowByID(long rowID) {
		List<RowData> localRows = getRowsData();
		int rowsSize = localRows.size();
		for (int ri = 0; ri < rowsSize; ri++) {
			RowData localRow = localRows.get(ri);
			if(rowID == localRow.uniqueID) {
				return localRow;
			}
		}
		return null;
	}
	
	public RowData getRowByPath(String path) {
		List<RowData> localRows = getRowsData();
		int rowsSize = localRows.size();
		for (int ri = 0; ri < rowsSize; ri++) {
			RowData localRow = localRows.get(ri);
			if(localRow.getFullFilePath().equals(path)) {
				return localRow;
			}
		}
		return null;
	}
	
	private boolean fileNotInList(String path) {
		RowData localRow = getRowByPath(path);
		return (localRow == null);
	}
	
}
