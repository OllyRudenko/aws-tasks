package com.awstasks.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.awstasks.task10.web.Resources;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "api_handler",
        roleName = "api_handler-role"
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "tables_table", value = "${tables_table}"),
        @EnvironmentVariable(key = "reservations_table", value = "${reservations_table}")
})
public class ApiHandler implements RequestHandler<Object, APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(Object request, Context context) {
        Map<String, String> sysEnv = new HashMap<>();
        sysEnv.put("region", System.getenv("region"));
        sysEnv.put("tables_table", System.getenv("tables_table"));
        sysEnv.put("reservations_table", System.getenv("reservations_table"));

        System.out.println("Hello from lambda COGNITO");
        LinkedHashMap apiRequest = (LinkedHashMap) request;
        System.out.println("ALL " + apiRequest.toString());
        System.out.println("queryStringParameters " + apiRequest.get("queryStringParameters"));
        System.out.println(apiRequest.get("resource"));
        System.out.println(apiRequest.get("body"));

        return new Resources().findResourceAndExecute(String.valueOf(apiRequest.get("resource")), apiRequest, sysEnv);
    }

}
