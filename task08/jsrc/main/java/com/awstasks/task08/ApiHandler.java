package com.awstasks.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.awstasks.task08.MeteoApi.getWeatherForecast;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role"
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = {"lib/meteo-api.jar"},
		runtime = DeploymentRuntime.JAVA8,
		artifactExtension = ArtifactExtension.ZIP
)
public class ApiHandler implements RequestHandler<Object, Map<String, Object>> {

	public Map<String, Object> handleRequest(Object request, Context context) {
		String weatherForecast = "";
		try {
			weatherForecast = getWeatherForecast();
			System.out.println("Weather Forecast:\n" + weatherForecast);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Hello from lambda");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", 200);
		resultMap.put("body", weatherForecast);
		return resultMap;
	}
}
