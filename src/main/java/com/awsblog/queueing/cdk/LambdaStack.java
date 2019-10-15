package com.awsblog.queueing.cdk;

import java.util.Locale;
import java.util.Map;

import com.awsblog.queueing.config.ConfigLambda;
import com.awsblog.queueing.config.Configuration;
import com.awsblog.queueing.utils.Utils;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.S3Code;
import software.amazon.awscdk.services.s3.Bucket;

/**
 * Stack for creating Lambda infrastructure
 *
 * @author zorani
 *
 */
public class LambdaStack extends Stack {

	/**
	 * C-tor
	 *
	 * @param parent
	 * @param id
	 * @param jsonMetaLocation
	 * @param jsonConfigLocation
	 */
	public LambdaStack(final Construct parent, final String id, StackProps props,
			Configuration config, Map<String, IRole> roles, Map<String, IFunction> functions) {

		this(parent, id, config, props, roles, functions);
	}

	/**
	 * C-tor with the properties
	 *
	 * @param parent
	 * @param id
	 * @param jsonMetaLocation
	 * @param jsonConfigLocation
	 * @param props
	 */
	public LambdaStack(final Construct parent, final String id,
			Configuration config, final StackProps props, Map<String, IRole> roles, Map<String, IFunction> functions) {

		super(parent, id, props);
		
		if (!config.getLambdas().isEmpty()) {

			System.out.printf(" >> Lambda function construction ... [%s]%n", id);

			int counter = 1;
			for (ConfigLambda lambdaInfo : config.getLambdas()) {

				System.out.printf(">>>>> Constructing Lambda [%s] ->%n    > class: [%s]%n    > deployment name: [%s]%n",
						lambdaInfo.getLogicalName(), lambdaInfo.getClassName(), lambdaInfo.getLambdaDeploymentName());

				String jarName = getJarNameFromLocalPath(lambdaInfo.getLocalJarPath());
				System.out.printf("    > Code bucket: [%s], JAR [%s]%n", config.getS3CodeBucket(), jarName);
				
				Code code = new S3Code(Bucket.fromBucketName(this, "S3LambdaCode-" + lambdaInfo.getLogicalName() + counter, config.getS3CodeBucket()),
																	jarName);

				FunctionProps lambdaProps = FunctionProps.builder()
						.runtime(getLambdaRuntime(lambdaInfo.getRuntime()))
						.functionName(lambdaInfo.getLambdaDeploymentName())
						.description(lambdaInfo.getDescription())
						//.code(Code.asset(Paths.get(lambdaInfo.getLocalJarPath()).toString()))
						.code(code)
						.handler(lambdaInfo.getClassName() + "::" + lambdaInfo.getHandler())
						.role(roles.get("lambda-role"))
						.timeout(Duration.seconds(lambdaInfo.getTimeoutInSeconds()))
						.memorySize(lambdaInfo.getMemoryInMegabytes())
						.build();
				
				IFunction fun = new Function(this, lambdaInfo.getLambdaDeploymentName(), lambdaProps);

				// add to the list of IFunction
				functions.put(lambdaInfo.getLambdaDeploymentName(), fun);

				System.out.printf("Function [%s] ... ARN: [%s]%n", fun.getFunctionName(), fun.getFunctionArn());
				
				++counter;
			}
		}
	}
	
	/**
	 * Get the JAR name
	 * 
	 * @param localJarPath
	 * @return
	 */
	protected static String getJarNameFromLocalPath(String localJarPath) {
		
		Utils.throwIfNullOrEmptyString(localJarPath, "Local JAR path cannot be NULL!");
		
		String[] arr = localJarPath.split("/");
		if (arr.length == 0) return null;
		
		return arr[arr.length - 1].trim();
	}

	/**
	 * Get the proper Lambda Runtime environment
	 *
	 * @param runtime
	 * @return
	 */
	private static Runtime getLambdaRuntime(String runtime) {

		if (Utils.checkIfNullOrEmptyString(runtime)) return Runtime.JAVA_8;
		else if (runtime.trim().toLowerCase(Locale.ENGLISH).startsWith("python27")) return Runtime.PYTHON_2_7;
		else if (runtime.trim().toLowerCase(Locale.ENGLISH).startsWith("python36")) return Runtime.PYTHON_3_6;
		else if (runtime.trim().toLowerCase(Locale.ENGLISH).startsWith("python37")) return Runtime.PYTHON_3_7;
		else if (runtime.trim().toLowerCase(Locale.ENGLISH).startsWith("node")) return Runtime.NODEJS_10_X;

		return Runtime.JAVA_8;
	}

} // end LambdaStack