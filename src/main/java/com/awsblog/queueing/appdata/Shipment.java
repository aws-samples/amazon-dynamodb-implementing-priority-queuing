package com.awsblog.queueing.appdata;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.awsblog.queueing.model.StatusEnum;
import com.awsblog.queueing.model.SystemInfo;
import com.awsblog.queueing.utils.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Shipment object using for education purposes
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
@DynamoDBTable(tableName="Shipment")
public class Shipment {

	/**
	 * C-tor
	 */
	public Shipment() {
		
		this.systemInfo = new SystemInfo();
		this.data = new ShipmentData();
	}

	/**
	 * C-tor
	 * 
	 * @param id
	 */
	public Shipment(String id) {
		
		Utils.throwIfNullOrEmptyString(id, "Shipment ID cannot be null!");
		
		this.id = id.trim();
		
		this.systemInfo = new SystemInfo(this.id);
		this.data = new ShipmentData(this.id);
	}

	/**
	 * @return the id
	 */
	@DynamoDBHashKey(attributeName = "id")
	@DynamoDBAttribute(attributeName="id")
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {

		Utils.throwIfNullOrEmptyString(id, "Shipment ID cannot be null!");
		
		this.id = id.trim();
		this.systemInfo.setId(this.id);
		this.data.setId(this.id);
	}

	/**
	 * Mark the object as a partially constructed
	 */
	public void markAsPartiallyConstructed() {
		
		this.systemInfo.setStatus(StatusEnum.UNDER_CONSTRUCTION);
	}
	
	/**
	 * Mark the object as read for Show
	 */
	public void markAsReadyForShipment() {
		
		this.systemInfo.setStatus(StatusEnum.READY_TO_SHIP);
	}
	
	/**
	 * @param systemInfo the systemInfo to set
	 */
	public void setSystemInfo(SystemInfo systemInfo) {
		this.systemInfo = systemInfo;
	}

	/**
	 * @return the systemInfo
	 */
	@DynamoDBAttribute(attributeName = "system_info")
	public SystemInfo getSystemInfo() {
		return systemInfo;
	}
		
	/**
	 * @return the isQueued
	 */
	@JsonIgnore
	@DynamoDBIgnore
	//@DynamoDBAttribute(attributeName = "queued")
	//@DynamoDBTyped(DynamoDBAttributeType.N)
	public boolean isQueued() {
		return this.systemInfo.isInQueue();
	}
	
	/**
	 * @return the lastUpdatedTimestamp
	 */
	@JsonIgnore
	@DynamoDBAttribute(attributeName = "last_updated_timestamp")
	public String getLastUpdatedTimestamp() {
		return this.systemInfo.getLastUpdatedTimestamp();
	}
	/**
	 * @param lastUpdatedTimestamp the lastUpdatedTimestamp to set
	 */
	public void setLastUpdatedTimestamp(String lastUpdatedTimestamp) {
		this.systemInfo.setLastUpdatedTimestamp(lastUpdatedTimestamp);
	}
	
	/**
	 * Reset the data inside the Shipment's system info object
	 */
	public void resetSystemInfo() {
		
		this.systemInfo = new SystemInfo(this.id);
	}

	/**
	 * @return the data
	 */
	@DynamoDBAttribute(attributeName = "data")
	public ShipmentData getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(ShipmentData data) {
		this.data = data;
	}

	
	// ---------------------- fields

	@JsonProperty("id")
	private String id = null;
	
	@JsonProperty("data")
	private ShipmentData data = null;
		
	@JsonProperty("system_info")
	private SystemInfo systemInfo = null;

} // end Shipment