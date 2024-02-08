package com.task10.web;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.dynamoDB.ReservationDynamoDB;
import com.task10.models.Reservation;
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

public class ReservationsResource extends BaseResourceModel {
    @Override
    public APIGatewayProxyResponseEvent execute(Map apiRequest, Map<String, String> sysEnv) {
        LinkedHashMap<String, String> headers = (LinkedHashMap<String, String>) apiRequest.get("headers");
        String httpMethod = (String) apiRequest.get("httpMethod");

        String token = headers.get("Authorization");
        String region = sysEnv.get("region");
        String userPoolName = sysEnv.get("booking_userpool");
        CognitoService cognitoService = new CognitoServiceImpl(region, userPoolName);

//        if (cognitoService.isValidIdToken(token)) {
        if (cognitoService.isTokenValid(token)) {
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

            return saveReceivedReservationToDynamoDB(sysEnv, parsedBody);

        } else if (httpMethod.equals("GET")) {

            ReservationDynamoDB reservationDynamoDB = new ReservationDynamoDB();
            List<Map<String, AttributeValue>> reservations = reservationDynamoDB.getAll(sysEnv.get("region"),
                    sysEnv.get("reservations_table"));

            Map<String, List<Reservation>> result = ConverterUtil.convertReservationItems(reservations);
            System.out.println("RESULT " + result);

//            String response = ConverterUtil.convertResponseWithListToJson(result);
//            System.out.println("RESPONSE all reservations" + response);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(new Gson().toJson(result));
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(400);
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

    private APIGatewayProxyResponseEvent saveReceivedReservationToDynamoDB(Map<String, String> sysEnv,
                                                                           LinkedHashMap<String, Object> parsedBody) {
        Integer tableNumber = (int) Double.parseDouble(String.valueOf(parsedBody.get("tableNumber")));
        String clientName = String.valueOf(parsedBody.get("clientName"));
        String phoneNumber = String.valueOf(parsedBody.get("phoneNumber"));
        String date = String.valueOf(parsedBody.get("date").toString());
        String slotTimeStart = String.valueOf(String.valueOf(parsedBody.get("slotTimeStart")));
        String slotTimeEnd = String.valueOf(String.valueOf(parsedBody.get("slotTimeEnd")));

        ReservationDynamoDB reservationDynamoDB = new ReservationDynamoDB();
        String result = reservationDynamoDB
                .save(sysEnv.get("region"), sysEnv.get("reservations_table"), sysEnv.get("tables_table"), tableNumber, clientName, phoneNumber,
                        date, slotTimeStart, slotTimeEnd);

        Map<String, Object> response = new HashMap<>();
        response.put("reservationId", result);

        if (result.length() == 0) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400);
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(new Gson().toJson(response)); // ConverterUtil.convertResponseToJson(response)
    }
}
