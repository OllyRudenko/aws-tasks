package com.task10.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.utils.CognitoUtil;
import com.task10.utils.RSAKeyProviderTokenUtils;
import com.google.gson.Gson;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CognitoServiceImpl implements CognitoService {
    private final CognitoIdentityProviderClient identityProviderClient;
    private static String REGION;
    private static String USER_POOL_NAME;

    public CognitoServiceImpl(String region, String userPoolName) {
        REGION = region;
        USER_POOL_NAME = userPoolName;
        this.identityProviderClient = getCognitoIdentityProviderClient(region);
    }

    @Override
    public void signUp(String firstName, String lastName, String userName,
                       String password) {
        System.out.println("Hello from signUp ");

        try {
            AdminCreateUserResponse createUserResponse = identityProviderClient.adminCreateUser(
                    AdminCreateUserRequest.builder()
                            .userPoolId(getUserPoolId(identityProviderClient))
                            .username(userName)
                            .messageAction(MessageActionType.SUPPRESS)
                            .temporaryPassword(password)
                            .messageAction("SUPPRESS") // щоб не було потрібно надсилати й вводити код підтвердження
                            .build()
            );
            identityProviderClient.adminSetUserPassword(
                    AdminSetUserPasswordRequest.builder()
                            .userPoolId(getUserPoolId(identityProviderClient))
                            .username(userName)
                            .password(password)
                            .permanent(true) // За вибором: true, якщо новий пароль постійний, або false, якщо тимчасовий
                            .build()
            );
            identityProviderClient.adminUpdateUserAttributes(
                    AdminUpdateUserAttributesRequest.builder()
                            .userPoolId(getUserPoolId(identityProviderClient))
                            .username(userName)
                            .userAttributes(
                                    AttributeType.builder()
                                            .name("email_verified")
                                            .value("true")
                                            .build(),
                                    AttributeType.builder()
                                            .name("custom:firstName")
                                            .value(firstName)
                                            .build(),
                                    AttributeType.builder()
                                            .name("custom:lastName")
                                            .value(lastName)
                                            .build()
                            )
                            .build()
            );
        } catch (CognitoIdentityProviderException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            throw new RuntimeException(e.getMessage());
//            System.exit(1);
        }
    }

    @Override
    public String loginUser(String username, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("USERNAME", username);
        params.put("PASSWORD", password);

        AdminInitiateAuthResponse authResponse = identityProviderClient.adminInitiateAuth(
                AdminInitiateAuthRequest.builder()
                        .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                        .clientId(getClientId(identityProviderClient))
                        .userPoolId(getUserPoolId(identityProviderClient))
                        .authParameters(params)
                        .build()
        );
        System.out.println("TOKEN " + authResponse.authenticationResult().idToken());
        return authResponse.authenticationResult().idToken();
    }

    @Override
    public boolean isValidIdToken(String accessToken) {
        AdminGetUserResponse getUserResponse = identityProviderClient.adminGetUser(
                AdminGetUserRequest.builder()
                        .userPoolId(getUserPoolId(identityProviderClient))
                        .username(getUsernameFromAccessToken(accessToken))
                        .build()
        );
        System.out.println(getUserResponse.username());

        System.out.println("Token is valid");
        return !getUserResponse.username().isEmpty();
    }

    private String getUsernameFromAccessToken(String token) { //com.auth0.jwt.exceptions.TokenExpiredException com.auth0.jwt.exceptions.JWTDecodeException
        System.out.println("getUsernameFromAccessToken " + token);
        //String convertedToken = token.replace("Bearer", "").trim();
        ///System.out.println("converted Token " + token);

        RSAKeyProvider keyProvider = new RSAKeyProviderTokenUtils(REGION, getUserPoolId(identityProviderClient));
        Algorithm algorithm = Algorithm.RSA256(keyProvider);
        JWTVerifier jwtVerifier = JWT.require(algorithm)
                //.withAudience("2qm9sgg2kh21masuas88vjc9se") // Validate apps audience if needed
                .build();

//        System.out.println(jwtVerifier.verify(token).getPayload());

        String string = new String(Base64.getUrlDecoder().decode(jwtVerifier.verify(token).getPayload()));
        Gson gson = new Gson();
        LinkedHashMap<String, Object> payloadMap = gson.fromJson(string, LinkedHashMap.class);
        return (String) payloadMap.get("cognito:username");
    }

    @Override
    public boolean isTokenValid(String accessToken) {
        //AWSCognitoIdentityProvider cognitoClient = AWSCognitoIdentityProviderClientBuilder.defaultClient();

        try {
            // Отримати інформацію про користувача за допомогою токену доступу
            GetUserRequest getUserRequest = GetUserRequest.builder().accessToken(accessToken).build();
            GetUserResponse getUserResult = identityProviderClient.getUser(getUserRequest);
            System.out.println("USER RESULT " + getUserResult.toString());
            // Перевірити, чи успішно отримано інформацію про користувача
            return getUserResult != null;
        } catch (Exception e) {
            // Обробити помилку, якщо отримання інформації про користувача не вдалося
            e.printStackTrace();
            return false;
        }
    }

    private String convertToJson(String token) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(token);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void listAllUsers() {
        try {
            ListUsersRequest usersRequest = ListUsersRequest.builder()
                    .userPoolId(getUserPoolId(identityProviderClient))
                    .build();

            ListUsersResponse response = identityProviderClient.listUsers(usersRequest);
            response.users().forEach(user -> {
                System.out.println("User " + user.username() + " Status " + user.userStatus() + " Created "
                        + user.userCreateDate() + user.toString());
            });

        } catch (CognitoIdentityProviderException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }


    private String getUserPoolId(CognitoIdentityProviderClient identityProviderClient) {
        return CognitoUtil.getUserPoolId(identityProviderClient, USER_POOL_NAME);
    }

    private String getClientId(CognitoIdentityProviderClient identityProviderClient) {
        return CognitoUtil.getClientId(identityProviderClient, getUserPoolId(identityProviderClient));
    }

    private CognitoIdentityProviderClient getCognitoIdentityProviderClient(String region) {
        return CognitoUtil.createCognitoIdentityProviderClient(region);

    }
}
