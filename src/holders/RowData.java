package holders;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import tools.Utils;

public class RowData {
	
	public final long uniqueID;

	private final File file;
	private final String fullFilePath;

	private final int rowIndex;
	
	private float status;
	private float targetStatus;
	
	private boolean changed = true;
	
	private String fileProcessStage;
	
	private boolean propertiesSet = false;
	private long nextPropertiesUpdate = -1;
	private int propsShowStage = 2;
	private String realFileProperty = null;
	
	private boolean readyToProcess = false;
	private boolean inProcess = false;
	private int currentProcessingStage = -1;
	
	private boolean itsImage = false;
	
	private long startTimeMilliseconds;
	private long finishTimeMilliseconds;
	
	// default video settings
	private String format = null;
	private float fps    = -1;
	private int duration = -1;
	private int width    = -1;
	private int height   = -1;

	public RowData(File inFile, int indexInList, String inFullFilePath) {
		this.uniqueID = Utils.getUniquieNanoValue();
		this.fullFilePath = inFullFilePath;
		this.file = inFile;
		this.rowIndex = indexInList;
		this.status = 0f;
	}
	
//  #### Deprecated ####
//	private final String type;
//	private final long length;
//	private final String checksum;
//	public RowData(File inFile, String hash, String type, int indexInList) {
//	this.checksum = hash;
//	this.type = type;
//	this.length = 0;//this.file.length();
//	public long getLength() {
//		return length;
//	}
//	public String getType() {
//		return type;
//	}
//	public String getCheckSum() {
//		return checksum;
//	}
	
	public File getFile() {
		return file;
	}
	
	public String getFullFilePath() {
		if(this.fullFilePath == null) {
			if(this.file != null) {
				return this.file.getAbsolutePath();
			}
		}
		return this.fullFilePath;
	}
	
	public String getFilePath() {
		return getFullFilePath();
	}
	
	public String getPath() {
		return getFullFilePath();
	}
	
	public int getRowIndex() {
		return rowIndex;
	}
	
	public float getStatus() {
		return status;
	}

	public RowData setStatus(float status) {
		this.changed = true;
		this.status = status;
		return this;
	}
	
	public boolean isChanged() {
		return changed;
	}

	public RowData setChanged(boolean c) {
		this.changed = c;
		return this;
	}
	
	public boolean isPropertiesSet() {
		return propertiesSet;
	}
	
	public boolean isReadyToProcess() {
		return readyToProcess;
	}

	public boolean itsImage() {
		return itsImage;
	}

	public boolean itsAnimated() {
		return (this.format.equals("apng") || (this.format.equals("gif") && (this.fps > 0)));
	}

	public float getFPS() {
		return fps;
	}
	
	public int[] getDimensions() {
		return new int[] { this.width, this.height };
	}

	public RowData setFileProperties(String props, boolean valid) {
		this.realFileProperty = props;
		this.changed = true;
		
		if(props.contains("CANT_RETRIEVE_FILE_INFO") || props.contains("INVALID_FILE_DATA") || props.contains("NOT_SUPPORTED")) {
			this.propertiesSet = true;
		}
		else if(valid && props.contains("/")) {
			String[] data = props.split("/");
			
			if(data.length < 4) { // this is image
				this.itsImage = true;
				this.format = data[0].trim();
				
				if(data.length > 1 && data[1] != null && !data[1].trim().equals("UNKN")) {
					String[] parts = data[1].split("x");
					try {
						if(parts.length == 2) {
							this.width  = Integer.parseInt(parts[0].trim());
							this.height = Integer.parseInt(parts[1].trim());
						}
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
				
				if(data.length > 2 && data[2] != null && !data[2].trim().equals("UNKN") && !data[2].contains("frames")) {
					try {
						this.fps = Float.valueOf(data[2].replace("fps", "").trim());
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
				else {
					this.fps = -1;
				}
				
				boolean badFormat = ((this.format == null) || (this.format.length() == 0) || this.format.equals("UNKN"));
				
				boolean badSize = ((this.width < 1) || (this.height < 1));
				
				if(badFormat || badSize) {
					try {
						BufferedImage image = ImageIO.read(this.file);
						if(image != null) {
							// Get the format of the image
							this.format = null;
							int imageType = image.getType();
							if (imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_ARGB_PRE) {
								this.format = "png";
							} else if (imageType == BufferedImage.TYPE_INT_RGB) {
								this.format = "jpg";
							} else if (imageType == BufferedImage.TYPE_BYTE_BINARY) {
								this.format = "bmp";
							}
							// Get the size of the image
							this.width = image.getWidth();
							this.height = image.getHeight();
						}
					} catch (IOException ioe) {

					}
				}
				
				// if i will need deeply get images FPS natively in Java ->
				
				// for GIF
//				import java.io.File;
//				import java.io.IOException;
//				import net.jamesbaca.asciigif.GifDecoder;
//
//				File gifFile = new File("animated.gif");
//				GifDecoder decoder = new GifDecoder();
//				decoder.read(gifFile.getAbsolutePath());
//				// Get the frame rate of the GIF
//				int frameRate = decoder.getFrameRate();
//				// Print out the frame rate
//				System.out.println("Frame rate: " + frameRate + " fps");
				
				
				// for APNG
//				import java.io.File;
//				import java.io.IOException;
//				import org.apache.commons.imaging.APNGDecoder;
//				import org.apache.commons.imaging.ImageReadException;
//				
//				File apngFile = new File("animated.apng");
//				APNGDecoder decoder = new APNGDecoder();
//				decoder.read(apngFile);
//				// Get the frame rate of the APNG
//				int frameRate = decoder.getFrameRate();
//				// Print out the frame rate
//				System.out.println("Frame rate: " + frameRate + " fps");
				
			}
			else { // this is video
				if(data.length > 0 && data[0] != null && !data[0].trim().equals("UNKN")) {
					String[] parts = data[0].trim().replace(':','.').split("\\.");
					
					try {
						int hours   = Integer.parseInt(parts[0]);
						int mins    = Integer.parseInt(parts[1]);
						int secs    = Integer.parseInt(parts[2]);
						int secPart = Integer.parseInt(parts[3]);
						
						this.duration = (hours * 60 * 60) + (mins * 60) + secs + ((secPart > 50) ? 1 : 0);
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
				
				if(data.length > 1 && data[1] != null && !data[1].trim().equals("UNKN")) {
					String[] parts = data[1].split("x");
					try {
						this.width  = Integer.parseInt(parts[0].trim());
						this.height = Integer.parseInt(parts[1].trim());
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
				
				if(data.length > 2 && data[2] != null && !data[2].trim().equals("UNKN")) {
					try {
						this.fps = Float.valueOf(data[2].replace("fps", "").trim().replace("k", "000"));
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
				
				if(data.length > 4 && data[4] != null) {
					this.format = data[4].trim();
				}
			}
			
//			System.out.println("D: " + duration);
//			System.out.println("F: " + fps);
//			System.out.println("W: " + width);
//			System.out.println("H: " + height);
//			System.exit(0);
			
			this.readyToProcess = true;
			this.propertiesSet = true;
		}
		
		return this;
	} 

	public String getFileProperties() {
		if(propertiesSet) { return realFileProperty; }
		long test = System.currentTimeMillis();
		
		if(test > nextPropertiesUpdate) {
			this.nextPropertiesUpdate = (test + 1000);
			this.changed = true;
			
			propsShowStage++;
			if(propsShowStage > 3) {
				propsShowStage = 1;
			}
		}
		
		switch (propsShowStage) {
			case  1 : return Text.CALCULATION_ANIMATION_STAGES[0];
			case  2 : return Text.CALCULATION_ANIMATION_STAGES[1];
			case  3 : return Text.CALCULATION_ANIMATION_STAGES[2];
			default : break;
		}
		
		return "";
	}

	public String getFileProcessStage() {
		return fileProcessStage;
	}

	public RowData setFileProgressStage(String stage) {
		this.fileProcessStage = stage;
		this.changed = true;
		return this;
	}

	public float getTargetStatus() {
		return targetStatus;
	}

	public RowData setTargetStatus(float sts) {
		this.targetStatus = sts;
		this.changed = true;
		return this;
	}
	
	public RowData setInProcess(boolean flag) {
		this.inProcess = flag;
		return this;
	}

	public boolean beingProcessed() {
		return inProcess;
	}

	public int getCurrentProcessingStage() {
		return currentProcessingStage;
	}
	
	public RowData initProcessingTime() {
		this.startTimeMilliseconds = System.nanoTime();
		return this;
	}
	
	public RowData endProcessingTime() {
		this.finishTimeMilliseconds = System.nanoTime();
		return this;
	} 
	
	public long getProcessingNanoTime() {
		return (this.finishTimeMilliseconds - this.startTimeMilliseconds);
	}
	
	public String getProcessingTime() {
		long processingTime = this.getProcessingNanoTime();
	    long secondsFromNanoTime = processingTime / 1_000_000_000;
	    long seconds = secondsFromNanoTime % 60;
	    long minutes = secondsFromNanoTime / 60;
	    long hours = 0;
	    if (minutes >= 60) {
	    	hours = minutes / 60;
	        minutes %= 60;
	    }
	    return	((hours < 10)   ? ("0" + hours)   : hours)   + ":" +
	    		((minutes < 10) ? ("0" + minutes) : minutes) + ":" +
	    		((seconds < 10) ? ("0" + seconds) : seconds);
	} 
	
	public RowData setCurrentProcessingStage(int stage) {
		this.currentProcessingStage = stage;
		return this;
	}
	
	public int getEstimatedFramesCount() {
		if(fps > 0 && duration > 0) {
			float fduration = duration;
			return ((int) (fduration * fps));
		}
		return 1;
	}
}