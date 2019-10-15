package com.awsblog.queueing.cli;

import java.io.File;
import java.util.List;
import java.util.Scanner;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.awsblog.queueing.Constants;
import com.awsblog.queueing.appdata.Shipment;
import com.awsblog.queueing.config.Configuration;
import com.awsblog.queueing.model.DLQResult;
import com.awsblog.queueing.model.EnqueueResult;
import com.awsblog.queueing.model.PeekResult;
import com.awsblog.queueing.model.QueueStats;
import com.awsblog.queueing.model.ReturnResult;
import com.awsblog.queueing.model.StatusEnum;
import com.awsblog.queueing.sdk.QueueSdkClient;
import com.awsblog.queueing.utils.CommandLineUtils;
import com.awsblog.queueing.utils.Utils;

/**
 * Command Line Interface (CLI)
 * 
 * @author zorani
 *
 */
public class CLI {

	private static String NEED_AWS_MESSAGE = "     Need first to run 'aws' command";
			
	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.printf( "===========================================================%n");
		System.out.printf( ">> Welcome to Queueing AWS Blog Post's CLI Tool!%n");
		System.out.printf( "===========================================================%n");
		System.out.printf( " for help, enter one of the following: ? or h or help%n");
		System.out.printf( " all commands in CLIs need to be typed in lowercase%n");
		
		String executionPath = new File(".").getAbsolutePath();
		
		System.out.printf( " current directory is: [%s]%n", executionPath);

		String region = Constants.AWS_REGION_DEFAULT;
		String credentialsProfile = Constants.AWS_PROFILE_DEFAULT;
		
		CommandLineUtils commandLineOptions = new CommandLineUtils(args);

		if (commandLineOptions.containsOption("--profile"))
			credentialsProfile = commandLineOptions.getOptionValue("--profile");
		
		if (commandLineOptions.containsOption("--region"))
			region = commandLineOptions.getOptionValue("--region");

		QueueSdkClient client = null;
		Configuration configuration = null;

		Shipment shipment = null;
		
		if (Utils.checkIfNotNullAndNotEmptyString(credentialsProfile)) {
			
			try {
				client = new QueueSdkClient.Builder()
						.withRegion(region)
						.withCredentialsProfileName(credentialsProfile)
						.build();
				
				Utils.throwIfNullObject(client, "QueueSdkClient is NULL!");
				
				AWSCredentials credentials = new ProfileCredentialsProvider(credentialsProfile).getCredentials();
				
				System.out.printf(" ... AWS session is properly established!%n");
				
			} catch (Exception e) {
				
				System.out.printf(" ... AWS session could not be established!%n");
			}
		}

		if (Utils.checkIfNullObject(configuration)) configuration = Configuration.loadConfiguration();
		
		Utils.throwIfNullObject(configuration, "Configuration object reference is NULL!");
		
		while (true) {

			// 1. Create a Scanner using the InputStream available.
			Scanner scanner = new Scanner( System.in );

			// 2. Don't forget to prompt the user
			if (Utils.checkIfNotNullObject(shipment))
				System.out.printf( "%nID <%s> >> Enter command: ", shipment.getId());
			else
				System.out.printf( "%n >> Enter command: ");

			// 3. Use the Scanner to read a line of text from the user.
			String input = scanner.nextLine();
			if (Utils.checkIfNullOrEmptyString(input)) continue;

			input = input.trim();

			String arr[] = input.split(" ");
			if (arr.length == 0) continue;
			
			String command = arr[0].toLowerCase();
			String[] params = null;
			
			if (arr.length > 1) {
				
				params = new String[arr.length - 1];
				for(int i = 1; i < arr.length; ++i) params[i-1] = arr[i].trim();
			}
			
			// 4. Now, you can do anything with the input string that you need to.
			// Like, output it to the user.
			
			if ("quit".equals(command) || "q".equals(command)) {

				if (scanner != null) scanner.close();
				if (client != null) client.shutdown();
				break;
			}
			else if ("h".equals(command) || "?".equals(command) || "help".equals(command)) {

				System.out.printf("  ... this is CLI HELP!%n");
				System.out.printf("    > aws <profile> [<region>]                      [Establish connection with AWS; Default profile name: `default` and region: `us-east-1`]%n");
				System.out.printf("    > qstat | qstats                                [Retrieves the Queue statistics (no need to be in App mode)]%n");
				System.out.printf("    > dlq                                           [Retrieves the Dead Letter Queue (DLQ) statistics]%n");
				System.out.printf("    > create-test | ct                              [Create test Shipment records in DynamoDB: A-101, A-202, A-303 and A-404; if already exists, it will overwrite it]%n");
				System.out.printf("    > purge                                         [It will remove all test data from DynamoDB]%n");
				System.out.printf("    > ls                                            [List all shipment IDs ... max 10 elements]%n");
				System.out.printf("    > id <id>                                       [Get the application object from DynamoDB by app domain ID; CLI is in the app mode, from that point on]%n");
				System.out.printf("      > sys                                         [Show system info data in a JSON format]%n");
				System.out.printf("      > data                                        [Print the data as JSON for the current shipment record]%n");
				System.out.printf("      > info                                        [Print all info regarding Shipment record: system_info and data as JSON]%n");
				System.out.printf("      > update <new Shipment status>                [Update Shipment status .. e.g.: from UNDER_CONSTRUCTION to READY_TO_SHIP]%n");
				System.out.printf("      > reset                                       [Reset the system info of the current shipment record]%n");
				System.out.printf("      > ready                                       [Make the record ready for the shipment]%n");				
				System.out.printf("      > enqueue | en                                [Enqueue current ID]%n");
				System.out.printf("      > peek                                        [Peek the Shipment from the Queue .. it will replace the current ID with the peeked one]%n");
				System.out.printf("      > done                                        [Simulate successful record processing completion ... remove from the queue]%n");
				System.out.printf("      > fail                                        [Simulate failed record's processing ... put back to the queue; needs to be peeked again]%n");
				System.out.printf("      > invalid                                     [Remove record from the regular queue to dead letter queue (DLQ) for manual fix]%n");				
				System.out.printf("    > id                                            [Reverting back to the standard CLI's system operational mode]%n");
			}
			else if (command.equals("aws")) {
				
				if (params == null) {
					System.out.printf("     ERROR: 'aws <profile> [<region>]' command requires parameter(s) to be specified!%n");
					continue;
				}
				
				String awsCredentialsProfile = params[0].trim();
				
				// specify AWS Region
				if (params.length > 1) region = params[1].trim();
				
				if (Utils.checkIfNullOrEmptyString(awsCredentialsProfile) && Utils.checkIfNotNullAndNotEmptyString(credentialsProfile)) awsCredentialsProfile = credentialsProfile;
				else awsCredentialsProfile = "default";
				
				try {
					client = new QueueSdkClient.Builder()
							.withRegion(region)
							.withCredentialsProfileName(awsCredentialsProfile)
							.build();
					
					Utils.throwIfNullObject(client, "QueueSdkClient is NULL!");
					
					AWSCredentials credentials = new ProfileCredentialsProvider(awsCredentialsProfile).getCredentials();
					
					AWSLambdaClientBuilder lambdaBuilder = AWSLambdaClientBuilder.standard();
					lambdaBuilder.withCredentials(new AWSStaticCredentialsProvider(credentials));
					lambdaBuilder.withRegion(region);
					
					System.out.printf(" ... AWS session is properly established!%n");
					
				} catch (Exception e) {
					e.printStackTrace();
					
				}
			}
			else if (command.equals("id")) {
				
				if (params == null || params.length == 0) {

					shipment = null;
					System.out.printf("     Going back to standard CLI mode!%n");
					continue;
				}

				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else {
					
					String id = params[0].trim();
					shipment = client.get(id);
					
					System.out.printf("     Shipment's [%s] record dump%n%s", id, Utils.toJSON(shipment));
				}
			}
			else if (command.equals("sys") || command.equals("system")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else if (Utils.checkIfNullObject(shipment)) {
					
					System.out.println("     ERROR: `system` or `sys` command can be only used in the CLI's App mode. Call first `id <record-id>`%n");
				}
				else {
					
						System.out.printf("     ID's system info:%n%s%n", Utils.toJSON(shipment.getSystemInfo()));
				}
			}			
			else if (command.equals("ls")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else {

					List<String> IDs = client.listExtendedIDs(10);
					
					if (IDs.isEmpty()) System.out.printf("     Shipment table is empty!%n");
					else {
						System.out.printf("     List of first 10 IDs:%n");
						for(String ID : IDs) System.out.printf("      >> ID : %s%n", ID);
					}
				}
			}
			else if (command.equals("purge")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else {

					List<String> IDs = client.listIDs(10);
					
					if (IDs.isEmpty()) System.out.printf("     Shipment table is empty ... nothing to remove!%n");
					else {
						System.out.printf("     List of removed IDs:%n");
						for(String ID : IDs) {
							
							client.delete(ID);
							System.out.printf("      >> Removed ID : %s%n", ID);
						}
					}
				}
			}
			else if (command.equals("create-test") || command.equals("ct")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else {

					client.createTestData("A-101");
					System.out.printf("      >> Creating shipment with ID : A-101%n");
					client.createTestData("A-202");
					System.out.printf("      >> Creating shipment with ID : A-202%n");
					client.createTestData("A-303");
					System.out.printf("      >> Creating shipment with ID : A-303%n");
					client.createTestData("A-404");
					System.out.printf("      >> Creating shipment with ID : A-404%n");
				}
			}
			else if (command.equals("qstat") || command.equals("stat")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else if (command.equals("stat") && Utils.checkIfNullObject(shipment)) {
					
					System.out.println("     ERROR: 'stat' command can be only used in the ID mode. Use 'qstat' instead!%n");
				}
				else {
					
					QueueStats result = client.getQueueStats();
					System.out.printf("     Queue status%n%s", Utils.toJSON(result));
				}
			}
			else if (command.equals("dlq")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else {
					
					DLQResult result = client.getDLQStats();
					System.out.printf("     DLQ status%n%s", Utils.toJSON(result));
				}
			}
			else if (command.equals("reset")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else if (Utils.checkIfNullObject(shipment)) {
					
					System.out.println("     ERROR: 'reset' command can be only used in the CLI's App mode. Call first `id <record-id>`%n");
				}
				else {
					
					shipment.resetSystemInfo();
					
					client.put(shipment);
					
					System.out.printf("     Reseted system info:%n%s", Utils.toJSON(shipment.getSystemInfo()));
				}
			}
			else if (command.equals("ready")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else if (Utils.checkIfNullObject(shipment)) {
					
					System.out.println("     ERROR: 'ready' command can be only used in the CLI's App mode. Call first `id <record-id>`%n");
				}
				else {
					
					shipment.resetSystemInfo();
					shipment.getSystemInfo().setStatus(StatusEnum.READY_TO_SHIP);
					
					client.put(shipment);
					
					System.out.printf("     Reseted system info:%n%s", Utils.toJSON(shipment.getSystemInfo()));
				}
			}
			else if (command.equals("done")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else if (Utils.checkIfNullObject(shipment)) {
					
					System.out.println("     ERROR: 'done' command can be only used in the CLI's App mode. Call first `id <record-id>`%n");
				}
				else {
					
					client.updateStatus(shipment.getId(), StatusEnum.COMPLETED);
					client.remove(shipment.getId());
					
					shipment = client.get(shipment.getId());
					
					System.out.printf("     Processing for ID [%s] is completed successfully! Remove from the queue!%n", shipment.getId());

					QueueStats result = client.getQueueStats();
					System.out.printf("     Queue status%n%s", Utils.toJSON(result));
				}
			}
			else if (command.equals("fail")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else if (Utils.checkIfNullObject(shipment)) {
					
					System.out.println("     ERROR: 'fail' command can be only used in the CLI's App mode. Call first `id <record-id>`%n");
				}
				else {
					
					client.restore(shipment.getId());
					
					shipment = client.get(shipment.getId());
					
					System.out.printf("     Processing for ID [%s] has failed! Put the record back to the queue!%n", shipment.getId());

					QueueStats result = client.getQueueStats();
					System.out.printf("     Queue status%n%s", Utils.toJSON(result));
				}
			}
			else if (command.equals("invalid")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else if (Utils.checkIfNullObject(shipment)) {
					
					System.out.println("     ERROR: 'invalid' command can be only used in the CLI's App mode. Call first `id <record-id>`%n");
				}
				else {
					
					client.sendToDLQ(shipment.getId());
					
					shipment = client.get(shipment.getId());
					
					System.out.printf("     Processing for ID [%s] has failed .. invalid data! Send record to DLQ!%n", shipment.getId());

					QueueStats result = client.getQueueStats();
					System.out.printf("     Queue status%n%s", Utils.toJSON(result));
				}
			}
			else if (command.equals("data")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else if (Utils.checkIfNullObject(shipment)) {
					
					System.out.println("     ERROR: 'data' command can be only used in the CLI's App mode. Call first `id <record-id>`%n");
				}
				else {
					
					System.out.printf("     Data info:%n%s", Utils.toJSON(shipment.getData()));
				}
			}			
			else if (command.equals("info")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else if (Utils.checkIfNullObject(shipment)) {
					
					System.out.println("     ERROR: 'info' command can be only used in the CLI's App mode. Call first `id <record-id>`%n");
				}
				else {
					
					System.out.printf("     Record's dump:%n%s", Utils.toJSON(shipment));
				}
			}			
			else if (command.equals("enqueue") || command.equals("en")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else if (Utils.checkIfNullObject(shipment)) {
					
					System.out.println("     ERROR: 'enqueue' command can be only used in the CLI's App mode. Call first `id <record-id>`%n");
				}
				else {
					
					shipment = client.get(shipment.getId());

					// convert under_construction to ready to ship
					if (shipment.getSystemInfo().getStatus() == StatusEnum.UNDER_CONSTRUCTION) {
						shipment.resetSystemInfo();
						shipment.getSystemInfo().setStatus(StatusEnum.READY_TO_SHIP);
						
						client.put(shipment);
						shipment = client.get(shipment.getId());
					}
										
					EnqueueResult result = client.enqueue(shipment.getId());
					
					shipment = result.getShipment();
					
					if (result.isSuccessful()) {
						
						System.out.printf("     Record's system info:%n%s", Utils.toJSON(shipment.getSystemInfo()));
						
						QueueStats queueStatsResult = client.getQueueStats();
						System.out.printf("     Queue stats%n%s", Utils.toJSON(queueStatsResult));
					}
					else {

						System.out.printf("     Enqueue has failed!%n Error message:%n%s", Utils.toJSON(result));
					}
				}
			}
			else if (command.equals("peek")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else if (Utils.checkIfNullObject(shipment)) {
					
					System.out.println("     ERROR: 'peek' command can be only used in the CLI's App mode. Call first `id <record-id>`%n");
				}
				else {
					
					PeekResult result = client.peek();
					if (result.isSuccessful()) {
						
						shipment = result.getPeekedShipmentObject();
						
						System.out.printf("     Peek was successful ... record peeked is: [%s]%n%s%n", shipment.getId(), Utils.toJSON(shipment.getSystemInfo()));
						
						QueueStats queueStatsResult = client.getQueueStats();
						System.out.printf("     Queue stats%n%s", Utils.toJSON(queueStatsResult));
					}
					else {

						System.out.printf("     peek() has failed!%n Error message:%n%s", Utils.toJSON(result));
					}
				}
			}			
			else if (command.equals("update")) {
				
				if (Utils.checkIfNullObject(client)) {
					
					System.out.println(CLI.NEED_AWS_MESSAGE);
				}
				else if (Utils.checkIfNullObject(shipment)) {
					
					System.out.println("     ERROR: 'update <status>' command can be only used in the CLI's App mode. Call first `id <record-id>`%n");
				}
				else if (params == null) {
					
					System.out.printf("     ERROR: 'update <status>' command requires a new Stataus parameter to be specified!%n");
				}
				else {

					String statusStr = params[0].toUpperCase().trim();
					
					if (statusStr.equals("READY_TO_SHIP")) {

						shipment.markAsReadyForShipment();
						ReturnResult rr = client.updateStatus(shipment.getId(), StatusEnum.valueOf(statusStr));
						
						System.out.printf("     Status changed result:%n%s%n", Utils.toJSON(rr));
					}
					else 
						System.out.printf("     Status change [%s] is not applied!%n", params[0].trim());
				}
			}			
			
			else {
				System.out.printf(" ... unrecognized command!%n");
			}
			
			//scanner.close();
		}
		
		System.out.printf(">> CLI is ending%n%n%n");

		System.exit(0);
	}
	
} // end CLI