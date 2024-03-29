package com.task10.dynamoDB;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReservationDynamoDB {
    public String save(String region, String reservationsTableName, String tablesTableName, Integer tableNumber,
                       String clientName, String phoneNumber, String date, String slotTimeStart, String slotTimeEnd) {
        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region).build();

        System.out.println("Hello from ReservationDynamoDB SAVE !!! ");

        if (isExistTable(region, tablesTableName, tableNumber)
                &&
                checkAndSave(region, reservationsTableName, tableNumber, date, slotTimeStart, slotTimeEnd)) {
            String reservationId = generateUniqueID();
            HashMap<String, AttributeValue> itemValues = new HashMap<>();
            itemValues.put("id", new AttributeValue().withS(reservationId));
            itemValues.put("tableNumber", new AttributeValue().withN(String.valueOf(tableNumber)));
            itemValues.put("clientName", new AttributeValue().withS(clientName));
            itemValues.put("phoneNumber", new AttributeValue().withS(phoneNumber));
            itemValues.put("date", new AttributeValue().withS(date));
            itemValues.put("slotTimeStart", new AttributeValue().withS(slotTimeStart));
            itemValues.put("slotTimeEnd", new AttributeValue().withS(slotTimeEnd));

            PutItemRequest request = new PutItemRequest()
                    .withTableName(reservationsTableName)
                    .withItem(itemValues);

            try {
                PutItemResult response = clientDynamoDB.putItem(request);
                System.out.println("RESPONSE " + response.toString());
                System.out.println(reservationsTableName + " was successfully saved");
                return reservationId;
            } catch (ResourceNotFoundException e) {
                System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", reservationsTableName);
                System.err.println("Be sure that it exists and that you've typed its name correctly!");
            } catch (RuntimeException e) {
                e.printStackTrace(System.out);
                System.err.println(e.getMessage());
            }
        }
        return "";
    }

    public boolean checkAndSave(String region, String tableName, Integer tableNumber, String date, String slotTimeStart, String slotTimeEnd) {
        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region).build();

        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#date", "date");

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":tableNumber", new AttributeValue().withN(String.valueOf(tableNumber)));
        expressionAttributeValues.put(":date", new AttributeValue().withS(date));
        expressionAttributeValues.put(":startTime", new AttributeValue().withS(slotTimeStart));
        expressionAttributeValues.put(":endTime", new AttributeValue().withS(slotTimeEnd));

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(tableName)
                .withFilterExpression("tableNumber = :tableNumber AND #date = :date " +
                        "AND :startTime < slotTimeEnd AND :endTime > slotTimeStart")
                .withExpressionAttributeNames(expressionAttributeNames)
                .withExpressionAttributeValues(expressionAttributeValues);

        try {
            ScanResult response = clientDynamoDB.scan(scanRequest);
            System.out.println("IS RESERVED TIME? (size = 0) " + response.getItems().size());
            return response.getItems().size() == 0;
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }

        return false;
    }

    public boolean isExistTable(String region, String tablesTableName, Integer tableNumber) {
        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region).build();

        // Create filtering and change name for system reserved param 'number'
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#number", "number");

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":number", new AttributeValue().withN(String.valueOf(tableNumber)));

        // prepare to scan
        String filterExpression = "#number = :number";

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(tablesTableName)
                .withFilterExpression(filterExpression)
                .withExpressionAttributeNames(expressionAttributeNames)
                .withExpressionAttributeValues(expressionAttributeValues);
        System.out.println("Scan Request " + scanRequest);

        try {
            ScanResult response = clientDynamoDB.scan(scanRequest);
            System.out.println("RESPONSE " + response.getItems());

            System.out.println("IS EXIST TABLE? (size) " + response.getItems().size());
            return !response.getItems().isEmpty();
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }

        return false;
    }

    private static String generateUniqueID() {
        return UUID.randomUUID().toString();
    }

    public List<Map<String, AttributeValue>> getAll(String region, String tableName) {
        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region).build();

        System.out.println("Hello from ReservationDynamoDB GET ALL!!!! " + tableName);

        ScanRequest scanRequest = new ScanRequest().withTableName(tableName);
        System.out.println("SCAN " + scanRequest.toString());

        List<Map<String, AttributeValue>> itemList = new ArrayList<>();

        try {
            // scan table
            ScanResult response = clientDynamoDB.scan(scanRequest);
            System.out.println("ALL Items reservations " + response.getItems().toString());

            itemList.addAll(response.getItems());
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
        return itemList;
    }
}
