package holders;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.TextProcessor;
import tools.Utils;

public class CurrentSessionFilesProcessingSettings {
	
	public final boolean userWantInterpolate;
	public final boolean userWantUpscale;
	public final boolean userWantRestoreFrameRate;
	public final boolean userWantSaveToOutputFolder;
	
	public final boolean upscaleWithJava;
	public final boolean scaleWithFFMPEG;
	
	public final int imageScalingOption;
	public final int imagePresetOption;
	
	public final int imageScalingFactor;
	
	public final int imageTargetW;
	public final int imageTargetH;
	
	public final int animationScalingOption;
	public final int animationPresetOption;
	
	public final int animationScalingFactor;
	
	public final int animationTargetW;
	public final int animationTargetH;
	
	public final String customFFMPEGFilterCMD;
	
	public final boolean removeTempFiles;

	public CurrentSessionFilesProcessingSettings() {
		
		String userSessionSettingsFilePath = Configuration.SESSION_CONFIG_FILE_PATH;
		List<String> settingsLines = Utils.readAllLines(new File(userSessionSettingsFilePath), userSessionSettingsFilePath);
		
		Map<String,String> settingsFromFile = new HashMap<String,String>();
		
		if(settingsLines != null) {
			
			int linesSize = settingsLines.size();
			for (int li = 0; li < linesSize; li++) {
				String line = settingsLines.get(li);
				String[] data = line.split("=");
				String name = data[0].trim();
				String val = ((data.length > 1) ? data[1].trim() : null);
				
				line.substring(line.indexOf('=') + 1);
				
				switch (name) {
					case "img_scale_option"          : settingsFromFile.put("img_scale_option",          val); break;
					case "img_preset_option"         : settingsFromFile.put("img_preset_option",         val); break;
					case "img_scale_factor"          : settingsFromFile.put("img_scale_factor",          val); break;
					case "img_target_size_w"         : settingsFromFile.put("img_target_size_w",         val); break;
					case "img_target_size_h"         : settingsFromFile.put("img_target_size_h",         val); break;
					
					case "anim_scale_option"         : settingsFromFile.put("anim_scale_option",         val); break;
					case "anim_preset_option"        : settingsFromFile.put("anim_preset_option",        val); break;
					case "anim_scale_factor"         : settingsFromFile.put("anim_scale_factor",         val); break;
					case "anim_target_size_w"        : settingsFromFile.put("anim_target_size_w",        val); break;
					case "anim_target_size_h"        : settingsFromFile.put("anim_target_size_h",        val); break;
					
					case "upscale_by_java"           : settingsFromFile.put("upscale_by_java",           val); break;
					
					case "interpolate"               : settingsFromFile.put("interpolate",               val); break;
					
					case "restore_oroginal_fps"      : settingsFromFile.put("restore_oroginal_fps",      val); break;
					
					case "scale_by_ffmpeg"           : settingsFromFile.put("scale_by_ffmpeg",           val); break;
					
					case "ffmpeg_custom_filters_cmd" : settingsFromFile.put("ffmpeg_custom_filters_cmd", val); break;
					
					case "remove_temp_files"         : settingsFromFile.put("remove_temp_files",         val); break;
					
					default: break;
				}
			}
		}
		
		this.removeTempFiles            = TextProcessor.parseBoolean(getValue(settingsFromFile, "remove_temp_files",     Configuration.getVAR("REMOVE_TEMP_FILES").getValue()));
		this.userWantInterpolate        = TextProcessor.parseBoolean(getValue(settingsFromFile, "interpolate",          (Configuration.OPTIONS.canSmooth() ? "true" : "false")));
		this.userWantUpscale            = true;
		this.userWantRestoreFrameRate   = TextProcessor.parseBoolean(getValue(settingsFromFile, "restore_oroginal_fps", "false"));
		this.userWantSaveToOutputFolder = false;
		
		this.upscaleWithJava       = TextProcessor.parseBoolean(getValue(settingsFromFile, "upscale_by_java", "false"));
		this.scaleWithFFMPEG       = TextProcessor.parseBoolean(getValue(settingsFromFile, "scale_by_ffmpeg", "false"));
		
		this.customFFMPEGFilterCMD = getValue(settingsFromFile, "ffmpeg_custom_filters_cmd", "");
		
		this.imageScalingOption     = Utils.clamp(0, 2,      TextProcessor.stringToInt(getValue(settingsFromFile, "img_scale_option",      "0"), 0));
		this.imagePresetOption      = Utils.clamp(0, 4,      TextProcessor.stringToInt(getValue(settingsFromFile, "img_preset_option",     "3"), 3));
		
		this.imageScalingFactor     = Utils.clamp(2, 128,    TextProcessor.stringToInt(getValue(settingsFromFile, "img_scale_factor",      "4"), 4));
		
		this.imageTargetW           = Utils.clamp(2, 999998, TextProcessor.stringToInt(getValue(settingsFromFile, "img_target_size_w",  "2560"), 2560));
		this.imageTargetH           = Utils.clamp(2, 999998, TextProcessor.stringToInt(getValue(settingsFromFile, "img_target_size_h",  "1440"), 1440));
		
		this.animationScalingOption = Utils.clamp(0, 2,      TextProcessor.stringToInt(getValue(settingsFromFile, "anim_scale_option",     "1"), 1));
		this.animationPresetOption  = Utils.clamp(0, 4,      TextProcessor.stringToInt(getValue(settingsFromFile, "anim_preset_option",    "2"), 2));
		
		this.animationScalingFactor = Utils.clamp(2, 32,     TextProcessor.stringToInt(getValue(settingsFromFile, "anim_scale_factor",     "2"), 2));
		
		this.animationTargetW       = Utils.clamp(2, 999998, TextProcessor.stringToInt(getValue(settingsFromFile, "anim_target_size_w", "1920"), 1920));
		this.animationTargetH       = Utils.clamp(2, 999998, TextProcessor.stringToInt(getValue(settingsFromFile, "anim_target_size_h", "1080"), 1080));
		
		settingsFromFile.clear();
		settingsFromFile = null;
		
		this.updateInfoLabels();
	}

	public CurrentSessionFilesProcessingSettings(boolean removeTmpFiles, boolean wantInterpolate, boolean wantUpscale, boolean restoreFrameRate, boolean inUpscaleWithJava, boolean inScaleWithFFMPEG, String ffmpegcmd,
		int imgScalingOption, int imgPresetOption, int imageSF, int imageW, int imageH,
		int animScalingOption,  int animPresetOption,  int animSF,  int animW,  int animH) {
		this.removeTempFiles = removeTmpFiles;
		this.customFFMPEGFilterCMD = ffmpegcmd;
		this.userWantInterpolate = wantInterpolate;
		this.userWantUpscale = wantUpscale;
		this.userWantRestoreFrameRate = restoreFrameRate;
		this.userWantSaveToOutputFolder = false;
		this.upscaleWithJava = inUpscaleWithJava;
		this.scaleWithFFMPEG = inScaleWithFFMPEG;
		this.imageScalingOption = imgScalingOption;
		this.imagePresetOption  = imgPresetOption;
		this.imageScalingFactor = imageSF;
		this.imageTargetW = imageW;
		this.imageTargetH = imageH;
		this.animationScalingOption = animScalingOption;
		this.animationPresetOption  = animPresetOption;
		this.animationScalingFactor = animSF;
		this.animationTargetW = animW;
		this.animationTargetH = animH;
		this.updateInfoLabels();
	}
	
	private String getValue(Map<String,String> map, String name, String def) {
		if(map != null) {
			String val = map.get(name);
			if(val != null && val.length() > 0) {
				return val;
			}
		}
		return def;
	}
	
	public boolean toFile() {
		List<TFVAR> settingsToStore = new ArrayList<TFVAR>();
		
		settingsToStore.add(new TFVAR("TOOL_VERSION",    Text.TOOL_VERSION));
		settingsToStore.add(new TFVAR("TOOL_NAME_SHORT", Text.TOOL_NAME_SHORT));
		settingsToStore.add(new TFVAR("TOOL_NAME_FULL",  Text.TOOL_NAME_FULL));
		
		settingsToStore.add(new TFVAR("FFMPEG_FILTERS_STRING", this.customFFMPEGFilterCMD));
				
		settingsToStore.add(new TFVAR("JAVA_UPSCALING",            (this.upscaleWithJava     ? "true" : "false")));
		settingsToStore.add(new TFVAR("WANT_INTERPOLATE",          (this.userWantInterpolate ? "true" : "false")));
		settingsToStore.add(new TFVAR("RESTORE_FPS",               (this.removeTempFiles     ? "true" : "false")));
		settingsToStore.add(new TFVAR("SCALE_WITH_FFMPEG",         (this.scaleWithFFMPEG     ? "true" : "false")));
		settingsToStore.add(new TFVAR("IS_NEED_REMOVE_TEMP_FILES", (this.removeTempFiles     ? "true" : "false")));
		
		settingsToStore.add(new TFVAR("IMG_SCALE_OPT",     ("" + this.imageScalingOption)));
		settingsToStore.add(new TFVAR("IMG_PRESET_OPT",    ("" + this.imagePresetOption)));
		settingsToStore.add(new TFVAR("IMG_SCALE_FACTOR",  ("" + this.imageScalingFactor)));
		settingsToStore.add(new TFVAR("IMG_SIZE_W",        ("" + this.imageTargetW)));
		settingsToStore.add(new TFVAR("IMG_SIZE_H",        ("" + this.imageTargetH)));
				
		settingsToStore.add(new TFVAR("ANIM_SCALE_OPT",    ("" + this.animationScalingOption)));
		settingsToStore.add(new TFVAR("ANIM_PRESET_OPT",   ("" + this.animationPresetOption)));
		settingsToStore.add(new TFVAR("ANIM_SCALE_FACTOR", ("" + this.animationScalingFactor)));
		settingsToStore.add(new TFVAR("ANIM_SIZE_W",       ("" + this.animationTargetW)));
		settingsToStore.add(new TFVAR("ANIM_SIZE_H",       ("" + this.animationTargetH)));
		
		Utils.writeLinesToFile(Configuration.SESSION_CONFIG_FILE_PATH, TextProcessor.overrideVARs(Text.DEFAULT_USER_SETTINGS_FILE_STRING, settingsToStore));
		
		settingsToStore.clear();
		settingsToStore = null;
		
		return new File(Configuration.SESSION_CONFIG_FILE_PATH).exists();
	}
	
	private void updateInfoLabels() {
		boolean canUpscale = (Configuration.OPTIONS != null && Configuration.OPTIONS.canUpscale());
		Configuration.INFOLABELTOOLS.setText(" | U:" + (this.upscaleWithJava ? "JV" : "AI" ) + (canUpscale ? "+" : "-" ) + " | I:" + (this.userWantInterpolate ? "+" : "-" ));
		Configuration.INFOLABELIMAGE.setText("IMG: " + ((this.imageScalingOption == 0)     ? ("ORIGx" + this.imageScalingFactor)     : (this.imageTargetW     + "x" + this.imageTargetH)));
		Configuration.INFOLABELANIMS.setText("| ANM: " + ((this.animationScalingOption == 0) ? ("ORIGx" + this.animationScalingFactor) : (this.animationTargetW + "x" + this.animationTargetH)));
	}
}
