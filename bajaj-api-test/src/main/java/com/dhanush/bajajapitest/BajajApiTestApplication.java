package com.dhanush.bajajapitest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class BajajApiTestApplication {

    @Value("${api.base-url}")
    private String apiBaseUrl;

    @Value("${user.name}")
    private String userName;

    @Value("${user.regNo}")
    private String userRegNo;

    @Value("${user.email}")
    private String userEmail;

    public static void main(String[] args) {
        SpringApplication.run(BajajApiTestApplication.class, args);
    }

    // This runs right after the app starts
    @Bean
    @org.springframework.context.annotation.Profile("!test")
    public CommandLineRunner run() {
        return args -> {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Call generateWebhook endpoint
            String generateWebhookUrl = apiBaseUrl + "/hiring/generateWebhook/JAVA";
            Map<String, String> generateWebhookRequestBody = new HashMap<>();
            generateWebhookRequestBody.put("name", userName);
            generateWebhookRequestBody.put("regNo", userRegNo);
            generateWebhookRequestBody.put("email", userEmail);

            HttpHeaders generateWebhookHeaders = new HttpHeaders();
            generateWebhookHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> generateWebhookRequestEntity = new HttpEntity<>(generateWebhookRequestBody, generateWebhookHeaders);

            ResponseEntity<Map> response = restTemplate.postForEntity(generateWebhookUrl, generateWebhookRequestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                // assuming JSON has keys: "accessToken" and "webhook"
                String accessToken = (String) body.get("accessToken");
                String webhookUrl = (String) body.get("webhook");

                System.out.println("Access token: " + accessToken);
                System.out.println("Webhook URL: " + webhookUrl);

                // 2. Your SQL answer
                String sqlAnswer =
                        "SELECT p.AMOUNT AS SALARY, " +
                        "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                        "FLOOR(DATEDIFF(CURDATE(), e.DOB) / 365) AS AGE, " +
                        "d.DEPARTMENT_NAME " +
                        "FROM PAYMENTS p " +
                        "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                        "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                        "WHERE EXTRACT(DAY FROM p.PAYMENT_TIME) <> 1 " +
                        "AND p.AMOUNT = ( " +
                        "SELECT MAX(p2.AMOUNT) " +
                        "FROM PAYMENTS p2 " +
                        "WHERE EXTRACT(DAY FROM p2.PAYMENT_TIME) <> 1)";

                // 3. Post SQL answer to the webhook
                String submitWebhookUrl = apiBaseUrl + "/hiring/testWebhook/JAVA";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(accessToken); // sets "Authorization: Bearer <token>"

                Map<String, String> payload = new HashMap<>();
                payload.put("finalQuery", sqlAnswer); // adjust key if they require a different JSON field

                HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(payload, headers);

                ResponseEntity<String> postResponse = restTemplate.postForEntity(submitWebhookUrl, requestEntity, String.class);

                System.out.println("Posted SQL answer, response: " + postResponse.getStatusCode());
                System.out.println("Body: " + postResponse.getBody());
            } else {
                System.out.println("Failed to generate webhook. Status: " + response.getStatusCode());
            }
        };
    }
}
