package com.awsblog.queueing.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * File and resource based utilities
 * 
 * @author zorani
 *
 */
public class FileUtils {
	
	/**
	 * Hide constructor
	 */
	private FileUtils() {
		
	}
	
	/**
	 * Check if the file exists
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean doesFileExists(String filePath) {
		
		if (Utils.checkIfNullOrEmptyString(filePath)) return false;
		
		File f = new File(filePath);
		return f.exists();
	}
	
	/**
	 * Retrieve file content as a String with provided file name
	 * 
	 * @param fileName file name for the file content to be retrieved from
	 * 
	 * @return content of the file returned as a String
	 */
	public static String getFileContentAsString(String fileName) {

		ByteArrayOutputStream buf = null;

		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName))) {
			buf = new ByteArrayOutputStream();
			
			int result = bis.read();
			
			while(result != -1) {
				buf.write((byte) result);
				result = bis.read();
			}
			
			buf.close();
			
			// StandardCharsets.UTF_8.name() > JDK 7
			return buf.toString(StandardCharsets.UTF_8.name());
			
		} catch (IOException e) {
			e.printStackTrace();
		} 

		return null;
	} 

	/**
	 * Retrieve file content as a String with provided file name
	 * 
	 * @param fileName file name for the file content to be retrieved from
	 * 
	 * @return content of the file returned as a String
	 */
	public static byte[] getFileContentAsByteArray(String fileName) {

		ByteArrayOutputStream buf = null;

		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName))) {

			buf = new ByteArrayOutputStream();

			int result = bis.read();
			
			while(result != -1) {
				buf.write((byte) result);
				result = bis.read();
			}
			
			buf.close();
			
			return buf.toByteArray();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new byte[0];
	} 
	
	/**
	 * Retrieve the resource content as a String with provided resource name
	 * 
	 * @param resourceFileName resource file name for the file content to be retrieved from
	 * 
	 * @return content of the file returned as a String
	 */

	public static String getFileFromResourcesAsString(String resourceFileName) {
		
		// try two methods to get the file from the resource folder
		InputStream stream = FileUtils.class.getResourceAsStream(resourceFileName);
		if (stream == null) stream = FileUtils.class.getResourceAsStream("/" + resourceFileName);
		
		if (stream == null) stream = FileUtils.class.getClassLoader().getResourceAsStream(resourceFileName);
		if (stream == null) stream = FileUtils.class.getClassLoader().getResourceAsStream("/" + resourceFileName);

		if (stream == null) throw new IllegalArgumentException("Resource file name [" + resourceFileName + "] is not found!");

		try (BufferedInputStream bis = new BufferedInputStream(stream)) {
			
			ByteArrayOutputStream buf = new ByteArrayOutputStream();

			int result = bis.read();
			while(result != -1) {
				buf.write((byte) result);
				result = bis.read();
			}
			
			buf.close();
			
			// StandardCharsets.UTF_8.name() > JDK 7
			return buf.toString(StandardCharsets.UTF_8.name());
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}	

	/**
	 * Write data to file
	 * 
	 * @param fileName
	 * @param content
	 * @return
	 */
	public static boolean writeToFile(String fileName, String content) {
		try {
			Files.write(Paths.get(fileName), content.getBytes());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
} // end FileUtils