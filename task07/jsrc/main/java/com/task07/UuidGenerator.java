package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "uuid_generator",
        roleName = "uuid_generator-role"
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
})
@RuleEventSource(targetRule = "uuid_trigger")
public class UuidGenerator implements RequestHandler<Object, Map<String, Object>> {
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);
    private final static String TYPE = ".txt";

    public Map<String, Object> handleRequest(Object request, Context context) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();

        byte[] contentBytes = createFileAndFillItUuids().getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentBytes.length);
        s3Client.putObject(System.getenv("target_bucket"), getFilePath(), inputStream, metadata);

        System.out.println("Hello from lambda with RULE! and Love B-)");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("statusCode", 200);
        resultMap.put("body", "Hello from Lambda with RULE and Love B-)");
        return resultMap;
    }

    private String createFileAndFillItUuids() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            content.append(generateUniqueID()).append("\n");
        }
        return content.toString();
    }

    private String getFilePath() {
        String isoTime = LocalDateTime.now().format(FORMATTER);
        String filePath = isoTime.concat(TYPE);
        return filePath;
    }

    private static String generateUniqueID() {
        return UUID.randomUUID().toString();
    }
}


// https://medium.com/@cemdrman/uploading-files-to-amazon-s3-using-java-d1d0a7e82890
// https://stackabuse.com/aws-s3-with-java-uploading-files-creating-and-deleting-s3-buckets/
// https://docs.aws.amazon.com/AmazonS3/latest/userguide/upload-objects.html