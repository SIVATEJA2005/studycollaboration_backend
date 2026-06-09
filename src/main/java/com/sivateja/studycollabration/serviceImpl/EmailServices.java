package com.sivateja.studycollabration.serviceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailServices {
    @Value("${brevo.api.key}")
    private String brevoApiKey;
    @Value("${brevo.sender.email}")
    private String senderEmail;
    @Value("${brevo.sender.name}")
    private String senderName;

    public void sendActivationEmail(String toEmail, String toName, String activationLink) {
        try {
            String body = """
                {
                  "sender": { "name": "%s", "email": "%s" },
                  "to": [{ "email": "%s", "name": "%s" }],
                  "subject": "Activate your Brain Bridge account",
                  "htmlContent": "<h2>Welcome to Brain Bridge!</h2><p>Click the link below to activate your account:</p><a href='%s' style='background:#3b82f6;color:white;padding:10px 20px;border-radius:8px;text-decoration:none;'>Activate Account</a><p>This link will not expire unless you request a new one.</p>"
                }
                """.formatted(senderName, senderEmail, toEmail, toName, activationLink);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send activation email: " + e.getMessage());
        }
    }


    public void sendPasswordResetEmail(String toEmail, String toName, String resetLink) {
        try {
            String body = """
            {
              "sender": { "name": "%s", "email": "%s" },
              "to": [{ "email": "%s", "name": "%s" }],
              "subject": "Reset your Brain Bridge password",
              "htmlContent": "<h2>Password Reset Request</h2><p>Hi %s,</p><p>Click below to reset your Brain Bridge password:</p><a href='%s' style='background:#ec4899;color:white;padding:10px 20px;border-radius:8px;text-decoration:none;'>Reset Password</a><p>If you did not request this, ignore this email.</p>"
            }
            """.formatted(senderName, senderEmail, toEmail, toName, toName, resetLink);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reset email: " + e.getMessage());
        }
    }






}
