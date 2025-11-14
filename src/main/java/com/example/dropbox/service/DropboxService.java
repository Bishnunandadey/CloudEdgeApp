package com.example.dropbox.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class DropboxService {

    private static final String AUTH_URL = "https://www.dropbox.com/oauth2/authorize";
    private static final String TOKEN_URL = "https://api.dropboxapi.com/oauth2/token";

    // USER ENDPOINTS
    private static final String USER_INFO_URL = "https://api.dropboxapi.com/2/users/get_current_account";
    private static final String LIST_FOLDER_URL = "https://api.dropboxapi.com/2/files/list_folder";
    private static final String UPLOAD_URL = "https://content.dropboxapi.com/2/files/upload";
    private static final String DOWNLOAD_URL = "https://content.dropboxapi.com/2/files/download";

    private final ObjectMapper mapper = new ObjectMapper();

    // Step 1: Build authorization URL
    public String getAuthorizationUrl(String clientId, String redirectUri) {
        String scopes = "account_info.read files.metadata.read files.content.write files.content.read";

        return AUTH_URL +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&token_access_type=offline" +
                "&scope=" + scopes;
    }

    // Step 2: Exchange code for access token
    public String exchangeCodeForToken(String code, String clientId, String clientSecret, String redirectUri) {

        RestTemplate rest = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);

        HttpEntity<?> entity = new HttpEntity<>(params, headers);

        ResponseEntity<String> response =
                rest.postForEntity(TOKEN_URL, entity, String.class);

        return response.getBody();
    }

    // Extract access_token
    public String extractAccessToken(String tokenJson) {
        try {
            JsonNode node = mapper.readTree(tokenJson);
            return node.get("access_token").asText();
        } catch (Exception e) {
            return null;
        }
    }

    public String getUserAccountInfo(String accessToken) {

        RestTemplate rest = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        // DO NOT SET CONTENT-TYPE
        headers.setContentType(null);

        HttpEntity<Void> entity = new HttpEntity<>(null, headers);

        return rest.exchange(
                USER_INFO_URL,
                HttpMethod.POST,
                entity,
                String.class
        ).getBody();
    }





    // Step 4: List folder
    public String listFiles(String accessToken) {

        RestTemplate rest = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"path\":\"\",\"recursive\":false}";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        return rest.postForEntity(LIST_FOLDER_URL, entity, String.class).getBody();
    }

    // Step 5: Upload file
    public String uploadFile(String accessToken, Path path, String dropboxPath) throws Exception {

        RestTemplate rest = new RestTemplate();

        byte[] fileBytes = Files.readAllBytes(path);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.add("Dropbox-API-Arg",
                "{\"path\": \"" + dropboxPath + "\",\"mode\": \"add\",\"autorename\": true}");

        HttpEntity<byte[]> entity = new HttpEntity<>(fileBytes, headers);

        return rest.postForEntity(UPLOAD_URL, entity, String.class).getBody();
    }

    // Step 6: Download file
    public byte[] downloadFile(String accessToken, String dropboxPath) {

        RestTemplate rest = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.add("Dropbox-API-Arg", "{\"path\": \"" + dropboxPath + "\"}");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response =
                rest.exchange(DOWNLOAD_URL, HttpMethod.POST, entity, byte[].class);

        return response.getBody();
    }
}
