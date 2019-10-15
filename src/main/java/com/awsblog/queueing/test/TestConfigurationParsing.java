package com.awsblog.queueing.test;

import com.awsblog.queueing.config.Configuration;
import com.awsblog.queueing.utils.FileUtils;
import com.awsblog.queueing.utils.Utils;

/**
 * Parsing test
 * 
 * @author zorani
 *
 */
public class TestConfigurationParsing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String configJSON = FileUtils.getFileFromResourcesAsString("configuration.json");
		Configuration config = Configuration.fromJSON(configJSON);
		
		System.out.println(Utils.toJSON(config));
	}

} // end TestConfigurationParsing