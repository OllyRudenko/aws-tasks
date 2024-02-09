package com.task10.web;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.LinkedHashMap;
import java.util.Map;

public class Resources {
    public final static String SING_UP = "/signup";
    public final static String SING_IN = "/signin";
    public final static String TABLES = "/tables";
    public final static String RESERVATIONS = "/reservations";
    public final static String TABLES_TABLE_ID = "/tables/{tableId}";

    public APIGatewayProxyResponseEvent findResourceAndExecute(String resource, LinkedHashMap apiRequest, Map<String, String> sysEnv) {
        System.out.println("Hello from APIGatewayProxyResponseEvent");
        System.out.println(resource);

        if (resource.equalsIgnoreCase(SING_UP)) {
            System.out.println("Hello from resource.equalsIgnoreCase(SING_UP) " + resource);
            return new SignUpResource().execute(apiRequest, sysEnv);
        } else if(resource.equalsIgnoreCase(SING_IN)){
            System.out.println("Hello from resource.equalsIgnoreCase(SING_IN) " + resource);
            return new SignInResource().execute(apiRequest, sysEnv);
        }else if(resource.equalsIgnoreCase(TABLES)){
            System.out.println("Hello from resource.equalsIgnoreCase(TABLES) " + resource);
            return new TablesResource().execute(apiRequest, sysEnv);
        }else if (resource.startsWith(TABLES_TABLE_ID)) {
            System.out.println("Hello from resource.equalsIgnoreCase(TABLES/TABLE_ID) " + resource);
            return new TableIdResource().execute(apiRequest, sysEnv);
        }else if (resource.equalsIgnoreCase(RESERVATIONS)){
            System.out.println("Hello from resource.equalsIgnoreCase(RESERVATIONS) " + resource);
            return new ReservationsResource().execute(apiRequest, sysEnv);
        }
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody("Wrong endpoint");
    }
}
