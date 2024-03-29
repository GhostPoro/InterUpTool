package holders;

/**
 * Class to hold all program static strings
 * such as parameters, text of help prompt (for console mode) and
 * default values for settings file generation.
 */
public class Text {
	
	/** Error message in case of inability to create or read core program settings file. */
	public static final String PANIC_EXIT_EXPLAIN = "ERROR! Can't load core program parameters\nfrom config file! Panic exit.";
	
	/** Error message in case of inability to create or read core program settings file. */
	public static final String ERROR_ZERO_FILE_SIZE = "File \"$FILE_PATH$\"\nwas skipped, because of 0 size file.\n\n";
	
	
	public static final String FILESANALYZER_ERROR_MAIN_THREAD      = "FilesAnalyzer Error! FFMPEG can't retrieve information about file:\n$FILE_PATH$.\nReason: Invalid path to file.\n";
	public static final String FILESANALYZER_ERROR_EXTRTACT_AND_LOG = "FilesAnalyzer Error! FFMPEG can't retrieve information about file:\n$FILE_PATH$.\nReason: $REASON$\n";

	
	/** Name of window pop-up for info message in case if User click 'Process' button, but did not add any files to process list. */
	public static final String POPUP_NOTHING_TO_PROCESS_NAME = "Tool Info";
	
	/** Text for info message pop-up window in case if User click 'Process' button, but did not add any files to process list. */
	public static final String POPUP_NOTHING_TO_PROCESS_TEXT = "There is no files to process.";
	
	public static final String[] CALCULATION_ANIMATION_STAGES = new String [] {
		"Calculating.",
		"Calculating..",
		"Calculating...",
	};
	
	public static final String[] MAIN_WINDOW_INTERFACE = new String [] {
		"InterUpTool", // Window Title
	};
	
	public static final String[] ERRORS_POPUP_TEXTS = new String [] {
		"Errors", // Window Title
	};
	
	/** Starting Pop-up Strings storage. Window name, text and button text. */
	public static final String[] STARTING_POPUP_TEXTS = new String [] {
		"$TOOL_NAME_SHORT$ ($TOOL_VERSION$) Configuration Error", // Window name
		
		// buttons
		"Reload Config",
		"Open Config File",
		"Try to fix config file",
		"Close Application",
		
		// Window text
		"Application cannot access FFMPEG executable by provided path in config file\n'$FFMPEG_ENCODER_APP_PATH$'.\nThere several options:"
	};
	
	/** String of help snippet for console mode application. */
	public static final String HELP_STRING = 
			"  Help:\n" +
			"    -h                  print this help and exit.\n\n" +

			"  Output:\n" +
			"    -v                  prints to 'console out' current file information\n" +
			"    -vv                 same as -v, additionaly prints to 'out'\n" +
			"                          execution commands per stage\n" +
			"    -vvv                same as -vv, additionally prints to 'out'\n" +
			"                          all child processes output.\n\n" +

			"  Temp files handling:\n" +
			"    -nodel              any one of this 4 arguments will turn OFF\n" +
			"    -nodelete           temporarily created files deletion, such as\n" +
			"    -temp               extracted audio and image frames, both\n" +
			"    -tempfiles          extracted from video and after all stages.";
	
	/**
	* Human readable name of tool
	* Open AI suggest variant of "InterUpTool"
	* my was "PAVINUT", so sad...
	*/
	public static final String TOOL_NAME_SHORT = "InterUpTool";
	
	/** Human readable full name of tool */
	public static final String TOOL_NAME_FULL  = "Pictures and Video, Interpolating and Up-scaling Tool";
	
	/** Version of tool */
	public static final String TOOL_VERSION    = "v0.09";
	
	/** Full template of User settings file in form of String */
	public static final String DEFAULT_USER_SETTINGS_FILE_STRING =
			//*
			"# Please do not to change program settings by editing this file,\n" +
			"# because it will be overridden by Tool, in next run.\n" +
			"# This is automatically generated users settings file\n" +
			"# for $TOOL_NAME_SHORT$ $TOOL_VERSION$\n" +
			"# ($TOOL_NAME_FULL$).\n" +
			"img_scale_option=$IMG_SCALE_OPT$\n" +
			"img_preset_option=$IMG_PRESET_OPT$\n" +
			"img_scale_factor=$IMG_SCALE_FACTOR$\n" +
			"img_target_size_w=$IMG_SIZE_W$\n" +
			"img_target_size_h=$IMG_SIZE_H$\n" +
					
			"anim_scale_option=$ANIM_SCALE_OPT$\n" +
			"anim_preset_option=$ANIM_PRESET_OPT$\n" +
			"anim_scale_factor=$ANIM_SCALE_FACTOR$\n" +
			"anim_target_size_w=$ANIM_SIZE_W$\n" +
			"anim_target_size_h=$ANIM_SIZE_H$\n" +
					
			"upscale_by_java=$JAVA_UPSCALING$\n" +
					
			"interpolate=$WANT_INTERPOLATE$\n" +
					
			"restore_oroginal_fps=$RESTORE_FPS$\n" +
					
			"scale_by_ffmpeg=$SCALE_WITH_FFMPEG$\n" +
					
			"ffmpeg_custom_filters_cmd=$FFMPEG_FILTERS_STRING$\n" +
					
			"remove_temp_files=$IS_NEED_REMOVE_TEMP_FILES$"; // */
			
	/** Full template of Tool configuration file in form of String */
	public static final String DEFAULT_CONFIGURATION_FILE_STRING =
			///*
			"# This automatically generated parameters file for $TOOL_NAME_SHORT$ $TOOL_VERSION$\n" +
			"# ($TOOL_NAME_FULL$).\n\n\n" +


			"# ##################### !!! CNANGE THIS PLEASE !!! #####################\n" +
			"# Please set $FFMPEG_EXEC_FILE_PATH$ variable to provide\n" +
			"# for this Tool access to FFMPEG executable.\n" +
			"# This is most important parameter, the core functionality\n" +
			"# of this tool builded around this single variable.\n" +
			"# Examples:\n" +
			"#\n" +
			"#	Windows (installed in system):\n" +
			"#		$FFMPEG_EXEC_FILE_PATH$=C:\\Program Files (x86)\\FFmpeg\\bin\\ffmpeg.exe\n" +
			"#	Windows (relevant to tool folder):\n" +
			"#		$FFMPEG_EXEC_FILE_PATH$=.\\ffmpeg-2022-11-21-git-459527108a-full_build\\bin\\ffmpeg.exe\n" +
			"#		$FFMPEG_EXEC_FILE_PATH$=./ffmpeg-2022-11-21-git-459527108a-full_build/bin/ffmpeg.exe\n" +
			"#\n" +
			"#	Linux (installed in system):\n" +
			"#		$FFMPEG_EXEC_FILE_PATH$=/usr/bin/ffmpeg\n" +
			"#		$FFMPEG_EXEC_FILE_PATH$=/opt/ffmpeg/ffmpeg\n" +
			"#	Linux (relevant to tool folder):\n" +
			"#		$FFMPEG_EXEC_FILE_PATH$=./ffmpeg-2022-11-21-git-459527108a-full_build/bin/ffmpeg\n\n" +
			
			"$FFMPEG_EXEC_FILE_PATH$=>>>PLEASE_PUT_PATH_TO_EXECUTABLE_OF_FFMPEG_PROGRAM_HERE<<<\n\n\n" +
			
			
			"# If You do not have FFMPEG installed on your PC, You can download it from here:\n" +
			"# https://ffmpeg.org/download.html\n\n" +
			
			"# Windows Executables:\n" +
			"# https://github.com/BtbN/FFmpeg-Builds/releases\n\n" +
			
			"# On Linux You can install 'ffmpeg' package, or download builded version from here:\n" +
			"# https://johnvansickle.com/ffmpeg/\n\n\n\n" +
			
			
			
			"# Below in this file important but optional values:\n\n" +
			
			"# Path to 'Real-ESRGAN' up-scaler executable.\n" +
			"# If not specified, in (this) tool\n" +
			"# will not be available function to up-scale images/video.\n\n" +
			
			"$UPSCALER_AI_APP_PATH$=PATH_TO_UPSCALER_APP_GOES_HERE\n\n" +
			
			"# Name of 'Real-ESRGAN' specific model, with cmd parameter.\n" +
			"# if not specified, tool will not specify model\n" +
			"# as a parameter to up-scaler executable.\n" +
			"#$UPSCALER_AI_MODEL_NAME_OPTION$=-n realesr-animevideov3\n\n" +
			
			"# Real-ESRGAN image up-scaler application Official GitHub web page:\n" +
			"# https://github.com/xinntao/Real-ESRGAN/releases\n\n\n\n" +
			
			
			
			"# Path to Frames Interpolation executable.\n" +
			"# If not specified, in (this) tool\n" +
			"# will not be available function to interpolate between frames.\n" +
			"# there is 2 (tested) options:\n\n" +
			
			"# RIFE (Real-Time Intermediate Flow Estimation for Video Frame Interpolation)\n" +
			"# https://github.com/nihui/rife-ncnn-vulkan/releases\n" +
			"# Faster, good at fast motion frames interpolation,\n" +
			"# cause artifacts on slow video fragments\n\n" +
			
			"# DAIN (Depth-Aware Video Frame Interpolation)\n" +
			"# https://github.com/nihui/dain-ncnn-vulkan/releases\n" +
			"# Much slower, good at slow motion or static frames interpolation,\n" +
			"# cause noticeable artifacts on fast video fragments\n\n" +
			
			"$INTERPOLATION_AI_APP_PATH$=PATH_TO_INTERPOLATION_AI_APP_GOES_HERE\n\n" +
			
			"# Model name for Interpolation Application, with cmd parameter.\n" +
			"# If not specified, tool will not specify model\n" +
			"# as a parameter to Frames Interpolation executable.\n" +
			"#$INTERPOLATION_AI_MODEL_NAME_OPTION$=-m rife-anime\n\n\n\n" +
			
			
			
			"# Path to FFMPEG ffprobe executable.\n" +
			"# If not specified, tool will use ffmpeg instead\n" +
			"# to extract video file information,\n" +
			"# at the time of adding to process list.\n" +
			"$FFPROBE_EXEC_FILE_PATH$=PATH_TO_FFPROBE_EXECUTABLE\n\n\n\n" +
			
			// TODO: Make proper explanation about webpmux
			"$WEBPMUX_EXEC_FILE_PATH$ = /usr/bin/webpmux\n\n\n\n" +
			
			"# Number of CPU threads dedicated to image scaling.\n" +
			"# Two times numbers of cores on processors is fine,\n" +
			"# for maximum performance and 100% CPU utilization.\n" +
			"$THREADS_FOR_IMAGE_PROCESSING$ = 8\n\n\n" +
			
			
			"# Folder to Temporally stored files.\n" +
			"# If not specified - program will try make one\n" +
			"# in root where executable of Tool located\n" +
			"# Be aware: Temporally extracted frames of 20 minute video\n" +
			"# can exceed 100GB limit, before up-scaling.\n" +
			"# So chose location for temp files where at least 200GB free hard disk space.\n" +
			"# $TEMP_FILES_FOLDER$ = /tmp\n\n\n\n" +


			"# FILE INFO and other predefined or less important options\n" +
			"# '#' or '//' can be used to comment in this file\n\n" +
			
			"# Be aware, please, that all variables in this file cowered in symbols '$'\n" +
			"# from both sides, in other case, they will be treated as regular text,\n" +
			"# spaces between two '$' symbols (in variable name) will be ignored\n" +
			"# and everything after equals ('=') symbol will be treated as variable value,\n" +
			"# even symbols '$' and any other text, except commented with '#' or '//'.\n" +
			"# Everything after '#' or '//' will be treated as a comment,\n" +
			"# and will be ignored.\n\n" +
			
			// TODO: Make here example for '$ ttt tttt t t ttt$ = ggg1 $ ttt tttt t t ttt1$ = ggg2'
			
			"# You can write variables by yourself, if need, in this way:\n" +
			"# $VAR_FOR_UPSCALER_SMOOTHNESS$=-hd\n" +
			"# Then thin new created variable $VAR_FOR_UPSCALER_SMOOTHNESS$ can be used like so:\n" +
			"# $VIDEO_PROCESSING_STAGE_FRAMES_UPSCALING$ = $UPSCALER_AI_APP_PATH$ -i $CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH$ -o $CURRENT_STAGE_OUTPUT_FRAMES_FILES_LOCATION_PATH$ -n realesr-animevideov3 -s $UPSCALER_AI_SCALE_FACTOR$ $VAR_FOR_UPSCALER_SMOOTHNESS$ -f $FRAMES_STORING_FORMAT$\n\n\n" +

			
			"# Delete temporally files\n" +
			"# Default: true\n" +
			"# $REMOVE_TEMP_FILES$ = false\n\n\n" +
			
			
			"# Video encode Library:\n" +
			"# Common used libs:\n" +
			"#      h264_amf   - H.264 AMD GPU Encoding\n" +
			"#      h264_nvenc - H.264 NVIDIA GPU Encoding\n" +
			"#      libx264    - H.264 CPU Encoding\n" +
			"#\n" +
			"#      hevc_amf   - H.265 AMD GPU Encoding\n" +
			"#      hevc_nvenc - H.265 NVIDIA GPU Encoding\n" +
			"#      libx265    - H.265 CPU Encoding\n" +
			"# Get full list: ffmpeg -encoders\n" +
			"# Note:	In my personal experience, on NVIDIA RTX 2070,\n" +
			"# 		using h264_nvenc just crushes ffmpeg,\n" +
			"#		so using GPU specific lib, can be less reliable.\n" +
			"$VIDEO_ENCODE_LIB$ = -c:v libx265 -vtag hvc1 -pix_fmt yuv420p -preset veryslow\n\n" +
			
			"# Frames format:\n" +
			"# Possible frames save formats:\n" +
			"#      jpg  - medium size\n" +
			"#      png  - bigger size, supports transparency\n" +
			"#      webp - smallest size, can not be supported by some applications\n" +
			"$FRAMES_EXTENSION$ = jpg\n" +
			"$FRAMES_STORING_FORMAT$     = frame%08d.$FRAMES_EXTENSION$ // images full format\n" +
			"$FRAMES_STORING_FORMAT_OUT$ = $FRAMES_STORING_FORMAT$       # simple fix\n\n" +
			
			"# Video processing stages:\n" +
			"# You can add any stage between (if You know what you are doing, of course)\n" +
			"# by adding variable which starts with '$VIDEO_PROCESSING_STAGE' and ends with symbol '$'.\n" +
			"# Please do not use already used variables in this file, thanks.\n" +
			"# Mentioning variable with same name will override variable value.\n" +
			"# Possible sequences of variables:\n" +
			"#      VIDEO_PROCESSING_STAGE - video processing\n" +
			"#      IMAGE_PROCESSING_STAGE - static images up-scaling\n" +
			"#      APNG_PROCESSING_STAGE  - processing of animated png (apng) images\n" +
			"#      GIF_PROCESSING_STAGE   - processing of animated gif images\n" +
			"#      WPANM_PROCESSING_STAGE - processing of animated webp images\n\n\n" +
			
			
			"# Extraction of frames\n" +
			"$VIDEO_PROCESSING_STAGE_FRAMES_EXTRACTION$ = $FFMPEG_EXEC_FILE_PATH$ -r $SOURCE_VIDEO_FILE_FPS$ -i $CURRENT_STAGE_INPUT_VIDEO_FILE_PATH$ -qscale:v 1 $CURRENT_STAGE_OUTPUT_FRAMES_FILES_LOCATION_PATH$/$FRAMES_STORING_FORMAT$\n\n" +
			
			"# Extract audio from file\n" +
			"$VIDEO_PROCESSING_STAGE_AUDIO_EXTRACTION$ = $FFMPEG_EXEC_FILE_PATH$ -r $SOURCE_VIDEO_FILE_FPS$ -i $CURRENT_STAGE_INPUT_VIDEO_FILE_PATH$ -vn -acodec copy $TEMP_FILES_FOLDER$/temp_file$TEMP_AUDIO_END_FILE_NAME_EXTRACT$\n\n" +
			
			"# Next 2 stages with up-scaling and interpolation\n" +
			"# can be skipped by Tool, depend on availability of executables\n" +
			"# provided by User at top of file, accessibility provided\n" +
			"# by user's OS and options selected before files processing in this Tool.\n\n" +
			
			"# Generate interpolated (or not) frames with RIFE\n" +
 			"$VIDEO_PROCESSING_STAGE_SMOOTHING_WITH_FRAME_INTERPOLATION$ = $INTERPOLATION_AI_APP_PATH$ -v $INTERPOLATION_AI_MODEL_NAME_OPTION$ -i $CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH$ -o $CURRENT_STAGE_OUTPUT_FRAMES_FILES_LOCATION_PATH$ -f $FRAMES_STORING_FORMAT$\n\n" +
 			
 			"# Generate interpolated (or not) frames with DAIN (alternative)\n" +
 			"#$VIDEO_PROCESSING_STAGE_SMOOTHING_WITH_FRAME_INTERPOLATION$ = $INTERPOLATION_AI_APP_PATH$ -v -i $CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH$ -o $CURRENT_STAGE_OUTPUT_FRAMES_FILES_LOCATION_PATH$ -f $FRAMES_STORING_FORMAT$\n\n" +
 			
 			"# Up-scale interpolated (or not) frames with Real-ESRGAN\n" +
 			"$VIDEO_PROCESSING_STAGE_FRAMES_UPSCALING$ = $UPSCALER_AI_APP_PATH$ -i $CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH$ -o $CURRENT_STAGE_OUTPUT_FRAMES_FILES_LOCATION_PATH$ $UPSCALER_AI_MODEL_NAME_OPTION$ -s $UPSCALER_AI_SCALE_FACTOR$ -f $FRAMES_EXTENSION$\n\n" +
 			
 			"# Pack everything inside output file, scale and convert\n" +
 			"# $VIDEO_SCALING_VALUE$ can be empty, if not defined in Tool\n" +
	 		"$VIDEO_PROCESSING_STAGE_CONVERT_FRAMES_TO_VIDEO$ = $FFMPEG_EXEC_FILE_PATH$ -y -r $OUTPUT_VIDEO_FILE_FPS$ -i $CURRENT_STAGE_INPUT_FRAMES_FILES_LOCATION_PATH$/$FRAMES_STORING_FORMAT$ -i $TEMP_FILES_FOLDER$/temp_file$TEMP_AUDIO_END_FILE_NAME_EXTRACT$ $VIDEO_ENCODE_LIB$ $VIDEO_SCALING_VALUE$ -c:a copy $CURRENT_STAGE_OUTPUT_VIDEO_FILE_PATH$\n\n" +
	 		
	 		"# Change FPS back to original.\n" +
	 		"#$VIDEO_PROCESSING_STAGE_RESTORE_SOURCE_FPS$ = $FFMPEG_EXEC_FILE_PATH$ -i $CURRENT_STAGE_INPUT_VIDEO_FILE_PATH$ $VIDEO_ENCODE_LIB$ -vf fps=$SOURCE_VIDEO_FILE_FPS$ -c:a copy $CURRENT_STAGE_OUTPUT_VIDEO_FILE_PATH$\n\n" +
	 		
	 		"# Simple Image up-scaling sequence:\n" +
	 		"$IMAGE_PROCESSING_STAGE_UPSCALING$ = $UPSCALER_AI_APP_PATH$ -i $INPUT_IMAGE_CURRENT_STAGE_FILE_PATH$ -o $OUTPUT_IMAGE_CURRENT_STAGE_FILE_PATH$ -n realesrgan-x4plus -s $UPSCALER_AI_SCALE_FACTOR$ -f $INPUT_IMAGE_EXTENSION$" + //*/
			""; // empty end for editing purposes

}
