package tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import holders.Configuration;
import holders.Text;
import holders.ToolOptions;
import hud.Popups;
import hud.UpdatableTableModel;

public class FilesAnalyzer {

	public static FilesAnalyzer CURRENT = null;

	private final List<File> myTasks;
	private final List<File> remoteTasks;

	public FilesAnalyzer(UpdatableTableModel model, ToolOptions opts) {
		this.myTasks = new ArrayList<File>();
		this.remoteTasks = new ArrayList<File>();

		Thread analyzerThread = new Thread() {
			public void run() {
				// Wait while JPanel add files to list
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}

				boolean useFFPROBE = opts.canGetInfo();

				String testerExec = (useFFPROBE ? opts.FFMPEG_INFO_APP_PATH : opts.FFMPEG_ENCODER_APP_PATH);

				while (myTasks.size() > 0 || remoteTasks.size() > 0) {

					for (int i = 0; i < remoteTasks.size(); i++) {
						myTasks.add(remoteTasks.get(i));
					}
					remoteTasks.clear();

					while (myTasks.size() > 0) {
						File fileToProcess = myTasks.get(0);
						
						String filePath = fileToProcess.getAbsolutePath();
						
						//boolean notWEBP = !filePath.toLowerCase().endsWith("webp");
						
						final long taskID = model.getRowByPath(filePath).uniqueID;
						
						String[] output = ProcessHandler.run(Utils.quotePath(testerExec) + " -i " + Utils.quotePath(filePath), null, true, true);
						
						if (contains(output, 1, "Duration:") || contains(output, 1, "Video:")) {
							model.setFileProperties(taskID, extractData(output[1], filePath), true);
						} else if (contains(output, 0, "Duration:") || contains(output, 0, "Video:")) {
							model.setFileProperties(taskID, extractData(output[0], filePath), true);
						} else {
							if (contains(output, 1, (filePath + ":"))) {
								extractAndLogError(output[1], filePath);
								model.setFileProperties(taskID, "INVALID_FILE_DATA", false);
							} else if (contains(output, 0, (filePath + ":"))) {
								extractAndLogError(output[0], filePath);
								model.setFileProperties(taskID, "INVALID_FILE_DATA", false);
							} else {
								String errrOutputLine = Text.FILESANALYZER_ERROR_MAIN_THREAD.replace("$FILE_PATH$", filePath);
								Logger.tempLog.append(errrOutputLine);
								model.setFileProperties(taskID, "CANT_RETRIEVE_FILE_INFO", false);
							}
						}
						
				        // show errors what may have been appear
				        Popups.showRuntimeErrorsLog();
				        
				        output = ProcessHandler.destroy(output);

						myTasks.remove(0);
					}
				}

				CURRENT = null;
			}
		};

		if (opts.canProcess()) {
			analyzerThread.start();
		}
	}
	
	private boolean contains(String[] holder, int idx, String what) {
		if(holder != null) {
			if(idx < holder.length) {
				if(holder[idx] != null) {
					return holder[idx].contains(what);
				}
			}
		}
		return false;
	}
	
	private boolean extractAndLogError(String output, String path) {
		String[] lines = output.split("\n");
		int size = lines.length;
		
		String possibleErrorLocation = (path + ":");
		
		for (int li = size - 1; li > -1; li--) {
			if(lines[li].contains(possibleErrorLocation)) {
				String line = lines[li];
				int lengthS = possibleErrorLocation.length();
				int lengthE = line.length();
				
				String errrOutputLine = Text.FILESANALYZER_ERROR_EXTRTACT_AND_LOG.replace("$FILE_PATH$", path).replace("$REASON$", line.substring(lengthS, lengthE).trim());
				
				Logger.tempLog.append(errrOutputLine);
				return true;
			}
		}
		return false;
	}

	private String extractData(String output, String sourceFilePath) {
		String[] lines = output.split("\n");
		int size = lines.length;

		boolean lookingForDurationLine = true;
		boolean lookingForVideoLine = true;
		boolean lookingForAudioLine = true;
		
		// WebP staff
		boolean isImageWEBP = false;
		boolean containFlagANIM = false;
		boolean containFlagANMF = false;
		String webpVideoLine = null;
		
		
		// return data
		String duration = "UNKN";
		String tempBitrate = null;

		String bitrate    = "UNKN";
		String vcodec     = "UNKN";
		String icodec     = "UNKN";
		String dimentions = "UNKN";
		String frames     = "UNKN";
		
		boolean isImage         = false;
		boolean isAnimatedImage = false;
		
		boolean containAudio = false;

		for (int li = 0; li < size && (lookingForVideoLine || lookingForDurationLine || lookingForAudioLine) && !isImageWEBP; li++) {
			String line = lines[li];

			if (lookingForVideoLine && line.contains(" Video:")) { // space needed to exclude error for webp
				lookingForVideoLine = false;
				
				try {
					List<String> allMatches = new ArrayList<String>();
					Matcher m = Pattern.compile("\\([^)(]+\\)").matcher(line);
					while (m.find()) {
						allMatches.add(m.group());
					}
					
					for(String match : allMatches) {
						line = line.replace(match, match.replace(" ", ""));
					}
					
					String[] parts = line.replaceAll("\\(\\s+|\\s+\\)", "()").split("Video:")[1].split(", ");

					String[] partsCodec = parts[0].split("\\) \\(");

					vcodec = partsCodec[0].trim().split(" ")[0];
					
					switch (vcodec) {
						case "png"   :
						case "mjpeg" : isImage = true;         break;
						
						case "webp"  : isImageWEBP = true;     break;
						
						case "gif"   :
						case "apng"  : isAnimatedImage = true; break;
						
						default      : break;
					}
					
					if(isImage || isAnimatedImage || isImageWEBP) {
						lookingForAudioLine = false;
					}
					
					if (partsCodec.length > 1) {
						vcodec += ("(" + partsCodec[1].trim().split("/")[0].replace(")", "").trim() + ")");
					}

					icodec = parts[1].split("\\(")[0].trim();

					dimentions = parts[2].trim().split(" ")[0].trim();
					
					int psize = parts.length;
					for (int pi = 3; pi < psize; pi++) {
						String val = parts[pi].trim();
						if (val.toLowerCase().contains("kb")) {
							bitrate = val.trim();
						}
						else if(val.toLowerCase().contains("fps")) {
							frames = val.trim();
						}
					}
				} catch (Exception e) {
					System.err.println("FilesAnalyzer.extractData() ERROR! Processing Line: " + line + "\n\n");
					e.printStackTrace();
				}
			}
			else if (lookingForAudioLine && line.contains("Audio:")) {
				lookingForAudioLine = false;
				containAudio = true;
			}
			else if (lookingForDurationLine && line.contains("Duration:")) {
				lookingForDurationLine = false;
				
				try {
					String[] parts = line.split(",");
					duration = parts[0].trim().split(" ")[1].trim();
					if(parts.length > 2 && parts[2] != null) {
						tempBitrate = parts[2].trim().split(":")[1].trim();
					}
				} catch (Exception e) {
					System.err.println("FilesAnalyzer.extractData() ERROR! Processing Line: " + line + "\n\n");
					e.printStackTrace();
				}
			}
			
			// special for webp
			else if (line.contains("webp") && line.contains("chunk")) {
				
				// [webp @ 0x55ecba2827c0] skipping unsupported chunk: ANIM
				// [webp @ 0x55ecba2827c0] skipping unsupported chunk: ANMF
				
				if(line.contains("ANIM")) {
					containFlagANIM = true;
				}
				else if(line.contains("ANMF")) {
					containFlagANMF = true;
				}
				
			}
		}
		
		if(isImageWEBP) {
			
			// can be animated
			if(containFlagANIM || containFlagANMF) {
				
				// get file info from external lib
				
				// return this info
				return WebpAnalyzer.getAnimWebpInfoSimple(sourceFilePath);
			}
			// otherwise
			return (vcodec + " / " + dimentions);
		}
		
		if(isImage && !containAudio) {
			if(isAnimatedImage) {
				return (vcodec + " / " + dimentions + " / " + frames);
			}
			else {
				return (vcodec + " / " + dimentions);
			}
		}
		
		if(bitrate.equals("UNKN")) {
			bitrate = tempBitrate;
		}

		String out = (duration + " / " + dimentions + " / " + frames + " / " + bitrate + " / " + vcodec + " / " + icodec);

		return out;
	}

	public FilesAnalyzer addTask(File file) {
		if (file.exists() && file.isFile()) {
			this.remoteTasks.add(file);
		}
		return this;
	}
	
	public static boolean addTask(UpdatableTableModel model, File file) {
		if(FilesAnalyzer.CURRENT == null) {
			FilesAnalyzer.CURRENT = new FilesAnalyzer(model, Configuration.OPTIONS);
		}
		FilesAnalyzer.CURRENT.addTask(file);
		return true;
	}

}
