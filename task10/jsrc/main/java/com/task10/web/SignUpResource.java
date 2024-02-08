package com.task10.web;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task10.service.CognitoService;
import com.task10.service.CognitoServiceImpl;
import com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.Map;

public class SignUpResource extends BaseResourceModel {

    @Override
    public APIGatewayProxyResponseEvent execute(Map apiRequest, Map<String, String> sysEnv) {
        String body = apiRequest.get("body").toString();
        System.out.println("BODY " + body);

        Gson gson = new Gson();
        LinkedHashMap<String, Object> parsedBody = gson.fromJson(body, LinkedHashMap.class);

        String firstName = (String) parsedBody.get("firstName");
        String lastName = (String) parsedBody.get("lastName");
        String userName = (String) parsedBody.get("email");
        String password = (String) parsedBody.get("password");

        String region = sysEnv.get("region");
        String userPoolName = sysEnv.get("booking_userpool");
        CognitoService cognitoService = new CognitoServiceImpl(region, userPoolName);
        try {
            cognitoService.signUp(firstName, lastName, userName, password);
            cognitoService.listAllUsers();
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200);
        } catch (RuntimeException e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400);
        }
    }
}
