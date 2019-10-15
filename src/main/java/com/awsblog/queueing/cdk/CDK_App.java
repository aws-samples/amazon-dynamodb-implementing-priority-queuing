package com.awsblog.queueing.cdk;

import java.util.Map;
import java.io.File;
import java.util.HashMap;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.awsblog.queueing.config.ConfigLambda;
import com.awsblog.queueing.config.Configuration;
import com.awsblog.queueing.utils.FileUtils;
import com.awsblog.queueing.utils.Utils;

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.lambda.IFunction;

/**
 * CDK for creation of DynamoDB tables and Lambda/Role constructs
 *
 * @author zorani
 *
 */
public class CDK_App {

	private static String CONFIG_FILE_NAME = "configuration.json";
	
	/**
	 * Main method
	 *
	 * @param argv
	 */
    public static void main(final String argv[]) {

		String configJsonFile = CONFIG_FILE_NAME;

		System.out.printf("%n");
		System.out.printf("***************************************************************%n");
		System.out.printf("******************      CDK App - v1.10      ******************%n");
		System.out.printf("***************************************************************%n");

        App app = new App();

        String credentialsProfile = (String)app.getNode().tryGetContext("aws-credentials-profile");
        String awsRegion = (String)app.getNode().tryGetContext("aws-region");

		System.out.printf("Options:%n");
		System.out.printf(" >> Configuration filename: [%s]%n", configJsonFile);
        if (Utils.checkIfNotNullAndNotEmptyString(credentialsProfile)) System.out.printf(" >> Credentials profile name: %s%n", credentialsProfile);
        if (Utils.checkIfNotNullAndNotEmptyString(awsRegion)) System.out.printf(" >> AWS region: %s%n", awsRegion);
		System.out.printf("---------------------------------------------------------------------------------%n");

		Configuration config = Configuration.fromJSON(FileUtils.getFileFromResourcesAsString(configJsonFile));

		Map<String, IRole> roles = new HashMap<>();
		Map<String, IFunction> functions = new HashMap<>();

		AWSCredentials credentials = new ProfileCredentialsProvider(credentialsProfile).getCredentials();

		AWSSecurityTokenServiceClientBuilder stsBuilder = AWSSecurityTokenServiceClientBuilder.standard();
		stsBuilder.withRegion(awsRegion);
		stsBuilder.withCredentials(new AWSStaticCredentialsProvider(credentials));
		AWSSecurityTokenService stsClient = stsBuilder.build();

		GetCallerIdentityRequest request = new GetCallerIdentityRequest();
		GetCallerIdentityResult response = stsClient.getCallerIdentity(request);

		String awsAccountID = response.getAccount();
		System.out.printf("AWS Account #: [%s]%n", awsAccountID);

		// -------------------- copy JAR to S3
		
		AmazonS3ClientBuilder s3builder = AmazonS3ClientBuilder.standard();
		if (!Utils.checkIfNullObject(credentialsProfile)) s3builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
		if (!Utils.checkIfNullObject(awsRegion)) s3builder.withRegion(awsRegion);
		AmazonS3 s3 = s3builder.build();
		
		//copyJARsToS3(s3, config);
		
		// ------------------- Build stacks using CDK
		
		Environment env = Environment.builder()
							.account(awsAccountID)
							.region(awsRegion)
							.build();

		StackProps props = StackProps.builder()
				.env(env)
				.build();

        new DynamoStack(app, "aws-blog-queue-dynamodb-stack", props, config, roles);
    	//new RoleStack(app, "aws-blog-queue-role-stack", props, config, roles);
        //new LambdaStack(app, "aws-blog-queue-lambda-stack", props, config, roles, functions);

        app.synth();
    }
    
    /**
     * Copy the JAR file to S3
     * 
     * @param s3
     * @param config
     */
    private static void copyJARsToS3(AmazonS3 s3, Configuration config) {

		for (ConfigLambda lambdaInfo : config.getLambdas()) {

			String jarName = LambdaStack.getJarNameFromLocalPath(lambdaInfo.getLocalJarPath());
    	
			System.out.printf(" ... JAR: [%s]%n", jarName);
			
			File fileToUpload = new File(lambdaInfo.getLocalJarPath());
			PutObjectRequest putRequest = new PutObjectRequest(config.getS3CodeBucket(), jarName, fileToUpload);
			ObjectMetadata metadata = new ObjectMetadata();
	        metadata.setContentType("application/java-archive");
	        metadata.setContentLength(fileToUpload.length());
	        putRequest.setMetadata(metadata);
			s3.putObject(putRequest);
			
			System.out.printf("---------------------------------------------------------------------------------%n");
		}
    }    
    
} // end CDK_App