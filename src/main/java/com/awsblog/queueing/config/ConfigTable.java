package com.awsblog.queueing.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * DynamoDB table configuration parameters
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
public class ConfigTable {

	/**
	 * C-tor
	 */
	public ConfigTable() {
		// ...
	}
	
	/**
	 * @return the logicalName
	 */
	public String getLogicalName() {
		return logicalName;
	}
	/**
	 * @param logicalName the logicalName to set
	 */
	public void setLogicalName(String logicalName) {
		this.logicalName = logicalName;
	}
	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
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
	 * @return the partitionKey
	 */
	public ConfigField getPartitionKey() {
		return partitionKey;
	}

	/**
	 * @param partitionKey the partitionKey to set
	 */
	public void setPartitionKey(ConfigField partitionKey) {
		this.partitionKey = partitionKey;
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
	 * @return the indexes
	 */
	public List<ConfigIndex> getIndexes() {
		return indexes;
	}
	/**
	 * @param indexes the indexes to set
	 */
	public void setIndexes(List<ConfigIndex> indexes) {
		this.indexes = indexes;
	}

	// ----- fields

	@JsonProperty("logical_name")
    private String logicalName = null;
	
	@JsonProperty("table_name")
    private String tableName = null;
	
	@JsonProperty("read_capacity")
    private int readCapacity = 0;

	@JsonProperty("write_capacity")
    private int writeCapacity = 0;

	@JsonProperty("partition_key")
    private ConfigField partitionKey = null;
	
	@JsonProperty("sort_key")
    private ConfigField sortKey = null;

	@JsonProperty("indexes")
    private List<ConfigIndex> indexes = null;
	
} // end ConfigTable