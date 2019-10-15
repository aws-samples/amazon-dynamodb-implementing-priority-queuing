package com.awsblog.queueing.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Model DynamoDB indexes
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
public class ConfigIndex {

	/**
	 * C-tor
	 */
	public ConfigIndex() {
		
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the readCapacity
	 */
	public int getReadCapacity() {
		return readCapacity;
	}

	/**
	 * @param readCapacity the readCapacity to set
	 */
	public void setReadCapacity(int readCapacity) {
		this.readCapacity = readCapacity;
	}

	/**
	 * @return the writeCapacity
	 */
	public int getWriteCapacity() {
		return writeCapacity;
	}

	/**
	 * @param writeCapacity the writeCapacity to set
	 */
	public void setWriteCapacity(int writeCapacity) {
		this.writeCapacity = writeCapacity;
	}

	/**
	 * @return the hashKey
	 */
	public ConfigField getHashKey() {
		return hashKey;
	}

	/**
	 * @param hashKey the hashKey to set
	 */
	public void setHashKey(ConfigField hashKey) {
		this.hashKey = hashKey;
	}

	/**
	 * @return the sortKey
	 */
	public ConfigField getSortKey() {
		return sortKey;
	}

	/**
	 * @param sortKey the sortKey to set
	 */
	public void setSortKey(ConfigField sortKey) {
		this.sortKey = sortKey;
	}

	/**
	 * @return the gsi
	 */
	public boolean isGsi() {
		return gsi;
	}
	/**
	 * @param gsi the gsi to set
	 */
	public void setGsi(boolean gsi) {
		this.gsi = gsi;
	}

	// ----------- fields

	@JsonProperty("index_name")
	private String name = null;
	
	@JsonProperty("hash_key")
	private ConfigField hashKey = null;
	
	@JsonProperty("sort_key")
	private ConfigField sortKey = null;
	
	@JsonProperty("GSI")
	private boolean gsi = false;
	
	@JsonProperty("read_capacity")
    private int readCapacity = 0;

	@JsonProperty("write_capacity")
    private int writeCapacity = 0;
	
} // end ConfigIndex