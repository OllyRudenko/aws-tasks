package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
        Map<String, Object> itemMap;

        try {
            itemMap = convertWeatherDataToObject(MeteoApi.getWeatherForecast());
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(System.getenv("region"))
                .build();
        DynamoDB dynamoDB = new DynamoDB(clientDynamoDB);
        Table auditTable = dynamoDB.getTable(System.getenv("target_table"));

        Item item = new Item()
                .withString("id", itemMap.get("id").toString())
                .with("forecast", itemMap.get("forecast"));
        auditTable.putItem(new PutItemSpec().withItem(item));

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

    public static void main(String[] args) throws IOException, ParseException {
        System.out.println(convertWeatherDataToObject(MeteoApi.getWeatherForecast()));
    }

    public static Map<String, Object> convertWeatherDataToObject(String weatherData) throws ParseException, IOException {
        Map<String, Object> item = new HashMap<>();
        item.put("id", generateUniqueID());

        JSONObject jsonParser = (JSONObject) new JSONParser().parse(weatherData);

        Double elevation = (Double) new JSONParser().parse(jsonParser.get("elevation").toString());
        Double generationtime_ms = (Double) new JSONParser().parse(jsonParser.get("generationtime_ms").toString());
        Map<String, List> hourlyJSON = (Map<String, List>) new JSONParser().parse(jsonParser.get("hourly").toString());
        Map<String, String> hourly_units = (Map<String, String>) new JSONParser().parse(jsonParser.get("hourly_units").toString());
        Double latitude = (Double) new JSONParser().parse(jsonParser.get("latitude").toString());
        Double longitude = (Double) new JSONParser().parse(jsonParser.get("longitude").toString());
        Long utc_offset_seconds = (Long) new JSONParser().parse(jsonParser.get("utc_offset_seconds").toString());


        Map<String, Object> forecast = new HashMap<>();
        forecast.put("elevation", elevation);
        forecast.put("generationtime_ms", generationtime_ms);

        Map<String, Object> hourly = new HashMap<>();
        hourly.put("temperature_2m", hourlyJSON.get("temperature_2m"));
        hourly.put("time", hourlyJSON.get("time"));

        forecast.put("hourly", hourly);

        Map<String, String> hourlyUnits = new HashMap<>();
        hourlyUnits.put("temperature_2m", hourly_units.get("temperature_2m"));
        hourlyUnits.put("time", hourly_units.get("time"));

        forecast.put("hourly_units", hourlyUnits);

        forecast.put("latitude", latitude);
        forecast.put("longitude", longitude);
        forecast.put("timezone", "Europe/Kiev");
        forecast.put("timezone_abbreviation", "EET");
        forecast.put("utc_offset_seconds", utc_offset_seconds);

        item.put("forecast", forecast);

        return item;
    }
}
