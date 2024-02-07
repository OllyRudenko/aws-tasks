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
    public String save(String region, String tableName, Integer tableNumber,
                       String clientName, String phoneNumber, String date, String slotTimeStart, String slotTimeEnd) {
        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region).build();

        System.out.println("Hello from ReservationDynamoDB SAVE!!!! ");

        if (checkAndSave(region, tableName, tableNumber, date, slotTimeStart, slotTimeEnd)) {
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
                    .withTableName(tableName)
                    .withItem(itemValues);

            try {
                PutItemResult response = clientDynamoDB.putItem(request);
                System.out.println(tableName + " was successfully updated. The request id is "
                        + response.toString());
                return reservationId;
            } catch (ResourceNotFoundException e) {
                System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
                System.err.println("Be sure that it exists and that you've typed its name correctly!");
            } catch (RuntimeException e) {
                System.err.println(e.getMessage());
            }
        }
        return "";
    }

    public boolean checkAndSave(String region, String tableName, Integer tableNumber, String date, String slotTimeStart, String slotTimeEnd) {
        // Створення клієнта DynamoDB
        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region).build();

        // Створення мапи для визначення умови фільтрації
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":tableNumber", new AttributeValue().withN(String.valueOf(tableNumber)));
        expressionAttributeValues.put(":date", new AttributeValue().withS(date));
        expressionAttributeValues.put(":startTime", new AttributeValue().withS(slotTimeStart));
        expressionAttributeValues.put(":endTime", new AttributeValue().withS(slotTimeEnd));

        // Підготовка параметрів запиту сканування
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(tableName)
                .withFilterExpression("tableNumber = :tableNumber AND date = :date " +
                        "AND :startTime < slotTimeEnd AND :endTime > slotTimeStart")
                .withExpressionAttributeValues(expressionAttributeValues);

        try {
            ScanResult response = clientDynamoDB.scan(scanRequest);

            return response.getItems().size() == 0;
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

        System.out.println("Hello from TableDynamoDB GET ALL!!!! " + tableName);

        ScanRequest scanRequest = new ScanRequest().withTableName(tableName);
        System.out.println("SCAN " + scanRequest.toString());
        List<Map<String, AttributeValue>> itemList = new ArrayList<>();

        try {
            // Виконую сканування таблиці та отримую відповідь
            ScanResult response = clientDynamoDB.scan(scanRequest);
            System.out.println("ALL Items reservations " + response.getItems().toString());
            itemList.addAll(response.getItems());
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
        return itemList;
    }
}
