package tools;

import java.util.ArrayList;
import java.util.List;

import holders.Configuration;

public class WebpAnalyzer {
	
	public static String getAnimWebpInfoSimple(String filePath) {
		
		String webpAppPath = Configuration.OPTIONS.WEBP_ANIMATION_APP_PATH; 
		
		if(webpAppPath == null) {
			return ("webp / ANM_NOT_SUPPORTED (no access to webpmux)");
		}
		
		List<String> infoOuts = getWebpFileInfo(webpAppPath, filePath);
		
		String frames = "UNKN";
		String dims   = "UNKN";
		
		for(String line : infoOuts) {
			if(line.trim().startsWith("Canvas size:")) {
				dims = line.replace("Canvas size:", "").trim().replace(" ", "").trim();
			}
			else if(line.trim().startsWith("Number of frames:")) {
				frames = line.replace("Number of frames:", "").trim();
			}
		}
		
		infoOuts.clear();
		infoOuts = null;
		return ("webp / " + dims + " / " + frames + " frames");
	}
	
	private static List<String> getWebpFileInfo(String appPath, String filePath) {
		String[] outputs = new String[2];
		List<String> appOutputNormal = new ArrayList<String>();
		List<String> appOutputErrors = new ArrayList<String>();
		String[] infoOuts = ProcessHandler.run(appPath + " -info " + filePath, null, true, true, appOutputNormal, appOutputErrors, outputs);
		outputs  = ProcessHandler.destroy(outputs);
		infoOuts = ProcessHandler.destroy(infoOuts);
		//appOutputNormal.clear();
		appOutputErrors.clear();
		//appOutputNormal = null;
		appOutputErrors = null;
		return appOutputNormal;
	}
	
	// for animation scaling need to be done
	// extract info
	// extract frames
	// scale every frame in single file mode (make local multithreading)
	// calculate new_offsets = old_offset_w * (current_w / original_w)

}
