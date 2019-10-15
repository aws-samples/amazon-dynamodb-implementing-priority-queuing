package com.awsblog.queueing.appdata;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Modeling some type of Shipment data (simulation purpose only)
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
@DynamoDBDocument
public class ShipmentData {

	/**
	 * C-tor
	 */
	public ShipmentData() {
		
		this.items = new ArrayList<>();
	}

	/**
	 * C-tor
	 * 
	 * @param id
	 */
	public ShipmentData(String id) {
		
		this.id = id;
		
		this.items = new ArrayList<>();
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
		this.id = id;
	}

	/**
	 * @return the data1
	 */
	@DynamoDBAttribute(attributeName="data_1")
	public String getData1() {
		return data1;
	}

	/**
	 * @param data1 the data1 to set
	 */
	public void setData1(String data1) {
		this.data1 = data1;
	}

	/**
	 * @return the data2
	 */
	@DynamoDBAttribute(attributeName="data_2")
	public String getData2() {
		return data2;
	}

	/**
	 * @param data2 the data2 to set
	 */
	public void setData2(String data2) {
		this.data2 = data2;
	}

	/**
	 * @return the data3
	 */
	@DynamoDBAttribute(attributeName="data_3") 
	public String getData3() {
		return data3;
	}

	/**
	 * @param data3 the data3 to set
	 */
	public void setData3(String data3) {
		this.data3 = data3;
	}

	/**
	 * @return the items
	 */
	@DynamoDBAttribute(attributeName="items")
	public List<ShipmentItem> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(List<ShipmentItem> items) {
		this.items = items;
	}
	
	// ---------------------- fields

	@JsonProperty("id")
	private String id = null;
	
	@JsonProperty("items")
	private List<ShipmentItem> items = null;
	
	@JsonProperty("data_element_1")
	private String data1 = null;
	
	@JsonProperty("data_element_2")
	private String data2 = null;
	
	@JsonProperty("data_element_3")
	private String data3 = null;

} // end ShipmentData