package com.awsblog.queueing.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * All possible status values
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
@DynamoDBDocument
public enum StatusEnum {

	NONE,
	UNDER_CONSTRUCTION,
	READY_TO_SHIP,
	PROCESSING_SHIPMENT,
	COMPLETED,
	IN_DLQ;
		
} // end StatusEnum