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
 * Create multiple Shipment objects and store it in the database (remove previous copy, if exists)
 * 
 * @author zorani
 *
 */
public class MultipleObjectsTest {

	/**
	 * The main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		QueueSdkClient client = new QueueSdkClient.Builder()
				.withCredentialsProfileName("default")
				.withRegion("us-east-1")
				.build();
		
		String id1 = "A-101";
		ShipmentData data = new ShipmentData(id1);
		data.setData1("Data 1"); data.setData2("Data 2"); data.setData3("Data 3");
		data.setItems(Arrays.asList(new ShipmentItem("Item-1", true), new ShipmentItem("Item-2", false)));
		
		Shipment shipment1 = new Shipment(id1);
		shipment1.setData(data);
		client.put(shipment1);
		
		String id2 = "A-202";
		ShipmentData data2 = new ShipmentData(id2);
		data2.setData1("Data 1"); data2.setData2("Data 2"); data2.setData3("Data 3");
		data2.setItems(Arrays.asList(new ShipmentItem("Item-1", true), new ShipmentItem("Item-2", false)));
		Shipment shipment2 = new Shipment(id2);
		shipment2.setData(data2);
		client.put(shipment2);

		String id3 = "A-303";
		ShipmentData data3 = new ShipmentData(id3);
		data3.setData1("Data 1"); data3.setData2("Data 2"); data3.setData3("Data 3");
		data3.setItems(Arrays.asList(new ShipmentItem("Item-1", true), new ShipmentItem("Item-2", false)));
		Shipment shipment3 = new Shipment(id3);
		shipment3.setData(data3);
		client.put(shipment3);

		QueueStats queueStats = client.getQueueStats();
		System.out.println(Utils.toJSON(queueStats));

		Shipment sh1 = client.get(id1);
		System.out.println(Utils.toJSON(sh1));		

		Shipment sh2 = client.get(id2);
		System.out.println(Utils.toJSON(sh2));		
		
		Shipment sh3 = client.get(id3);
		System.out.println(Utils.toJSON(sh3));		

		// ----------------------------------------------------

		System.out.println("====================> Start enqueue process <=====================");
		
		client.updateStatus(id1, StatusEnum.READY_TO_SHIP);
		EnqueueResult er = client.enqueue(id1);
		System.out.println(Utils.toJSON(er));
		
		queueStats = client.getQueueStats();
		System.out.println(Utils.toJSON(queueStats));		

		client.updateStatus(id2, StatusEnum.READY_TO_SHIP);
		er = client.enqueue(id2);
		System.out.println(Utils.toJSON(er));

		queueStats = client.getQueueStats();
		System.out.println(Utils.toJSON(queueStats));		

		client.updateStatus(id3, StatusEnum.READY_TO_SHIP);
		er = client.enqueue(id3);
		System.out.println(Utils.toJSON(er));

		queueStats = client.getQueueStats();
		System.out.println(Utils.toJSON(queueStats));		

		// ----------------------------------------------------

		System.out.println("====================> PEEK <=====================");
		
		PeekResult peek = client.peek();
		System.out.println(Utils.toJSON(peek.getPeekedShipmentId()));
		
		queueStats = client.getQueueStats();
		System.out.println(Utils.toJSON(queueStats));
		
		peek = client.peek();
		System.out.println(Utils.toJSON(peek.getPeekedShipmentId()));
		
		queueStats = client.getQueueStats();
		System.out.println(Utils.toJSON(queueStats));		
	}

} // end MultipleObjectsTest