package com.awsblog.queueing.appdata;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Model an order item that will be shipped
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
@DynamoDBDocument
public class ShipmentItem {

	/**
	 * C-tor
	 */
	public ShipmentItem() {
		// ...
	}
	
	/**
	 * C-tor
	 * 
	 * @param sku
	 * @param isPacked
	 */
	public ShipmentItem(String sku, boolean isPacked) {
		
		this.SKU = sku;
		this.packed = isPacked;
	}
	
	/**
	 * @return the sKU
	 */
	@JsonIgnore
	@DynamoDBAttribute(attributeName = "SKU")
	public String getSKU() {
		return SKU;
	}

	/**
	 * @param sKU the sKU to set
	 */
	public void setSKU(String sku) {
		this.SKU = sku;
	}

	/**
	 * @return the packed
	 */
	@JsonIgnore
	@DynamoDBAttribute(attributeName = "is_packed")
	@DynamoDBTyped(DynamoDBAttributeType.BOOL)
	public boolean isPacked() {
		return packed;
	}

	/**
	 * @param packed the packed to set
	 */
	public void setPacked(boolean packed) {
		this.packed = packed;
	}

	// ------------- fields

	@JsonProperty("SKU")
	private String SKU = null;
	
	@JsonProperty("is_packed")
	private boolean packed = false;
	
} // end ShipmentItem