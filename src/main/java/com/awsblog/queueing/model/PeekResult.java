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
public class PeekResult extends ReturnResult {

	/**
	 * Default empty c-tor
	 */
	public PeekResult() {
		
		super();
	}

	/**
	 * C-tor
	 */
	public PeekResult(String id) {
		
		super(id);
	}
	
	/**
	 * @return the peekedShipmentObject
	 */
	public Shipment getPeekedShipmentObject() {
		return peekedShipmentObject;
	}

	/**
	 * @param peekedShipmentObject the peekedShipmentObject to set
	 */
	public void setPeekedShipmentObject(Shipment peekedShipmentObject) {
		this.peekedShipmentObject = peekedShipmentObject;
	}	
	
	/**
	 * @return the timestampMillisUTC
	 */
	public long getTimestampMillisUTC() {
		return timestampMillisUTC;
	}

	/**
	 * @param timestampMillisUTC the timestampMillisUTC to set
	 */
	public void setTimestampMillisUTC(long timestampMillisUTC) {
		this.timestampMillisUTC = timestampMillisUTC;
	}

	/**
	 * Get the peeked shipment id
	 * 
	 * @return
	 */
	@JsonProperty("peeked_id")
	public String getPeekedShipmentId() {
		
		if (Utils.checkIfNotNullObject(this.peekedShipmentObject)) 
			return this.peekedShipmentObject.getId();
		
		return "NOT FOUND";
	}

	// ---------------- fields

	@JsonProperty("timestamp_milliseconds_utc")
	private long timestampMillisUTC = 0L;
	
	@JsonIgnore
	private Shipment peekedShipmentObject = null;
	
} // end PeekResult