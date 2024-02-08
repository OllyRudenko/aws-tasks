package com.task10.web;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.service.CognitoService;
import com.task10.service.CognitoServiceImpl;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SignInResource extends BaseResourceModel {
    @Override
    public APIGatewayProxyResponseEvent execute(Map apiRequest, Map<String, String> sysEnv) {
        String body = apiRequest.get("body").toString();

        Gson gson = new Gson();
        LinkedHashMap<String, Object> parsedBody = gson.fromJson(body, LinkedHashMap.class);

        String userName = (String) parsedBody.get("email");
        String password = (String) parsedBody.get("password");

        String region = sysEnv.get("region");
        String userPoolName = sysEnv.get("booking_userpool");
        CognitoService cognitoService = new CognitoServiceImpl(region, userPoolName);
        System.out.println("Before get login");

        try {
            String token = cognitoService.loginUser(userName, password);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", token);
            String jsonResponse = convertToJson(response);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(jsonResponse);
        } catch (RuntimeException e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400);
        }
    }

    private String convertToJson(Map<String, String> response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
