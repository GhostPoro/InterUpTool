package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public enum Hash {

    MD5("MD5"),
    SHA1("SHA1"),
    SHA256("SHA-256"),
    SHA512("SHA-512");

    private String name;
	
	private static char[] a = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	

    Hash(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String checksum(String filePath) {
    	return checksum(new File(filePath));
    }
    
    public String checksum(File inputFile) {
    	try {
			return checksum(new FileInputStream(inputFile));
		} catch (FileNotFoundException e) { e.printStackTrace(); }
		return "-1";
    }
    
    public String checksum(InputStream stream) {
    	return checksum(new InputStream[] {stream});
    }
    
    public String checksum(InputStream[] streams) {
        try {
            MessageDigest digest = MessageDigest.getInstance(getName());
            byte[] block = new byte[4096];
            int size = streams.length;
            for (int s = 0; s < size; s++) {
            	processStream(streams[s], digest, block);
			}
            return bytesAsString(digest.digest());
        } catch (Exception e) { e.printStackTrace(); }
        return "-1";
    }
    
    private MessageDigest processStream(InputStream stream, MessageDigest digest, byte[] block) {
        int length;
        try {
			while ((length = stream.read(block)) > 0) {
			    digest.update(block, 0, length);
			}
		} catch (IOException e) { e.printStackTrace(); }
    	return digest;
    }
    
    public static String bytesAsString(byte[] b) {
        char[] r = new char[b.length * 2];
        for (int i = 0, v; i < b.length; i++) {
            v = b[i] & 0xFF; r[i * 2] = a[v / 16]; r[i * 2 + 1] = a[v % 16];
        }
        return new String(r);
    }
}