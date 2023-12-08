package holders;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.ProgramLogic;
import holders.Configuration.OS;
import tools.Logger;
import tools.TextProcessor;
import tools.Utils;

public class ToolOptions {
	
	public final String FFMPEG_ENCODER_APP_PATH;
	public final String FFMPEG_INFO_APP_PATH;
	
	public final String REALESRGAN_AI_APP_PATH;
	public final String REALESRGAN_AI_MODEL_PATH;
	
	public final String INTERPOLATION_IA_APP_PATH;
	public final String INTERPOLATION_IA_MODEL_PATH;
	
	public final String WEBP_ANIMATION_APP_PATH;
	
	// frames format
	public final String FRAMES_EXTENSION;
	public final String FRAMES_STORING_FORMAT;
	
	// if its not set -> store in source file/folder location
	private String outputFilesFolderPath = null;
	
	// if its not set -> create folder in program location folder 
	private String tempFilesFolderPath = null;
	
	// program settings
	private boolean canUpscale = false;
	private boolean canSmooth  = false;
	private boolean canGetInfo = false;
	private boolean canProcess = false;
	
	private boolean supportAWEBP = false;
	
	// optional settings (can be altered with run/config options)
	private boolean removeTempFiles = true;
	private int logLevel = 0;
	
	public ToolOptions(TFVAR ffmpegEncAppPathVar, TFVAR ffmpegInfoAppPathVar, TFVAR scalerAppPathVar, TFVAR scalerModelPathVar, TFVAR fpsMakerAppPathVar, TFVAR fpsMakerModelPathVar, TFVAR webpmuxAppPathVar, TFVAR framesNameVar, TFVAR framesTypeVar, int logLevel) {

		this.increaseLogLevelTo(logLevel);
		
		if(Logger.logLevelAbove(1)) { System.out.println("Tool Available Options:"); }
		
		this.FFMPEG_ENCODER_APP_PATH = ((ffmpegEncAppPathVar  != null) ? ffmpegEncAppPathVar.getValue()  : null);
		this.FFMPEG_INFO_APP_PATH    = ((ffmpegInfoAppPathVar != null) ? ffmpegInfoAppPathVar.getValue() : null);
		
		this.checkFFMPEG(this.FFMPEG_ENCODER_APP_PATH, this.FFMPEG_INFO_APP_PATH);
		if(ffmpegEncAppPathVar  != null) { ffmpegEncAppPathVar .setValid(this.canProcess); }
		if(ffmpegInfoAppPathVar != null) { ffmpegInfoAppPathVar.setValid(this.canGetInfo); }
		
		if(Logger.logLevelAbove(1)) {
			//System.out.println(this.canProcess ? "FFMPEG:  Good!" : "FFMPEG:  NOT FOUND" );
			System.out.println(this.canGetInfo ? "FFPROBE: Good!" : "FFPROBE: NOT FOUND" );
		}
		
		this.REALESRGAN_AI_APP_PATH   = ((scalerAppPathVar   == null) ? null : scalerAppPathVar.getValue());
		this.REALESRGAN_AI_MODEL_PATH = ((scalerModelPathVar == null) ? null : scalerModelPathVar.getValue());
		this.canUpscale = ProgramLogic.checkExistence(this.REALESRGAN_AI_APP_PATH);
		boolean upscalerModelValid = ProgramLogic.checkExistence(this.REALESRGAN_AI_MODEL_PATH);
		if(scalerAppPathVar   != null) { scalerAppPathVar.setValid(this.canUpscale); }
		if(scalerModelPathVar != null) { scalerModelPathVar.setValid(upscalerModelValid); }
		
		if(Logger.logLevelAbove(1)) {
			System.out.println(this.canUpscale    ? "AI Up-scale:       Good!" : "AI Up-scale:       NOT FOUND");
			System.out.println(upscalerModelValid ? "AI Up-scale Model: Good!" : "AI Up-scale Model: NOT FOUND");
		}
		
		this.INTERPOLATION_IA_APP_PATH   = ((fpsMakerAppPathVar   == null) ? null : fpsMakerAppPathVar.getValue());
		this.INTERPOLATION_IA_MODEL_PATH = ((fpsMakerModelPathVar == null) ? null : fpsMakerModelPathVar.getValue());
		this.canSmooth = ProgramLogic.checkExistence(this.INTERPOLATION_IA_APP_PATH);
		boolean smoothModelValid = ProgramLogic.checkExistence(this.INTERPOLATION_IA_MODEL_PATH);
		if(fpsMakerAppPathVar   != null) { fpsMakerAppPathVar.setValid(this.canSmooth); }
		if(fpsMakerModelPathVar != null) { fpsMakerModelPathVar.setValid(smoothModelValid); }
		
		if(Logger.logLevelAbove(1)) {
			System.out.println(this.canSmooth   ? "AI Interpolation:       Good!" : "AI Interpolation:       NOT FOUND");
			System.out.println(smoothModelValid ? "AI Interpolation Model: Good!" : "AI Interpolation Model: NOT FOUND");
		}
		
		// frames format
		if(framesTypeVar != null) {
			String varsExt = framesTypeVar.getValue();
			switch (varsExt) {
				// if supplied good one
				case "jpg"  :
				case "png"  :
				case "webp" :
					this.FRAMES_EXTENSION = varsExt;
					framesTypeVar.setValid(true);
				break;

				// if supplied bad one
				default     :
					this.FRAMES_EXTENSION = "jpg";
					framesTypeVar.setValid(false);
				break;
			}
		}
		else {
			this.FRAMES_EXTENSION = "jpg";
		}
		
		if(framesNameVar != null) {
			String framesFormat = framesNameVar.getValue();
			if(framesFormat.contains("%0") && framesFormat.contains("d")) {
				this.FRAMES_STORING_FORMAT = (framesFormat + "." + this.FRAMES_EXTENSION);
				framesNameVar.setValid(true);
			}
			else {
				this.FRAMES_STORING_FORMAT = ("frame%08d" + "." + this.FRAMES_EXTENSION);
				framesNameVar.setValid(false);
			}
		}
		else {
			this.FRAMES_STORING_FORMAT = ("frame%08d" + "." + this.FRAMES_EXTENSION);
		}
		
		if(webpmuxAppPathVar != null) {
			String webpmuxAppPath = webpmuxAppPathVar.getValue();
			if(checkWEBPmux(webpmuxAppPath)) {
				this.WEBP_ANIMATION_APP_PATH = webpmuxAppPath;
			}
			else {
				this.WEBP_ANIMATION_APP_PATH = null;
			}
		}
		else {
			this.WEBP_ANIMATION_APP_PATH = null;
		}
	}
	
	// try check if its what we need
	private boolean checkFFMPEG(String ecoder, String info) {
		if(ecoder != null && new File(ecoder.replace("\"", "")).exists()) {
			this.canProcess = ProgramLogic.checkExecOut(ecoder + " -h", "ffmpeg", "version");
		}
		if(info != null && new File(info.replace("\"", "")).exists()) {
			this.canGetInfo = ProgramLogic.checkExecOut(info + " -h", "ffprobe", "version");
		}
		return this.canProcess;
	}
	
	// try check if webpmux installed
	private boolean checkWEBPmux(String webpmuxAppPath) {
		if(webpmuxAppPath != null && new File(webpmuxAppPath.replace("\"", "")).exists()) {
			this.supportAWEBP = ProgramLogic.checkExecOut(webpmuxAppPath + " -h", "Usage:", "webpmux");
		}
		return this.supportAWEBP;
	}
	
	public boolean canProcess() {
		return canProcess;
	}
	
	public boolean canGetInfo() {
		return canGetInfo;
	}

	public boolean canUpscale() {
		return canUpscale;
	}

	public boolean canSmooth() {
		return canSmooth;
	}

	public boolean removeTempFiles() {
		return removeTempFiles;
	}
	
	private ToolOptions setRemoveTempFiles(boolean key) {
		this.removeTempFiles = key;
		if(Logger.logLevelAbove(1)) {
			System.out.println("Remove Temp Files: " + this.removeTempFiles);
		}
		return this;
	}

	public int getLogLevel() {
		return logLevel;
	}
	
	private ToolOptions increaseLogLevelTo(int level) {
		if(this.logLevel < level) {
			this.logLevel = level;
			System.out.println("Log Level set to: " + this.logLevel);
		}
		return this;
	}

	public boolean allValid() {
		List<TFVAR> vars = Configuration.VARS_PARAMETERS;
		for(TFVAR var : vars) {
			if(!var.isValid()) {
				System.out.println("Invalid variable: " + var.getName() + ": " + var.getValue());
				return false;
			}
		}
		return true;
	}

	public static TFVAR addProgramParameter(String name, String value) {
		TFVAR fileVAR = new TFVAR(name, value);
		Configuration.VARS_PARAMETERS_MAP.put(name, fileVAR);
		Configuration.VARS_PARAMETERS.add(fileVAR);
		return fileVAR;
	}
	
	public static ToolOptions load(String[] inputARGs) {
		
		boolean localSetRemoveTempFiles = true;
		int localLogLevel = 0;
		
		int argSize = ((inputARGs != null) ? inputARGs.length : 0);
		for (int ai = 0; ai < argSize; ai++) {
			String arg = inputARGs[ai].trim();
			
			if(arg.startsWith("-")) {
				switch (arg) {
				
					// prevent removing temp files
					case "-nodel"     :
					case "-nodelete"  :
					case "-temp"      :
					case "-tempfiles" : localSetRemoveTempFiles = false; break;
					
					// show execution progress in console
					// simple output (aka statistic)
					case "-v"   : localLogLevel = 1; break;
					// show run commands
					case "-vv"  : localLogLevel = 2; break;
					// show processes outputs
					case "-vvv" : localLogLevel = 3; break;
					
					// TODO: create functionality for only commands generation
		
					default     : break;
				}
			}
			
		}
		
		String coreSettingsFileName = Configuration.CORE_CONFIG_FILE_PATH;
		File settingsFile = new File(coreSettingsFileName);
		
		if(Logger.logLevelAbove(1, localLogLevel)) {
			System.out.println("Program ROOT: " + new File(".").getAbsolutePath());
			System.out.println("Program Looking for Config in: " + new File(Configuration.CORE_CONFIG_FILE_PATH).getAbsolutePath());
		}
		
		// predefined variables here
		Configuration.VARS_TO_OVERRIDE_IN_CONFIG.add(new TFVAR("TOOL_VERSION",    Text.TOOL_VERSION));
		Configuration.VARS_TO_OVERRIDE_IN_CONFIG.add(new TFVAR("TOOL_NAME_SHORT", Text.TOOL_NAME_SHORT));
		Configuration.VARS_TO_OVERRIDE_IN_CONFIG.add(new TFVAR("TOOL_NAME_FULL",  Text.TOOL_NAME_FULL));
		
		// HERE ARE DEFAULT PROGRAM VALUES
		// predefined variables, which can be overridden in script
		addProgramParameter("TEMP_AUDIO_END_FILE_NAME_EXTRACT", Configuration.TEMP_AUDIO_END_FILE_NAME_EXTRACT);
		addProgramParameter("TEMP_FILES_FOLDER",                Configuration.DEFAULT_TEMP_FILES_FOLDER_LOCATION);
		addProgramParameter("THREADS_FOR_IMAGE_PROCESSING",     Configuration.DEFAULT_THREADS_NUMBER_FOR_IMAGE_SCALING + "");
		addProgramParameter("FRAMES_STORING_FORMAT_OUT",        "$FRAMES_STORING_FORMAT$");
		addProgramParameter("REMOVE_TEMP_FILES",                "true");
		
		boolean configValid = false;
		
		if(settingsFile.exists()) {
			// try read here
			TextProcessor.initVariablesFromConfig(settingsFile, coreSettingsFileName);
			
			if(Logger.logLevelAbove(2, localLogLevel)) {
				System.out.println("\nConfig VARs:");
				for(TFVAR var : Configuration.VARS_PARAMETERS) {
					System.out.println(var.getName() + " = " + var.getValue());
				}
				System.out.println();
			}
		}
		
		ToolOptions opts = new ToolOptions(
			Configuration.getVAR("FFMPEG_EXEC_FILE_PATH"),
			Configuration.getVAR("FFPROBE_EXEC_FILE_PATH"),
			Configuration.getVAR("UPSCALER_AI_APP_PATH"),
			Configuration.getVAR("UPSCALER_AI_MODEL_PATH"),
			Configuration.getVAR("INTERPOLATION_AI_APP_PATH"),
			Configuration.getVAR("INTERPOLATION_AI_MODEL_PATH"),
			Configuration.getVAR("WEBPMUX_EXEC_FILE_PATH"),
			Configuration.getVAR("FRAMES_STORING_FORMAT"),
			Configuration.getVAR("FRAMES_EXTENSION"),
			localLogLevel // set it here, for internal checks
		);
		
		// set options from arguments
		opts.setRemoveTempFiles(localSetRemoveTempFiles);
		
		/* if config file invalid or not exist -> recreate 
		 * with predefined and retrieved and approved settings
		 * from (possible) existing file
		 * */
		
		//System.exit(0);
		return opts;
	}



}
