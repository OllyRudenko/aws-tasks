package com.task10.web;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task10.dynamoDB.ReservationDynamoDB;
import com.task10.service.CognitoService;
import com.task10.service.CognitoServiceImpl;
import com.task10.utils.ConverterUtil;
import com.google.gson.Gson;

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
        CognitoService cognitoService = new CognitoServiceImpl(region);

        if (cognitoService.isValidIdToken(token)) {
            System.out.println("HELLO - token is valid");
        } else {
            System.out.println("HELLO - token is NOT valid");
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(401)
                    .withBody("HELLO - token is NOT valid");
        }

        if (httpMethod.equals("POST")) {

            String body = apiRequest.get("body").toString();
            Gson gson = new Gson();
            LinkedHashMap<String, Object> parsedBody = gson.fromJson(body, LinkedHashMap.class);

            return saveReceivedReservationToDynamoDB(sysEnv, parsedBody);

        } else if (httpMethod.equals("GET")) {

            ReservationDynamoDB reservationDynamoDB = new ReservationDynamoDB();
            List<Map<String, AttributeValue>> result = reservationDynamoDB.getAll(sysEnv.get("region"),
                    sysEnv.get("reservations_table"));

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(ConverterUtil.convertResponseWithListToJson(ConverterUtil.convertReservationItems(result)));
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(400);
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
                .save(sysEnv.get("region"), sysEnv.get("reservations_table"), tableNumber, clientName, phoneNumber,
                        date, slotTimeStart, slotTimeEnd);

        Map<String, Object> response = new HashMap<>();
        response.put("id", result);

        if (result.length() == 0) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("Reservation can't be created");
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(ConverterUtil.convertResponseToJson(response));
    }
}
