package tools;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import core.ProgramLogic;
import holders.Configuration;
import holders.CurrentSessionFilesProcessingSettings;
import holders.ToolOptions;
import hud.Popups;
import holders.RowData;
import holders.TFVAR;

public class FileProcessor {
	
	public static boolean process(RowData fileData, CurrentSessionFilesProcessingSettings curFileSettings) {
		
		// notify user something is being done
		fileData.setTargetStatus(0.01f);
		
		ToolOptions opts = Configuration.OPTIONS;
		
		String absSourceFilePath = fileData.getFile().getAbsolutePath();
		
		//System.err.println("\n\n\nFile: " + absSourceFilePath + "\n");
		if(Logger.logLevelAbove(1)) { System.err.println("\nFile: " + absSourceFilePath); }
		
		boolean interpolateFiles = (opts.canSmooth() && curFileSettings.userWantInterpolate);
		boolean upscaleFiles = (opts.canUpscale() && curFileSettings.userWantUpscale);
		
		List<String> outList = null;
		List<String> errList = null;
		
		String[] outputs = null;
		
		if(interpolateFiles || upscaleFiles) {
			
			if(fileData.itsImage()) {
				if(fileData.itsAnimated()) {
					float fps = fileData.getFPS();
					if(interpolateFiles && (fps > 0)) {
						// extract frames
						// up-scale frames
						// interpolate (aka double) frames
						// maybe scale back to original size
						// pack frames in output file
					}
					else { // just upscale
						// extract frames
						// up-scale frames
						// maybe scale back to original size
						// pack frames in output file
					}
				}
				else if(upscaleFiles) { // regular up-scaling
					// up-scale image
				}
			}
			else { /* File its VIDEO */
				
				// Source file info
				String sourceFilePath = Utils.quotePath(absSourceFilePath);
				String sourceFileExt = Utils.getExtensionFromPath(absSourceFilePath);
				
				float sourceFileFPS = fileData.getFPS();
				
				float outputFileFPS = sourceFileFPS;
				
				String FRAMES_TYPE = Configuration.getVAR("$FRAMES_EXTENSION$").getValue();
				
				String FRAMES_STORING_FORMAT = Configuration.getVAR("$FRAMES_STORING_FORMAT_OUT$").getValue();
				
				String rootTempFolderPath = ProgramLogic.getTempFolderPath();
				
				String currentFileInitTempFolderPath = (rootTempFolderPath + "/uitool_temp_proccessing_" + System.currentTimeMillis());
				
				File temFolderFile = new File(currentFileInitTempFolderPath);
				temFolderFile.mkdirs();
				String currentFileTempFolderPath = temFolderFile.getAbsolutePath();
//				String currentFileTempFolderPath = "/mnt/files_4gb/Programs/image_ai_generator/upscaling/testing/uitool_temp_proccessing_1673000703403";
				
				List<TFVAR> vars = Configuration.VARS_TO_VIDEO_PROCESSING;
				
				String CURRENT_STAGE_INPUT_VIDEO_PATH = sourceFilePath;
				
				String CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH = ""; // "/mnt/files_4gb/Programs/image_ai_generator/upscaling/testing/uitool_temp_proccessing_1673000703403/frames_03_upscaled";
				
				// -vf scale=504:376
				boolean framesSizeBad = false;
				boolean alreadyInterpolated = false;
				
				int targetW = curFileSettings.outW;
				int targetH = curFileSettings.outH;
				
				int video_stage_idx = 1; // 1
				int frame_stage_idx = 0; // 0
				
//				targetW = curFileSettings.outW;
//				targetH = curFileSettings.outH;
				
				// calculate max process steps
				int maxSteps = 0;
				int curStep = 0;
				
				int vSize = vars.size();
				for (int vi = 0; vi < vSize; vi++) {
					String smallVARName = vars.get(vi).getName().toLowerCase();
					boolean upscalingStage = smallVARName.contains("upscal");
					boolean interpolationStage = smallVARName.contains("interpol");
					if(interpolationStage) {
						if(!interpolateFiles) {
						// skipping this stage, if needed
						}
						else {
							maxSteps++;
						}
					}
					else if(upscalingStage) {
						if(!upscaleFiles) {
						// skipping this stage, if needed
						}
						else {
							maxSteps++;
						}
					}
					else { // all cool -> execute and process what needed
						maxSteps++;
					}
					
				}
				
				fileData.setTargetStatus(0.02f);
				fileData.initProcessingTime();
				
				for (int vi = 0; vi < vSize && Configuration.PROCESSING; vi++) {
					TFVAR var = vars.get(vi);
					
					if(outList != null) {
						outList.clear();
						outList = null;
					}
					
					if(errList != null) {
						errList.clear();
						errList = null;
					}
					
					outList = new ArrayList<String>();
					errList = new ArrayList<String>();
					
					outputs = new String[2];
					
					String smallVARName = var.getName().toLowerCase();
					
					boolean upscalingStage = smallVARName.contains("upscal");
					boolean interpolationStage = smallVARName.contains("interpol");
					
					boolean encoderStartStage = smallVARName.contains("frames_extraction");
					
					boolean encoderEndStage = smallVARName.contains("frames_to_video");
					
					String command = var.getValue();
					
					//System.out.println("Stage: " + curStep + " CMD: " + command);
					
					if(interpolationStage) {
						
						outputFileFPS *= 2;
						
						// path to output files
						String java_scaled_frames_out_location = (currentFileTempFolderPath + "/frames_" + Utils.addZerosToName(frame_stage_idx, 2) + "_scaled_by_java");
						File folderToStore = new File(java_scaled_frames_out_location);
						folderToStore.mkdirs();
						
						curStep++;
						//ProgramLogic.folderStatusWatcher(fileData.setCurrentProcessingStage(curStep), CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH, java_scaled_frames_out_location, curStep, maxSteps, false);
						int[] status = new int[1];
						ProgramLogic.runJavaImageScalerWatcher(fileData.setCurrentProcessingStage(curStep), status, CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH, curStep, maxSteps);
						if(ImageProcessor.scaleImagesToSize(CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH, java_scaled_frames_out_location, 1280, 720, Configuration.THREADS, status)) {
							// store this value for next step as input source
							CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH = java_scaled_frames_out_location;
							
							maxSteps++;
							
							// increment step index
							frame_stage_idx++;
							//System.out.println("Frames was fixed by java (A)");
						}
						else {
							curStep--;
							
							boolean successfulRemove = false;
							if(folderToStore.listFiles().length == 0) {
								folderToStore.delete();
								successfulRemove = true;
							}
							
							if(!successfulRemove) {
								System.err.print("Error removing temp directory. Its can be not empty. Panic exit.");
								
								Logger.tempLog.append("Can't remove temp directory\n" + folderToStore.getAbsolutePath());
								Popups.showRuntimeErrorsLog();
								
								//System.exit(-1);
							}

						}
					}
					else if((vi + 1) == vSize) { // end stage, before pack and export video
						// path to output files
						String java_scaled_frames_out_location = (currentFileTempFolderPath + "/frames_" + Utils.addZerosToName(frame_stage_idx, 2) + "_scaled_by_java");
						File folderToStore = new File(java_scaled_frames_out_location);
						folderToStore.mkdirs();
						
						curStep++;
						//ProgramLogic.folderStatusWatcher(fileData.setCurrentProcessingStage(curStep), CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH, java_scaled_frames_out_location, curStep, maxSteps, false);
						// integer holder, aka pointer, for number of currently processed files by java image scaler
						int[] status = new int[1];
						ProgramLogic.runJavaImageScalerWatcher(fileData.setCurrentProcessingStage(curStep), status, CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH, curStep, maxSteps);
						if(ImageProcessor.scaleImagesToSize(CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH, java_scaled_frames_out_location, targetW, targetH, Configuration.THREADS, status)) {
							// store this value for next step as input source
							CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH = java_scaled_frames_out_location;
							
							maxSteps++;
							
							// increment step index
							frame_stage_idx++;
							//System.out.println("Frames was fixed by java (B)");
						}
						else {
							curStep--;
							
							boolean successfulRemove = false;
							if(folderToStore.listFiles().length == 0) {
								folderToStore.delete();
								successfulRemove = true;
							}
							
							if(!successfulRemove) {
								System.err.print("Error removing temp directory. Its can be not empty. Panic exit.");
								
								Logger.tempLog.append("Can't remove temp directory\n" + folderToStore.getAbsolutePath());
								Popups.showRuntimeErrorsLog();
								
								//System.exit(-1);
							}

						}
					}
					
					// preparing array for swapping on current execution stage 
					String[][] swaps = new String[][] {
						// set and mask FFMPEG path
						{ "$FFMPEG_EXEC_FILE_PATH$", Utils.quotePath(opts.FFMPEG_ENCODER_APP_PATH) },
						
						// set and mask UpscalerApp path
						{ "$UPSCALER_AI_APP_PATH$", (upscaleFiles ? Utils.quotePath(opts.REALESRGAN_AI_APP_PATH) : "NO_UPSCALING_APP_SPECIFIED_OR_NOT_ALLOWED") },
						
						// set and mask InterpolationApp path
						{ "$INTERPOLATION_AI_APP_PATH$", (interpolateFiles ? Utils.quotePath(opts.INTERPOLATION_IA_APP_PATH) : "NO_INTERPOLATION_APP_SPECIFIED_OR_NOT_ALLOWED") },
						
						// set source FPS value (from source file)
						{ "$SOURCE_VIDEO_FILE_FPS$", ("" + sourceFileFPS) },
						
						// set output FPS variable
						{ "$OUTPUT_VIDEO_FILE_FPS$", ("" + outputFileFPS) },
						
						// set current file temp folder
						{ "$TEMP_FILES_FOLDER$", currentFileTempFolderPath },
						
						// set scaler factor for upscaler
						{ "$UPSCALER_AI_SCALE_FACTOR$", "2" },
						
						// set scaling value for ffmpeg filter, unused, because before packing frames scaling done by Java module
						{ "$VIDEO_SCALING_VALUE$", "" },
						
						// set path for current stage input video file (can be changed by previous step)
						{ "$CURRENT_STAGE_INPUT_VIDEO_FILE_PATH$", Utils.quotePath(CURRENT_STAGE_INPUT_VIDEO_PATH) },
						
						// set path for current input folder with frames to process on current stage (can be changed by previous step)
						{ "$CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH$/" + FRAMES_STORING_FORMAT, Utils.quotePath(CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH + "/" + FRAMES_STORING_FORMAT.replace("$FRAMES_EXTENSION$", FRAMES_TYPE)) },
						
						{ "$CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH$", Utils.quotePath(CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH) },
						
						// set frames type variable
						{ "$FRAMES_EXTENSION$", FRAMES_TYPE }
					};
					
					if(!interpolateFiles && interpolationStage) {
						// skipping this stage, if needed
					}
					else if(!upscaleFiles && upscalingStage) {
						// skipping this stage, if needed
					}
					else { // all cool -> execute and process what needed
						
						//System.out.println("UP: " + upscalingStage);
						
						if(upscalingStage) {
							
							String statusSourceFolderPath = CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH;
							String statusTargetFolderPath = null;
							
							if(interpolateFiles && !alreadyInterpolated) { // TODO: Can be bag when this is newer changed, if there is no interpolation stage
								targetW = 1280;
								targetH = 720;
							}
							else {
								targetW = curFileSettings.outW;
								targetH = curFileSettings.outH;
							}
							
							if(framesSizeBad || ProgramLogic.needScalingStage(CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH, targetW, targetH)) {
								// can ge skipped if image size is bigger then minimum expected
								
								if(command.contains("CURRENT_STAGE_OUTPUT_FRAMES_FILES_LOCATION_PATH")) {
									// path to output files
									String current_stage_frames_out_location = (currentFileTempFolderPath + "/frames_" + Utils.addZerosToName(frame_stage_idx, 2) + "_" + getFramesStageName(frame_stage_idx, smallVARName));
									
									// make folder
									new File(current_stage_frames_out_location).mkdirs();
									
									// store this value for next step as input source
									CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH = current_stage_frames_out_location;
									
									statusTargetFolderPath = current_stage_frames_out_location;
									
									// swap value in command before execution
									command = command.replace("$CURRENT_STAGE_OUTPUT_FRAMES_FILES_LOCATION_PATH$", Utils.quotePath(current_stage_frames_out_location));
									
									// increment step index
									frame_stage_idx++;
								}
								
								//System.out.println("HERE A");
								
								int swsize = swaps.length;
								for (int swi = 0; swi < swsize; swi++) {
									command = command.replace(swaps[swi][0], swaps[swi][1]);
								}
								
								if(Configuration.PROCESSING) {
									curStep++;
									//ProgramLogic.folderStatusWatcher(fileData.setCurrentProcessingStage(curStep), statusSourceFolderPath, statusTargetFolderPath, curStep, maxSteps, false);
									ProgramLogic.runUpscalerStatusWatcher(fileData.setCurrentProcessingStage(curStep), outList, errList, statusSourceFolderPath, curStep, maxSteps);
									outputs = ProcessHandler.run(command, null, true, false, outList, errList, outputs);
									if(outputs == null) {
										System.err.println("ERROR EXEC: " + command);
										return false;
									}
								}
								else {
									return false;
								}
								
								if(ProgramLogic.needScalingStage(CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH, targetW, targetH)) {
									framesSizeBad = true;
									vi--; // go for another loop, if needed
								}
							}
							else {
								maxSteps--;
								//ProgramLogic.folderStatusWatcher(fileData.setCurrentProcessingStage(curStep), statusSourceFolderPath, statusTargetFolderPath, curStep, maxSteps, false);
							}
							
							// reset target size
							targetW = curFileSettings.outW;
							targetH = curFileSettings.outH;
						}
						else if(!upscalingStage) {
							//System.out.println("HERE B");
							//System.out.println(var.getName() + ": " + command);
							
							String statusSourceFolderPath = CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH;
							String statusTargetFolderPath = null;
							
							if(command.contains("CURRENT_STAGE_OUTPUT_VIDEO_FILE_PATH")) {
								// path to output files
								String current_stage_video_out_location = (currentFileTempFolderPath + "/video_" + Utils.addZerosToName(video_stage_idx, 2) + "." + sourceFileExt);
								
								if((vi + 1) == vSize) {
									String sourceFileName = fileData.getFile().getName();
									String sourceFileLocation = absSourceFilePath.trim().replace("/"+sourceFileName, "/").replace("\\"+sourceFileName, "\\");
									
									int extSplitLoc = sourceFileName.lastIndexOf('.');
									String fileName = ((extSplitLoc > 0) ? sourceFileName.substring(0, extSplitLoc)  : sourceFileName);
									String fileExt  = ((extSplitLoc > 0) ? ("." + sourceFileName.substring(extSplitLoc + 1)) : "");
									
									if(new File(sourceFileLocation + fileName + fileExt).exists()) {
										current_stage_video_out_location = (sourceFileLocation + fileName + "_fancy_" + targetW + "x" + targetH + fileExt);
									}

//									System.out.println("OutFile: " + current_stage_video_out_location);
//									System.exit(0);
								}
								
								// store this value for next step as input source
								CURRENT_STAGE_INPUT_VIDEO_PATH = current_stage_video_out_location;
								
								// swap value in command before execution
								command = command.replace("$CURRENT_STAGE_OUTPUT_VIDEO_FILE_PATH$", Utils.quotePath(current_stage_video_out_location));
								
								// increment step index
								video_stage_idx++;
							}
							
							if(command.contains("CURRENT_STAGE_OUTPUT_FRAMES_FILES_LOCATION_PATH")) {
								// path to output files
								String current_stage_frames_out_location = (currentFileTempFolderPath + "/frames_" + Utils.addZerosToName(frame_stage_idx, 2) + "_" + getFramesStageName(frame_stage_idx, smallVARName));
								
								// make folder
								new File(current_stage_frames_out_location).mkdirs();
								
								// store this value for next step as input source
								CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH = current_stage_frames_out_location;
								
								statusTargetFolderPath = current_stage_frames_out_location;
								
								// swap value in command before execution
								command = command.replace("$CURRENT_STAGE_OUTPUT_FRAMES_FILES_LOCATION_PATH$", Utils.quotePath(current_stage_frames_out_location));
								
								// increment step index
								frame_stage_idx++;
							}
							
							//System.out.println(var.getName() + ": " + command);
							//System.out.println(command);
							//System.exit(0);
							
							// replace all left variables, with corresponding values
							int swsize = swaps.length;
							for (int swi = 0; swi < swsize; swi++) {
								command = command.replace(swaps[swi][0], swaps[swi][1]);
							}
							
							if(Configuration.PROCESSING) {
								curStep++;
//								if(statusSourceFolderPath != null && statusTargetFolderPath && != null)
								//ProgramLogic.folderStatusWatcher(fileData.setCurrentProcessingStage(curStep), statusSourceFolderPath, statusTargetFolderPath, curStep, maxSteps, interpolationStage);
								if(encoderStartStage) {
									ProgramLogic.runVideoEncodeStatusWatcher(fileData.setCurrentProcessingStage(curStep), outList, errList, fileData.getEstimatedFramesCount(), curStep, maxSteps);
								}
								else if(interpolationStage) {
									alreadyInterpolated = true;
									ProgramLogic.runInterpolatorStatusWatcher(fileData.setCurrentProcessingStage(curStep), outList, errList, statusSourceFolderPath, curStep, maxSteps);
								}
								else if(encoderEndStage) {
									ProgramLogic.runVideoEncodeStatusWatcher(fileData.setCurrentProcessingStage(curStep), outList, errList, ProgramLogic.getNumFiles(statusSourceFolderPath), curStep, maxSteps);
								}

								outputs = ProcessHandler.run(command, null, true, false, outList, errList, outputs);
								if(outputs == null) {
									System.err.println("ERROR EXEC: " + command);
									return false;
								}
							}
							else {
								return false;
							}
						}
						

					}
				}
				
				// extract audio
				// extract frames
				
				// if interpolate
				// up-scale if less then 1280x720
				// scale-down with pixel filling if more then 1280x720
				// interpolate 
				// up-scale to target or higher resolution
				// scale images again
				// pack everything in output file
				
				// remove temp folder if its needed, and enhancing process does not interrupted
				if(Configuration.PROCESSING && curFileSettings.removeTempFiles && opts.removeTempFiles()) {
					Utils.deleteDirectory(temFolderFile);
				}
			}

		}
		else { // do something else with video, if no tools available, or nothing is selected
			
		}
		

		
		return Configuration.PROCESSING;
	}
	
	private static String getFramesStageName(int stage, String key) {
		if(key != null && key.contains("interpol")) {
			return "interpolated";
		}
		else if(key != null && key.contains("upscal")) {
			return "upscaled";
		}
		else { // all cool -> execute and process what needed
			switch (stage) {
				case 0  : return "source";
				default : return "scaled_by_java";
			}
		}
	}

}
