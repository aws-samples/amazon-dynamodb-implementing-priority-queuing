package com.awsblog.queueing.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.awsblog.queueing.Constants;
import com.awsblog.queueing.utils.FileUtils;
import com.awsblog.queueing.utils.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Parent Configuration POJO
 * 
 * @author zorani
 *
 */
@JsonInclude(Include.NON_NULL)
public class Configuration {

	/**
	 * C-tor
	 */
	public Configuration() {
		
		this.tables = new ArrayList<>();
		this.tablesMap = new HashMap<>();
		this.lambdasMap = new HashMap<>();
		this.lambdas = new ArrayList<>();
	}
	
	/**
	 * Create a Configuration object from JSON
	 * 
	 * @param jsonPayload
	 * @return
	 */
	public static Configuration fromJSON(String jsonPayload) {

		ObjectMapper mapper = new ObjectMapper();

		//JSON string to Java Object
		Configuration configurationObject = null;
		
		try {
			configurationObject = mapper.readValue(jsonPayload, Configuration.class);
			
			for(ConfigTable tbl : configurationObject.getTables()) {
				configurationObject.getTablesMap().put(tbl.getLogicalName(), tbl);
			}
			
			for(ConfigLambda lamnda : configurationObject.getLambdas()) {
				configurationObject.getLambdasMap().put(lamnda.getLogicalName(), lamnda);
			}
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return configurationObject;
	}
	
	/**
	 * Load the Configuration from JSON definition
	 * 
	 * @return
	 */
	public static Configuration loadConfiguration() {
		
		String jsonPayload = FileUtils.getFileFromResourcesAsString(Constants.CONFIGURATION_FILE_NAME);
		
		Utils.throwIfNullOrEmptyString(jsonPayload, "configuration.json is not found!");
		
		return Configuration.fromJSON(jsonPayload);
	}
	
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the lastUpdatedDate
	 */
	public String getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	/**
	 * @param lastUpdatedDate the lastUpdatedDate to set
	 */
	public void setLastUpdatedDate(String lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	/**
	 * @return the tables
	 */
	public List<ConfigTable> getTables() {
		return tables;
	}

	/**
	 * @param tables the tables to set
	 */
	public void setTables(List<ConfigTable> tables) {
		this.tables = tables;
	}

	/**
	 * @return the s3CodeBucket
	 */
	public String getS3CodeBucket() {
		return s3CodeBucket;
	}

	/**
	 * @param s3CodeBucket the s3CodeBucket to set
	 */
	public void setS3CodeBucket(String s3CodeBucket) {
		this.s3CodeBucket = s3CodeBucket;
	}

	/**
	 * @return the tablesMap
	 */
	public Map<String, ConfigTable> getTablesMap() {
		return tablesMap;
	}

	/**
	 * @return the lambdasMap
	 */
	public Map<String, ConfigLambda> getLambdasMap() {
		return lambdasMap;
	}

	/**
	 * @return the lambdas
	 */
	public List<ConfigLambda> getLambdas() {
		return lambdas;
	}

	/**
	 * @param lambdas the lambdas to set
	 */
	public void setLambdas(List<ConfigLambda> lambdas) {
		this.lambdas = lambdas;
	}

	/**
	 * Get the logical lambda names
	 * 
	 * @return
	 */
	public List<String> getLogicalLambdaNames() {
		
		return this.lambdas.stream()
				.map(x -> x.getLogicalName())
				.collect(Collectors.toList());
	}
	
	/**
	 * Get the deployment lambda names
	 * 
	 * @return
	 */
	public List<String> getDeploymentLambdaNames() {
		
		return this.lambdas.stream()
				.map(x -> x.getLambdaDeploymentName())
				.collect(Collectors.toList());
	}

	/**
	 * Retrieve the DynamoDB configuration info by provided domain object name
	 * 
	 * @param domainObjectName
	 * @return ConfigTableInfo
	 */
	public ConfigTable getConfigTableInfo(String logicalTableName) {
		
		Utils.throwIfNullOrEmptyString(logicalTableName, "Missing domain object name!");
		
		return this.tablesMap.get(logicalTableName.trim());
	}
	
	/**
	 * Get configuration lambda info
	 * Lambda's full Java class name has to be provided.
	 *  
	 * @param lambdaClassName
	 * @return ConfigLambdaInfo
	 */
	public ConfigLambda getConfigLambdaInfoByLogicalName(String lambdaLogicalName) {
		
		Utils.throwIfNullOrEmptyString(lambdaLogicalName, "Missing Lambda logical name!");
		
		return this.lambdasMap.get(lambdaLogicalName.trim());
	}

	/**
	 * Get the Lambda config info by the deployment name
	 * 
	 * @param lambdaName
	 * @return
	 */
	public ConfigLambda getConfigLambdaInfoByDeploymentName(String lambdaDeploymentName) {
		
		Utils.throwIfNullOrEmptyString(lambdaDeploymentName, "Missing Lambda deployment name!");
		
		for(ConfigLambda cli : this.lambdas) {
			if (cli.getLambdaDeploymentName().equalsIgnoreCase(lambdaDeploymentName)) return cli;
		}
		
		return null;
	}
	
	// ------------ fields

	@JsonProperty("version")
	private String version = null;
	
	@JsonProperty("last_updated_date")
	private String lastUpdatedDate = null;
	
	@JsonProperty("tables")
	private List<ConfigTable> tables = null;
	
	@JsonIgnore private Map<String, ConfigTable> tablesMap = null;
	@JsonIgnore private Map<String, ConfigLambda> lambdasMap = null;
	
	@JsonProperty("s3_code_bucket")
	private String s3CodeBucket = null;
	
	@JsonProperty("lambdas")
	private List<ConfigLambda> lambdas = null;
	
} // end Configuration