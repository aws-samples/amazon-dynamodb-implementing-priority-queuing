package com.awsblog.queueing.test;

import com.awsblog.queueing.model.PeekResult;
import com.awsblog.queueing.model.QueueStats;
import com.awsblog.queueing.sdk.QueueSdkClient;
import com.awsblog.queueing.utils.Utils;

/**
 * Simple test app for peeking the record from the queue
 * 
 * @author zorani
 *
 */
public class PeekSDK {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		QueueSdkClient client = new QueueSdkClient.Builder().withCredentialsProfileName("default").withRegion("us-east-1").build();
		
		QueueStats queueStats = client.getQueueStats();
		System.out.println(Utils.toJSON(queueStats));

		PeekResult peek = client.peek();
		System.out.println(Utils.toJSON(peek.getPeekedShipmentId()));
		
		queueStats = client.getQueueStats();
		System.out.println(Utils.toJSON(queueStats));		
	}

} // end TestSDK