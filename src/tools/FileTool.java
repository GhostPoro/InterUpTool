package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileTool {
	
	public static boolean exist(String filename) throws IOException {
		
		File object = new File(filename);
		if (object.exists()) {
			System.out.println("File " + filename + " exist! Yey!");
			return true;
		}
		else {
			System.out.println("File " + filename + " NOT EXIST!");
			return false;
		}
	}
	
	public static String getAbsoluteProgramPathbyClassPath() {
		
		String classpath = System.getProperty("java.class.path");
		String[] classPathParts = classpath.split(";");
		return classPathParts[0];
	}
	
	public static String getPath(String resFolder, String fileName, String extension) {
		if(fileName == null || fileName.length() == 0) { return null; }
		
		String separator = "/";
		String pathStart = "./";
		
		String programPath = System.getProperty("workdir"); //filedummy.getAbsolutePath();
		if (programPath != null && programPath.length() > 2) {
			separator = File.separator;
			pathStart = programPath;
		}
		
		String fullFileName = fileName;
		if(!fullFileName.endsWith(extension)) {
			fullFileName += extension;
		}
		
		String finPath = (pathStart + resFolder + separator + fullFileName);
		
		finPath = finPath.replace("//", "/");
		finPath = finPath.replace("././", "./");
		finPath = finPath.replace("./res/./res", "./res");
		finPath = finPath.replace("/./", "/");

		return finPath;
	}
	
	public static boolean copyFile(File source, File dest) {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } catch (IOException e) {
			e.printStackTrace();
		} finally {
	        try {
				if(is != null) { is.close(); }
				if(os != null) { os.close(); }
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	    }
	    return false;
	}
	
    public static boolean saveTextFile(List<String> lines, String path) {
    	
    	try {
			PrintWriter out = new PrintWriter(path);
			for(String line : lines) {
				out.println(line);
			}
			out.println();
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	return false;
    }
	
	public static class FileInfo {
		private String location;
		private String name;
		private String ext;
		
		private boolean hidden;
		private boolean hasExt;
		
		public FileInfo(String l, String n) {
			this.location = l;
			this.name = n;
			this.ext = "noext";
			this.hidden = false;
			this.hasExt = false;
		}
		
		public FileInfo(String l, String n, String e) {
			this.location = l;
			this.name = n;
			this.ext = e;
			this.hidden = false;
			this.hasExt = true;
		}

		public String getLocation() {
			return location;
		}

		public FileInfo setLocation(String l) {
			this.location = l;
			return this;
		}

		public String getName() {
			return name;
		}

		public FileInfo setName(String n) {
			this.name = n;
			this.hidden = n.startsWith(".");
			return this;
		}

		public String getExtension() {
			return ext;
		}

		public FileInfo setExtension(String e) {
			this.ext = e;
			this.hasExt = (e != null && e.length() > 0 && !e.equals("noext"));
			return this;
		}
		
		public boolean isHidden() {
			return hidden;
		}

		public FileInfo setHidden(boolean k) {
			this.hidden = k;
			return this;
		}

		public boolean hasExtension() {
			return hasExt;
		}

		public String getFullName() {
			return new String((hidden ? "." : "") + name + (hasExt ? ("." + ext) : ""));
		}

		public String getPath() {
			return new String(location + File.separatorChar + getFullName());
		}
		
	}
	
	public static class FilesBatch {
		private Map<String, List<FileInfo>> batch;
		private List<FileInfo> list;
		
		public FilesBatch(String location) {
			this.batch = new HashMap<String, List<FileInfo>>();
			this.list = new ArrayList<FileInfo>();
			parseLocation(location, list, batch);
		}
		
		public List<FileInfo> getFilesListByExt(String ext) {
			return batch.get(ext);
		}
		
		public List<FileInfo> getFullFilesList() {
			return list;
		}
		
		public Set<String> getExtList() {
			return batch.keySet();
		}
		
		private boolean parseLocation(String path, List<FileInfo> list, Map<String, List<FileInfo>> batch) {
			
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();
			
			if(listOfFiles != null && listOfFiles.length > 0) {
				for (int i = 0; i < listOfFiles.length; i++) {
					File file = listOfFiles[i];
					String fileFullName = file.getName();
					boolean hidden = false;

					if (file.isFile()) {
						if(fileFullName.length() > 1) {
							
							if(fileFullName.startsWith(".")) {
								fileFullName = fileFullName.substring(1);
								hidden = true;
							}
							
							if(fileFullName.contains(".")) {
								int extIndex = fileFullName.lastIndexOf(".");
								boolean hasExt = true;
								
								String fname = fileFullName.substring(0, extIndex);
								String fext = fileFullName.substring(extIndex + 1);
								
								if(fname.length() == 0) {
									fname = "noname";
								}
								
								if(fext.length() == 0) {
									fext = "noext";
									hasExt = false;
								}
								
								storeData(path, fname, fext, hasExt, hidden, list, batch);
							}
							else {
								storeData(path, fileFullName, "noext", false, hidden, list, batch);
							}
							
						}
					}
					else if(file.isDirectory()) {
						String deeperPath = new String(path + File.separatorChar + fileFullName);
						parseLocation(deeperPath, list, batch);
					}
				}
			}
			return true;
		}
		
		private boolean storeData(String path, String fname, String fext, boolean hasExt, boolean hidden, List<FileInfo> list, Map<String, List<FileInfo>> batch) {
			FileInfo finfo = (hasExt ? new FileInfo(path, fname, fext) : new FileInfo(path, fname)).setHidden(hidden);
			list.add(finfo);
			List<FileInfo> extList = batch.get(fext);
			if(extList == null) {
				extList = new ArrayList<FileInfo>();
				batch.put(fext, extList);
			}
			extList.add(finfo);
			return true;
		}
	}

}
