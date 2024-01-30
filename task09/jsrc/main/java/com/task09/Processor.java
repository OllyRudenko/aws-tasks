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
import com.task09.model.Forecast;
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
        Forecast forecast = null;

        try {
            forecast = convertWeatherDataToObject(MeteoApi.getWeatherForecast());
        } catch (IOException | ParseException e) {
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

    public static Forecast convertWeatherDataToObject(String weatherData) throws ParseException, IOException {
        Forecast forecast = new Forecast();
        JSONObject jsonParser = (JSONObject) new JSONParser().parse(weatherData);
        System.out.println(jsonParser);

        Double elevation = (Double) new JSONParser().parse(jsonParser.get("elevation").toString());
        forecast.setElevation(elevation);
        Double generationtime_ms = (Double) new JSONParser().parse(jsonParser.get("generationtime_ms").toString());
        forecast.setGenerationtime_ms(generationtime_ms);

        Map<String, List> hourly = (Map<String, List>) new JSONParser().parse(jsonParser.get("hourly").toString());
        forecast.setHourly(hourly);

        Map<String, String> hourly_units = (Map<String, String>) new JSONParser().parse(jsonParser.get("hourly_units").toString());
        forecast.setHourly_units(hourly_units);

        Double latitude = (Double) new JSONParser().parse(jsonParser.get("latitude").toString());
        forecast.setLatitude(latitude);

        Double longitude = (Double) new JSONParser().parse(jsonParser.get("longitude").toString());
        forecast.setLongitude(longitude);

        forecast.setTimezone("Europe/Kiev");
        forecast.setTimezone_abbreviation("EET");

        Long utc_offset_seconds = (Long) new JSONParser().parse(jsonParser.get("utc_offset_seconds").toString());
        forecast.setUtc_offset_seconds(utc_offset_seconds);

        return forecast;
    }
}
