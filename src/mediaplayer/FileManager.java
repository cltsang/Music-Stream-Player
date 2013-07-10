package mediaplayer;

import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;

public final class FileManager {
	private FileManager(){
		
	}
	
	public static boolean twoFilesAreTheSame(File file1, File file2) throws Exception{
		return calculateMD5(file1).equals(file2);
	}
	
	public static boolean twoFilesAreTheSame(String filePath1, String filePath2) throws Exception{
		return twoFilesAreTheSame(new File(filePath1), new File(filePath2));
	}
	
	public static String calculateMD5(File file) throws Exception{
    	MessageDigest md = MessageDigest.getInstance("MD5");
    	FileInputStream fis = new FileInputStream(file);
 
    	byte[] dataBytes = new byte[1024];
 
    	int nread = 0; 
    	while ((nread = fis.read(dataBytes)) != -1) {
    		md.update(dataBytes, 0, nread);
    	};
    	byte[] mdbytes = md.digest();
 
    	//convert the byte to hex format method 1
    	StringBuffer sb = new StringBuffer();
    	for (int i = 0; i < mdbytes.length; i++)
    		sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
    	
		return sb.toString();
	}
	
    public static String calculateMD5(String filePath) throws Exception {
		return calculateMD5(new File(filePath));
	}
	
	/**
	 * @param node
	 * @return an ArrayList containing all absolute paths of the file or the files inside the directory 
	 */
	public static ArrayList<String> listDirectory(File node){
		ArrayList<String> filePathList = new ArrayList<String>();
		// if the node represents a file
		filePathList.add(node.getAbsolutePath());
		
		// if the node represents a directory
		if(node.isDirectory()){
			String[] subNote = node.list();
			for(String filename : subNote){
				filePathList.addAll(listDirectory(new File(node, filename)));
			}
		}

		return filePathList;
	}
	
	public static ArrayList<String> listDirectory(String filePath) {
		return listDirectory(new File(filePath));
	}
}
