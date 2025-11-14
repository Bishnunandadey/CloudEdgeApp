package com.example.dropbox.controller;

import com.example.dropbox.service.DropboxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class DropboxController {

    private final DropboxService dropboxService;

    @Value("${dropbox.client.id}")
    private String clientId;

    @Value("${dropbox.client.secret}")
    private String clientSecret;

    @Value("${dropbox.redirect.uri}")
    private String redirectUri;

    public DropboxController(DropboxService dropboxService) {
        this.dropboxService = dropboxService;
    }

    // STEP 1: Generate Dropbox OAuth URL
    @GetMapping(value = "/auth/start", produces = MediaType.TEXT_PLAIN_VALUE)
    public String startAuth() {
        return dropboxService.getAuthorizationUrl(clientId, redirectUri);
    }

    // STEP 2: OAuth callback from Dropbox
    @GetMapping(value = "/callback", produces = MediaType.TEXT_PLAIN_VALUE)
    public String callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error) {

        if (error != null)
            return "Error returned from Dropbox: " + error;

        if (code == null)
            return "Missing OAuth 'code' parameter.";

        StringBuilder sb = new StringBuilder();

        try {
            // Exchange code → access token JSON
            String tokenJson =
                    dropboxService.exchangeCodeForToken(code, clientId, clientSecret, redirectUri);

            sb.append("TOKEN RESPONSE (RAW JSON):\n")
                    .append(tokenJson).append("\n\n");

            // Extract access_token
            String accessToken = dropboxService.extractAccessToken(tokenJson);

            sb.append("ACCESS TOKEN:\n")
                    .append(accessToken).append("\n\n");

            // STEP 3 — User Info
            sb.append("==== USER ACCOUNT INFO ====\n");
            sb.append(dropboxService.getUserAccountInfo(accessToken))
                    .append("\n\n");

            // STEP 4 — List Files (root folder)
            sb.append("==== LIST FILES ====\n");
            sb.append(dropboxService.listFiles(accessToken))
                    .append("\n\n");

        } catch (Exception ex) {
            sb.append("ERROR OCCURRED:\n")
                    .append(ex.getMessage())
                    .append("\n\n");

        }

        return sb.toString();
    }
}
