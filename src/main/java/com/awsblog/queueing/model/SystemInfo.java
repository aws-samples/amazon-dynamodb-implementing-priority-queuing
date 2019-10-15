package com.awsblog.queueing.model;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Model all system info for the Shipment queuing
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
@DynamoDBDocument
public class SystemInfo {

	/**
	 * Default C-tor
	 */
	public SystemInfo() {
		
		OffsetDateTime odt = OffsetDateTime.now(ZoneOffset.UTC);
		
		// creation should be overwrote with the real value from DDB
		this.creationTimestamp = odt.toString();
		this.lastUpdatedTimestamp = odt.toString();
	}
	
	/**
	 * C-tor
	 * 
	 * @param id
	 */
	public SystemInfo(String id) {
		
		this.id = id;

		OffsetDateTime odt = OffsetDateTime.now(ZoneOffset.UTC);

		// creation should be overwrote with the real value from DDB
		this.creationTimestamp = odt.toString();
		this.lastUpdatedTimestamp = odt.toString();
	}
	
	/**
	 * @return the creationTimestamp
	 */
	@DynamoDBAttribute(attributeName = "creation_timestamp")
	public String getCreationTimestamp() {
		return creationTimestamp;
	}
	/**
	 * @param creationTimestamp the creationTimestamp to set
	 */
	public void setCreationTimestamp(String creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	/**
	 * @return the lastUpdatedTimestamp
	 */
	@DynamoDBAttribute(attributeName = "last_updated_timestamp")
	public String getLastUpdatedTimestamp() {
		return lastUpdatedTimestamp;
	}
	/**
	 * @param lastUpdatedTimestamp the lastUpdatedTimestamp to set
	 */
	public void setLastUpdatedTimestamp(String lastUpdatedTimestamp) {
		this.lastUpdatedTimestamp = lastUpdatedTimestamp;
	}
	
	/**
	 * @return the version
	 */
	@DynamoDBAttribute(attributeName = "version")
	@DynamoDBVersionAttribute
	public int getVersion() {
		return version;
	}
	
	/**
	 * @return the inQueue
	 */
	@DynamoDBAttribute(attributeName = "queued")
	public boolean isInQueue() {
		return inQueue;
	}

	/**
	 * @param inQueue the inQueue to set
	 */
	public void setInQueue(boolean inQueue) {
		this.inQueue = inQueue;
	}	
	
	/**
	 * @return the selectedFromQueue
	 */
	@DynamoDBAttribute(attributeName = "queue_selected")
	@DynamoDBTyped(DynamoDBAttributeType.BOOL)
	public boolean isSelectedFromQueue() {
		return selectedFromQueue;
	}

	/**
	 * @param selectedFromQueue the selectedFromQueue to set
	 */
	public void setSelectedFromQueue(boolean selectedFromQueue) {
		this.selectedFromQueue = selectedFromQueue;
	}

	
	/**
	 * @return the addToQueueTimestamp
	 */
	@DynamoDBAttribute(attributeName = "queue_add_timestamp")
	public String getAddToQueueTimestamp() {
		return addToQueueTimestamp;
	}

	/**
	 * @param addToQueueTimestamp the addToQueueTimestamp to set
	 */
	public void setAddToQueueTimestamp(String addToQueueTimestamp) {
		this.addToQueueTimestamp = addToQueueTimestamp;
	}

	/**
	 * @return the peekFromQueueTimestamp
	 */
	@DynamoDBAttribute(attributeName = "queue_peek_timestamp")
	public String getPeekFromQueueTimestamp() {
		return peekFromQueueTimestamp;
	}

	/**
	 * @param peekFromQueueTimestamp the peekFromQueueTimestamp to set
	 */
	public void setPeekFromQueueTimestamp(String peekFromQueueTimestamp) {
		this.peekFromQueueTimestamp = peekFromQueueTimestamp;
	}

	/**
	 * @return the removeFromQueueTimestamp
	 */
	@DynamoDBAttribute(attributeName = "queue_remove_timestamp")
	public String getRemoveFromQueueTimestamp() {
		return removeFromQueueTimestamp;
	}

	/**
	 * @param removeFromQueueTimestamp the removeFromQueueTimestamp to set
	 */
	public void setRemoveFromQueueTimestamp(String removeFromQueueTimestamp) {
		this.removeFromQueueTimestamp = removeFromQueueTimestamp;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the status
	 */
	@DynamoDBAttribute(attributeName = "status")
	@DynamoDBTyped(DynamoDBAttributeType.S)
	public StatusEnum getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String statusStr) {
		this.status = StatusEnum.valueOf(statusStr.toUpperCase());
	}

	/**
	 * @return the addToDlqTimestamp
	 */
	@DynamoDBAttribute(attributeName = "dlq_add_timestamp")
	public String getAddToDlqTimestamp() {
		return addToDlqTimestamp;
	}

	/**
	 * @param addToDlqTimestamp the addToDlqTimestamp to set
	 */
	public void setAddToDlqTimestamp(String addToDlqTimestamp) {
		this.addToDlqTimestamp = addToDlqTimestamp;
	}

	/**
	 * @return the ID
	 */
	@JsonIgnore	
	@DynamoDBAttribute(attributeName = "id")
	public String getId() {
		return id;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	
	// ------------------------ fields

	@JsonProperty("id")
	private String id = null;
	
	@JsonProperty("creation_timestamp")
	private String creationTimestamp = null;

	@JsonProperty("last_updated_timestamp")
	private String lastUpdatedTimestamp = null;

	@JsonProperty("status")
	private StatusEnum status = StatusEnum.UNDER_CONSTRUCTION;
	
	@JsonProperty("version")
	private int version = 1;
		
	@JsonProperty("queued")
	private boolean inQueue = false;
	
	@JsonProperty("queue_selected")  
	private boolean selectedFromQueue = false;
	
	@JsonProperty("queue_add_timestamp")
	private String addToQueueTimestamp = null;
	
	@JsonProperty("dlq_add_timestamp")
	private String addToDlqTimestamp = null;

	@JsonProperty("queue_peek_timestamp")
	private String peekFromQueueTimestamp = null;
	
	@JsonProperty("queue_remove_timestamp")
	private String removeFromQueueTimestamp = null;	
		
} // end SystemInfo