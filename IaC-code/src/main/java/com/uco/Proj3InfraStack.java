package com.uco;

import software.constructs.Construct;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.cognito.*;
import software.amazon.awscdk.services.config.CustomRule;
import software.amazon.awscdk.services.amplify.*;

public class Proj3InfraStack extends Stack {
    public Proj3InfraStack(final Construct scope, final String id) {
        super(scope, id);

        // S3 Bucket
        Bucket bucket = Bucket.Builder.create(this, "Proj3Bucket")
                .bucketName("proj3-uco-bucket")
                .cors(Arrays.asList(
                        CorsRule.builder()
                                .allowedOrigins(Arrays.asList("*"))
                                .allowedMethods(Arrays.asList(HttpMethods.PUT, HttpMethods.POST, HttpMethods.GET))
                                .allowedHeaders(Arrays.asList("*"))
                                .build()))
                .build();

        // DynamoDB
        Table table = Table.Builder.create(this, "Proj3Records")
                .tableName("proj3-records")
                .partitionKey(Attribute.builder().name("email").type(AttributeType.STRING).build())
                .sortKey(Attribute.builder().name("classdate").type(AttributeType.STRING).build())
                .removalPolicy(RemovalPolicy.RETAIN)
                .build();

        // Lambda Iam Role
        Map<String, PolicyDocument> inlinePolicies = new HashMap<>();
        inlinePolicies.put("CustomPolicy",
                PolicyDocument.Builder.create().statements(
                        Arrays.asList(
                                PolicyStatement.Builder.create()
                                        .effect(Effect.ALLOW)
                                        .actions(Arrays.asList(
                                                "s3:GetObject",
                                                "s3:GetBucketLocation",
                                                "s3:ListBucket",
                                                "rekognition:CompareFaces"))
                                        .resources(Arrays.asList(
                                                "arn:aws:s3:::proj3-uco-bucket",
                                                "arn:aws:s3:::proj3-uco-bucket/*"))
                                        .build(),
                                PolicyStatement.Builder.create()
                                        .effect(Effect.ALLOW)
                                        .actions(Arrays.asList("rekognition:CompareFaces", "rekognition:DetectFaces"))
                                        .resources(Arrays.asList("*"))
                                        .build(),
                                PolicyStatement.Builder.create()
                                        .effect(Effect.ALLOW)
                                        .actions(Arrays.asList("textract:DetectDocumentText"))
                                        .resources(Arrays.asList("*"))
                                        .build(),
                                PolicyStatement.Builder.create()
                                        .effect(Effect.ALLOW)
                                        .actions(Arrays.asList("dynamodb:PutItem"))
                                        .resources(Arrays
                                                .asList("arn:aws:dynamodb:us-east-1:976193232154:table/proj3-records"))
                                        .build()))
                        .build());
        Role lambdaRole = Role.Builder.create(this, "LambdaExecutionRole")
                .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
                .managedPolicies(Arrays.asList(
                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole")))
                .inlinePolicies(inlinePolicies)
                .build();

        Map<String, String> environment = new HashMap<>();
        environment.put("S3_BUCKET_NAME", "proj3-uco-bucket");
        environment.put("DYNAMODB_TABLE_NAME", "proj3-records");

        Function attendanceLambda = Function.Builder.create(this, "Proj3AttendanceLambda")
                .functionName("proj3-attendanceMatch")
                .runtime(Runtime.PYTHON_3_12)
                .handler("attendanceMatch.lambda_handler")
                .code(Code.fromAsset("lambda")) // adjust path to attendanceMatch.py
                .timeout(Duration.seconds(30))
                .environment(environment)
                .role(lambdaRole)
                .build();
        bucket.grantRead(attendanceLambda);
        table.grantWriteData(attendanceLambda);

        // API Gateway
        RestApi api = RestApi.Builder.create(this, "Proj3Api")
                .restApiName("proj3-attendance-api")
                .build();

        Resource attendance = api.getRoot().addResource("attendance");
        // OPTIONS method for preflight
        attendance.addMethod("OPTIONS",
                MockIntegration.Builder.create()
                        .integrationResponses(Arrays.asList(IntegrationResponse.builder()
                                .statusCode("200")
                                .responseParameters(new HashMap<String, String>() {
                                    {
                                        put("method.response.header.Access-Control-Allow-Headers",
                                                "'Content-Type,X-Amz-Date,Authorization,X-Api-Key'");
                                        put("method.response.header.Access-Control-Allow-Origin", "'*'");
                                        put("method.response.header.Access-Control-Allow-Methods",
                                                "'OPTIONS,POST,GET'");
                                    }
                                })
                                .build()))
                        .passthroughBehavior(PassthroughBehavior.NEVER)
                        .requestTemplates(Collections.singletonMap("application/json", "{\"statusCode\": 200}"))
                        .build(),
                MethodOptions.builder()
                        .methodResponses(Arrays.asList(MethodResponse.builder()
                                .statusCode("200")
                                .responseParameters(new HashMap<String, Boolean>() {
                                    {
                                        put("method.response.header.Access-Control-Allow-Headers", true);
                                        put("method.response.header.Access-Control-Allow-Methods", true);
                                        put("method.response.header.Access-Control-Allow-Origin", true);
                                    }
                                })
                                .build()))
                        .build());

        // Lambda integration with integration responses
        LambdaIntegration lambdaIntegration = LambdaIntegration.Builder.create(attendanceLambda)
                .proxy(false) // Important: proxy(false) to control headers
                .integrationResponses(Arrays.asList(
                        IntegrationResponse.builder()
                                .statusCode("200")
                                .responseParameters(Collections.singletonMap(
                                        "method.response.header.Access-Control-Allow-Origin", "'*'"))
                                .build(),
                        IntegrationResponse.builder()
                                .statusCode("400")
                                .responseParameters(Collections.singletonMap(
                                        "method.response.header.Access-Control-Allow-Origin", "'*'"))
                                .build()))
                .build();

        // Method response with headers allowed
        attendance.addMethod("POST", lambdaIntegration, MethodOptions.builder()
                .methodResponses(Arrays.asList(
                        MethodResponse.builder()
                                .statusCode("200")
                                .responseParameters(Collections.singletonMap(
                                        "method.response.header.Access-Control-Allow-Origin", true))
                                .build(),
                        MethodResponse.builder()
                                .statusCode("400")
                                .responseParameters(Collections.singletonMap(
                                        "method.response.header.Access-Control-Allow-Origin", true))
                                .build()))
                .build());

        // Cognito Identity Pool
        CfnIdentityPool identityPool = CfnIdentityPool.Builder.create(this, "Proj3IdentityPool")
                .allowUnauthenticatedIdentities(true)
                .identityPoolName("proj3-uco-pool")
                .build();

        // IAM Role for unauthenticated users
        Map<String, Object> assumeRoleCondition = new HashMap<>();
        assumeRoleCondition.put("StringEquals",
                Collections.singletonMap("cognito-identity.amazonaws.com:aud", identityPool.getRef()));
        assumeRoleCondition.put("ForAnyValue:StringLike",
                Collections.singletonMap("cognito-identity.amazonaws.com:amr", "unauthenticated"));

        FederatedPrincipal unauthPrincipal = new FederatedPrincipal(
                "cognito-identity.amazonaws.com",
                assumeRoleCondition,
                "sts:AssumeRoleWithWebIdentity");

        Map<String, PolicyDocument> unauthPolicies = new HashMap<>();
        unauthPolicies.put("S3Access", PolicyDocument.Builder.create()
                .statements(Arrays.asList(
                        PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .actions(Arrays.asList("s3:PutObject", "s3:GetObject"))
                                .resources(Arrays.asList("arn:aws:s3:::proj3-uco-bucket/*"))
                                .build()))
                .build());

        Role unauthRole = Role.Builder.create(this, "UnauthenticatedRole")
                .assumedBy(unauthPrincipal)
                .inlinePolicies(unauthPolicies)
                .managedPolicies(Arrays.asList(
                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole")))
                .build();

        // Attach role to Identity Pool
        Map<String, String> rolesMap = new HashMap<>();
        rolesMap.put("unauthenticated", unauthRole.getRoleArn());

        CfnIdentityPoolRoleAttachment.Builder.create(this, "IdentityPoolRoleAttachment")
                .identityPoolId(identityPool.getRef())
                .roles(rolesMap)
                .build();

        // Output: Identity Pool ID
        CfnOutput.Builder.create(this, "CognitoIdentityPoolId")
                .description("Cognito Identity Pool ID for unauthenticated access")
                .value(identityPool.getRef())
                .build();

        // Output: API Invoke URL
        CfnOutput.Builder.create(this, "AttendanceAPIInvokeURL")
                .description("API Gateway invoke URL for attendance endpoint")
                .value(api.getUrl() + "attendance")
                .build();

    }
}