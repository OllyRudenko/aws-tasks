package com.task10.web;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
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
        CognitoService cognitoService = new CognitoServiceImpl(region);
        try {
            String token = cognitoService.loginUser(userName, password);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", token);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(response.toString());
        } catch (RuntimeException e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400);
        }
    }
}
