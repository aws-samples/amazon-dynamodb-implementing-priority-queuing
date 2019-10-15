package com.awsblog.queueing.model;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Retrieve DLQ depth statistics
 *
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
public class DLQResult {

	/**
	 * C-tor
	 *
	 * @param BAN
	 */
	public DLQResult() {
		// ...
	}

	/**
	 * Create a RFM_QueueDepthResult object from JSON
	 */
	public static DLQResult createObjectFromJson(String jsonInString) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(jsonInString, DLQResult.class);
	}

	/**
	 * @return the totalRecordsInDLQ
	 */
	public int getTotalRecordsInDLQ() {
		return this.totalRecordsInDLQ;
	}
	/**
	 * @param totalRecordsInDLQ the totalRecordsInDLQ to set
	 */
	public void setTotalRecordsInQueue(int totalRecordsInQueue) {
		this.totalRecordsInDLQ = totalRecordsInQueue;
	}

	/**
	 * @return the first100IDsInQueue
	 */
	public List<String> getFirst100IDsInQueue() {
		return first100IDsInQueue;
	}

	/**
	 * @param first100iDsInQueue the first100IDsInQueue to set
	 */
	public void setFirst100IDsInQueue(List<String> first100iDsInQueue) {
		first100IDsInQueue = first100iDsInQueue;
	}

	/**
	 * toString
	 */
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Queue statistics:%n"));

		sb.append(String.format(" >> Total records in DLQ: %d%n", this.totalRecordsInDLQ));

		if (!this.first100IDsInQueue.isEmpty()) {

			sb.append("   >>> BANs in DLQ: { ");
			int count = 0;
			for(String ban : this.first100IDsInQueue) {

				if (count++ > 0) sb.append(", ");
				sb.append(ban);
			}
			sb.append(String.format(" }%n"));
		}

		return sb.toString();
	}

	// ---------------- fields
	
	@JsonProperty("first_100_IDs_in_queue")
	private List<String> first100IDsInQueue = null;

	@JsonProperty("total_records_in_DLQ")
	private int totalRecordsInDLQ = 0;

} // end DLQResult