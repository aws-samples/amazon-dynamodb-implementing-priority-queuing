package com.awsblog.queueing.utils;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Bunch of utility methods
 * 
 * @author zorani
 *
 */
public class Utils {

	/**
	 * Convert an Object to JSON (Object should be properly annotated)
	 */
	public static String toJSON(Object obj) {

		if (Utils.checkIfNullObject(obj)) return null;
		
		String jsonInString = null;
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			Utils.throwIfConditionIsTrue(true, e.getMessage());
		}
		
		return jsonInString;
	}
	
	/**
	 * Compare the values
	 * 
	 * @param val1
	 * @param val2
	 * @param percentAllowed
	 * @return
	 */
	public static boolean compareValues(double val1, double val2, double percentAllowed) {
		
		if (val1 == val2) return true;
		if (val1 < 0.000001 && val2 < 0.000001) return true;
		
		double diff = val1 - val2;
		double percent = (diff / Math.max(val1,  val2)) * 100;
		
		if (percent <= percentAllowed) return true;
		return false;
	}
	
	/**
	 * Check if NULL or EMPTY string
	 * 
	 * @param strToValidate
	 * @return
	 */
	public static boolean nullOrEmpty(String strToValidate) {
		return strToValidate == null || strToValidate.isEmpty();
	}

	/**
	 * Check if the string is Null or Empty
	 * 
	 * @param strToValidate
	 * @return
	 */
	public static boolean checkIfNullOrEmptyString(String strToValidate) {
		return Utils.nullOrEmpty(strToValidate);
	}

	/**
	 * Check if the object is NULL
	 * 
	 * @param o
	 * @return
	 */
	public static boolean checkIfNullObject(Object o) {
		return o == null;
	}

	/**
	 * Check if the object is NOT NULL
	 * 
	 * @param o
	 * @return
	 */
	public static boolean checkIfNotNullObject(Object o) {
		return o != null;
	}

	/**
	 * Throw exception if the object is NULL
	 * 
	 * @param o
	 * @param exceptionMessage
	 */
	public static void throwIfNullObject(Object o, String exceptionMessage) {
		if (o == null) throw new IllegalArgumentException(exceptionMessage);
	}

	/**
	 * Throw exception if the Collection is NULL or Empty
	 * 
	 * @param c
	 * @param exceptionMessage
	 */
	public static void throwIfNullOrEmptyCollection(Collection c, String exceptionMessage) {
		if (c == null || c.isEmpty()) throw new IllegalArgumentException(exceptionMessage);
	}

	/**
	 * Throw exception if object is NOT NULL
	 * 
	 * @param o
	 * @param exceptionMessage
	 */
	public static void throwIfNotNullObject(Object o, String exceptionMessage) {
		if (o != null) throw new IllegalArgumentException(exceptionMessage);
	}

	/**
	 * Check if not NULL and not Empty string
	 * 
	 * @param strToValidate
	 * @return
	 */
	public static boolean checkIfNotNullAndNotEmptyString(String strToValidate) {
		return strToValidate != null && !strToValidate.isEmpty();
	}

	/**
	 * Throw exception if this an empty String
	 * 
	 * @param strToValidate
	 * @param exceptionMessage
	 */
	public static void throwIfNullOrEmptyString(String strToValidate, String exceptionMessage) {
		if (strToValidate == null || strToValidate.isEmpty()) throw new IllegalArgumentException(exceptionMessage);
	}

	/**
	 * Throw exception if the passed condition is TRUE
	 * 
	 * @param condition
	 * @param exceptionMessage
	 */
	public static void throwIfConditionIsTrue(boolean condition, String exceptionMessage) {
		if (condition) throw new IllegalArgumentException(exceptionMessage);
	}
	
	/**
	 * Throw exception if the passed condition is FALSE
	 * 
	 * @param condition
	 * @param exceptionMessage
	 */
	public static void throwIfConditionIsFalse(boolean condition, String exceptionMessage) {
		if (!condition) throw new IllegalArgumentException(exceptionMessage);
	}

	/**
	 * Assert on the value
	 * 
	 * @param condition
	 * @param exceptionMessage
	 */
	public static void assertValue(boolean condition, String exceptionMessage) {
		if (!condition) throw new IllegalArgumentException(exceptionMessage);
	}
	
	/**
	 * Check if collection exists and it is not empty
	 * @param c
	 * @return
	 */
	public static boolean checkIfNotNullAndNotEmptyCollection(Collection c) {
		if (c != null && !c.isEmpty()) return true;
		return false;
	}
	
	/**
	 * Check if collection is NULL or empty
	 * 
	 * @param c
	 * @return
	 */
	public static boolean checkIfNullOrEmptyCollection(Collection c) {
		if (c == null || c.isEmpty()) return true;
		return false;
	}

	/**
	 * Remove special characters due to UTF8 BOM
	 * 
	 * @param s
	 * @return
	 */
	public static String cleanUFT8_BOM(String s) {
		
		if (Utils.nullOrEmpty(s)) return s;
		
		//int ch = (int)s.charAt(0);
		//System.out.printf("Char : %d%n", ch);
		
		byte[] byteArray = s.getBytes();
		int len = byteArray.length;
		
		if (byteArray[0] == 0xEF && byteArray[1] == 0xBB && byteArray[2] == 0xBF) {
			try {
				return new String(byteArray, 3, len - 3, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		int i = 0;
		
		while (i < byteArray.length) {
			if (byteArray[i] < ' ' || byteArray[i] > 255) {
				++i;
				--len;
			}
			else break;
		}
		
		//if ((int)s.charAt(0) == 65279 || (int)s.charAt(0) == 65533) return s.substring(1);
		//return s;
		
		try {
			return new String(byteArray, i, len, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Remove binary zeros
	 * 
	 * @param s
	 * @return
	 */
	public static String removeBinaryZeros(String s) {
		
		byte[] byteInArray = s.getBytes();
		byte[] byteOutArray = new byte[byteInArray.length];
		
		int i = 0;
		int k = 0;
		
		while (i < byteInArray.length) {
			if (byteInArray[i] >= ' ' || byteInArray[i] == 10 || byteInArray[i] == 13) {
				byteOutArray[k++] = byteInArray[i];
			}
			
			++i;
		}
		
		try {
			return new String(byteOutArray, 0, k, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}	
	
	/**
	 * Print bytes
	 * 
	 * @param totalBytes
	 * @param s
	 */
	public static void printBytes(int totalBytes, String s) {
		
		byte[] bytes = s.getBytes();
		
		for(int i = 0; i < totalBytes; ++i) {
			System.out.printf("%d ", bytes[i]);
		}
		
		System.out.printf("%n");
	}

	/**
	 * Print bytes
	 * 
	 * @param totalBytes
	 * @param s
	 */
	public static void printBytes(String s) {
		
		byte[] bytes = s.getBytes();
		
		for(int i = 0; i < bytes.length; ++i) {
			System.out.printf("%d ", bytes[i]);
		}
		
		System.out.printf("%n");
	}
	
	/**
	 * Print raw bytes as String
	 * 
	 * @param s
	 * @return
	 */
	public static String rawBytesAsString(String s) {
		byte[] bytes = s.getBytes();

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < bytes.length; ++i) {
			sb.append(String.format("%d ", bytes[i]));
		}

		return sb.toString().trim();
	}
	
	/**
	 * Get the UTC time as LocalDateTime
	 * 
	 * @return
	 */
	public static LocalDateTime getLocalDateTimeInUTC(){
	    ZonedDateTime nowUTC = ZonedDateTime.now(ZoneOffset.UTC);

	    return nowUTC.toLocalDateTime();
	}

	/**
	 * Sleep (parameter - sleep time in milliseconds)
	 * 
	 * @param seconds
	 */
	public static void sleep(long milliseconds) {
		
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			
			//e.printStackTrace();
			System.out.println("WARNING: Sleep() is interrupted!");			
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Sleep (parameter - sleep time in seconds)
	 * 
	 * @param seconds
	 */
	public static void sleepInSeconds(int seconds) {
		
		try {
			Thread.sleep(seconds * 1000L);
		} catch (InterruptedException e) {
			//e.printStackTrace();
			
			System.out.println("WARNING: Sleep() is interrupted!");
			Thread.currentThread().interrupt();
		}
	}
	
} // end Utils