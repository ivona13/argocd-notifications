package com.ag04.notifications.kubernetes.service.impl;

import com.ag04.notifications.kubernetes.service.ArgoCDService;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArgoCDServiceImpl implements ArgoCDService {

    @Value("${argo.url}")
    String url;

    @Value("${argo.username}")
    String argoUsername;

    @Value("${argo.password}")
    String argoPassword;

    @Override
    public List<String> listApplications() {
        List<String> applicationNames = new ArrayList<>();

        String token = generateToken();
        JSONArray applications = getApplicationsAsJsonObjects(token);

        for (int ind = 0; ind < applications.length(); ind++) {
            JSONObject app = applications.getJSONObject(ind);
            String appName = app.getJSONObject("metadata").getString("name");
            applicationNames.add(appName);
        }

        return applicationNames;
    }

    @Override
    public String subscribeApplication(String applicationName, String webhookName) {
        String token = generateToken();

        HttpResponse<JsonNode> applicationResponse = getApplicationInfo(applicationName, token);
        JSONObject updatedApp = addSubscription(applicationResponse, webhookName);
        HttpResponse<JsonNode> updatedApplication = updateApplication(applicationName, updatedApp, token);

        return updatedApplication.getStatusText();
    }

    private JSONArray getApplicationsAsJsonObjects (String token) {
        HttpResponse<JsonNode> applicationsResponse = Unirest.get(url + "api/v1/applications")
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .asJson();

        JSONArray applications = new JSONArray();
        try {
            applications = applicationsResponse.getBody().getObject().getJSONArray("items");
        } catch (Exception e) {
            // TODO: Handle this
        }
        return applications;
    }
    private HttpResponse<JsonNode> getApplicationInfo(String applicationName, String token) {
        HttpResponse<JsonNode> applicationResponse = Unirest.get(url + "api/v1/applications/" + applicationName)
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .asJson();

        return applicationResponse;
    }

    private HttpResponse<JsonNode> updateApplication(String applicationName, JSONObject body, String token) {
        HttpResponse<JsonNode> updateAppResponse = Unirest.put(url + "api/v1/applications/" + applicationName)
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(body)
                .asJson();

        return updateAppResponse;
    }

    private JSONObject addSubscription(HttpResponse<JsonNode> applicationResponse, String webhookName) {
        JSONObject app = applicationResponse.getBody().getObject();
        JSONObject metadata = app.getJSONObject("metadata");
        JSONObject annotations;
        try {
            annotations = metadata.getJSONObject("annotations");
        } catch (Exception e) {
            metadata.put("annotations", new JSONObject());
            annotations = metadata.getJSONObject("annotations");
        }
        JSONObject updatedData = annotations.put("notifications.argoproj.io/subscribe.on-sync-succeeded." + webhookName, "");
        metadata.put("annotations", updatedData);
        app.put("metadata", metadata);

        return app;
    }

    private String generateToken() {
        JSONObject obj = new JSONObject();
        obj.put("username", argoUsername);
        obj.put("password", argoPassword);

        HttpResponse<JsonNode> tokenResponse = Unirest.post(url + "api/v1/session")
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .body(obj)
                .asJson();

        String token = null;
        if (tokenResponse.isSuccess()) {
            token = tokenResponse.getBody().getObject().getString("token");
        }
        return token;
    }
}
