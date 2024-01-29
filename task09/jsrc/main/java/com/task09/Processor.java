package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "processor",
        roleName = "processor-role",
        tracingMode = TracingMode.Active
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "target_table", value = "${target_table}")
}
)
public class Processor implements RequestHandler<Object, Map<String, Object>> {

    public Map<String, Object> handleRequest(Object request, Context context) {
        String forecast = "";

        try {
            forecast = MeteoApi.getWeatherForecast();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(System.getenv("region"))
                //.withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
                .build();
        DynamoDB dynamoDB = new DynamoDB(clientDynamoDB);
        Table auditTable = dynamoDB.getTable(System.getenv("target_table"));

        Item item = new Item()
                .withString("id", generateUniqueID())
                .with("forecast", forecast);
        auditTable.putItem(item);

        System.out.println("Hello from lambda X-RAY");
        Map<String, Object> resultMap = getResponse(item);
        return resultMap;
    }

    private Map<String, Object> getResponse(Item item) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("statusCode", 200);
        resultMap.put("id", item.get("id").toString());
        resultMap.put("forecast", item.get("forecast"));
        return resultMap;
    }

    private static String generateUniqueID() {
        return UUID.randomUUID().toString();
    }
}
