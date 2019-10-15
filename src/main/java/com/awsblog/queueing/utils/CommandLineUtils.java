package com.awsblog.queueing.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Command Line Utils
 * 
 * @author zorani
 *
 */
public class CommandLineUtils {

	/**
	 * C-tor
	 * 
	 * @param argv
	 */
	public CommandLineUtils(String[] argv) {
		
		this.options = new HashMap<>();
		
		parse(argv);
	}
	
	/**
	 * Parse the command line arguments
	 * 
	 * @param argv
	 */
	private void parse(String[] argv) {
		
		if (argv == null || argv.length == 0) return;
		
		for(String arg : argv) {
			
			String option = arg.trim();
			
			int totalDashes = 0;
			if (option.startsWith("-")) {
				
				for(int i = 0; i < option.length(); ++i) {
					
					char ch = option.charAt(i);
					
					if (ch == '-') {
						
						++totalDashes;
						continue;
					}
					
					if (Character.isUpperCase(ch) || Character.isLowerCase(ch)) break;
					
					Utils.throwIfConditionIsTrue(true, "Invalid character used in the Command Line option key!");
				}
				
				Utils.throwIfConditionIsTrue(totalDashes == 0, "Option key needs to start with dashes!");
				
				String[] arr = option.substring(totalDashes).split("=");
				
				if (arr.length == 0 || Utils.checkIfNullOrEmptyString(arr[0])) {
					if (arr.length == 1) System.out.printf("*** ERROR *** Command line parameters are invalid! [0] -> [%s]%n", arr[0]);
					Utils.throwIfConditionIsTrue(true, "Invalid parameters!");
				}
				
				if (arr.length == 2) this.options.put(arr[0], arr[1]);
				else this.options.put(arr[0], "");
			}
		}
	}
	
	/**
	 * Check if the options map contains provided option key
	 * 
	 * @param optionKey
	 * @return
	 */
	public boolean containsOption(String optionKey) {
		
		if (Utils.checkIfNullOrEmptyString(optionKey)) return false;
		
		return this.options.containsKey(this.removeLeadingDashes(optionKey));
	}
	
	/**
	 * Get the option's value from the Options Map
	 * 
	 * @param optionKey
	 * @return options's value
	 */
	public String getOptionValue(String optionKey) {
		
		if (Utils.checkIfNullOrEmptyString(optionKey)) return null;
		
		return this.options.get(this.removeLeadingDashes(optionKey));
	}
	
	/**
	 * Remove the leading dashes
	 * 
	 * @param s
	 * @return
	 */
	private String removeLeadingDashes(String s) {
		
		if (Utils.checkIfNullOrEmptyString(s)) return null;
		
		String tmp = s.trim();
		
		int totalDashes = 0;
		if (tmp.startsWith("-")) {
			
			for(int i = 0; i < tmp.length(); ++i) {
				
				if (tmp.charAt(i) == '-') {
					++totalDashes;
				}
				else {
					break;
				}
			}

			return tmp.substring(totalDashes);
		}

		return tmp;
	}
	
	/**
	 * toString()
	 */
	public String toString() {
		
		StringBuilder sb = new StringBuilder(" >> Total command line options: " + options.size());
		for(Entry<String, String> entry : this.options.entrySet()) {
			sb.append(String.format(" >>> Option: [%s] .. value: [%s]%n", entry.getKey(), entry.getValue()));
		}
		
		return sb.toString();
	}
	
	// -------------- fields
	
	private Map<String, String> options = null;
	
} // end CommandLineUtils