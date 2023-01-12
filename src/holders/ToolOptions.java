package holders;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.ProgramLogic;
import holders.Configuration.OS;
import tools.TextProcessor;
import tools.Utils;

public class ToolOptions {
	
	public final int windowInitWidth  = 720;
	public final int windowInitHeight = 500;
	
	public final String FFMPEG_ENCODER_APP_PATH;
	public final String FFMPEG_INFO_APP_PATH;
	
	public final String REALESRGAN_AI_APP_PATH;
	public final String REALESRGAN_AI_MODEL_PATH;
	
	public final String INTERPOLATION_IA_APP_PATH;
	public final String INTERPOLATION_IA_MODEL_PATH;
	
	// frames format
	public final String FRAMES_EXTENSION;
	public final String FRAMES_STORING_FORMAT;
	
	// if its not set -> store in source file/folder location
	private String outputFilesFolderPath = null;
	
	// if its not set -> create folder in program location folder 
	private String tempFilesFolderPath = null;
	
	public final String[] validImageExtensions = new String[] { "jpeg", "jpg", "png" };
	
	// program settings
	private boolean canUpscale = false;
	private boolean canSmooth  = false;
	private boolean canGetInfo = false;
	private boolean canProcess = false;

	public ToolOptions(String ffmpegEncAppPath, String ffmpegInfoAppPath, String scalerAppPath, String scalerModelPath, String fpsMakerAppPath, String fpsMakerModelPath, String framesName, String framesType) {
		
		this.FFMPEG_ENCODER_APP_PATH = ffmpegEncAppPath;
		this.FFMPEG_INFO_APP_PATH    = ffmpegInfoAppPath;
		this.checkFFMPEG(this.FFMPEG_ENCODER_APP_PATH, this.FFMPEG_INFO_APP_PATH);
		
		this.REALESRGAN_AI_APP_PATH   = scalerAppPath;
		this.REALESRGAN_AI_MODEL_PATH = scalerModelPath;
		this.canUpscale = ProgramLogic.checkExistence(this.REALESRGAN_AI_APP_PATH);
		
		this.INTERPOLATION_IA_APP_PATH   = fpsMakerAppPath;
		this.INTERPOLATION_IA_MODEL_PATH = fpsMakerModelPath;
		this.canSmooth = ProgramLogic.checkExistence(this.INTERPOLATION_IA_APP_PATH);
		
		// frames format
		this.FRAMES_EXTENSION = ((framesType == null) ? "jpg" : framesType);
		this.FRAMES_STORING_FORMAT = (((framesName == null) ? "frame%08d" : framesName) + "." + FRAMES_EXTENSION);
	}
	
	public ToolOptions(TFVAR ffmpegEncAppPathVar, TFVAR ffmpegInfoAppPathVar, TFVAR scalerAppPathVar, TFVAR scalerModelPathVar, TFVAR fpsMakerAppPathVar, TFVAR fpsMakerModelPathVar, TFVAR framesNameVar, TFVAR framesTypeVar) {

		System.out.println("Tool Available Options:");
		
		if(ffmpegEncAppPathVar != null) {
			String ffmpegPath = ffmpegEncAppPathVar.getValue();
			this.FFMPEG_ENCODER_APP_PATH = ffmpegPath;
		}
		else {
			this.FFMPEG_ENCODER_APP_PATH = null;
		}
		
		if(ffmpegInfoAppPathVar != null) {
			String ffprobePath = ffmpegInfoAppPathVar.getValue();
			this.FFMPEG_INFO_APP_PATH = ffprobePath;
		}
		else {
			this.FFMPEG_INFO_APP_PATH = null;
		}
		
		this.checkFFMPEG(this.FFMPEG_ENCODER_APP_PATH, this.FFMPEG_INFO_APP_PATH);
		if(ffmpegEncAppPathVar  != null) { ffmpegEncAppPathVar .setValid(this.canProcess); }
		if(ffmpegInfoAppPathVar != null) { ffmpegInfoAppPathVar.setValid(this.canGetInfo); }
		
		//System.out.println(this.canProcess ? "FFMPEG: Good!" : "FFMPEG: NOT FOUND");
		System.out.println(this.canGetInfo ? "FFPROBE: Good!" : "FFPROBE: NOT FOUND");
		
		this.REALESRGAN_AI_APP_PATH   = ((scalerAppPathVar   == null) ? null : scalerAppPathVar.getValue());
		this.REALESRGAN_AI_MODEL_PATH = ((scalerModelPathVar == null) ? null : scalerModelPathVar.getValue());
		this.canUpscale = ProgramLogic.checkExistence(this.REALESRGAN_AI_APP_PATH);
		boolean upscalerModelValid = ProgramLogic.checkExistence(this.REALESRGAN_AI_MODEL_PATH);
		if(scalerAppPathVar   != null) { scalerAppPathVar.setValid(this.canUpscale); }
		if(scalerModelPathVar != null) { scalerModelPathVar.setValid(upscalerModelValid); }
		
		System.out.println(this.canUpscale ? "AI Up-scale: Good!" : "AI Up-scale: NOT FOUND");
		System.out.println(upscalerModelValid ? "AI Up-scale Model: Good!" : "AI Up-scale Model: NOT FOUND");
		
		this.INTERPOLATION_IA_APP_PATH   = ((fpsMakerAppPathVar   == null) ? null : fpsMakerAppPathVar.getValue());
		this.INTERPOLATION_IA_MODEL_PATH = ((fpsMakerModelPathVar == null) ? null : fpsMakerModelPathVar.getValue());
		this.canSmooth = ProgramLogic.checkExistence(this.INTERPOLATION_IA_APP_PATH);
		boolean smoothModelValid = ProgramLogic.checkExistence(this.INTERPOLATION_IA_MODEL_PATH);
		if(fpsMakerAppPathVar   != null) { fpsMakerAppPathVar.setValid(this.canSmooth); }
		if(fpsMakerModelPathVar != null) { fpsMakerModelPathVar.setValid(smoothModelValid); }
		
		System.out.println(this.canSmooth ? "AI Interpolation: Good!" : "AI Interpolation: NOT FOUND");
		System.out.println(smoothModelValid ? "AI Interpolation Model: Good!" : "AI Interpolation Model: NOT FOUND");
		
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
				default    :
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
		
	}
	
	// try check if its what we need
	private boolean checkFFMPEG(String ecoder, String info) {
		if(ecoder != null && new File(ecoder.replace("\"", "")).exists()) {
			this.canProcess = ProgramLogic.checkExecOut(ecoder + " -h", "ffmpeg", "version");
		}
		if(info != null && new File(info.replace("\"", "")).exists()) {
			this.canGetInfo = ProgramLogic.checkExecOut(info + " -h", "ffprobe", "version");
		}
		return canProcess;
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
		String coreSettingsFileName = Configuration.CORE_CONFIG_FILE_PATH;
		File settingsFile = new File(coreSettingsFileName);
		
		// predefined variables here
		Configuration.VARS_TO_OVERRIDE_IN_CONFIG.add(new TFVAR("TOOL_VERSION",    Text.TOOL_VERSION));
		Configuration.VARS_TO_OVERRIDE_IN_CONFIG.add(new TFVAR("TOOL_NAME_SHORT", Text.TOOL_NAME_SHORT));
		Configuration.VARS_TO_OVERRIDE_IN_CONFIG.add(new TFVAR("TOOL_NAME_FULL",  Text.TOOL_NAME_FULL));
		
		// HERE ARE DEFAULT PROGRAM VALUES
		// predefined variables, which can be overridden in script
		addProgramParameter("TEMP_AUDIO_END_FILE_NAME_EXTRACT", Text.TEMP_AUDIO_END_FILE_NAME_EXTRACT);
		addProgramParameter("TEMP_FILES_FOLDER", Configuration.DEFAULT_TEMP_FILES_FOLDER_LOCATION);
		addProgramParameter("THREADS_FOR_IMAGE_PROCESSING", Configuration.DEFAULT_THREADS_NUMBER_FOR_IMAGE_SCALING);
		addProgramParameter("FRAMES_STORING_FORMAT_OUT","$FRAMES_STORING_FORMAT$");
		addProgramParameter("REMOVE_TEMP_FILES","true");
		
		boolean configValid = false;
		
		if(settingsFile.exists()) {
			// try read here
			TextProcessor.initVariablesFromConfig(settingsFile, coreSettingsFileName);
			
//			for(TFVAR var : Configuration.VARS_PARAMETERS) {
//				System.out.println(var.getName() + " = " + var.getValue());
//			}
//			System.exit(0);
		}
		
		ToolOptions opts = new ToolOptions(
			Configuration.getVAR("FFMPEG_EXEC_FILE_PATH"),
			Configuration.getVAR("FFPROBE_EXEC_FILE_PATH"),
			Configuration.getVAR("UPSCALER_AI_APP_PATH"),
			Configuration.getVAR("UPSCALER_AI_MODEL_PATH"),
			Configuration.getVAR("INTERPOLATION_AI_APP_PATH"),
			Configuration.getVAR("INTERPOLATION_AI_MODEL_PATH"),
			Configuration.getVAR("FRAMES_STORING_FORMAT"),
			Configuration.getVAR("FRAMES_EXTENSION")
		);
		
		/* if config file invalid or not exist -> recreate 
		 * with predefined and retrieved and approved settings
		 * from (possible) existing file
		 * */
		
		//System.exit(0);
		return opts;
	}



}
