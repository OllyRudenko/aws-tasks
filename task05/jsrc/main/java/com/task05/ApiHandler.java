package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.GetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.task05.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.time.LocalDateTime;
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

        // uuid
        String eventID = generateUniqueID();

        // save to DB
        Table table = dynamoDB.getTable(System.getenv("target_table"));

        Item item = new Item().withString("id", eventID)
                        .withNumber("principalId", event.getPrincipalId())
                        .withString("createdAt", event.getCreatedAt().toString())
                        .withMap("body", event.getBody());

        // get from DB saved object
        PutItemOutcome outcome = table
                .putItem(item);
        System.out.println("Outcome !!! " + outcome);
        GetItemOutcome getItemOutcome = table.getItemOutcome(
                new PrimaryKey("principalId", event.getPrincipalId()));
        Item eventNew = getItemOutcome.getItem();


        System.out.println("eventNew " + eventNew.toJSON());

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(201);
        response.setBody(eventNew.toJSON());
        return response;
    }

    private Event getEventFromRequest(String requestBody) {
        Event event = new Event();
        JSONObject reqObject;

        try {
            reqObject = (JSONObject) jsonParser.parse(requestBody);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        event.setId(generateUniqueID());
        event.setPrincipalId(Integer.valueOf((reqObject.get("principalId").toString())));
        event.setCreatedAt(LocalDateTime.now());
        JSONObject bodyJson;
        try {
            bodyJson = (JSONObject) new JSONParser().parse(reqObject.get("content").toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> body = getBodyValues(bodyJson);
        event.setBody(body);

        return event;
    }

    private Map<String, String> getBodyValues(JSONObject bodyJson) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("name", bodyJson.get("name").toString());
        body.put("surname", bodyJson.get("surname").toString());
        return body;
    }

    private String generateUniqueID() {
        return UUID.randomUUID().toString();
    }

}
