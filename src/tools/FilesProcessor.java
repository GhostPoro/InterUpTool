package tools;

import java.util.List;

import holders.Configuration;
import holders.CurrentSessionFilesProcessingSettings;
import holders.RowData;
import hud.UpdatableTableModel;

public class FilesProcessor {
	
	public static boolean process(UpdatableTableModel model, List<RowData> fileList) {

		
		CurrentSessionFilesProcessingSettings sessionSettingsForFiles = Configuration.SETTINGS;
		
		int filesReadyToProcessCounter = 0;
		int filesSuccessfullyProcessedCounter = 0;
		
		// run thread with FileProcessor
		// to control file conversion execution
		int size = fileList.size();
		int attempts = 0;
		
		//System.out.print("REMOVE: " + sessionSettingsForFiles.removeTempFiles);
		//System.exit(0);
		
//		ProcessHandler.DEBUG = true;
		
		for (int i = 0; i < size && attempts < 2 && Configuration.PROCESSING; i++) {
			RowData fileData = fileList.get(i);
			if(fileData.isReadyToProcess()) {
				filesReadyToProcessCounter++;
				if(FileProcessor.process(startFileStatusUpdater(model, fileData.setInProcess(true).setStatus(0).setTargetStatus(0).setFileProgressStage("Calculating...")), sessionSettingsForFiles)) {
					filesSuccessfullyProcessedCounter++;
				}
				fileData.endProcessingTime();
				fileData.setInProcess(false);
				attempts = 0; // reset attempts counter
			}
			else {
				attempts++;
				i--;
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace();}
			}
		}
		
		ProcessHandler.DEBUG = false;

		// return true if all files with valid META data successfully processed, and cycle did not finished because of attempts limit exceed
		return ((filesSuccessfullyProcessedCounter == filesReadyToProcessCounter) && (attempts == 0)) && Configuration.PROCESSING;
	}
	
	private static RowData startFileStatusUpdater(UpdatableTableModel model, RowData currentFile) {
		Thread fileStatusUpdatingThread = new Thread() {
			public void run() {
				
				String oldProgressStageLine = "";
				
				boolean statusMax = false;
				
				while(Configuration.PROCESSING && currentFile.beingProcessed()) {
					try {
						float curStatus = currentFile.getStatus();
						float tgtStatus = currentFile.getTargetStatus();
						
						String currentProgressStageLine = currentFile.getFileProcessStage();
						
						if(curStatus != tgtStatus || !oldProgressStageLine.equals(currentProgressStageLine)) {
							String rowKey = currentFile.getCheckSum();
							model.updateFileProgressStageColumn(rowKey);
							oldProgressStageLine = currentProgressStageLine;
							if(!statusMax && curStatus < tgtStatus) {
								curStatus += 0.01f;
								if(curStatus < 0.99f) {
									currentFile.setStatus(curStatus);
									model.updateProgressBar(rowKey, curStatus);
								}
								else {
									currentFile.setStatus(0.99f);
									model.updateProgressBar(rowKey, 0.99f);
									statusMax = true;
								}
							}
							Thread.sleep(200);
						}
						else {
							Thread.sleep(2000);
						}
					}
					catch (InterruptedException e) { e.printStackTrace();}
				}
				
				
				currentFile.setStatus(1f);
				currentFile.setTargetStatus(1f);
				currentFile.setFileProgressStage("T: " + currentFile.getProcessingTime());
				String rowKey = currentFile.getCheckSum();
				model.updateProgressBar(rowKey, 1f);
				model.updateFileProgressStageColumn(rowKey);
				if(Logger.logLevelAbove(1)) { System.err.println("FilesProcessor: set current file status to 100%"); }
			}
		};
		fileStatusUpdatingThread.start();
		return currentFile;
	}

}
