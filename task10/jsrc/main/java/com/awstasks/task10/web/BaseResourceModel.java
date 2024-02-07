package com.awstasks.task10.web;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseResourceModel {
    public abstract APIGatewayProxyResponseEvent execute(Map apiRequest, Map<String, String> sysEnv);
}
