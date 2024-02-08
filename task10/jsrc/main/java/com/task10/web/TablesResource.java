package com.task10.web;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.dynamoDB.TableDynamoDB;
import com.task10.models.Table;
import com.task10.service.CognitoService;
import com.task10.service.CognitoServiceImpl;
import com.task10.utils.ConverterUtil;
import com.google.gson.Gson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TablesResource extends BaseResourceModel {
    @Override
    public APIGatewayProxyResponseEvent execute(Map apiRequest, Map<String, String> sysEnv) {

        LinkedHashMap<String, String> headers = (LinkedHashMap<String, String>) apiRequest.get("headers");
        LinkedHashMap<String, String> pathParam = (LinkedHashMap<String, String>) apiRequest.get("queryStringParameters");

        String httpMethod = (String) apiRequest.get("httpMethod");

        String authorization = headers.get("Authorization");
        String region = sysEnv.get("region");
        String userPoolName = sysEnv.get("booking_userpool");
        String token = "";
        if(Objects.isNull(pathParam)){
            token=authorization.replace("Bearer", "").trim();
        }else{
            token = authorization;
        }
        CognitoService cognitoService = new CognitoServiceImpl(region, userPoolName);
        if (cognitoService.isValidIdToken(token)) {
//        if (cognitoService.isTokenValid(token)) {
            System.out.println("HELLO - token is valid");
        } else {
            System.out.println("HELLO - token is NOT valid");
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(401)
                    .withBody("HELLO - token is NOT valid");
        }

        System.out.println("METHOD " + httpMethod);
        if (httpMethod.equals("POST")) {
            String body = apiRequest.get("body").toString();
            Gson gson = new Gson();
            LinkedHashMap<String, Object> parsedBody = gson.fromJson(body, LinkedHashMap.class);

            return saveReceivedTableToDynamoDB(sysEnv, parsedBody);
        } else if (httpMethod.equals("GET")) {
            if (Objects.isNull(pathParam)) {

                TableDynamoDB tableDynamoDB = new TableDynamoDB();
                List<Map<String, AttributeValue>> tables = tableDynamoDB
                        .getAll(sysEnv.get("region"), sysEnv.get("tables_table"));

                Map<String, List<Table>> result = ConverterUtil.convertItems(tables);
                System.out.println("RESULT " + result);

                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withBody(new Gson().toJson(result));  //ConverterUtil.convertResponseWithListToJson(result)
            } else {

                String tableId = pathParam.get("tableId");
                TableDynamoDB tableDynamoDB = new TableDynamoDB();
                Map<String, AttributeValue> table = tableDynamoDB
                        .get(sysEnv.get("region"), sysEnv.get("tables_table"), tableId);

                // Map<String, Object> result = ConverterUtil.convertItem(table);
                Table result = ConverterUtil.convertItemToTable(table);
                System.out.println("RESULT " + result);

//                String response = ConverterUtil.convertResponseToJson(result);
//                System.out.println("response " + response);

                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withBody(convertToJson(result));
            }
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(500);
    }

    public static String convertToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public JSONObject convert(Map<String, List> result) {
        JSONObject jsonResult = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        List<Map<String, Object>> tables = result.get("tables");

        for (Map<String, Object> table : tables) {
            JSONObject jsonTable = new JSONObject(table);
            jsonArray.add(jsonTable);
        }

        jsonResult.put("tables", jsonArray);

        System.out.println(jsonResult.toString());
        return jsonResult;
    }

    private APIGatewayProxyResponseEvent saveReceivedTableToDynamoDB(Map<String, String> sysEnv, LinkedHashMap<String, Object> parsedBody) {
        Integer id = (int) Double.parseDouble(String.valueOf(parsedBody.get("id")));
        Integer number = (int) Double.parseDouble(String.valueOf(parsedBody.get("number")));
        Integer places = (int) Double.parseDouble(String.valueOf(parsedBody.get("places")));
        Boolean isVip = Boolean.valueOf(parsedBody.get("isVip").toString());
        Integer minOrder = (int) Double.parseDouble(String.valueOf(parsedBody.get("minOrder")));

        TableDynamoDB tableDynamoDB = new TableDynamoDB();
        boolean result = tableDynamoDB
                .save(sysEnv.get("region"), sysEnv.get("tables_table"), id, number, places, isVip, minOrder);
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);

        if (!result) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("Object was not saved");
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(ConverterUtil.convertResponseToJson(response)); //
    }
}
