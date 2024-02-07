package com.awstasks.task10.service;

import com.amazonaws.services.cognitoidp.model.SignUpResult;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

public interface CognitoService {
    //SignUpResult signUp(String region, String firstName, String lastName, String email, String password);


    void signUp(String firstName, String lastName, String userName,
                String password);

    String loginUser(
                     String username,
                     String password);

    boolean isValidIdToken(String accessToken);

    void listAllUsers();
}
