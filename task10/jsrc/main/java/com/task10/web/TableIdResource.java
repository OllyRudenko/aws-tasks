package com.task10.web;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.task10.dynamoDB.TableDynamoDB;
import com.task10.models.Table;
import com.task10.utils.ConverterUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

public class TableIdResource extends BaseResourceModel {
    @Override
    public APIGatewayProxyResponseEvent execute(Map apiRequest, Map<String, String> sysEnv) {
        String httpMethod = (String) apiRequest.get("httpMethod");

        System.out.println("METHOD " + httpMethod);
        if (httpMethod.equals("GET")) {
            String tableId = apiRequest.get("path").toString().substring("/tables/".length());

            TableDynamoDB tableDynamoDB = new TableDynamoDB();
            Map<String, AttributeValue> table = tableDynamoDB
                    .get(sysEnv.get("region"), sysEnv.get("tables_table"), tableId);

            Table result = ConverterUtil.convertItemToTable(table);
            System.out.println("RESULT " + result);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(new Gson().toJson(result));
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(500);
    }

    @Deprecated
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
}
