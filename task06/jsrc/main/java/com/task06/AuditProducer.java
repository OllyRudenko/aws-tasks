package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "audit_producer",
        roleName = "audit_producer-role"
)
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 1)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "target_table", value = "${target_table}")
})
public class AuditProducer implements RequestHandler<DynamodbEvent, String> {
    private final static String INSERT_ACTION = "INSERT";
    private final static String MODIFY_ACTION = "MODIFY";
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);

    public String handleRequest(DynamodbEvent ddbEvent, Context context) {
        for (DynamodbEvent.DynamodbStreamRecord record : ddbEvent.getRecords()) {
            System.out.println("NEW " + record.getDynamodb().getNewImage());
            System.out.println("OLD " + record.getDynamodb().getOldImage());
            System.out.println("ACTION: " + record.getEventName());
            System.out.println("BD " + record.getDynamodb().toString());
        }

        // connect with DBs
        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(System.getenv("region")).build();
        DynamoDB dynamoDB = new DynamoDB(clientDynamoDB);
        Table auditTable = dynamoDB.getTable(System.getenv("target_table"));
        DynamodbEvent.DynamodbStreamRecord receivedObject = ddbEvent.getRecords().get(0);

        if (receivedObject.getEventName().equals(INSERT_ACTION)) {
            Map<String, AttributeValue> newImage = receivedObject.getDynamodb().getNewImage();

            Map<String, String> newImageConverted = new HashMap<>();
            for (Map.Entry entry : newImage.entrySet()) {
                newImageConverted.put(String.valueOf(entry.getKey()), newImage.get(entry.getKey()).getS());
            }

            Item item = new Item()
                    .withString("id", generateUniqueID())
                    .with("itemKey", newImage.get("key").getS())
                    .withString("modificationTime", LocalDateTime.now().format(formatter))
                    .withMap("newValue", newImageConverted);

            auditTable.putItem(item);
        }

        // https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/JavaDocumentAPICRUDExample.html
        if (receivedObject.getEventName().equals(MODIFY_ACTION)) {
            Map<String, AttributeValue> newImage = receivedObject.getDynamodb().getNewImage();
            Map<String, AttributeValue> oldImage = receivedObject.getDynamodb().getOldImage();
            Map<String, String> newImageConverted = new HashMap<>();
            for (Map.Entry entry : newImage.entrySet()) {
                newImageConverted.put(String.valueOf(entry.getKey()),  newImage.get(entry.getKey()).getS());
            }

            auditTable
                    .putItem(new PutItemSpec().withItem(new Item()
                            .with("itemKey", newImageConverted.get("key"))
                            .withString("id", generateUniqueID())
                            .withString("modificationTime", LocalDateTime.now().format(formatter))
                            .withString("updatedAttribute", "value")
                            .with("oldValue", oldImage.get("value").getS())
                            .with("newValue", newImageConverted.get("value"))));
        }

        return "Successfully processed " + ddbEvent.getRecords().size() + " records.";
    }

    private String generateUniqueID() {
        return UUID.randomUUID().toString();
    }
}
