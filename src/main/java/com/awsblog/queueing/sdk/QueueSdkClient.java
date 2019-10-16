package com.awsblog.queueing.sdk;

import static com.amazonaws.retry.PredefinedRetryPolicies.DYNAMODB_DEFAULT_BACKOFF_STRATEGY;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.ScanResultPage;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.awsblog.queueing.Constants;
import com.awsblog.queueing.appdata.Shipment;
import com.awsblog.queueing.appdata.ShipmentData;
import com.awsblog.queueing.appdata.ShipmentItem;
import com.awsblog.queueing.config.ConfigField;
import com.awsblog.queueing.config.Configuration;
import com.awsblog.queueing.model.DLQResult;
import com.awsblog.queueing.model.DequeueResult;
import com.awsblog.queueing.model.EnqueueResult;
import com.awsblog.queueing.model.PeekResult;
import com.awsblog.queueing.model.QueueStats;
import com.awsblog.queueing.model.ReturnResult;
import com.awsblog.queueing.model.ReturnStatusEnum;
import com.awsblog.queueing.model.StatusEnum;
import com.awsblog.queueing.model.SystemInfo;
import com.awsblog.queueing.utils.Utils;

/**
 * Queue SDK client
 * 
 * @author zorani
 * @version 1.00
 *
 */
public class QueueSdkClient {
	
	/**
	 * Private constructor using Builder
	 * 
	 * @param builder Builder object reference
	 */
	private QueueSdkClient(Builder builder) {

		this.logicalTableName = builder.logicalTableName;

		this.awsRegion = builder.awsRegion;
		this.credentials = builder.credentials;
		this.awsCredentialsProfileName = builder.awsCredentialsProfileName;
	}

	/**
	 * Part of the fluid API chaining. Once all parameters are set with .with(...) methods, build() method needs to be called. 
	 * No other client call can be used before build() call was executed.
	 * 
	 * @return QueueSdkClient
	 */
	private QueueSdkClient initialize() {
		
		// for this AWS Blog Post we are doing string comparison using English locale
		Locale.setDefault(Locale.ENGLISH);

		String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
		String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");

		if (Utils.checkIfNotNullAndNotEmptyString(accessKey) && Utils.checkIfNotNullAndNotEmptyString(secretKey)) {

			if (Utils.checkIfNullOrEmptyString(accessKey))
				accessKey = System.getenv("AWS_ACCESS_KEY_ID");

			if (Utils.checkIfNullOrEmptyString(secretKey))
				secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");

			this.credentials = new BasicAWSCredentials(accessKey, secretKey);
		}
		else if (Utils.checkIfNotNullAndNotEmptyString(this.awsCredentialsProfileName)) {

			this.credentials = new ProfileCredentialsProvider(this.awsCredentialsProfileName).getCredentials();
		}

		AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
		if (!Utils.checkIfNullObject(this.credentials)) builder.withCredentials(new AWSStaticCredentialsProvider(this.credentials));
		if (!Utils.checkIfNullObject(this.awsRegion)) builder.withRegion(this.awsRegion);

		this.dynamoDB = builder
				.withClientConfiguration(new ClientConfiguration().withMaxConnections(100).withConnectionTimeout(30000).withRetryPolicy(RETRY_POLICY)).build();
		
		// get the configuration information
		this.config = Configuration.loadConfiguration();
		
		this.actualTableName = this.config.getTablesMap().get(this.logicalTableName).getTableName();
		Utils.throwIfNullOrEmptyString(this.actualTableName, "Actual DynamoDB table name is not found!");
		
		this.key = this.config.getTablesMap().get(this.logicalTableName).getPartitionKey();
		
		AmazonS3ClientBuilder s3builder = AmazonS3ClientBuilder.standard();
		if (!Utils.checkIfNullObject(this.credentials)) s3builder.withCredentials(new AWSStaticCredentialsProvider(this.credentials));
		if (!Utils.checkIfNullObject(this.awsRegion)) s3builder.withRegion(this.awsRegion);
		this.s3 = s3builder.build();
		
		AmazonSNSClientBuilder snsBuilder = AmazonSNSClientBuilder.standard();
		if (!Utils.checkIfNullObject(this.credentials)) snsBuilder.withCredentials(new AWSStaticCredentialsProvider(this.credentials));
		if (!Utils.checkIfNullObject(this.awsRegion)) snsBuilder.withRegion(this.awsRegion);
		this.sns = snsBuilder.build();
		
		DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
		        .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.CLOBBER)
		        .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
		        //.withConsistentReads(DynamoDBMapperConfig.ConsistentReads.EVENTUAL)
		        .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(this.actualTableName))
		        .withPaginationLoadingStrategy(DynamoDBMapperConfig.PaginationLoadingStrategy.EAGER_LOADING)
		    .build();

		this.dbMapper = new DynamoDBMapper(this.dynamoDB, mapperConfig);		
		
		return this;
	}
	
	/**
	 * Get the queue depth statistics
	 *
	 * @return
	 */
	public QueueStats getQueueStats() {

		int totalQueueSize = 0;

		Map<String,AttributeValue> exclusiveStartKey = null;

		Map<String,String> names = new HashMap<>();
		names.put("#q", "queued");
		Map<String,AttributeValue> values = new HashMap<>();
		values.put(":one", new AttributeValue().withN("1"));

		int peekedRecords = 0;

		List<String> allQueueIDs = new ArrayList<>();
		List<String> processingIDs = new ArrayList<>();

		do {

			QueryRequest queryRequest = new QueryRequest()
					.withProjectionExpression("id, system_info")
					.withIndexName(Constants.QUEUEING_INDEX_NAME)
					.withTableName(this.actualTableName)
					.withExpressionAttributeNames(names)
					.withKeyConditionExpression("#q = :one")
					.withScanIndexForward(true)
					.withLimit(250)
					.withExpressionAttributeValues(values);

			queryRequest.withExclusiveStartKey(exclusiveStartKey);

			QueryResult queryResult = this.dynamoDB.query(queryRequest);
			exclusiveStartKey = queryResult.getLastEvaluatedKey();

			for(Map<String,AttributeValue> itemMap : queryResult.getItems()) {

		    	++totalQueueSize;

		    	Map<String,AttributeValue> sysMap = itemMap.get("system_info").getM();

		    	boolean isQueueSelected = false;
		    	if (sysMap.containsKey("queue_selected")) isQueueSelected = sysMap.get("queue_selected").getBOOL();

		    	if (isQueueSelected) {

		    		++peekedRecords;
		    		if (processingIDs.size() < 100) processingIDs.add(itemMap.get("id").getS());
		    	}

			    if (allQueueIDs.size() < 100) allQueueIDs.add(itemMap.get("id").getS());
			}

		} while (exclusiveStartKey != null);

		QueueStats result = new QueueStats();
		result.setTotalRecordsInProcessing(peekedRecords);
		result.setTotalRecordsInQueue(totalQueueSize);
		result.setTotalRecordsNotStarted(totalQueueSize - peekedRecords);
		if (Utils.checkIfNotNullAndNotEmptyCollection(allQueueIDs)) result.setFirst100IDsInQueue(allQueueIDs);
		if (Utils.checkIfNotNullAndNotEmptyCollection(processingIDs)) result.setFirst100SelectedIDsInQueue(processingIDs);

		return result;
	}	
	
	/**
	 * Retrieve DLQ information
	 * 
	 * @return
	 */
	public DLQResult getDLQStats() {

		long startTime = System.currentTimeMillis();

		int totalDLQSize = 0;

		Map<String,AttributeValue> exclusiveStartKey = null;

		Map<String,String> names = new HashMap<>();
		names.put("#DLQ", "DLQ");

		Map<String,AttributeValue> values = new HashMap<>();
		values.put(":one", new AttributeValue().withN("1"));

		List<String> listBANs = new ArrayList<>();

		do {

			QueryRequest queryRequest = new QueryRequest()
					.withProjectionExpression("id, DLQ, system_info")
					.withIndexName(Constants.DLQ_QUEUEING_INDEX_NAME)
					.withTableName(this.actualTableName)
					.withExpressionAttributeNames(names)
					.withKeyConditionExpression("#DLQ = :one")
					.withLimit(250)
					.withExpressionAttributeValues(values);

			queryRequest.withExclusiveStartKey(exclusiveStartKey);

			QueryResult queryResult = this.dynamoDB.query(queryRequest);
			exclusiveStartKey = queryResult.getLastEvaluatedKey();

			for(Map<String,AttributeValue> itemMap : queryResult.getItems()) {

				++totalDLQSize;

				if (listBANs.size() < 100) listBANs.add(itemMap.get("id").getS());
			}

		} while (exclusiveStartKey != null);

		DLQResult result = new DLQResult();
		result.setFirst100IDsInQueue(listBANs);
		result.setTotalRecordsInQueue(totalDLQSize);

		return result;
	}	
	
	/**
	 * Get the Shipment object/record from DynamoDB
	 *
	 * @param id
	 * @return
	 */
	public Shipment get(String id) {

		if (Utils.checkIfNullOrEmptyString(id)) {

			System.out.printf("ID is not provided ... cannot retrieve the shipment record!%n");
			return null;
		}

		return this.dbMapper.load(Shipment.class, id.trim());
	}
	
	/**
	 * Put the new object into DynamoDB - replaces values, if there is already data with the same primary key
	 *
	 * @param shipment
	 */
	public void put(Shipment shipment) {
		
		this.putImpl(shipment, false);
	}	
	
	/**
	 * Put & Merge object in the DynamoDB
	 *
	 * @param shipment
	 */
	public void upsert(Shipment shipment) {
		
		this.putImpl(shipment, true);
	}	

	/**
	 * Put/Replace the new object into DynamoDB
	 *
	 * @param shipment
	 */
	private void putImpl(Shipment shipment, boolean useUpsert) {

		Utils.throwIfNullObject(shipment, "Shipment object cannot be NULL!");

		int version = 0;

		// check if already present
		Shipment retrievedShipment = this.dbMapper.load(Shipment.class, shipment.getId());
		if (!Utils.checkIfNullObject(retrievedShipment)) {
			
			if (useUpsert) {
				version = retrievedShipment.getSystemInfo().getVersion();
			}
			else {
				this.dbMapper.delete(retrievedShipment);
			}
		}

		OffsetDateTime odt = OffsetDateTime.now(ZoneOffset.UTC);

		SystemInfo system = new SystemInfo(shipment.getId());
		system.setInQueue(false);
		system.setSelectedFromQueue(false);
		system.setStatus(shipment.getSystemInfo().getStatus());
		system.setCreationTimestamp(odt.toString());
		system.setLastUpdatedTimestamp(odt.toString());
		system.setVersion(version + 1);

		shipment.setSystemInfo(system);

		// store it in DynamoDB
		this.dbMapper.save(shipment);
	}
		
	/**
	 * Method for changing the status of the record 
	 * This call should not be used unless there are operational issues and there are live issues that needs to be resolved. 
	 * 
	 * @param id
	 * @param newStatus
	 */
	public ReturnResult updateStatus(String id, StatusEnum newStatus) {
	
		ReturnResult result = new ReturnResult(id);
		
		if (Utils.checkIfNullOrEmptyString(id)) {

			System.out.printf("ERROR: ID is not provided ... cannot retrieve the record!%n");
			result.setReturnValue(ReturnStatusEnum.FAILED_ID_NOT_FOUND);
			return result;
		}

		Map<String,AttributeValue> key = new HashMap<>();
		key.put("id", new AttributeValue().withS(id));

		DynamoDB ddb = new DynamoDB(this.dynamoDB);
		Table table = ddb.getTable(this.actualTableName);

		Shipment shipment = this.get(id);

		if (Utils.checkIfNullObject(shipment)) {

			System.out.printf("ERROR: Customer with ID [%s] cannot be found!%n", id);
			result.setReturnValue(ReturnStatusEnum.FAILED_ID_NOT_FOUND);
			return result;
		}

		StatusEnum prevStatus = shipment.getSystemInfo().getStatus();
		int version = shipment.getSystemInfo().getVersion();

		result.setStatus(newStatus);

		if (prevStatus == newStatus) {
            result.setVersion(version);
            result.setLastUpdatedTimestamp(shipment.getLastUpdatedTimestamp());
			result.setReturnValue(ReturnStatusEnum.SUCCESS);
			return result;
		}
		
		OffsetDateTime odt = OffsetDateTime.now(ZoneOffset.UTC);

        try {

            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", id)
                .withUpdateExpression("ADD #sys.#v :inc SET #sys.#st = :st, #sys.last_updated_timestamp = :lut, last_updated_timestamp = :lut")
                .withNameMap(new NameMap()
                		.with("#v", "version")
                		.with("#st", "status")
                		.with("#sys", "system_info"))
                .withValueMap(
                    new ValueMap()
                    	.withInt(":inc", 1)
                    	.withInt(":v", version)
                    	.withString(":lut", odt.toString())
                    	.withString(":st", newStatus.toString()))
                .withConditionExpression("#sys.#v = :v")
                .withReturnValues(ReturnValue.ALL_NEW);

            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);

            Map<String, Object> sysMap = outcome.getItem().getRawMap("system_info");
            result.setStatus(StatusEnum.valueOf((String)sysMap.get("status")));
            result.setVersion(((BigDecimal)sysMap.get("version")).intValue());
            result.setLastUpdatedTimestamp((String)sysMap.get("last_updated_timestamp"));
        }
        catch (Exception e) {
            System.err.println("updateFullyConstructedFlag() - failed to update multiple attributes in " + this.actualTableName);
            System.err.println(e.getMessage());

            result.setReturnValue(ReturnStatusEnum.FAILED_DYNAMO_ERROR);
    		return result;
        }

        result.setReturnValue(ReturnStatusEnum.SUCCESS);
		return result;
	}	
	
	/**
	 * enqueue() the record into the queue
	 *
	 * @param id
	 * @return
	 */
	public EnqueueResult enqueue(String id) {

		EnqueueResult result = new EnqueueResult(id);

		if (Utils.checkIfNullOrEmptyString(id)) {

			System.out.printf("ID is not provided ... cannot proceed with the enqueue() operation!%n");
			result.setReturnValue(ReturnStatusEnum.FAILED_ID_NOT_PROVIDED);
			return result;
		}

		Map<String,AttributeValue> key = new HashMap<>();
		key.put("id", new AttributeValue().withS(id));

		Shipment retrievedShipment = this.get(id);

		if (Utils.checkIfNullObject(retrievedShipment)) {

			System.out.printf("Shipment with ID [%s] cannot be found!%n", id);
			result.setReturnValue(ReturnStatusEnum.FAILED_ID_NOT_FOUND);
			return result;
		}

		int version = retrievedShipment.getSystemInfo().getVersion();
		StatusEnum status = retrievedShipment.getSystemInfo().getStatus();

		DynamoDB ddb = new DynamoDB(this.dynamoDB);
		Table table = ddb.getTable(this.actualTableName);
		
		result.setStatus(status);
		result.setVersion(version);
		result.setLastUpdatedTimestamp(retrievedShipment.getSystemInfo().getLastUpdatedTimestamp());
		
		if (status == StatusEnum.UNDER_CONSTRUCTION) {
			result.setReturnValue(ReturnStatusEnum.FAILED_RECORD_NOT_CONSTRUCTED);
			return result;
		}

		if (status != StatusEnum.READY_TO_SHIP) {
			result.setReturnValue(ReturnStatusEnum.FAILED_ILLEGAL_STATE);
			return result;
		}

		OffsetDateTime odt = OffsetDateTime.now(ZoneOffset.UTC);

        try {
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", id)
                .withUpdateExpression("ADD #sys.#v :one "
                		+ "SET queued = :one, #sys.queued = :one, #sys.queue_selected = :false, "
                		+ "last_updated_timestamp = :lut, #sys.last_updated_timestamp = :lut, "
                		+ "#sys.queue_added_timestamp = :lut, #sys.#st = :st")
                .withNameMap(new NameMap()
                		.with("#v", "version")
                		.with("#st", "status")
                		.with("#sys", "system_info"))
                .withValueMap(
                        new ValueMap()
                        	.withInt(":one", 1)
                        	.withBoolean(":false", false)
                        	.withInt(":v", version)
                        	.withString(":st", StatusEnum.READY_TO_SHIP.toString())
                        	.withString(":lut", odt.toString()))
                .withConditionExpression("#sys.#v = :v")
                .withReturnValues(ReturnValue.ALL_NEW);

            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);

            Map<String, Object> sysMap = outcome.getItem().getRawMap("system_info");
            result.setVersion(((BigDecimal)sysMap.get("version")).intValue());
            result.setLastUpdatedTimestamp((String)sysMap.get("last_updated_timestamp"));
            result.setStatus(StatusEnum.valueOf((String)sysMap.get("status")));
            
            Shipment shipment = this.get(id);
            result.setShipment(shipment);
        }
        catch (Exception e) {
            System.err.println("enqueue() - failed to update multiple attributes in " + this.actualTableName);
            System.err.println(e.getMessage());

            result.setReturnValue(ReturnStatusEnum.FAILED_DYNAMO_ERROR);
    		return result;
        }

        result.setReturnValue(ReturnStatusEnum.SUCCESS);
		return result;
	}
	
	/**
	 * Peek the record from the queue
	 *
	 * @return
	 */
	public PeekResult peek() {

		Map<String,AttributeValue> exclusiveStartKey = null;

		PeekResult result = new PeekResult();

		Map<String,AttributeValue> values = new HashMap<>();
		values.put(":one", new AttributeValue().withN("1"));

		String selectedID = null;

		int selectedVersion = 0;
		boolean recordForPeekIsFound = false;

		do {

			QueryRequest queryRequest = new QueryRequest()
					.withProjectionExpression("id, queued, system_info")
					.withIndexName(Constants.QUEUEING_INDEX_NAME)
					.withTableName(this.actualTableName)
					.withKeyConditionExpression("queued = :one")
					//.withFilterExpression("attribute_not_exists(queue_selected)")   // we need to look for the stragglers
					.withLimit(250)
					.withScanIndexForward(true)
					.withExpressionAttributeValues(values);

			queryRequest.withExclusiveStartKey(exclusiveStartKey);

			QueryResult queryResult = this.dynamoDB.query(queryRequest);
			exclusiveStartKey = queryResult.getLastEvaluatedKey();
			
			for(Map<String,AttributeValue> itemMap : queryResult.getItems()) {

		        Map<String, AttributeValue> sysMap = itemMap.get("system_info").getM();

		        boolean isQueueSelected = false;
		    	if (sysMap.containsKey("queue_selected")) isQueueSelected = sysMap.get("queue_selected").getBOOL();
		    	
		        // check if there are no stragglers (marked to be in processing but actually orphan)
		        if (sysMap.containsKey("peek_utc_timestamp") && isQueueSelected) {
		        	
			        long currentTS = System.currentTimeMillis();
			        long lastPeekTimeUTC = Long.parseLong(sysMap.get("peek_utc_timestamp").getN());

			        // if more than VISIBILITY_TIMEOUT_IN_MINUTES
			        if (currentTS - lastPeekTimeUTC > (Constants.VISIBILITY_TIMEOUT_IN_MINUTES * 60 * 1000)) {

		        		selectedID = itemMap.get("id").getS();
				        selectedVersion = Integer.parseInt(sysMap.get("version").getN());
				        recordForPeekIsFound = true;
				        
						System.out.printf(" >> Converted struggler, Shipment ID: [%s], age: %d%n", itemMap.get("id").getS(), currentTS - lastPeekTimeUTC);
			        }
		        }
			        
				// otherwise, peek first record that satisfy basic condition (queued = :one)
				else {

	        		selectedID = itemMap.get("id").getS();
			        selectedVersion = Integer.parseInt(sysMap.get("version").getN());
			        recordForPeekIsFound = true;
		        }
			        
				// no need to go further
				if (recordForPeekIsFound) break;
			}

		} while (!recordForPeekIsFound && exclusiveStartKey != null);

		if (Utils.checkIfNullObject(selectedID)) {

			result.setReturnValue(ReturnStatusEnum.FAILED_EMPTY_QUEUE);
			return result;
		}

		// assign ID to 'result'
		result.setId(selectedID);
		
		// this is a simplest way to construct an App object
		Shipment shipment = this.get(selectedID);

		OffsetDateTime odt = OffsetDateTime.now(ZoneOffset.UTC);

		DynamoDB ddb = new DynamoDB(this.dynamoDB);
		Table table = ddb.getTable(this.actualTableName);

		long tsUTC = System.currentTimeMillis();

		UpdateItemOutcome outcome = null;
		
        try {

        	// IMPORTANT
        	// please note, we are not updating top-level attribute `last_updated_timestamp` in order to avoid re-indexing the order
        	
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
            	.withPrimaryKey("id", shipment.getId())
                .withUpdateExpression("ADD #sys.#v :one "
                		+ "SET #sys.queue_selected = :true, "
                		+ "#sys.last_updated_timestamp = :lut, "
                		+ "#sys.queue_peek_timestamp = :lut, "
                		+ "#sys.peek_utc_timestamp = :ts, #sys.#st = :st")
                .withNameMap(new NameMap()
                		.with("#v", "version")
                		.with("#st", "status")
                		.with("#sys", "system_info"))
                .withValueMap(
                        new ValueMap()
                        	.withInt(":one", 1)
                        	.withInt(":v", selectedVersion)
                        	.withBoolean(":true", true)
                        	.withLong(":ts", tsUTC)
                        	.withString(":st", StatusEnum.PROCESSING_SHIPMENT.toString())
                        	.withString(":lut", odt.toString()))
                .withConditionExpression("#sys.#v = :v")
                .withReturnValues(ReturnValue.ALL_NEW);

            outcome = table.updateItem(updateItemSpec);     
        }
        catch (Exception e) {
            System.err.println("peek() - failed to update multiple attributes in " + this.actualTableName);
            System.err.println(e.getMessage());

            result.setReturnValue(ReturnStatusEnum.FAILED_DYNAMO_ERROR);
    		return result;
        }
            
        result.setId(outcome.getItem().getString("id"));

        // adding this to get the fresh data from DDB
        Shipment peekedShipment = this.get(selectedID);
        result.setPeekedShipmentObject(peekedShipment);

        Map<String, Object> sysMap = outcome.getItem().getRawMap("system_info");
        result.setVersion(((BigDecimal)sysMap.get("version")).intValue());
        result.setLastUpdatedTimestamp((String)sysMap.get("last_updated_timestamp"));
        result.setStatus(StatusEnum.valueOf((String)sysMap.get("status")));            
        result.setTimestampMillisUTC(((BigDecimal)sysMap.get("peek_utc_timestamp")).intValue());

        result.setReturnValue(ReturnStatusEnum.SUCCESS);
		return result;
	}

	/**
	 * dequeue() logic
	 * It peeks the record from the queue, it locks it for the short time and removes from the queue
	 * 
	 * @return
	 */
	public DequeueResult dequeue() {
		
		PeekResult peekResult = this.peek();
		
		DequeueResult dequeueResult = null;
		
		if (peekResult.isSuccessful()) {
			
			String ID = peekResult.getId();
			ReturnResult removeResult = this.remove(ID);
			
			dequeueResult = DequeueResult.fromReturnResult(removeResult);

			if (removeResult.isSuccessful()) {
				
				dequeueResult.setDequeuedShipmentObject(peekResult.getPeekedShipmentObject());
			}
		}
		else {
			
			dequeueResult = DequeueResult.fromReturnResult(peekResult);
		}

		return dequeueResult;
	}
	
	/**
	 * Acknowledge that the account is processed and that can be removed from the queue
	 * queued = 0 (and REMOVE), queue_selected = false
	 *
	 * @param id
	 * @return
	 */
	public ReturnResult remove(String id) {

		ReturnResult result = new ReturnResult(id);

		Shipment shipment = this.get(id);
		if (Utils.checkIfNullObject(shipment)) {

	        result.setReturnValue(ReturnStatusEnum.FAILED_ID_NOT_FOUND);
			return result;
		}

		OffsetDateTime odt = OffsetDateTime.now(ZoneOffset.UTC);

		DynamoDB ddb = new DynamoDB(this.dynamoDB);
		Table table = ddb.getTable(this.actualTableName);

		UpdateItemOutcome outcome = null;
		
        try {

            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
            	.withPrimaryKey("id", id)
                .withUpdateExpression("ADD #sys.#v :one "
                		+ "REMOVE #sys.peek_utc_timestamp, queued, #DLQ "
                		+ "SET #sys.queued = :zero, #sys.queue_selected = :false, "
                		+ "#sys.last_updated_timestamp = :lut, "
                		+ "last_updated_timestamp = :lut, "
                		+ "#sys.queue_remove_timestamp = :lut")
                .withNameMap(new NameMap().with("#v", "version")
                		.with("#DLQ", "DLQ")
                		.with("#sys", "system_info"))
                .withValueMap(
                        new ValueMap()
                        	.withInt(":one", 1)
                        	.withInt(":zero", 0)
                        	.withBoolean(":false", false)
                        	.withInt(":v", shipment.getSystemInfo().getVersion())
                        	.withString(":lut", odt.toString()))
                .withConditionExpression("#sys.#v = :v") 
                .withReturnValues(ReturnValue.ALL_NEW);

            outcome = table.updateItem(updateItemSpec);
            
        } catch (Exception e) {
            System.err.println("remove() - failed to update multiple attributes in " + this.actualTableName);
            System.err.println(e.getMessage());

            result.setReturnValue(ReturnStatusEnum.FAILED_DYNAMO_ERROR);
    		return result;
        }
            
        Map<String, Object> sysMap = outcome.getItem().getRawMap("system_info");
        result.setVersion(((BigDecimal)sysMap.get("version")).intValue());
        result.setStatus(StatusEnum.valueOf((String)sysMap.get("status")));
        result.setLastUpdatedTimestamp((String)sysMap.get("last_updated_timestamp"));

        result.setReturnValue(ReturnStatusEnum.SUCCESS);
		return result;
	}

	/**
	 * Put back the record in the queue 
	 *
	 * @param id
	 * @return
	 */
	public ReturnResult restore(String id) {

		ReturnResult result = new ReturnResult(id);

		Shipment shipment = this.get(id);
		if (Utils.checkIfNullObject(shipment)) {

	        result.setReturnValue(ReturnStatusEnum.FAILED_ID_NOT_FOUND);
			return result;
		}

		OffsetDateTime odt = OffsetDateTime.now(ZoneOffset.UTC);

		DynamoDB ddb = new DynamoDB(this.dynamoDB);
		Table table = ddb.getTable(this.actualTableName);

		UpdateItemOutcome outcome = null;
		
        try {

            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
            	.withPrimaryKey("id", id)
                .withUpdateExpression("ADD #sys.#v :one "
                		+ "REMOVE #DLQ "
                		+ "SET #sys.queued = :one, queued = :one, "
                		+ "#sys.queue_selected = :false, "
                		+ "last_updated_timestamp = :lut, "
                		+ "#sys.last_updated_timestamp = :lut, "
                		+ "#sys.queue_add_timestamp = :lut, "
                		+ "#sys.#st = :st")
                .withNameMap(new NameMap()
                		.with("#v", "version")
                		.with("#DLQ", "DLQ")
                		.with("#st", "status")
                		.with("#sys", "system_info"))
                .withValueMap(
                        new ValueMap().withInt(":one", 1)
                        	.withInt(":v", shipment.getSystemInfo().getVersion())
                        	.withBoolean(":false", false)
                        	.withString(":st", StatusEnum.READY_TO_SHIP.toString())
                        	.withString(":lut", odt.toString()))
                .withConditionExpression("#sys.#v = :v")
                .withReturnValues(ReturnValue.ALL_NEW);

            outcome = table.updateItem(updateItemSpec);
        }
        catch (Exception e) {
            System.err.println("restore() - failed to update multiple attributes in " + this.actualTableName);
            System.err.println(e.getMessage());

            result.setReturnValue(ReturnStatusEnum.FAILED_DYNAMO_ERROR);
    		return result;
        }

        Map<String, Object> sysMap = outcome.getItem().getRawMap("system_info");
        result.setVersion(((BigDecimal)sysMap.get("version")).intValue());
        result.setStatus(StatusEnum.valueOf((String)sysMap.get("status")));
        result.setLastUpdatedTimestamp((String)sysMap.get("last_updated_timestamp"));

        result.setReturnValue(ReturnStatusEnum.SUCCESS);
		return result;
	}

	/**
	 * Send the problematic record to DLQ 
	 *
	 * @param id
	 * @return ReturnResult
	 */
	public ReturnResult sendToDLQ(String id) {

		ReturnResult result = new ReturnResult(id);

		Shipment shipment = this.get(id);
		if (Utils.checkIfNullObject(shipment)) {

	        result.setReturnValue(ReturnStatusEnum.FAILED_ID_NOT_FOUND);
			return result;
		}

		OffsetDateTime odt = OffsetDateTime.now(ZoneOffset.UTC);

		DynamoDB ddb = new DynamoDB(this.dynamoDB);
		Table table = ddb.getTable(this.actualTableName);

		UpdateItemOutcome outcome = null;
		
        try {

            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
            	.withPrimaryKey("id", id)
                .withUpdateExpression("ADD #sys.#v :one "
                		+ "REMOVE queued "
                		+ "SET #DLQ = :one, #sys.queued = :zero, "
                		+ "#sys.queue_selected = :false, "
                		+ "last_updated_timestamp = :lut, "
                		+ "#sys.last_updated_timestamp = :lut, "
                		+ "#sys.dlq_add_timestamp = :lut, #sys.#st = :st")
                .withNameMap(new NameMap()
                		.with("#v", "version")
                		.with("#DLQ", "DLQ")
                		.with("#st", "status")
                		.with("#sys", "system_info"))
                .withValueMap(
                        new ValueMap().withInt(":one", 1)
                        	.withInt(":v", shipment.getSystemInfo().getVersion())
                        	.withInt(":zero", 0)
                        	.withBoolean(":false", false)
                        	.withString(":st", StatusEnum.IN_DLQ.toString())
                        	.withString(":lut", odt.toString()))
                .withConditionExpression("#sys.#v = :v and #sys.queued = :one")
                .withReturnValues(ReturnValue.ALL_NEW);

            outcome = table.updateItem(updateItemSpec);
        }
        catch (Exception e) {
            System.err.println("restore() - failed to update multiple attributes in " + this.actualTableName);
            System.err.println(e.getMessage());

            result.setReturnValue(ReturnStatusEnum.FAILED_DYNAMO_ERROR);
    		return result;
        }

        Map<String, Object> sysMap = outcome.getItem().getRawMap("system_info");
        result.setVersion(((BigDecimal)sysMap.get("version")).intValue());
        result.setStatus(StatusEnum.valueOf((String)sysMap.get("status")));
        result.setLastUpdatedTimestamp((String)sysMap.get("last_updated_timestamp"));

        result.setReturnValue(ReturnStatusEnum.SUCCESS);
		return result;
	}

	/**
	 * Changes the last_updated_timestamp and increments 'version' by 1
	 *
	 * @param id
	 * @return
	 */
	public ReturnResult touch(String id) {

		ReturnResult result = new ReturnResult(id);

		Shipment shipment = this.get(id);
		if (Utils.checkIfNullObject(shipment)) {

	        result.setReturnValue(ReturnStatusEnum.FAILED_ID_NOT_FOUND);
			return result;
		}

		OffsetDateTime odt = OffsetDateTime.now(ZoneOffset.UTC);

		DynamoDB ddb = new DynamoDB(this.dynamoDB);
		Table table = ddb.getTable(this.actualTableName);

		UpdateItemOutcome outcome = null;
		
        try {

            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
            	.withPrimaryKey("id", id)
                .withUpdateExpression("ADD #sys.#v :one "
                		+ "SET last_updated_timestamp = :lut, "
                		+ "#sys.last_updated_timestamp = :lut")
                .withNameMap(new NameMap()
                		.with("#v", "version")
                		.with("#sys", "system_info"))
                .withValueMap(
                        new ValueMap().withInt(":one", 1)
                        	.withInt(":v", shipment.getSystemInfo().getVersion())
                        	.withString(":lut", odt.toString()))
                .withConditionExpression("#sys.#v = :v")
                .withReturnValues(ReturnValue.ALL_NEW);

            outcome = table.updateItem(updateItemSpec);
            
        }
        catch (Exception e) {
            System.err.println("restore() - failed to update multiple attributes in " + this.actualTableName);
            System.err.println(e.getMessage());

            result.setReturnValue(ReturnStatusEnum.FAILED_DYNAMO_ERROR);
    		return result;
        }

        Map<String, Object> sysMap = outcome.getItem().getRawMap("system_info");
        result.setVersion(((BigDecimal)sysMap.get("version")).intValue());
        result.setStatus(StatusEnum.valueOf((String)sysMap.get("status")));
        result.setLastUpdatedTimestamp((String)sysMap.get("last_updated_timestamp"));

        result.setReturnValue(ReturnStatusEnum.SUCCESS);
		return result;
	}	
	
	/**
	 * Get the first 'size' items form the Shipment table
	 * 
	 * @return
	 */
	public List<String> listIDs(int size) {
		
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		scanExpression.withLimit(size);
		scanExpression.withProjectionExpression("id, system_info");
		
		//List<String> listOfIDs = new ArrayList<>();
		
		ScanResultPage<Shipment> result = this.dbMapper.scanPage(Shipment.class, scanExpression);
		
		List<String> listOfIDs = result.getResults().stream()
										.map(s -> s.getId())
										.limit(size)
										.collect(Collectors.toList());
		
		return listOfIDs;
	}
	
	/**
	 * Get the first 'size' items form the Shipment table
	 * 
	 * @return
	 */
	public List<String> listExtendedIDs(int size) {
		
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		scanExpression.withLimit(size);
		scanExpression.withProjectionExpression("id, system_info");
		
		//List<String> listOfIDs = new ArrayList<>();
		
		ScanResultPage<Shipment> result = this.dbMapper.scanPage(Shipment.class, scanExpression);
		
		List<String> listOfIDs = result.getResults().stream()
										.map(s -> s.getId() + " - status: " + s.getSystemInfo().getStatus().toString())
										.limit(size)
										.collect(Collectors.toList());
		
		return listOfIDs;
	}

	/**
	 * Delete the shipment record from DynamoDB by shipment ID
	 * 
	 * @param ID
	 */
	public void delete(String id) {

		Utils.throwIfNullOrEmptyString(id, "Shipment ID cannot be NULL!");

		this.dbMapper.delete(new Shipment(id));
	}
	
	/**
	 * Create the test data 
	 * 
	 * @param ID
	 * @return Shipment
	 */
	public Shipment createTestData(String ID) {
		
		Utils.throwIfNullOrEmptyString(ID, "ID is not provided!");
		
		this.delete(ID);
		
		ShipmentData data = new ShipmentData(ID);
		data.setData1("Data 1"); data.setData2("Data 2"); data.setData3("Data 3");
		data.setItems(Arrays.asList(new ShipmentItem("Item-1", true), new ShipmentItem("Item-2", true), new ShipmentItem("Item-3", true)));
		
		Shipment shipment = new Shipment(ID);
		shipment.setData(data);
		//shipment.markAsReadyForShipment();
		
		this.put(shipment);

		return shipment;
	}
	
	/**
	 * @return the dynamoDB
	 */
	public AmazonDynamoDB getDynamoDB() {
		return dynamoDB;
	}

	/**
	 * @return the SNS
	 */
	public AmazonSNS getSns() {
		return sns;
	}

	/**
	 * @return the s3
	 */
	public AmazonS3 getS3() {
		return s3;
	}

	/**
	 * Get the AWS Region
	 * 
	 * @return the awsRegion
	 */
	public String getAwsRegion() {

		return this.awsRegion;
	}

	/**
	 * Get the AWS Credential's Profile name
	 * 
	 * @return the awsCredentialsProfileName
	 */
	public String getAwsCredentialsProfileName() {

		return this.awsCredentialsProfileName;
	}	

	/**
	 * Get the AWSCredentials object reference
	 * 
	 * @return AWSCredentials
	 */
	public AWSCredentials getAWSCredentials() {

		return this.credentials;
	}

	/**
	 * @return the actualTableName
	 */
	public String getActualTableName() {
		return actualTableName;
	}

	/**
	 * @return the config
	 */
	public Configuration getConfig() {
		return config;
	}

	/**
	 * This method should be called at the end of the SDK usage
	 */
	public void shutdown() {

		if (this.dynamoDB != null) this.dynamoDB.shutdown();
		if (this.sns != null) this.sns.shutdown();
		if (this.s3 != null) this.s3.shutdown();

		this.credentials = null;
		this.dynamoDB = null;
		this.sns = null;
		this.s3 = null;
	}

	// ------------- fields

	private AmazonS3 s3 = null;
	private AmazonSNS sns = null;
	private AmazonDynamoDB dynamoDB = null;  	 
	
	private ConfigField key = null;
	
	private AWSCredentials credentials = null;

	private String actualTableName = null;
	private String logicalTableName = null;
	
	private Configuration config = null;
	//private String configFileName = null;
	//private String configContent = null;

	private String awsRegion = null;
	private String awsCredentialsProfileName = null;

	private DynamoDBMapper dbMapper = null;
	
	private static final RetryPolicy RETRY_POLICY = new RetryPolicy(null, DYNAMODB_DEFAULT_BACKOFF_STRATEGY, 10, false);

	/**
	 * Inner builder class
	 * 
	 * @author zorani
	 *
	 */
	public static class Builder {

		private String configFileName = null;
		private String configContent = null;

		private AWSCredentials credentials = null;

		private String logicalTableName = null;
		
		private String awsRegion = null;
		private String awsCredentialsProfileName = null;

		private QueueSdkClient client = null;

		/**
		 * Default constructor
		 */
		public Builder() {
			// ...
		}

		/**
		 * Create a QueueSDK
		 * 
		 * @return QueueSdkClient
		 */
		public QueueSdkClient build() {

			if (Utils.checkIfNullObject(this.logicalTableName)) this.logicalTableName = Constants.DEFAULT_SHIPMENT_TABLE_NAME;
						
			// only if QueueSdkClient is not formed yet
			if (Utils.checkIfNullObject(this.client)) {

				this.client = new QueueSdkClient(this);
				this.client.initialize();
			}

			return this.client;
		}

		/**
		 * Set the configuration filename 
		 * This can be used if running on the host/desktop/Fargate. For Lambda, rely on the configuration stored as a JAR resource file
		 * 
		 * @param fileName Local filename where configuration json file is located
		 * @return Builder
		 */
		public Builder withConfigurationFileName(String fileName) {

			this.configFileName = fileName;
			return this;
		}

		/**
		 * Specify AWS region 
		 * If not used, default value is 'us-east-1'.
		 * 
		 * @param region Proper AWS Region string
		 * @return Builder
		 */
		public Builder withRegion(String region) {

			this.awsRegion = region;
			return this;
		}

		/**
		 * Specify local credential profile
		 * 
		 * @param profile This is the name of the local AWS Credential profile
		 * @return Builder
		 */
		public Builder withCredentialsProfileName(String profile) {

			this.awsCredentialsProfileName = profile;
			return this;
		}

		/**
		 * Specify local credential profile
		 * Shortcut named method. This is the same method as withCredentialsProfileName()
		 * 
		 * @param profile This is the name of the local AWS Credential profile
		 * @return Builder
		 */
		public Builder withProfile(String profile) {

			this.awsCredentialsProfileName = profile;
			return this;
		}

		/**
		 * Specify a logical table name
		 * The builder will find the actual DynamoDB table name by provided logical name.
		 * 
		 * @param logicalTableName
		 * @return
		 */
		public Builder withLogicalTableName(String logicalTableName) {

			this.logicalTableName = logicalTableName;
			return this;
		}
		
		/**
		 * Provide the AWS Credentials
		 * 
		 * @param awsCredentials User's provided AWSCredential
		 * @return Builder
		 */
		public Builder withCredentials(AWSCredentials awsCredentials) {

			this.credentials = awsCredentials;
			return this;
		}

		/**
		 * Set the configuration JSON content for the Client to load it at the startup time.
		 * This is used if the configuration data sits in S3, DynamoDB or other not-local file places
		 * 
		 * @param configurationJsonContent This should represent a JSON configuration content
		 * @return Builder
		 */
		public Builder withConfigurationContent(String configurationJsonContent) {

			this.configContent = configurationJsonContent;
			return this;
		}

	} //end Builder

} // end QueueSdkClient