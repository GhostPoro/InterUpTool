package tools;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import holders.Configuration;
import tools.FileTool.FileInfo;
import tools.FileTool.FilesBatch;

public class ImageProcessor {
	
	private static final int THREAD_DONE_IMAGE_PROCESSING = -99; 
	
	private static final boolean DEBUG = false;
	
	public static boolean scaleImagesToSize(String sourcePath, String outPath, int desiredWidthInt, int desiredHeightInt, final int threadsNum, int[] status) {
		File sourceResource = new File(sourcePath);
		
		final boolean processingInSingleFileMode = (sourceResource.exists() && sourceResource.isFile());
		
		String[] allowedImageExtensions = Configuration.validImageExtensions;
		
		// for file processing variables
		String outFileName = null;
		String outFileExt = null;
		String outFileLocation = null;
		
		String[] sourceFileNameParts = generateFullFileName(sourcePath, true);

		
		// files list for folder processing
		final List<String> pathesToProcess = new ArrayList<String>();
		
		if(processingInSingleFileMode) {
			
			if(outPath != null) {
				String[] fileNameParts = generateFullFileName(outPath, false);
				File outputResource = new File(outPath);
				if(outputResource.exists()) {
					outFileName = (fileNameParts[0] + "_" + System.currentTimeMillis());
					outFileExt  = fileNameParts[1];
					outFileLocation = fileNameParts[2];
				}
				else {
					outFileName = fileNameParts[0];
					outFileExt  = fileNameParts[1];
					outFileLocation = fileNameParts[2];
				}
			}
			
			// if extraction from valuable data fail or does not exist, try generate from source file name 
			if(outFileName == null || outFileExt == null || outFileLocation == null) {
				outFileName = ((outFileName == null) ? sourceFileNameParts[0] : outFileName);
				outFileExt  = ((outFileExt  == null) ? sourceFileNameParts[1] : outFileExt);
				outFileLocation = ((outFileLocation  == null) ? sourceFileNameParts[2] : outFileLocation);
			}
			
			if(outFileLocation == null || outFileLocation.length() == 0 || outFileLocation.equals("/")) {
				System.out.println("ImageProcessor.scaleImagesToSize: Can't extract output file Location! Assigning default program storage location.");
				outFileLocation = Configuration.DEFAULT_TEMP_FILES_FOLDER_LOCATION;
			}
						
			if(outFileName == null) {
				System.err.println("ImageProcessor.scaleImagesToSize: Can't extract output file extension! Fatal Error!");
				System.exit(-1);
				return false;
			}
			
			if(outFileExt == null) {
				System.err.println("ImageProcessor.scaleImagesToSize: Can't extract output file extension! Fatal Error!");
				System.exit(-1);
				return false;
			}
			
			if(!isValidExt(allowedImageExtensions, sourceFileNameParts[1])) {
				System.err.println("ImageProcessor.scaleImagesToSize: Can't process unknown '" + sourceFileNameParts[1] + "' source file format! Fatal Error!");
				System.exit(-1);
				return false;
			}
			
			if(!isValidExt(allowedImageExtensions, outFileExt)) {
				outFileExt = sourceFileNameParts[2];
			}
			
			// just to be sure, change output file name, if its the same as source file name
			if(sourceResource.getAbsolutePath().equals(new File(outFileLocation + "/" + outFileName + "." + outFileExt).getAbsolutePath())) {
				outFileName += ("_" + System.currentTimeMillis());
			}
			
//			System.out.println("ImageProcessor.scaleImagesToSize: Name: "     + outFileName);
//			System.out.println("ImageProcessor.scaleImagesToSize: Ext: "      + outFileExt);
//			System.out.println("ImageProcessor.scaleImagesToSize: Location: " + outFileLocation);
//			System.exit(0);
			
		}
		else { // folder processing
			
			if(!sourceResource.exists() || !sourceResource.isDirectory() || (sourceResource.listFiles().length == 0)) {
				System.err.println("ImageProcessor.scaleImagesToSize: Can't locate source directory! Fatal Error!\nLooking for: " + sourcePath);
				return false;
			}
			
			FilesBatch batch = new FileTool.FilesBatch(sourcePath);
			
			// collect all acceptable files to process
			int esize = sourceFileNameParts.length;
			for (int ei = 0; ei < esize; ei++) {
				List<FileInfo> extFilesList = batch.getFilesListByExt(allowedImageExtensions[ei]);
				if(extFilesList != null) {
					int lsize = extFilesList.size();
					for (int li = 0; li < lsize; li++) {
						pathesToProcess.add(extFilesList.get(li).getPath());
					}
				}
			}
			
			if(pathesToProcess.size() == 0) {
				System.err.println("ImageProcessor.scaleImagesToSize: ImageProcessor: No valid files in input path! Nothing to process. Skipping...");
				return false;
			}
		}
		
		
		BufferedImage firstFrameToAnalyze = loadFromFile(processingInSingleFileMode ? sourcePath : pathesToProcess.get(0));
		
		final boolean imageHasAlpha = isTransperent(firstFrameToAnalyze);
		
		int realImgWidthInt  = firstFrameToAnalyze.getWidth();
		int realImgHeightInt = firstFrameToAnalyze.getHeight();
		
		// exit if dont need to process
		if(realImgWidthInt == desiredWidthInt && realImgHeightInt == desiredHeightInt) {
			System.err.println("ImageProcessor.scaleImagesToSize: Image rescaling and fix resolution skipped - input and desired resolutions are the same.");
			return false;
		}
		
		if(!processingInSingleFileMode) {
			File outputResource = new File(outPath);
			
			boolean goodOutputResource = (outputResource.exists() && outputResource.isDirectory() && (outputResource.listFiles().length == 0));
			
			if(!goodOutputResource) {
				System.out.println("ImageProcessor.scaleImagesToSize: Can't validate ouput files directory! Trying create new one...");
				
				File createdOutputResource = new File(outPath + "_" + System.currentTimeMillis());
				createdOutputResource.mkdirs();
				
				goodOutputResource = (createdOutputResource.exists() && createdOutputResource.isDirectory() && (createdOutputResource.listFiles().length == 0));
				if(goodOutputResource) {
					System.out.println("ImageProcessor.scaleImagesToSize: The newly created folder for output files is valid. Processing...");
					outPath = createdOutputResource.getAbsolutePath();
				}
				else {
					System.err.println("ImageProcessor.scaleImagesToSize: Can't new directory for output files! Fatal Error!");
					return false;
				}
			}
		}
		
		float realImgWidthFlt  = realImgWidthInt;
		float realImgHeightFlt = realImgHeightInt;
		
		float desiredWidthFlt  = desiredWidthInt;
		float desiredHeightFlt = desiredHeightInt;
		
		float scaledTargetWidthFlt  = desiredWidthFlt;
		float scaledTargetHeightFlt = ((desiredWidthFlt / realImgWidthFlt) * realImgHeightFlt);
		
		if(realImgHeightInt > realImgWidthInt) {
			scaledTargetWidthFlt = ((desiredHeightFlt / realImgHeightFlt) * realImgWidthFlt);
			scaledTargetHeightFlt = desiredHeightFlt;
		}
		
		int scaledTargetWidthInt  = ((int) scaledTargetWidthFlt);
		int scaledTargetHeightInt = ((int) scaledTargetHeightFlt);
		
		// fix not multiple of 2 cases
		if((scaledTargetWidthInt  & 1) != 0) { scaledTargetWidthInt  -= 1; }
		if((scaledTargetHeightInt & 1) != 0) { scaledTargetHeightInt -= 1; }
		
		final int finScaledTargetWidthInt  = scaledTargetWidthInt;
		final int finScaledTargetHeightInt = scaledTargetHeightInt;
		
		int blackColor = toColor(0, 0, 0, 0);
		
		final int[] threadsStatus = new int[threadsNum];
		final int[] prevThreadsStatus = new int[threadsNum];
		
		if(Logger.logLevelAbove(2) || DEBUG) {
			System.out.println("\nImage Rescale process start.");
			System.out.println("Path: " + sourcePath);
			System.out.println("Target: " + outPath);
			System.out.println("Mode: " + (processingInSingleFileMode ? "SingleFileMode" : ("Multithreaded(" + threadsNum + ")")));
			System.out.println("From: " + realImgWidthInt + "x" + realImgHeightInt + " To: " + finScaledTargetWidthInt + "x" + finScaledTargetHeightInt);
		}
		
		if(desiredWidthInt > scaledTargetWidthInt) {
			int halfDiff = ((int)((desiredWidthFlt - scaledTargetWidthFlt) / 2f));

			/* ####################################### PER IMAGE PART START ####################################### */
			if(processingInSingleFileMode) {
				BufferedImage img = loadFromFile(sourcePath);
				
				int[][] imageArray = loadAsArray(resizeImage(img, scaledTargetWidthInt, scaledTargetHeightInt, imageHasAlpha, true));
				int[][] outarr = new int[desiredWidthInt][desiredHeightInt];
				
				for (int y = 0; y < desiredHeightInt; y++) {
					for (int x = 0, sx = 0; x < desiredWidthInt; x++) {
						if(halfDiff < x && sx < scaledTargetWidthInt) {
							outarr[x][y] = imageArray[y][sx];
							sx++;
						}
						else {
							outarr[x][y] = blackColor;
						}
					}
				}
				
				BufferedImage outImg = createImage(outarr, imageHasAlpha);
				save(outImg, outFileExt, outFileLocation + "/" + outFileName);
			}
			else {
				
				final int processListSize = pathesToProcess.size();
				
				final String outputFolderPath = outPath;
				
				// create threads
				for (int ti = 0; ti < threadsNum; ti++) {
					
					final int startCountFrom = ti;
					
					Thread imageScalerThread = new Thread() {
						public void run() {
							
							for (int imgIdx = startCountFrom; imgIdx < processListSize && Configuration.PROCESSING; imgIdx += threadsNum) {
								
								if(imgIdx < processListSize) {
									String imagePath = pathesToProcess.get(imgIdx);
									
									BufferedImage img = loadFromFile(imagePath);
									
									int[][] imageArray = loadAsArray(resizeImage(img, finScaledTargetWidthInt, finScaledTargetHeightInt, imageHasAlpha, true));
									int[][] outarr = new int[desiredWidthInt][desiredHeightInt];
									
									for (int y = 0; y < desiredHeightInt; y++) {
										for (int x = 0, sx = 0; x < desiredWidthInt; x++) {
											if(halfDiff < x && sx < finScaledTargetWidthInt) {
												outarr[x][y] = imageArray[y][sx];
												sx++;
											}
											else {
												outarr[x][y] = blackColor;
											}
										}
									}
									
									BufferedImage outImg = createImage(outarr, imageHasAlpha);
									
									String[] outputFilsNameParts = generateFullFileName(imagePath, false);
									
									save(outImg, outputFilsNameParts[1], outputFolderPath + "/" + outputFilsNameParts[0]);
									threadsStatus[startCountFrom] = imgIdx;
									updateProgressStatus(status, imgIdx);
								}
							}
							threadsStatus[startCountFrom] = THREAD_DONE_IMAGE_PROCESSING;
						}
					};
					imageScalerThread.start();
				}
			}
			
			/* ######################################## PER IMAGE PART END ######################################## */
		}
		else if(desiredHeightInt > scaledTargetHeightInt) {
			int halfDiff = ((int)((desiredHeightFlt - scaledTargetHeightFlt) / 2f));

			/* ####################################### PER IMAGE PART START ####################################### */
			
			if(processingInSingleFileMode) {
				BufferedImage img = loadFromFile(sourcePath);
				
				int[][] imageArray = loadAsArray(resizeImage(img, scaledTargetWidthInt, scaledTargetHeightInt, imageHasAlpha, true));
				int[][] outarr = new int[desiredWidthInt][desiredHeightInt];
				
				for (int x = 0; x < desiredWidthInt; x++) {
					for (int y = 0, sy = 0; y < desiredHeightInt; y++) {
						if(y > halfDiff && sy < scaledTargetHeightInt) {
							outarr[x][y] = imageArray[sy][x];
							sy++;
						}
						else {
							outarr[x][y] = blackColor;
						}
					}
				}
				BufferedImage outImg = createImage(outarr, imageHasAlpha);
				save(outImg, outFileExt, outFileLocation + "/" + outFileName);
			}
			else {
				final int processListSize = pathesToProcess.size();
				
				final String outputFolderPath = outPath;
				
				// create threads
				for (int ti = 0; ti < threadsNum; ti++) {
					
					final int startCountFrom = ti; 
					
					Thread imageScalerThread = new Thread() {
						public void run() {
							
							for (int imgIdx = startCountFrom; imgIdx < processListSize && Configuration.PROCESSING; imgIdx += threadsNum) {
								
								if(imgIdx < processListSize) {
									String imagePath = pathesToProcess.get(imgIdx);
									
									BufferedImage img = loadFromFile(imagePath);
									
									int[][] imageArray = loadAsArray(resizeImage(img, finScaledTargetWidthInt, finScaledTargetHeightInt, imageHasAlpha, true));
									int[][] outarr = new int[desiredWidthInt][desiredHeightInt];
									
									for (int x = 0; x < desiredWidthInt; x++) {
										for (int y = 0, sy = 0; y < desiredHeightInt; y++) {
											if(y > halfDiff && sy < finScaledTargetHeightInt) {
												outarr[x][y] = imageArray[sy][x];
												sy++;
											}
											else {
												outarr[x][y] = blackColor;
											}
										}
									}
									BufferedImage outImg = createImage(outarr, imageHasAlpha);
									
									String[] outputFilsNameParts = generateFullFileName(imagePath, false);
									
									save(outImg, outputFilsNameParts[1], outputFolderPath + "/" + outputFilsNameParts[0]);
									threadsStatus[startCountFrom] = imgIdx;
									updateProgressStatus(status, imgIdx);
								}
							}
							threadsStatus[startCountFrom] = THREAD_DONE_IMAGE_PROCESSING;
						}
					};
					imageScalerThread.start();
				}
			}
			
			/* ######################################## PER IMAGE PART END ######################################## */
		}
		else {
			/* ####################################### PER IMAGE PART START ####################################### */
			if(processingInSingleFileMode && Configuration.PROCESSING) {
				BufferedImage img = loadFromFile(sourcePath);
				BufferedImage outImg = resizeImage(img, scaledTargetWidthInt, scaledTargetHeightInt, imageHasAlpha, false);
				save(outImg, outFileExt, outFileLocation + "/" + outFileName);
			}
			else {
				final int processListSize = pathesToProcess.size();
				
				final String outputFolderPath = outPath;
				
				// create threads
				for (int ti = 0; ti < threadsNum; ti++) {
					
					final int startCountFrom = ti; 
					
					Thread imageScalerThread = new Thread() {
						public void run() {
							
							for (int imgIdx = startCountFrom; imgIdx < processListSize && Configuration.PROCESSING; imgIdx += threadsNum) {
								
								if(imgIdx < processListSize) {
									String imagePath = pathesToProcess.get(imgIdx);
									BufferedImage img = loadFromFile(imagePath);
									BufferedImage outImg = resizeImage(img, finScaledTargetWidthInt, finScaledTargetHeightInt, imageHasAlpha, false);
									String[] outputFilsNameParts = generateFullFileName(imagePath, false);
									save(outImg, outputFilsNameParts[1], outputFolderPath + "/" + outputFilsNameParts[0]);
									threadsStatus[startCountFrom] = imgIdx;
									updateProgressStatus(status, imgIdx);
								}
							}
							threadsStatus[startCountFrom] = THREAD_DONE_IMAGE_PROCESSING;
						}
					};
					imageScalerThread.start();
				}
			}
			
			/* ######################################## PER IMAGE PART END ######################################## */
		}
		
		int maxErrors = 30; // 30 sec time out
		int curErrors = 0;
		int doneThreads = 0;
		
		// wait while treads finish work
		if(!processingInSingleFileMode) {
			while(doneThreads < threadsNum && Configuration.PROCESSING) {
				
				for (int ti = 0; ti < threadsNum; ti++) {
					if(threadsStatus[ti] == THREAD_DONE_IMAGE_PROCESSING) {
						doneThreads++;
					}
				}
				
				
				for (int ti = 0; ti < threadsNum; ti++) {
					
					if(prevThreadsStatus[ti] != threadsStatus[ti]) {
						prevThreadsStatus[ti] = threadsStatus[ti];
						doneThreads = 0;
					}
				}
				
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
			}
			
		}
		
		if(doneThreads < threadsNum) {
			return false;
		}
		
		//System.out.println("ImageProcessor.scaleImagesToSize: Image Mode: " + (processingInSingleFileMode ? "TRUE" : "FALSE"));
		//System.out.println(scaledTargetWidthInt + "x" + scaledTargetHeightInt);
		return Configuration.PROCESSING;
	}
	
	private static void updateProgressStatus(int[] status, int num) {
		if(status != null) {
			if(status[0] < num) {
				status[0] = num;
			}
		}
	}
	
	public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight, boolean transperent, boolean fix) {
	    Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
	    BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, (transperent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB));
	    Graphics2D g = outputImage.createGraphics();
	    g.drawImage(resultingImage, 0, 0, null);
	    g.dispose();
	    if(fix) {
		    // hack because Graphics2D transform TYPE_INT_RGB in TYPE_3BYTE_BGR, and then does not work transform BufferedImage to int array
		    return loadFromStream(toInputStream(outputImage));
	    }
	    return outputImage;
	}
//	public static int[][] loadAsArray(BufferedImage image) {
//		final int width  = image.getWidth();
//		final int height = image.getHeight();
//		
//	    // Create a 2D integer array to store the pixels
//	    int[][] pixels = new int[height][width];
//	
//	    // Copy the pixels from the image into the array
//	    for (int y = 0; y < height; y++) {
//	        for (int x = 0; x < width; x++) {
//	            pixels[y][x] = image.getRGB(x, y);
//	        }
//	    }
//	    return pixels;
//	}
	
	public static int[][] loadAsArray(BufferedImage image) {

		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final int width  = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = isTransperent(image);

		int[][] result = new int[height][width];
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += (((int) pixels[pixel]     & 0xff) << 24);	// alpha
				argb += ( (int) pixels[pixel + 1] & 0xff);			// blue
				argb += (((int) pixels[pixel + 2] & 0xff) << 8);	// green
				argb += (((int) pixels[pixel + 3] & 0xff) << 16);	// red
				result[row][col] = argb;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
				int argb = -16777216;								// 255 alpha
				//argb = 0;
				argb += ( (int) pixels[pixel]     & 0xff);			// blue
				argb += (((int) pixels[pixel + 1] & 0xff) << 8);	// green
				argb += (((int) pixels[pixel + 2] & 0xff) << 16);	// red
				result[row][col] = argb;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		}
		return result;
	}
	
	public static BufferedImage createImage(int[][] pixelArray) {
		return createImage(pixelArray, false);
	}
	
    // convert int array to BufferedImage
	public static BufferedImage createImage(int[][] pixelArray, boolean transperent) {
		int sizeX = pixelArray.length;
		int sizeY = pixelArray[0].length;
    	
		BufferedImage image = new BufferedImage(sizeX, sizeY, (transperent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB));

		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				image.setRGB(x, y, pixelArray[x][y]); 
			}
		}
		return image;
	}
	
    public static int toColor(int R, int G, int B, int alpha) {
    	return (alpha << 24) | (R << 16) | (G << 8) | B;
    }
    
    public static int toColor(int R, int G, int B) {
    	return (R << 16) | (G << 8) | B;
    }
    
    public static BufferedImage save(BufferedImage image) {
    	return save(image, "png", "autoName");
    }
    
    public static BufferedImage save(BufferedImage image, String type, String name) {
    	type = ((type == null) ? "png" : type);
        try {
        	
        	String ext = ("." + type);
        	
        	String addon = (name.endsWith(ext) ? "" : ext);
        	
            // retrieve image
            File outputfile = new File(name + addon);
            outputfile.createNewFile();

            ImageIO.write(image, type, outputfile);
            ImageIO.createImageInputStream(image);
        } catch (Exception e) {
            // oh no! Blank catches are bad
        	System.err.println("ImageProcessor.scaleImagesToSize: ImageTools.save: Can't save image: " + name);
            e.printStackTrace();
            System.exit(-1);
        }
        return image;
    }
    
	public static BufferedImage loadFromFile(String path) {
		try { return ImageIO.read(new FileInputStream(path)); } catch (Exception e) { e.printStackTrace(); } return null;
	}
	
    public static InputStream toInputStream(BufferedImage image) {
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	try { ImageIO.write(image, "png", os); } catch (IOException e) { e.printStackTrace(); }
    	return new ByteArrayInputStream(os.toByteArray());
    }
    
	public static BufferedImage loadFromStream(InputStream stream) {
		try { return ImageIO.read(stream); } catch (IOException e) { e.printStackTrace(); } return null;
	}
	
	private static String[] generateFullFileName(String inPath, boolean itsSource) {
		
		String outFileName = null;
		String outFileExt = null;
		String location = null;
		
		int extSplitLoc = inPath.lastIndexOf('.');
		
		int idxA = inPath.lastIndexOf('\\');
		int idxB = inPath.lastIndexOf('/');
		
		int pathSplitLoc = ((idxA > idxB) ? idxA : idxB);
		
		if(pathSplitLoc > 0) {
			location = inPath.substring(0, pathSplitLoc);;
		}
		
		if(extSplitLoc > 0 && pathSplitLoc < extSplitLoc) {
			try {
				String fileFullName = ((pathSplitLoc > -1) ? inPath.substring(pathSplitLoc + 1) : inPath);
				
				extSplitLoc = fileFullName.lastIndexOf('.');
				String fileName = fileFullName.substring(0, extSplitLoc);
				String fileExt = fileFullName.substring(extSplitLoc + 1);
				
				if(fileName.length() > 0) {
					outFileName = (itsSource ? (fileName + "_fix_dim") : fileName);
				}
				else {
					outFileName = (System.currentTimeMillis() + "_auto_gen_file_name_fix_dim");
				}
				
				if(fileExt.length() > 0) {
					outFileExt = fileExt;
				}
			}
			catch (Exception e) {
				System.err.println("ImageProcessor.scaleImagesToSize: ERROR! Extracting File name or extension from source path file.");
				e.printStackTrace();
			}
		}
		else if(inPath.length() > 0) {
			outFileName = inPath;
		}
		
		return new String[] { outFileName, outFileExt, location };
	}
	
	private static boolean isValidExt(String[] valid, String ext) {
		if(valid == null || ext == null || valid.length == 0) {
			return false;
		}
		
		int size = valid.length;
		for (int i = 0; i < size; i++) {
			if(ext.equals(valid[i])) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean isTransperent(BufferedImage image) {
		return ((image == null) ? false : (image.getAlphaRaster() != null));
	}

}
