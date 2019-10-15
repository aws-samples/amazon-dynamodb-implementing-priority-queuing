package com.awsblog.queueing.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Lambda configuration
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
public class ConfigLambda {

	/**
	 * C-tor
	 */
	public ConfigLambda() {
		
	}
	
	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	/**
	 * @return the lambdaDeploymentName
	 */
	public String getLambdaDeploymentName() {
		return lambdaDeploymentName;
	}
	/**
	 * @param lambdaDeploymentName the lambdaDeploymentName to set
	 */
	public void setLambdaDeploymentName(String lambdaDeploymentName) {
		this.lambdaDeploymentName = lambdaDeploymentName;
	}
	/**
	 * @return the runtime
	 */
	public String getRuntime() {
		return runtime;
	}
	/**
	 * @param runtime the runtime to set
	 */
	public void setRuntime(String runtime) {
		this.runtime = runtime;
	}
	/**
	 * @return the handler
	 */
	public String getHandler() {
		return handler;
	}
	/**
	 * @param handler the handler to set
	 */
	public void setHandler(String handler) {
		this.handler = handler;
	}
	/**
	 * @return the localJarPath
	 */
	public String getLocalJarPath() {
		return localJarPath;
	}
	/**
	 * @param localJarPath the localJarPath to set
	 */
	public void setLocalJarPath(String localJarPath) {
		this.localJarPath = localJarPath;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the memoryInMegabytes
	 */
	public int getMemoryInMegabytes() {
		return memoryInMegabytes;
	}
	/**
	 * @param memoryInMegabytes the memoryInMegabytes to set
	 */
	public void setMemoryInMegabytes(int memoryInMegabytes) {
		this.memoryInMegabytes = memoryInMegabytes;
	}
	
	/**
	 * @return the logicalName
	 */
	public String getLogicalName() {
		return logicalName;
	}

	/**
	 * @param logicalName the logicalName to set
	 */
	public void setLogicalName(String logicalName) {
		this.logicalName = logicalName;
	}

	/**
	 * @return the timeoutInSeconds
	 */
	public int getTimeoutInSeconds() {
		return timeoutInSeconds;
	}
	/**
	 * @param timeoutInSeconds the timeoutInSeconds to set
	 */
	public void setTimeoutInSeconds(int timeoutInSeconds) {
		this.timeoutInSeconds = timeoutInSeconds;
	}

	// ------------ fields

	@JsonProperty("logical_name")
	private String logicalName = null;
	@JsonProperty("lambda_class")
	private String className = null;
	@JsonProperty("lambda_name")
	private String lambdaDeploymentName = null;
	@JsonProperty("runtime")
	private String runtime = null;
	@JsonProperty("handler")
	private String handler = null;
	@JsonProperty("local_jar")
	private String localJarPath = null;
	@JsonProperty("description")
	private String description = null;
	
	@JsonProperty("memory_megabytes")
	private int memoryInMegabytes = 0;
	@JsonProperty("timeout_seconds")
	private int timeoutInSeconds = 0;
	
} // end ConfigLambda