package tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import holders.Configuration;
import holders.TFVAR;
import holders.Text;

public class TextProcessor {

	public static String overrideVARs(String text, List<TFVAR> vars) {
		for (TFVAR var : vars) {
			text = overrideVAR(text, var.getName(), var.getValue());
		}
		return text;
	}
	
	public static String overrideVAR(String text, String varName, String varValue) {
		String inFileVarName = ("$" + varName + "$");
		return text.replace(inFileVarName, varValue);
	}
	
	public static boolean valid(TFVAR var) {
		if(var == null)            { return false; }
		
		String varName  = var.getName();
		if(varName == null)        { return false; }
		if(varName.length() == 0)  { return false; }
		
		String varValue = var.getValue();
		if(varValue == null)       { return false; }
		if(varValue.length() == 0) { return false; }
		
		return notInIgnoreList(varName);
	}
	
	private static boolean notInIgnoreList(String varName) {
		int ilsize = Configuration.VARIABLES_IGNORE_ARRAY.length;
		for (int i = 0; i < ilsize; i++) {
			if(Configuration.VARIABLES_IGNORE_ARRAY[i].equals(varName)) {
				return false;
			}
		}
		return true;
	}
	
	public static String[] extractVARsNames(String line) {
		String[] result = new String[0];
		
		StringBuilder current = null;
		
		char[] lineChars = line.toCharArray();
		int csize = lineChars.length;
		for (int ci = 0; ci < csize; ci++) {
			char curChar = lineChars[ci];
			if(curChar == '$') {
				if(current == null) {
					current = new StringBuilder();
				}
				else {
					int oldSize = result.length;
					String[] tempArray = new String[result.length + 1];
					for (int ai = 0; ai < oldSize; ai++) {
						tempArray[ai] = result[ai];
					}
					result = tempArray;
					result[oldSize] = current.toString();
					current = null;
				}
			}
			else if(curChar == ' ') {
				current = null;
			}
			else {
				if(current != null) {
					current.append(curChar);
				}
			}
		}
		
//		for(String var : result) {
//			System.out.println("V: " + var);
//		}
        
        return result;
	}
	
	public static boolean initVariablesFromConfig(File configFile, String configFilePath) {
		List<String> configLines = Utils.readAllLines(configFile, configFilePath);
		
		int linesSize = configLines.size();
		for (int li = 0; li < linesSize; li++) {
			char[] lineChars = configLines.get(li).toCharArray();
			
			boolean lineEnd = false;
			
			StringBuilder varNameStorage = null;
			StringBuilder varValueStorage = null;
			
			String varName = null;
			
			int charsLengs = lineChars.length;
			for (int ci = 0; ci < charsLengs && !lineEnd; ci++) {
				
				// look for comments in file 
				
				if((lineChars[ci] == '#') || (((ci + 1) < charsLengs) && (lineChars[ci] == '/') && (lineChars[ci + 1] == '/'))) {
					lineEnd = true;
				}
				else { // its not a comment
					
					/* Variable Name still not exist -> no reason to collect its value */
					if(varValueStorage == null) {
						/* Collect variable name characters here */
						if(varNameStorage != null) {
							if(varName != null && (lineChars[ci] == '=')) {
								// if variable has its name -> time to make it valuable
								varValueStorage = new StringBuilder();
							}
							else if(lineChars[ci] == '$') {
								// finalize variable here
								String tempVarName = varNameStorage.toString().trim();
								if(tempVarName.length() > 0) {
									varName = tempVarName;
								}
							}
							else if(lineChars[ci] == ' ') {
								// do nothing if space
							}
							else {
								// add chars to var Name
								varNameStorage.append(lineChars[ci]);
							}
						}
						else {
							if(lineChars[ci] == '$') {
								varNameStorage = new StringBuilder();
							}
						}
					}
					else {
						/* Store variable value here */
						varValueStorage.append(lineChars[ci]);
					}
				}
				
			} // END of char parsing
			
			if(varName != null) {
				if(varValueStorage != null && varValueStorage.length() > 0) {
					String varValue = Utils.trimSpaces(varValueStorage.toString().trim());
					TFVAR configVar = Configuration.VARS_PARAMETERS_MAP.get(varName);
					if(configVar == null) { // if there is no value stored for this variable in presets, or in file before -> add new
						configVar = new TFVAR(varName, varValue);
						Configuration.VARS_PARAMETERS.add(configVar);
						Configuration.VARS_PARAMETERS_MAP.put(varName, configVar);
						
						if(varName.startsWith("VIDEO_PROCESSING_STAGE")) {
							//System.out.println(configVar);
							Configuration.VARS_TO_VIDEO_PROCESSING.add(configVar);
						}
						else if(varName.startsWith("IMAGE_PROCESSING_STAGE")) {
							Configuration.VARS_TO_IMAGE_PROCESSING.add(configVar);
						}
						else if(varName.startsWith("APNG_PROCESSING_STAGE")) {
							Configuration.VARS_TO_APNG_PROCESSING.add(configVar);
						}
						else if(varName.startsWith("GIF_PROCESSING_STAGE")) {
							Configuration.VARS_TO_GIF_PROCESSING.add(configVar);
						}
						else if(varName.startsWith("WPANM_PROCESSING_STAGE")) {
							Configuration.VARS_TO_WPANM_PROCESSING.add(configVar);
						}
					}
					else { // if there was a variable with this name -> re-define its value
						configVar.setValue(varValue);
					}
				}
				else {
					// TODO: Warning here "NO VALUE IN VARIABLE VARIABLE_NAME_HERE"
				}
			}
			// no ELSE, because in this line just does not exist any VARs
		}
		
		// backward propagation here
		TextProcessor.unpackVariablesValues(null, 0);
		
		return true;
	}
	
	public static String unpackVariablesValues(String overrideVARsName, int stage) {
		// exit to prevent infinite loop
		if(stage > Configuration.MAX_RECURTION_DEPTH) { return null; }
		
		// if there is nothing looking for (start from bottom of file)
		if(overrideVARsName == null) {
			for (int vi = (Configuration.VARS_PARAMETERS.size() - 1); vi > -1; vi--) {
				TFVAR var = Configuration.VARS_PARAMETERS.get(vi);
				if(valid(var)) {
					String varValue = var.getValue();
					String[] varsInVAR = extractVARsNames(varValue);
					int insideVARsSize = varsInVAR.length;
					if(insideVARsSize > 0) {
						for (int i = 0; i < insideVARsSize; i++) {
							String checkVariableName = varsInVAR[i];
							if(notInIgnoreList(checkVariableName)) {
								String insideVariableValue = unpackVariablesValues(checkVariableName, stage++);
								if(insideVariableValue != null) {
									var.setValue(overrideVAR(varValue, checkVariableName, insideVariableValue));
									varValue = var.getValue();
									
									// reset to start (to be sure all possible variables was processed)
									vi = (Configuration.VARS_PARAMETERS.size() - 1);
								}
								else {
									System.err.println("ERROR 1 Processing Varieble '" + varsInVAR[i] + "' - Return value was NULL! Panic exit!");
									System.exit(-1);
								}
							}
							else {
								// ignoring variable
							}
						}
						//return var.getValue();
					}
					else {
						// do nothing, because inside variable value is no any other variables
					}
				}
			}
		}
		else {
			TFVAR localVAR = Configuration.VARS_PARAMETERS_MAP.get(overrideVARsName);
			if(valid(localVAR)) {
				String varValue = localVAR.getValue();
				String[] varsInVAR = extractVARsNames(varValue);
				int insideVARsSize = varsInVAR.length;
				if(insideVARsSize > 0) {
					for (int i = 0; i < insideVARsSize; i++) {
						String checkVariableName = varsInVAR[i];
						if(notInIgnoreList(checkVariableName)) {
							String insideVariableValue = unpackVariablesValues(checkVariableName, stage++);
							if(insideVariableValue != null) {
								localVAR.setValue(overrideVAR(varValue, checkVariableName, insideVariableValue));
								varValue = localVAR.getValue();
							}
							else {
								System.err.println("ERROR 2 Processing Varieble '" + checkVariableName + "' - Return value was NULL! Panic exit!");
								System.exit(-1);
							}
						}
						else {
							// ignoring variable
						}
					}
					return localVAR.getValue();
				}
				else {
					return localVAR.getValue();
				}
			}
			return "";
		}
		return null;
	}
	
	public static String restoreVarsValues(String text) {
		String[] lines = text.split("\n");
		
		int linesSize = lines.length;
		int varsSize  = Configuration.VARS_PARAMETERS.size();
		
		boolean changed = false;
		
		for (int vi = 0; vi < varsSize; vi++) {
			TFVAR var = Configuration.VARS_PARAMETERS.get(vi);
			
			if(!var.isValid()) {
				String varName = var.getName();
				
				String defValue = Configuration.DEAFULT_CORE_VARIABLES_VALUES_MAP.get(varName);
				
				if(defValue == null) { // no value for restore -> comment
					for (int li = 0; li < linesSize; li++) {
						String line = lines[li];
						//System.out.println(lines[li].trim());
						if(line.contains(varName) && !line.startsWith("#") && !line.startsWith("//")) {
							if(line.contains("PROCESSING_STAGE")) {
								
								int commentAidx = line.indexOf("#");
								int commentBidx = line.indexOf("//");
								
								int max = ((commentAidx > commentBidx) ? commentAidx : commentBidx);
								
								if(max > 0) {
									String code = line.substring(0, max - 1);
									String comment = line.substring(max);
									//System.out.println("REPLACE 01 : " + lines[li]);
									lines[li] = (code.replace("$" + varName + "$", "") + " # " + comment);
									//System.out.println("REPLACE 02 : " + lines[li]);
								}
								else {
									//System.out.println("REPLACE 03 : " + lines[li]);
									lines[li] = line.replace("$" + varName + "$", "");
									//System.out.println("REPLACE 04 : " + lines[li]);
								}
							}
							else {
								//System.out.println("REPLACE 05 : " + lines[li]);
								lines[li] = ("# " + line + " # Commented because invalid (and removed from video processing queue)");
								//System.out.println("REPLACE 06 : " + lines[li]);
							}
						}
					}
				}
				else { // restore default value
					for (int li = 0; li < linesSize; li++) {
						String line = lines[li];
						//System.out.println(lines[li].trim());
						if(line.contains(varName) && !line.contains("PROCESSING_STAGE") && !line.startsWith("#") && !line.startsWith("//")) {
							
							if(line.split("=")[0].trim().contains(varName)) {
								int commentAidx = line.indexOf("#");
								int commentBidx = line.indexOf("//");
								
								int max = ((commentAidx > commentBidx) ? commentAidx : commentBidx);
								
								if(max > 0) {
									String comment = line.substring(max);
									//System.out.println("REPLACE 07 : " + lines[li]);
									lines[li] = ("$" + varName + "$ = " + defValue + " " + comment);
									//System.out.println("REPLACE 08 : " + lines[li]);
								}
								else {
									//System.out.println("REPLACE 09 VAR: " + varName + " : " + lines[li]);
									lines[li] = ("$" + varName + "$ = " + defValue);
									//System.out.println("REPLACE 10 VAR: " + varName + " : " + lines[li]);
								}
							}
						}
					}
				}
				changed = true;
			}
			
		}
		
		if(changed) {
			StringBuilder outText = new StringBuilder();
			for (int li = 0; li < linesSize; li++) {
				//System.out.println(lines[li].trim());
				outText.append(lines[li].trim() + "\n");
			}
			return outText.toString();
		}
		
		return text;
	}
	
	public static boolean parseBoolean(TFVAR var) {
		return parseBoolean(var.getValue());
	}
	
	public static boolean parseBoolean(String str) {
		String value = str.trim();
		try {
			int num = Integer.parseInt(value);
			return (num > 0);
		}
		catch (NumberFormatException  e) {}
		value = value.toLowerCase();
		return (value.startsWith("y") || value.startsWith("t"));
	}
	
	public static int stringToInt(String str, int def) {
		if(str != null) { int out = def; try { out = Integer.parseInt(str); } catch (NumberFormatException e) {} return out; }
		return def;
	}

}
