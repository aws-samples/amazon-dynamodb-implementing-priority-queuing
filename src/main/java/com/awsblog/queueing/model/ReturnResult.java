package com.awsblog.queueing.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Return value base object 
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
public class ReturnResult {

	/**
	 * Default empty c-tor
	 * 
	 * @param id
	 */
	public ReturnResult() {
	
	}

	/**
	 * C-tor
	 * 
	 * @param id
	 */
	public ReturnResult(String id) {
	
		this.id = id;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Was it successful?
	 * 
	 * @return
	 */
	public boolean isSuccessful() {
		
		return this.returnValue == ReturnStatusEnum.SUCCESS;
	}

	/**
	 * Get the error message
	 * @return
	 */
	public String getErrorMessage() {
		
		return this.returnValue.getErrorMessage();
	}
	
	/**
	 * @return the lastUpdatedTimestamp
	 */
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
	 * @return the Id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the status
	 */
	public ReturnStatusEnum getReturnValue() {
		return this.returnValue;
	}

	/**
	 * @param status the status to set
	 */
	public void setReturnValue(ReturnStatusEnum returnValue) {
		this.returnValue = returnValue;
	}

	/**
	 * @return the status
	 */
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
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	// ---------------- fields
	
	@JsonProperty("id")
	private String id = null;
	
	@JsonProperty("return_value")
	private ReturnStatusEnum returnValue = ReturnStatusEnum.NONE;

	@JsonProperty("status")
	private StatusEnum status = StatusEnum.NONE;
	
	@JsonProperty("last_updated_timestamp")
	private String lastUpdatedTimestamp = null;
	
	@JsonProperty("version")
	private int version = 0;	
	
} // end ReturnResult