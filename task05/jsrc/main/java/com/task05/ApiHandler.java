package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.task05.model.Event;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
        roleName = "api_handler-role"
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "target_table", value = "${target_table}")
})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String TABLE_NAME = "Events";
    private static final String INSERT = "INSERT";
    private AmazonS3 s3Client;
    private ObjectMapper objectMapper;
    JSONParser jsonParser = new JSONParser();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String requestBody = request.getBody();
        System.out.println("!!!!!!! Hello " + requestBody);
        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(System.getenv("region")).build();
        DynamoDB dynamoDB = new DynamoDB(clientDynamoDB);
        Event event = getEventFromRequest(requestBody);

        // save to DB
        Table table = dynamoDB.getTable(System.getenv("target_table"));

        Item item = new Item().withString("id", event.getId())
                .withInt("principalId", event.getPrincipalId())
                .withString("createdAt", event.getCreatedAt())
                .withMap("body", event.getBody());

        PutItemResult outcome = table
                .putItem(item).getPutItemResult();

        System.out.println("!!! event " + event);
        StringBuilder sb = new StringBuilder(event.getId()).append(event.getPrincipalId()).append(event.getCreatedAt())
                .append(event.getBody());

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(201);
        response.setBody(sb.toString());
        return response;
    }

    private Event getEventFromRequest(String requestBody) {
        Event event = new Event();
        JSONObject reqObject;
        JSONObject bodyJson;

        try {
            reqObject = (JSONObject) jsonParser.parse(requestBody);
            bodyJson = (JSONObject) new JSONParser().parse(reqObject.get("content").toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        event.setId(generateUniqueID());
        event.setPrincipalId(Integer.parseInt((reqObject.get("principalId").toString())));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        event.setCreatedAt(LocalDateTime.now().format(formatter));

        Map<String, String> body = getBodyValues(bodyJson.toString());
        event.setBody(body);

        return event;
    }

    private Map<String, String> getBodyValues(String bodyJson) {
        Gson g = new Gson();
        Map<String, String> body = g.fromJson(bodyJson, LinkedHashMap.class);
        return body;
    }

    private String generateUniqueID() {
        return UUID.randomUUID().toString();
    }
}
