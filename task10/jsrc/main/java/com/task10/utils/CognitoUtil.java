package com.task10.utils;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolClientsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolClientsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientDescription;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;

import java.util.List;

public class CognitoUtil {

    public static String getUserPoolId(CognitoIdentityProviderClient client, String userPoolName) {
        ListUserPoolsRequest request = ListUserPoolsRequest.builder()
                .maxResults(10)
                .build();

        ListUserPoolsResponse response = client.listUserPools(request);
        List<UserPoolDescriptionType> userPools = response.userPools();

        if (userPools.isEmpty()) {
            throw new RuntimeException("No user pools found");
        }
        UserPoolDescriptionType userPool = getByName(userPools, userPoolName);
        return userPool.id();
    }

    private static UserPoolDescriptionType getByName(List<UserPoolDescriptionType> userPools, String nameUserPool) {
        return userPools.stream().filter(userPool -> (userPool.name()).equals(nameUserPool)).findFirst().get();
    }

    public static String getClientId(CognitoIdentityProviderClient client, String userPoolId) {
        ListUserPoolClientsRequest request = ListUserPoolClientsRequest.builder()
                .userPoolId(userPoolId)
                .maxResults(1)
                .build();

        ListUserPoolClientsResponse response = client.listUserPoolClients(request);
        List<UserPoolClientDescription> userPoolClients = response.userPoolClients();

        if (userPoolClients.isEmpty()) {
            throw new RuntimeException("No user pool clients found");
        }

        UserPoolClientDescription userPoolClient = userPoolClients.get(0);
        return userPoolClient.clientId();
    }

    public static CognitoIdentityProviderClient createCognitoIdentityProviderClient(String region) {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .build();
    }
}