package com.awsblog.queueing.cdk;

import java.util.Locale;
import java.util.Map;

import com.awsblog.queueing.config.ConfigIndex;
import com.awsblog.queueing.config.ConfigTable;
import com.awsblog.queueing.config.Configuration;
import com.awsblog.queueing.utils.Utils;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;
import software.amazon.awscdk.services.iam.IRole;

/**
 * Stack for creating a DynamoDB
 *
 * @author zorani
 *
 */
public class DynamoStack extends Stack {

	/**
	 * C-tor
	 *
	 * @param parent
	 * @param id
	 * @param jsonMetaLocation
	 * @param jsonConfigLocation
	 */
	public DynamoStack(final Construct parent, final String id, StackProps props,
			Configuration config, Map<String, IRole> roles) {

		this(parent, id, config, props, roles);
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
	public DynamoStack(final Construct parent, final String id,
							Configuration config, final StackProps props, Map<String, IRole> roles) {

		super(parent, id, props);

		Table table = null;

		int count = 0;

		for(ConfigTable tableMeta : config.getTables()) {

			System.out.printf("[%d] CDK .. table processing: [%s]%n", ++count, tableMeta.getTableName());

			//String logicalTableName = tableMeta.getLogicalName();

			AttributeType pkType = AttributeType.STRING;
			if (tableMeta.getPartitionKey().getAttributeType().toLowerCase().startsWith("num")) pkType = AttributeType.NUMBER;
			else if (tableMeta.getPartitionKey().getAttributeType().toLowerCase().startsWith("bin")) pkType = AttributeType.BINARY;

			TableProps.Builder tableBuilder = TableProps.builder()
					.tableName(tableMeta.getTableName())
					//.withBillingMode(BillingMode.PAY_PER_REQUEST)
					.serverSideEncryption(true)
					.partitionKey(Attribute.builder()
							.name(tableMeta.getPartitionKey().getAttributeName())
							.type(pkType)
							.build());

			if (!Utils.checkIfNullObject(tableMeta.getSortKey())) {

				AttributeType skType = AttributeType.STRING;
				if (tableMeta.getSortKey().getAttributeType().toLowerCase(Locale.ENGLISH).startsWith("num")) pkType = AttributeType.NUMBER;
				else if (tableMeta.getSortKey().getAttributeType().toLowerCase(Locale.ENGLISH).startsWith("bin")) pkType = AttributeType.BINARY;

				tableBuilder.sortKey(Attribute.builder()
						.name(tableMeta.getSortKey().getAttributeName())
						.type(skType)
						.build());
			}

			if (tableMeta.getReadCapacity() > 0) tableBuilder.readCapacity(tableMeta.getReadCapacity());
			if (tableMeta.getWriteCapacity() > 0) tableBuilder.writeCapacity(tableMeta.getWriteCapacity());

			table = new Table(this, "queue-sample-" + tableMeta.getLogicalName(),
					tableBuilder.build());

			// index creation
			for(ConfigIndex indexMeta : tableMeta.getIndexes()) {

				System.out.printf(" >> GSI ... build: [%s]%n", indexMeta.getName());

				AttributeType keyType = AttributeType.STRING;
				if (indexMeta.getHashKey().getAttributeType().toLowerCase().startsWith("num")) keyType = AttributeType.NUMBER;
				else if (indexMeta.getHashKey().getAttributeType().toLowerCase().startsWith("bin")) keyType = AttributeType.BINARY;

				GlobalSecondaryIndexProps.Builder b = GlobalSecondaryIndexProps.builder()
						.indexName(indexMeta.getName())
						.partitionKey(Attribute.builder()
								.name(indexMeta.getHashKey().getAttributeName())
								.type(keyType)
								.build());

				if (indexMeta.getReadCapacity() > 0) b.readCapacity(indexMeta.getReadCapacity());
				if (indexMeta.getWriteCapacity() > 0) b.writeCapacity(indexMeta.getWriteCapacity());
				
				if (Utils.checkIfNotNullObject(indexMeta.getSortKey())) {

					keyType = AttributeType.STRING;
					if (indexMeta.getSortKey().getAttributeType().toLowerCase().startsWith("num")) keyType = AttributeType.NUMBER;
					else if (indexMeta.getSortKey().getAttributeType().toLowerCase().startsWith("bin")) keyType = AttributeType.BINARY;

					b.sortKey(Attribute.builder()
							.name(indexMeta.getSortKey().getAttributeName())
							.type(keyType)
							.build());
				}

				table.addGlobalSecondaryIndex(b
						.build());
			}
		}
	}

} // end DynamoStack