package holders;

import tools.TextProcessor;

public class CurrentSessionFilesProcessingSettings {
	
	public final boolean userWantInterpolate = true;
	public final boolean userWantUpscale = true;
	public final boolean userWantRestoreFrameRate = true;
	public final boolean userWantSaveToOutputFolder = true;
	
	public final float outFPS = 30; // no less then this one please
//	public final int outW = 2560;
//	public final int outH = 1440;
	
	public final int outW = 1920;
	public final int outH = 1080;
	
	public final boolean removeTempFiles = TextProcessor.parseBoolean(Configuration.getVAR("REMOVE_TEMP_FILES"));

	public CurrentSessionFilesProcessingSettings() {}
}
