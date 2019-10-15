package com.awsblog.queueing.test;

import java.util.Arrays;

import com.awsblog.queueing.appdata.ShipmentItem;
import com.awsblog.queueing.appdata.Shipment;
import com.awsblog.queueing.appdata.ShipmentData;
import com.awsblog.queueing.model.EnqueueResult;
import com.awsblog.queueing.model.PeekResult;
import com.awsblog.queueing.model.QueueStats;
import com.awsblog.queueing.model.StatusEnum;
import com.awsblog.queueing.sdk.QueueSdkClient;
import com.awsblog.queueing.utils.Utils;

/**
 * Test SDK
 * 
 * @author zorani
 *
 */
public class TestSDK {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String ID = "A-101";
		ShipmentData data = new ShipmentData(ID);
		data.setData1("Data 1"); data.setData2("Data 2"); data.setData3("Data 3");
		data.setItems(Arrays.asList(new ShipmentItem("Item-1", true), new ShipmentItem("Item-2", false)));
		
		Shipment shipment = new Shipment();
		shipment.setId(ID);		
		shipment.setData(data);
		
		QueueSdkClient client = new QueueSdkClient.Builder()
									.withCredentialsProfileName("default")
									.withRegion("us-east-1").build();
		client.put(shipment);
		
		QueueStats queueStats = client.getQueueStats();
		System.out.println(Utils.toJSON(queueStats));

		Shipment retrievedShipment = client.get("A-101");
		System.out.println(Utils.toJSON(retrievedShipment));
		
		EnqueueResult result = client.enqueue("A-101");
		System.out.println(Utils.toJSON(result));
		
		if (!result.isSuccessful()) {
			
			client.updateStatus("A-101", StatusEnum.READY_TO_SHIP);
			client.enqueue("A-101");
		}
		
		Shipment enqueuedShipment = result.getShipment();
		System.out.println(Utils.toJSON(enqueuedShipment));

		queueStats = client.getQueueStats();
		System.out.println(Utils.toJSON(queueStats));
		
		PeekResult peek = client.peek();
		System.out.println(Utils.toJSON(peek.getPeekedShipmentId()));
	}

} // end TestSDK