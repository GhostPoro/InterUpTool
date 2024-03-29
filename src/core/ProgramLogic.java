package core;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import holders.Configuration;
import holders.Configuration.OS;
import holders.CurrentSessionFilesProcessingSettings;
import holders.RowData;
import holders.TFVAR;
import holders.Text;
import tools.ImageProcessor;
import tools.Logger;
import tools.ProcessHandler;
import tools.Utils;

public class ProgramLogic {
	
	public static String[] simpleShellExec(String cmd) {
		if(Logger.logLevelAbove(2)) { System.err.println("EXEC: " + cmd); }
		return ProcessHandler.run(cmd, null, true, true);
	}
	
	public static boolean checkExecOut(String exec, String a, String b) {
		String[] out = ProgramLogic.simpleShellExec(exec);
		if((out != null) && (out.length > 1)) {
			String[] words = combineOutsToLine(out).split(" ");
			int wsize = words.length;
			if(wsize > 1) {
				int wsizemo = (wsize - 1);
				for (int i = 0; i < wsizemo; i++) {
					if(words[i].equals(a) && words[i + 1].equals(b)) {
						out = ProcessHandler.destroy(out);
						return true;
					}
				}
			}
		}
		else {
			if(Logger.logLevelAbove(1)) { System.err.println("ERROR! Can't read EXEC output!"); }
		}
		
		if(Logger.logLevelAbove(1)) {
			System.err.println("ERROR for 'checkExecOut' and '" + exec + "' -> No expected parameters in out! Looking for A - '" + a + "' and B - '" + b + "'");
		}
		
		out = ProcessHandler.destroy(out);
		return false;
	}
	
	public static String combineOutsToLine(String[] outs) {
		String outAsLine = Utils.trimSpaces(((outs[0] == null) ? "" : outs[0]).replace('\n', ' ') + " " + ((outs[1] == null) ? "" : outs[1]).replace('\n', ' '));
		//System.err.println("OUT_LINE: " + outAsLine);
		return outAsLine;
	}
	
	public static boolean checkExistence(String path) {
		if(path != null) {
			File dummy = new File(path);
			return (dummy.exists() && dummy.isFile());
		}
		return false;
	}
	
	public static String getTempFolderPath() {
		TFVAR tmpFolderVAR = Configuration.getVAR("$TEMP_FILES_FOLDER$");
		if(tmpFolderVAR != null) {
			String tmpFolderPath = tmpFolderVAR.getValue();
			if(tmpFolderPath != null) {
				File folderFile = new File(tmpFolderPath);
				if(folderFile.exists() && folderFile.isDirectory()) {
					return folderFile.getAbsolutePath();
				}
				
				folderFile.mkdirs();
				if(folderFile.exists() && folderFile.isDirectory()) {
					return folderFile.getAbsolutePath();
				}
			}
		}
		
		File folderFile = new File(Configuration.DEFAULT_PATH_ALL_TEMP_FILES_LOCATION);
		folderFile.mkdirs();
		
		return folderFile.getAbsolutePath();
	}
	
	public static boolean needScalingStage(String location, int targetW, int targetH) {
		File framesFolder = new File(location);
		if(framesFolder.exists() && framesFolder.isDirectory()) {
			File[] filesInside = framesFolder.listFiles();
			if(filesInside != null) {
				int size = filesInside.length;
				for (int fi = 0; fi < size; fi++) {
					if(filesInside[fi].isFile()) {
						BufferedImage image = ImageProcessor.loadFromFile(filesInside[fi].getAbsolutePath());
						if(image == null || image.getWidth() < targetW || image.getHeight() < targetH) {
							return true;
						}
						else {
							return false;
						}
					}
				}
			}
		}
		return false;
	}
	
	public static int[] needScalingStage(String location, int targetW, int targetH, int[] holder) {
		File framesFolder = new File(location);
		
		if(Logger.logLevelAbove(1)) {
			System.out.println("Image Path: " + location + " EXIST: " + framesFolder.exists());
		}
		
		String filePath = null;
		
		if(framesFolder.exists()) {
			if(framesFolder.isDirectory()) {
				File[] filesInside = framesFolder.listFiles();
				if(filesInside != null) {
					boolean noFile = true;
					int size = filesInside.length;
					for (int fi = 0; fi < size && noFile; fi++) {
						File curFile = filesInside[fi];
						if(curFile.isFile()) {
							noFile = false;
							filePath = curFile.getAbsolutePath();
						}
					}
				}
			}
		}
		else {
			filePath = location;
		}
		
		if(filePath != null) {
			BufferedImage image = ImageProcessor.loadFromFile(filePath);
			if(image != null) {
				int sourceW = image.getWidth();
				int sourceH = image.getHeight();
				if(sourceW < targetW || sourceH < targetH) {
					return new int[] { sourceW, sourceH };
				}
				else {
					return null;
				}
			}
		}
		
		return null;
	}
	
	public static boolean folderStatusWatcher(final RowData row, String sourceFolderPath, String targetFolderPath, int curStage, int endStage, boolean multiply) {
		if(sourceFolderPath != null) {
			final File sourceFolderFile = new File(sourceFolderPath);
			if(sourceFolderFile.isDirectory()) {
				int quantityOfFilesInSourceFolder = getNumFiles(sourceFolderFile);
				final int stageMax = (multiply ? (quantityOfFilesInSourceFolder * 2) : quantityOfFilesInSourceFolder);
				return folderStatusWatcher(row, targetFolderPath, stageMax, curStage, endStage);
			}
		}
		return false;
	}
	
	public static boolean folderStatusWatcher(final RowData row, String targetFolderPath, final int stageMax, final int curStage, final int endStage) {
		
		float fcurStage = curStage;
		float fendStage = endStage;
		
		updateRowStatus(row, fendStage, fcurStage, 0);
		row.setFileProgressStage("0/" + stageMax + "/" + curStage + " of " + endStage);
		
		if(targetFolderPath != null) {
			final File targetFolderFile = new File(targetFolderPath);
			if(targetFolderFile.isDirectory()) {
				Thread thread = new Thread() {
				    public void run() {
				    	
				    	int filesInTargetFolder = 0;
				    	
				    	do {
				    		if(targetFolderFile != null) {
				    			
				    		}
				    		filesInTargetFolder = getNumFiles(targetFolderFile);
				    		if(filesInTargetFolder > 0) {
					    		float fstageCur = filesInTargetFolder;
					    		float fstageMax = stageMax;
					    		updateRowStatus(row, fendStage, fcurStage, (1f / fstageMax * fstageCur));
					    		row.setFileProgressStage(filesInTargetFolder + "/" + stageMax + "/" + curStage + " of " + endStage);
				    		}
				    		try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
				    	}
				    	while(Configuration.PROCESSING && row.beingProcessed() && (filesInTargetFolder < stageMax) && (row.getCurrentProcessingStage() == curStage));
				    }
				};
				thread.start();
				return true;
			}
		}
		
		return false;
	}
	
	
	// TODO: fix inconsistency 
	public static boolean runUpscalerStatusWatcher(final RowData row, final List<String> outList, List<String> errList, final String sourceFolderPath, final int curStage, final int endStage) {
		
		float fcurStage = curStage;
		float fendStage = endStage;
		
		final int stageMax = getNumFiles(sourceFolderPath);
		float fstageMax = stageMax;
		
		updateRowStatus(row, fendStage, fcurStage, 0);
		row.setFileProgressStage("0/" + stageMax + "/" + curStage + " of " + endStage);

		Thread thread = new Thread() {
		    public void run() {
		    	
		    	int fileIdx = 0;
		    	int prevFileIdx = 0;
		    	
		    	int outListMaxIdx = 0;
		    	int errListMaxIdx = 0;
		    	
		    	int outListCurIdx = 0;
		    	int errListCurIdx = 0;
		    	
		    	do {
		    		
			    	outListCurIdx = outList.size();
			    	errListCurIdx = errList.size();
			    	
			    	if(outListMaxIdx < outListCurIdx) {
			    		outListMaxIdx = outListCurIdx;
			    		
			    		if(outListMaxIdx > 0) {
			    			
			    			int counter = 0;
			    			
			    			boolean partA = false;
			    			boolean partB = false;
			    			boolean partC = false;
			    			
			    			for (int li = 0; li < outListMaxIdx; li++) {
			    				String line = outList.get(li);
			    				if(line.contains("%")) {
			    					try {
				    					float currProcentage = Float.parseFloat(line.replace("%","").trim());
				    					
				    					if(!partC && partB && (currProcentage > 75f)) {
				    						partC = true;
				    					}
	
				    					if(!partB && partA && (currProcentage > 50f)) {
				    						partB = true;	
				    					}
	
				    					if(!partA && (currProcentage > 25f)) {
				    						partA = true;
				    					}
	
			    					} catch (Exception e) {}
			    				}
							}
			    			
			    			if(fileIdx < counter) {
			    				fileIdx = counter;
			    			}
			    		}
			    	}
			    	
			    	if(errListMaxIdx < errListCurIdx) {
			    		errListMaxIdx = errListCurIdx;
			    		
			    		if(errListMaxIdx > 0) {
			    			
			    			int counter = 0;
			    			
			    			boolean partA = false;
			    			boolean partB = false;
			    			boolean partC = false;
			    			
			    			for (int li = 0; li < errListMaxIdx; li++) {
			    				String line = errList.get(li);
			    				if(line.contains("%")) {
			    					try {
				    					float currProcentage = Float.parseFloat(line.replace("%","").trim());
				    					
				    					if(partA && partB && partC && (currProcentage < 25f)) {
							    			partA = false;
							    			partB = false;
							    			partC = false;
							    			counter++;
				    					}
				    					
				    					if(!partC && partB && (currProcentage > 75f)) {
				    						partC = true;
				    					}
	
				    					if(!partB && partA && (currProcentage > 50f)) {
				    						partB = true;	
				    					}
	
				    					if(!partA && (currProcentage > 25f)) {
				    						partA = true;
				    					}
				    				} catch (Exception e) {}
			    				}
							}
			    			
			    			if(fileIdx < counter) {
			    				fileIdx = counter;
			    			}
			    		}
			    	}
		    		
		    		if(prevFileIdx != fileIdx) {
			    		float fstageCur = fileIdx;
			    		updateRowStatus(row, fendStage, fcurStage, (1f / fstageMax * fstageCur));
			    		row.setFileProgressStage(fileIdx + "/" + stageMax + "/" + curStage + " of " + endStage);
			    		prevFileIdx = fileIdx;
		    		}

		    		try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
		    	}
		    	while(Configuration.PROCESSING && row.beingProcessed() && (fileIdx < stageMax) && (row.getCurrentProcessingStage() == curStage));
		    	
		    	updateRowStatus(row, fendStage, fcurStage, 1);
				row.setFileProgressStage(stageMax + "/" + stageMax + "/" + curStage + " of " + endStage);
		    }
		};
		thread.start();
		return true;
	}
	
	public static boolean runInterpolatorStatusWatcher(final RowData row, final List<String> outList, List<String> errList, final String sourceFolderPath, final int curStage, final int endStage) {
		
		float fcurStage = curStage;
		float fendStage = endStage;
		
		final int stageMax = (getNumFiles(sourceFolderPath) * 2);
		float fstageMax = stageMax;
		
		updateRowStatus(row, fendStage, fcurStage, 0);
		row.setFileProgressStage("0/" + stageMax + "/" + curStage + " of " + endStage);

		Thread thread = new Thread() {
		    public void run() {
		    	
		    	int fileIdx = 0;
		    	int prevFileIdx = 0;
		    	
		    	int outListCurIdx = 0;
		    	int errListCurIdx = 0;
		    	
		    	int outListStartIdx = -1;
		    	int errListStartIdx = -1;
		    	
		    	do {
		    		
			    	outListCurIdx = outList.size();
			    	errListCurIdx = errList.size();
			    	
			    	if(outListStartIdx > 0) {
			    		int sizeWithoutStart = (outListCurIdx - outListStartIdx);
		    			if(fileIdx < sizeWithoutStart) {
		    				fileIdx = sizeWithoutStart;
		    			}
			    	}
			    	else {
		    			for (int li = 0; li < outListCurIdx; li++) {
		    				String line = outList.get(li);
		    				if(line.contains(" done")) {
		    					outListStartIdx = li;
		    				}
						}
			    	}
			    	
			    	if(errListStartIdx > 0) {
			    		int sizeWitherrStart = (errListCurIdx - errListStartIdx);
		    			if(fileIdx < sizeWitherrStart) {
		    				fileIdx = sizeWitherrStart;
		    			}
			    	}
			    	else {
		    			for (int li = 0; li < errListCurIdx; li++) {
		    				String line = errList.get(li);
		    				if(line.contains(" done")) {
		    					errListStartIdx = li;
		    				}
						}
			    	}
		    		
		    		if(prevFileIdx != fileIdx) {
			    		float fstageCur = fileIdx;
			    		updateRowStatus(row, fendStage, fcurStage, (1f / fstageMax * fstageCur));
			    		row.setFileProgressStage(fileIdx + "/" + stageMax + "/" + curStage + " of " + endStage);
			    		prevFileIdx = fileIdx;
		    		}

		    		try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
		    	}
		    	while(Configuration.PROCESSING && row.beingProcessed() && (fileIdx < stageMax) && (row.getCurrentProcessingStage() == curStage));
		    	
				updateRowStatus(row, fendStage, fcurStage, 1);
				row.setFileProgressStage(stageMax + "/" + stageMax + "/" + curStage + " of " + endStage);
		    }
		};
		thread.start();
		return true;
	}
	
	public static boolean runJavaImageScalerWatcher(final RowData row, int[] status, final String sourceFolderPath, final int curStage, final int endStage) {
		
		float fcurStage = curStage;
		float fendStage = endStage;
		
		final int stageMax = getNumFiles(sourceFolderPath);
		float fstageMax = stageMax;
		
		updateRowStatus(row, fendStage, fcurStage, 0);
		row.setFileProgressStage("0/" + stageMax + "/" + curStage + " of " + endStage);

		if(status != null && status.length > 0) {
			Thread thread = new Thread() {
			    public void run() {
			    	
			    	int lastIndex = -1;
			    	
			    	do {
			    		int statusIndex = status[0];
			    		if(statusIndex > lastIndex) {
			    			float fstageCur = statusIndex;
			    			updateRowStatus(row, fendStage, fcurStage, (1f / fstageMax * fstageCur));
			    			row.setFileProgressStage(statusIndex + "/" + stageMax + "/" + curStage + " of " + endStage);
			    			lastIndex = statusIndex;
			    		}
			    		
			    		try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
			    	}
			    	while(Configuration.PROCESSING && row.beingProcessed() && (lastIndex < stageMax) && (row.getCurrentProcessingStage() == curStage));
			    	
					updateRowStatus(row, fendStage, fcurStage, 1);
					row.setFileProgressStage(stageMax + "/" + stageMax + "/" + curStage + " of " + endStage);
			    }
			};
			thread.start();
			return true;
		}
		return false;
	}
	
	public static boolean runVideoEncodeStatusWatcher(final RowData row, final List<String> outList, List<String> errList, final int stageMax, final int curStage, final int endStage) {
		
		float fcurStage = curStage;
		float fendStage = endStage;
		float fstageMax = stageMax;
		
		updateRowStatus(row, fendStage, fcurStage, 0);
		row.setFileProgressStage("0/" + stageMax + "/" + curStage + " of " + endStage);

		Thread thread = new Thread() {
		    public void run() {
		    	
		    	int curFrame = 0;
		    	int lastFrame = 0;
		    	
		    	int outListSize = 0;
		    	int errListSize = 0;
		    	
		    	int outListStart = 0;
		    	int errListStart = 0;
		    	
		    	do {
		    		
			    	outListSize = outList.size();
			    	errListSize = errList.size();
			    	
			    	if(outListStart < outListSize) {
		    			for (int li = outListStart; li < outListSize; li++) {
		    				
		    				// sometimes because of concurrency, in this place list size 
		    				// can be == 0, so need to check it once more
		    				
		    				if(li < outList.size()) {
			    				String line = outList.get(li);
			    				if(line != null && line.trim().startsWith("frame")) {
			    					try {
			    						int localFrame = Integer.parseInt(line.split(" ")[0].replace("frame=", "").trim());
			    						if(localFrame > curFrame) {
			    							curFrame = localFrame;
			    						}
			    					}
			    					catch (Exception e) {}
			    				}
		    				}
						}
			    	}
	    			
	    			if(errListStart < errListSize) {
		    			for (int li = errListStart; li < errListSize; li++) {
		    				
		    				// sometimes because of concurrency, in this place list size 
		    				// can be == 0, so need to check it once more
		    				
		    				if(li < errList.size()) {
		    					String line = errList.get(li);
			    				if(line != null && line.trim().startsWith("frame")) {
			    					try {
			    						int localFrame = Integer.parseInt(line.split(" ")[0].replace("frame=", "").trim());
			    						if(localFrame > curFrame) {
			    							curFrame = localFrame;
			    						}
			    					}
			    					catch (Exception e) {}
			    				}
		    				}
						}
	    			}
	    			
			    	outListStart = outListSize;
			    	errListStart = errListSize;
		    		
		    		if(curFrame > lastFrame) {
			    		float fstageCur = curFrame;
			    		updateRowStatus(row, fendStage, fcurStage, (1f / fstageMax * fstageCur));
			    		row.setFileProgressStage(curFrame + "/" + stageMax + "/" + curStage + " of " + endStage);
			    		lastFrame = curFrame;
		    		}

		    		try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
		    	}
		    	while(Configuration.PROCESSING && row.beingProcessed() && (curFrame < stageMax) && (row.getCurrentProcessingStage() == curStage));
		    	
				updateRowStatus(row, fendStage, fcurStage, 1);
				row.setFileProgressStage(stageMax + "/" + stageMax + "/" + curStage + " of " + endStage);
		    }
		};
		thread.start();
		return true;
	}
	
	public static float updateRowStatus(RowData row, float endS, float curS, float inc) {
		float fileStatus = (1f / endS * ((curS - 1f) + inc));
		if(fileStatus >= 1f) {
			row.setTargetStatus(0.992f);
		}
		else if(fileStatus < 0.1f) {
			row.setTargetStatus(0.012f);
		}
		else {
			row.setTargetStatus(fileStatus);
		}
		return fileStatus;
	}
	
	public static int getNumFiles(String path) {
		return getNumFiles(new File(path));
	}
	
	private static int getNumFiles(File dir) {
		if(dir != null) {
			String[] list = dir.list();
			if(list != null) {
				return list.length;
			}
		}
		return -1;
	}

	public static OS getOperetionSystemType() {
		OS currentOS = OS.LINUX;
        String osName = System.getProperty("os.name");
        if(osName.toLowerCase().contains("mac")) {
        	currentOS = OS.MAC;
        }
        else if(osName.toLowerCase().contains("win")) {
        	currentOS = OS.WIN;
        }
        return currentOS;
	}

}
