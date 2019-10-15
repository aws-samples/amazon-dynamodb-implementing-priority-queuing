package com.awsblog.queueing.model;

import com.awsblog.queueing.appdata.Shipment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Result for the enqueue() API call
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
public class EnqueueResult extends ReturnResult {

	/**
	 * Default empty c-tor
	 */
	public EnqueueResult() {
		
		super();
	}

	/**
	 * C-tor
	 */
	public EnqueueResult(String id) {
		
		super(id);
	}
	
	/**
	 * @return the shipment
	 */
	public Shipment getShipment() {
		return shipment;
	}

	/**
	 * @param shipment the shipment to set
	 */
	public void setShipment(Shipment shipment) {
		this.shipment = shipment;
	}

	// ---------------- fields

	@JsonIgnore
	private Shipment shipment = null;
	
} // end EnqueueResult