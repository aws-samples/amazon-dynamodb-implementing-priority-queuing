package com.awsblog.queueing.model;

import com.awsblog.queueing.appdata.Shipment;
import com.awsblog.queueing.utils.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Result for the peek() API call
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
public class DequeueResult extends ReturnResult {

	/**
	 * Default empty c-tor
	 */
	public DequeueResult() {
		
		super();
	}

	/**
	 * C-tor
	 * 
	 * @param id
	 */
	public DequeueResult(String id) {
		
		super(id);
	}
	
	/**
	 * Get the values from ReturnResult object
	 * 
	 * @param result
	 * @return
	 */
	public static DequeueResult fromReturnResult(ReturnResult result) {
		
		Utils.throwIfNullObject(result, "Resulting object is NULL!");
		
		DequeueResult dequeueResult = new DequeueResult(result.getId());
		dequeueResult.setLastUpdatedTimestamp(result.getLastUpdatedTimestamp());
		dequeueResult.setStatus(result.getStatus());
		dequeueResult.setVersion(result.getVersion());
		dequeueResult.setReturnValue(result.getReturnValue());
		
		return dequeueResult;
	}
		
	/**
	 * @return the dequeuedShipmentObject
	 */
	public Shipment getDequeuedShipmentObject() {
		return dequeuedShipmentObject;
	}

	/**
	 * @param dequeuedShipmentObject the dequeuedShipmentObject to set
	 */
	public void setDequeuedShipmentObject(Shipment dequeuedShipmentObject) {
		this.dequeuedShipmentObject = dequeuedShipmentObject;
	}

	/**
	 * Get the peeked shipment id
	 * 
	 * @return
	 */
	@JsonProperty("dequeue_id")
	public String getPeekedShipmentId() {
		
		if (Utils.checkIfNotNullObject(this.dequeuedShipmentObject)) 
			return this.dequeuedShipmentObject.getId();
		
		return "NOT FOUND";
	}

	// ---------------- fields

	@JsonIgnore
	private Shipment dequeuedShipmentObject = null;
	
} // end DequeueResult