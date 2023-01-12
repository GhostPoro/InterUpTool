package hud;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
	private final Map<String, RowData> mapLookup;
	private final List<File> fileList;
	private boolean listChanged;

	public UpdatableTableModel(List<File> inFileList) {
		this.mapLookup = new HashMap<String, RowData>(25);
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
	public String getColumnName(int column) {
		String name = "??";
		switch (column) {
		case 0:
			name = "File Name";
			break;
		case 1:
			name = "File Path";
			break;
		case 2:
			name = "File Properties";
			break;
		case 3:
			name = "Current / End / Stage";
			break;
		case 4:
			name = "Progress";
			break;
		}
		return name;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		RowData rowData = rows.get(rowIndex);
		Object value = null;
		switch (columnIndex) {
		case 0:
			value = rowData.getFile().getName();
			break;
		case 1:
			value = rowData.getFile().getAbsolutePath();
			break;
		case 2:
			value = rowData.getFileProperties(); // "mp4/jpg";
			break;
		case 3:
			value = rowData.getFileProcessStage(); //"0/1982/5";
			break;
		case 4:
			value = rowData.getStatus();
			break;
		}
		return value;
	}

	public void addFile(File file) {
		if(file.length() > 0) {
			String hash = file.getAbsolutePath(); //Hash.SHA256.checksum(file);
			RowData rowData = mapLookup.get(hash);
			if(rowData == null) {
				rowData = new RowData(file, hash, "FILE_TYPE_HERE", rows.size());
				mapLookup.put(hash, rowData);
				rows.add(rowData);
				fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
				
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
		mapLookup.remove(rows.get(fileIndex).getCheckSum());
		rows.remove(fileIndex);
		
		fileList.clear();
		for(RowData rowData : rows) {
			fileList.add(rowData.getFile());
		}
		
		fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
	}

	/* Progress  here is float from 0.00f to 1.00f as 0-100% */
	public void updateProgressBar(String rowKey, float progress) {
		RowData rowData = mapLookup.get(rowKey);
		if (rowData != null) {
			int rowIndex = rowData.getRowIndex();
			fireTableCellUpdated(rowIndex, 4);
		}
	}
	
	public void updateFileProgressStageColumn(String rowKey) {
		RowData rowData = mapLookup.get(rowKey);
		if (rowData != null) {
			fireTableCellUpdated(rowData.getRowIndex(), 3);
		}
	}
	
	public void setFileProgressStage(String rowKey, String text) {
		RowData rowData = mapLookup.get(rowKey);
		if (rowData != null) {
			rowData.setFileProgressStage(text);
			fireTableCellUpdated(rowData.getRowIndex(), 3);
		}
	}
	
	public void setFileProperties(String rowKey, String text, boolean valid) {
		RowData rowData = mapLookup.get(rowKey);
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
	
	
}
