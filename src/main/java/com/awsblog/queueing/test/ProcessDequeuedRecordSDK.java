package com.awsblog.queueing.test;

import com.awsblog.queueing.appdata.Shipment;
import com.awsblog.queueing.model.PeekResult;
import com.awsblog.queueing.model.QueueStats;
import com.awsblog.queueing.model.ReturnResult;
import com.awsblog.queueing.sdk.QueueSdkClient;
import com.awsblog.queueing.utils.Utils;

/**
 * Testing dequeuing - peek and remove
 * 
 * @author zorani
 *
 */
public class ProcessDequeuedRecordSDK {

	/**
	 * Test main 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		QueueSdkClient client = new QueueSdkClient.Builder().withCredentialsProfileName("default").withRegion("us-east-1").build();
		
		QueueStats queueStats = client.getQueueStats();
		System.out.println(Utils.toJSON(queueStats));

		PeekResult peek = client.peek();
		System.out.println(Utils.toJSON(peek.getPeekedShipmentId()));
		
		Shipment shipment = peek.getPeekedShipmentObject();
		if (Utils.checkIfNullObject(shipment)) {
			
			System.out.println("Nothing to peek() from the queue!");
			System.exit(1);
		}
		
		String ID = shipment.getId();
		
		ReturnResult result = client.remove(ID);
		if (!result.isSuccessful()) {
			System.out.println("remove() on ID [" + ID + "] has failed!");
			System.exit(1);
		}
		
		queueStats = client.getQueueStats();
		System.out.println(Utils.toJSON(queueStats));
	}

} // end ProcessDequeuedRecordSDK