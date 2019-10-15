package com.awsblog.queueing.model;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Retrieve various queue depth statistics
 *
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
public class QueueStats {

	/**
	 * C-tor
	 *
	 * @param id
	 */
	public QueueStats() {
		
		// ...
	}

	/**
	 * Create a RFM_QueueDepthResult object from JSON
	 */
	public static QueueStats createObjectFromJson(String jsonInString) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(jsonInString, QueueStats.class);
	}

	/**
	 * @return the totalRecordsInQueue
	 */
	public int getTotalRecordsInQueue() {
		return totalRecordsInQueue;
	}
	/**
	 * @param totalRecordsInQueue the totalRecordsInQueue to set
	 */
	public void setTotalRecordsInQueue(int totalRecordsInQueue) {
		this.totalRecordsInQueue = totalRecordsInQueue;
	}
	/**
	 * @return the totalRecordsInProcessing
	 */
	public int getTotalRecordsInProcessing() {
		return totalRecordsInProcessing;
	}
	/**
	 * @param totalRecordsInProcessing the totalRecordsInProcessing to set
	 */
	public void setTotalRecordsInProcessing(int totalRecordsInProcessing) {
		this.totalRecordsInProcessing = totalRecordsInProcessing;
	}
	/**
	 * @return the totalRecordsNotStarted
	 */
	public int getTotalRecordsNotStarted() {
		return totalRecordsNotStarted;
	}
	/**
	 * @param totalRecordsNotStarted the totalRecordsNotStarted to set
	 */
	public void setTotalRecordsNotStarted(int totalRecordsNotStarted) {
		this.totalRecordsNotStarted = totalRecordsNotStarted;
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
	 * @return the first100SelectedIDsInQueue
	 */
	public List<String> getFirst100SelectedIDsInQueue() {
		return first100SelectedIDsInQueue;
	}

	/**
	 * @param first100SelectedIDsInQueue the first100SelectedIDsInQueue to set
	 */
	public void setFirst100SelectedIDsInQueue(List<String> first100SelectedIDsInQueue) {
		this.first100SelectedIDsInQueue = first100SelectedIDsInQueue;
	}

	/**
	 * toString
	 */
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Queue statistics:%n"));

		sb.append(String.format(" >> Total records in the queue: %d%n", this.getTotalRecordsInQueue()));

		if (!this.first100IDsInQueue.isEmpty()) {

			sb.append("   >>> IDs in the queue: { ");
			int count = 0;
			for(String id : this.first100IDsInQueue) {

				if (count++ > 0) sb.append(", ");
				sb.append(id);
			}
			sb.append(String.format(" }%n"));
		}

		sb.append(String.format("   >>> Records in processing: %d%n", this.getTotalRecordsInProcessing()));
		if (!this.first100SelectedIDsInQueue.isEmpty()) {

			sb.append("   >>> Selected IDs in the queue: { ");
			int count = 0;
			for(String id : this.first100SelectedIDsInQueue) {

				if (count++ > 0) sb.append(", ");
				sb.append(id);
			}
			sb.append(String.format(" }%n"));
		}

		sb.append(String.format("   >>> Records in queue but not in processing: %d%n", this.getTotalRecordsNotStarted()));

		return sb.toString();
	}

	// ---------------- fields
	
	@JsonProperty("first_100_IDs_in_queue")
	private List<String> first100IDsInQueue = null;
	@JsonProperty("first_100_selected_IDs_in_queue")
	private List<String> first100SelectedIDsInQueue = null;

	@JsonProperty("total_records_in_queue")
	private int totalRecordsInQueue = 0;
	@JsonProperty("total_records_in_queue_selected_for_processing")
	private int totalRecordsInProcessing = 0;
	@JsonProperty("total_records_in_queue_pending_for_processing")
	private int totalRecordsNotStarted = 0;

} // end QueueStats