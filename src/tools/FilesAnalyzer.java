package tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import holders.Configuration;
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
						
//						(((fileExt.length() > 0) ? (fileExt + ": ") : (filePath + ":"))))
//						String fileExt = Utils.getExtensionFromPath(filePath);

						String[] output = ProcessHandler.run(Utils.quotePath(testerExec) + " -i " + Utils.quotePath(filePath), null, true, true);
						
//						System.out.println(output[0]);
//						System.out.println(output[1]);

						if (contains(output, 1, "Duration:") || contains(output, 1, "Video:")) {
							model.setFileProperties(filePath, extractData(output[1]), true);
						} else if (contains(output, 0, "Duration:") || contains(output, 0, "Video:")) {
							model.setFileProperties(filePath, extractData(output[0]), true);
						} else {
							if (contains(output, 1, (filePath + ":"))) {
								extractAndLogError(output[1], filePath);
								model.setFileProperties(filePath, "INVALID_FILE_DATA", true);
							} else if (contains(output, 0, (filePath + ":"))) {
								extractAndLogError(output[0], filePath);
								model.setFileProperties(filePath, "INVALID_FILE_DATA", true);
							} else {
								String errrOutputLine = "FilesAnalyzer Error! FFMPEG can't retrieve information about file:\n" + filePath + ".\nReason: Invalid path to file.\n";
								Logger.tempLog.append(errrOutputLine);
								model.setFileProperties(filePath, "CANT_retrieve_FILE_INFO", true);
							}
						}
						
				        // show errors what may have been appear
				        Popups.showRuntimeErrorsLog();

						myTasks.remove(0);
					}
				}

				CURRENT = null;
				// System.out.println("FilesAnalyzer Done!");
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
				
				String errrOutputLine = "FilesAnalyzer Error! FFMPEG can't retrieve information about file:\n" + path + ".\nReason: " + line.substring(lengthS, lengthE).trim() + "\n";
				
				Logger.tempLog.append(errrOutputLine);
				return true;
			}
		}
		return false;
	}

	private String extractData(String output) {
		String[] lines = output.split("\n");
		int size = lines.length;

		boolean lookingForDurationLine = true;
		boolean lookingForVideoLine = true;
		boolean lookingForAudioLine = false;

		// return data
		String duration = "UNKN";
		String tempBitrate = null;

		String bitrate = "UNKN";
		String vcodec = "UNKN";
		String icodec = "UNKN";
		String dimentions = "UNKN";
		String frames = "UNKN";
		
		boolean isImage         = false;
		boolean isAnimatedImage = false;
		
		boolean containAudio = false;

		for (int li = 0; li < size && (lookingForVideoLine || lookingForDurationLine || lookingForAudioLine); li++) {
			String line = lines[li];
			if (line.contains("Audio:")) {
				lookingForAudioLine = false;
				containAudio = true;
			}
			else if (line.contains("Duration:")) {
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
			} else if (line.contains(" Video:")) {
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
						case "webp"  : 
						case "png"   :
						case "mjpeg" : isImage = true; break;
						
						case "gif"   :
						case "apng"  : isAnimatedImage = true; isImage = true; break;
						
						default      : break;
					}
					
					if(isImage) {
						lookingForAudioLine = true;
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

//		System.out.println(out);
//		System.exit(0);

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
