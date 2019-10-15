package com.awsblog.queueing.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Modeling table fields
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
public class ConfigField {

	/**
	 * C-tor
	 */
	public ConfigField() {
		// ...
	}
	
	/**
	 * @return the attributeName
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * @param attributeName the attributeName to set
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * @return the attributeType
	 */
	public String getAttributeType() {
		return attributeType;
	}

	/**
	 * @param attributeType the attributeType to set
	 */
	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}

	// ---- fields

	@JsonProperty("attribute_name")
	private String attributeName = null;
	
	@JsonProperty("attribute_type")
	private String attributeType = null;
	
} // end ConfigField