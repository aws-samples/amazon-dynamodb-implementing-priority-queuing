package com.awsblog.queueing.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Queue operations return status
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
public enum ReturnStatusEnum {

	NONE,
	SUCCESS,
	FAILED_ID_NOT_PROVIDED,
	FAILED_ID_NOT_FOUND,
	FAILED_RECORD_NOT_CONSTRUCTED,
	FAILED_ON_CONDITION,
	FAILED_EMPTY_QUEUE,
	FAILED_ILLEGAL_STATE,
	FAILED_DYNAMO_ERROR;
	
	/**
	 * Get the error message
	 * 
	 * @return
	 */
	public String getErrorMessage() {
		
		if (this == ReturnStatusEnum.SUCCESS) return "No error";
		else if (this == ReturnStatusEnum.FAILED_ID_NOT_PROVIDED) return "ID was not provided!";
		else if (this == ReturnStatusEnum.FAILED_ID_NOT_FOUND) return "Provided Shipment ID was not found in the Dynamo DB!";
		else if (this == ReturnStatusEnum.FAILED_RECORD_NOT_CONSTRUCTED) return "Shipment record not yet fully constructed .. cannot execute API!";
		else if (this == ReturnStatusEnum.FAILED_ON_CONDITION) return "Condition on the 'version' attribute has failed!";
		else if (this == ReturnStatusEnum.FAILED_EMPTY_QUEUE) return "Cannot proceed, queue is empty!";
		else if (this == ReturnStatusEnum.FAILED_ILLEGAL_STATE) return "Illegal state, cannot proceed!";
		else if (this == ReturnStatusEnum.FAILED_DYNAMO_ERROR) return "Unspecified DynamoDB error is encountered!";
		
		return "API was not called!";
	}

} // end ReturnStatusEnum