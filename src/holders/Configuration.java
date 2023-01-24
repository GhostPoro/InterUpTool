package holders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import core.ProgramLogic;

public class Configuration {
	
	public static enum OS { WIN, LINUX, MAC };
	
	public final static int windowInitWidth  = 720;
	public final static int windowInitHeight = 500;
	
	public final static String[] validImageExtensions = new String[] { "jpeg", "jpg", "png", "webp" };
	
	public static final int MAX_RECURTION_DEPTH = 16;
	
	public static final List<TFVAR> VARS_TO_OVERRIDE_IN_CONFIG = new ArrayList<TFVAR>();
	public static final List<TFVAR> VARS_PARAMETERS = new ArrayList<TFVAR>();
	public static final Map<String, TFVAR> VARS_PARAMETERS_MAP = new HashMap<String, TFVAR>();
	
	public static final List<TFVAR> VARS_TO_VIDEO_PROCESSING = new ArrayList<TFVAR>();
	public static final List<TFVAR> VARS_TO_IMAGE_PROCESSING = new ArrayList<TFVAR>();
	public static final List<TFVAR> VARS_TO_APNG_PROCESSING  = new ArrayList<TFVAR>();
	public static final List<TFVAR> VARS_TO_GIF_PROCESSING   = new ArrayList<TFVAR>();
	public static final List<TFVAR> VARS_TO_WPANM_PROCESSING = new ArrayList<TFVAR>();
	
	// TODO: Find (maybe) another way to do it
	public static final JLabel INFOLABELTOOLS = new JLabel("");
	public static final JLabel INFOLABELIMAGE = new JLabel("");
	public static final JLabel INFOLABELANIMS = new JLabel("");

	
	public static ToolOptions OPTIONS;
	
	public static CurrentSessionFilesProcessingSettings SETTINGS;
	
	public static boolean PROCESSING = false;
	
	public static final OS os = ProgramLogic.getOperetionSystemType();
	
	/** Global configuration file path for entire Tool. */
	public static final String CORE_CONFIG_FILE_PATH = "./iutool_settings.conf";
	
	/** Files processing configuration file for user chosen file processing preferences (optional). */
	public static final String SESSION_CONFIG_FILE_PATH = "./session_settings.conf";
	
	/** Predefined folder path for temporally storing files. */
	public static final String DEFAULT_TEMP_FILES_FOLDER_LOCATION = "./tempuitoolfiles";
	
	/** Predefined Number of Threads for Image scaling. */
	public static final String DEFAULT_THREADS_NUMBER_FOR_IMAGE_SCALING = "12";
	public static final int THREADS = 24;
	
	/** Array of ignored Variables names from program config
    on back-propagation check stage. */
	public static final String[] VARIABLES_IGNORE_ARRAY = new String[] {
			"FFMPEG_EXEC_FILE_PATH",
			"UPSCALER_AI_APP_PATH",
			"INTERPOLATION_AI_APP_PATH",
			"TEMP_FILES_FOLDER",
			"OUTPUT_VIDEO_FILE_FPS",
			
			"FRAMES_EXTENSION",
			"SOURCE_VIDEO_FILENAME",
			"SOURCE_VIDEO_FILE_FPS",
			"VIDEO_SCALING_VALUE",
			"CURRENT_STAGE_INPUT_VIDEO_FILE_PATH",
			"CURRENT_STAGE_OUTPUT_VIDEO_FILE_PATH",
			"CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH",
			"CURRENT_STAGE_OUTPUT_FRAMES_FILES_LOCATION_PATH",
			"UPSCALER_AI_SCALE_FACTOR",
			
			"IMAGE_PROCESSING_STAGE_UPSCALING"
	};
	
	/** HAshMap of default values for core variables */
	public static final Map<String, String> DEAFULT_CORE_VARIABLES_VALUES_MAP = new HashMap<String, String>();
	
	/** Initialization of Configuration class, for advanced structures */
	public static boolean init() {
		// default values for possibly wrong provided variables
		DEAFULT_CORE_VARIABLES_VALUES_MAP.put("VIDEO_ENCODE_LIB","-c:v libx265 -vtag hvc1 -pix_fmt yuv420p -preset veryslow");
		DEAFULT_CORE_VARIABLES_VALUES_MAP.put("TEMP_FILES_FOLDER","./tempuitoolfiles");
		DEAFULT_CORE_VARIABLES_VALUES_MAP.put("FFMPEG_EXEC_FILE_PATH",">>>PLEASE_PUT_PATH_TO_EXECUTABLE_OF_FFMPEG_PROGRAM_HERE<<<");
		DEAFULT_CORE_VARIABLES_VALUES_MAP.put("UPSCALER_AI_APP_PATH","PATH_TO_UPSCALER_APP_GOES_HERE");
		DEAFULT_CORE_VARIABLES_VALUES_MAP.put("INTERPOLATION_AI_APP_PATH","PATH_TO_INTERPOLATION_AI_MODEL_GOES_HERE");
		DEAFULT_CORE_VARIABLES_VALUES_MAP.put("FRAMES_EXTENSION","jpg");
		DEAFULT_CORE_VARIABLES_VALUES_MAP.put("FRAMES_STORING_FORMAT","frame%08d.$FRAMES_EXTENSION$");
		return true;
	}
	
	public static TFVAR getVAR(String varName) {
		return VARS_PARAMETERS_MAP.get(varName.replace("$", ""));
	}

}
