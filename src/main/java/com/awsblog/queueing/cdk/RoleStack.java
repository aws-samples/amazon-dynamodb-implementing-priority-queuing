package com.awsblog.queueing.cdk;

import java.util.HashMap;
import java.util.Map;

import com.awsblog.queueing.config.ConfigTable;
import com.awsblog.queueing.config.Configuration;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.PolicyDocument;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.iam.ServicePrincipalOpts;
import software.amazon.awscdk.services.iam.RoleProps.Builder;

/**
 * Stack for creating an IAM Role
 *
 * @author zorani
 *
 */
public class RoleStack extends Stack {

	/**
	 * C-tor
	 *
	 * @param parent
	 * @param id
	 * @param props
	 * @param config
	 * @param roles
	 */
	public RoleStack(final Construct parent, final String id, StackProps props,
			Configuration config, Map<String, IRole> roles) {

		this(parent, id, config, props, roles);
	}

	/**
	 * C-tor with the properties
	 *
	 * @param parent
	 * @param id
	 * @param config
	 * @param props
	 * @param roles
	 */
	public RoleStack(final Construct parent, final String id,
							Configuration config, final StackProps props, Map<String, IRole> roles) {

		super(parent, id, props);
		
		PolicyDocument pd = new PolicyDocument();

		for(ConfigTable tbl : config.getTables()) {

			// create a DynamoDB IAM policy statement
			PolicyStatement ps = new PolicyStatement(); 

			ps.addActions("dynamodb:GetItem", "dynamodb:BatchGetItem", 
							"dynamodb:PutItem", "dynamodb:UpdateItem",
							"dynamodb:BatchWriteItem", "dynamodb:GetRecords", "dynamodb:DeleteItem",
							"dynamodb:Query", "dynamodb:DescribeTable", "dynamodb:Scan", "dynamodb:TagResource");
			ps.addResources("arn:aws:dynamodb:*:*:table/" + tbl.getTableName(), 
							"arn:aws:dynamodb:*:*:table/" + tbl.getTableName() + "/index/*");
			
			pd.addStatements(ps);
		}

		// CloudWatchLogs
		PolicyStatement logs_ps = new PolicyStatement(); 
		logs_ps.addActions("logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents", "logs:DescribeLogStreams");
		logs_ps.addAllResources();
		pd.addStatements(logs_ps);

		// ec2 permissions
		PolicyStatement ec2_ps = new PolicyStatement(); 
		ec2_ps.addActions("ec2:CreateNetworkInterface", "ec2:DescribeNetworkInterfaces", "ec2:DeleteNetworkInterface", 
				"ec2:DescribeSecurityGroups", "ec2:DescribeSubnets", "ec2:DescribeVpcs");
		ec2_ps.addAllResources();
		pd.addStatements(ec2_ps);
		
		// SNS policy
		PolicyStatement sns_ps = new PolicyStatement(); 
		sns_ps.addActions("sns:List*", "sns:Publish");
		sns_ps.addAllResources();
		pd.addStatements(sns_ps);

		// CloudWatchLogs
		PolicyStatement cw_ps = new PolicyStatement(); 
		cw_ps.addActions("cloudwatch:*");
		cw_ps.addAllResources();
		pd.addStatements(cw_ps);

		// Lambda execution
		PolicyStatement lambda_exec_ps = new PolicyStatement(); 
		lambda_exec_ps.addActions("lambda:InvokeFunction");
		lambda_exec_ps.addAllResources();
		
		Map<String, PolicyDocument> map = new HashMap<>();
		map.put("aws-blog-queue.lambda.policy_document", pd);

		ServicePrincipalOpts.Builder spob = new ServicePrincipalOpts.Builder();

		Builder b = new Builder()
				.roleName("aws-blog-queue.lambda.role")
				.assumedBy(new ServicePrincipal("lambda.amazonaws.com", spob.build()))
				.inlinePolicies(map);

		IRole role = new Role(this, "aws-blog-queue.Role", b.build());
		roles.put("lambda-role", role);
	}

} // end RoleStack