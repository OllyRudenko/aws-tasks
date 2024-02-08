package com.task10.dynamoDB;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableDynamoDB {

    public boolean save(String region, String tableName,
                        Integer id, Integer number, Integer places, Boolean isVip, Integer minOrder) {
        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region).build();

        System.out.println("Hello from TableDynamoDB!!!! SAVE ");

        HashMap<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("id", new AttributeValue().withN(String.valueOf(id)));
        itemValues.put("number", new AttributeValue().withN(String.valueOf(number)));
        itemValues.put("places", new AttributeValue().withN(String.valueOf(places)));
        itemValues.put("isVip", new AttributeValue().withBOOL(isVip));
        itemValues.put("minOrder", new AttributeValue().withN(String.valueOf(minOrder)));

        PutItemRequest request = new PutItemRequest()
                .withTableName(tableName)
                .withItem(itemValues);
        System.out.println("PutItemRequest " + request.toString());

        try {
            System.out.println("Start try clientDynamoDB.putItem(request)");
            PutItemResult response = clientDynamoDB.putItem(request);
            System.out.println(tableName + " was successfully saved.");
            return true;
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
        } catch (RuntimeException e) {
            e.printStackTrace(System.out);
            System.err.println(e.getMessage());
        }
        System.out.println("return false");
        return false;
    }

    public List<Map<String, AttributeValue>> getAll(String region, String tableName) {
        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region).build();

        System.out.println("Hello from TableDynamoDB GET ALL!!!! ");

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(tableName);
        List<Map<String, AttributeValue>> itemList = new ArrayList<>();

        System.out.println("scanRequest " + scanRequest);

        try {
            // Виконуємо сканування таблиці та отримуємо відповідь
            ScanResult response = clientDynamoDB.scan(scanRequest);

            itemList.addAll(response.getItems());
            System.out.println("Item List: " + itemList);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
        return itemList;
    }

    public Map<String, AttributeValue> get(String region, String tableName, String id) {
        System.out.println("Hello from GET by ID " + id);
        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region).build();

        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put("id", new AttributeValue()
                .withN(id));

        GetItemRequest request = new GetItemRequest()
                .withKey(keyToGet)
                .withTableName(tableName);

        Map<String, AttributeValue> returnedItem = null;

        try {
            // If there is no matching item, GetItem does not return any data.
            returnedItem = clientDynamoDB.getItem(request).getItem();
            if (returnedItem.isEmpty())
                System.out.format("No item found with the key %s!\n", id);
            else {
                Set<String> keys = returnedItem.keySet();
                System.out.println("Amazon DynamoDB table attributes: \n");
                for (String key1 : keys) {
                    System.out.format("%s: %s\n", key1, returnedItem.get(key1).toString());
                }
            }

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return returnedItem;
    }
}
