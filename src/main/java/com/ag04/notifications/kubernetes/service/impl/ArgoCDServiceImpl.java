package com.ag04.notifications.kubernetes.service.impl;

import com.ag04.notifications.kubernetes.EnvironmentVariable;
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
    String argoUrl;

    @Value("${argo.username}")
    String argoUsername;

    @Value("${argo.password}")
    String argoPassword;

    @Override
    public List<String> listApplications() {
        Unirest.config().verifySsl(false);
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
    public String subscribeApplication(String applicationName, String serviceName, List<String> triggers, String subscriptionValue) {
        String token = generateToken();

        HttpResponse<JsonNode> applicationResponse = getApplicationInfo(applicationName, token);
        JSONObject updatedApp = addSubscriptions(applicationResponse, serviceName, triggers, subscriptionValue);
        HttpResponse<JsonNode> updatedApplication = updateApplication(applicationName, updatedApp, token);

        return updatedApplication.getStatusText();
    }

    @Override
    public void updateArgoProperty(EnvironmentVariable env, String value) {
        switch (env) {
            case ARGO_USERNAME:
                this.argoUsername = value;
            case ARGO_PASSWORD:
                this.argoPassword = value;
            case ARGO_URL:
                this.argoUrl = value;
            default:
                break;
        }
    }

    @Override
    public String getArgoProperty(EnvironmentVariable env) {
        switch (env) {
            case ARGO_USERNAME:
                return argoUsername;
            case ARGO_PASSWORD:
                return argoPassword;
            case ARGO_URL:
                return argoUrl;
            default:
                return "";
        }
    }

    private JSONArray getApplicationsAsJsonObjects (String token) {
        HttpResponse<JsonNode> applicationsResponse = Unirest.get(argoUrl + "api/v1/applications")
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .asJson();

        JSONArray applications = new JSONArray();
        try {
            applications = applicationsResponse.getBody().getObject().getJSONArray("items");
        } catch (Exception e) {}
        return applications;
    }
    private HttpResponse<JsonNode> getApplicationInfo(String applicationName, String token) {
        HttpResponse<JsonNode> applicationResponse = Unirest.get(argoUrl + "api/v1/applications/" + applicationName)
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .asJson();

        return applicationResponse;
    }

    private HttpResponse<JsonNode> updateApplication(String applicationName, JSONObject body, String token) {
        HttpResponse<JsonNode> updateAppResponse = Unirest.put(argoUrl + "api/v1/applications/" + applicationName)
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(body)
                .asJson();

        return updateAppResponse;
    }

    private JSONObject addSubscriptions(HttpResponse<JsonNode> applicationResponse, String serviceName, List<String> triggers, String subscriptionValue) {
        JSONObject app = applicationResponse.getBody().getObject();
        JSONObject metadata = app.getJSONObject("metadata");
        JSONObject annotations;
        try {
            annotations = metadata.getJSONObject("annotations");
        } catch (Exception e) {
            metadata.put("annotations", new JSONObject());
            annotations = metadata.getJSONObject("annotations");
        }
        JSONObject updatedData = annotations;
        for (String trigger : triggers) {
            updatedData = updatedData.put("notifications.argoproj.io/subscribe." + trigger + "." + serviceName, subscriptionValue);
        }
        metadata.put("annotations", updatedData);
        app.put("metadata", metadata);

        return app;
    }

    private String generateToken() {
        JSONObject obj = new JSONObject();
        obj.put("username", argoUsername);
        obj.put("password", argoPassword);

        HttpResponse<JsonNode> tokenResponse = Unirest.post(argoUrl + "api/v1/session")
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .body(obj)
                .asJson();

        if (tokenResponse.isSuccess()) {
            String token = tokenResponse.getBody().getObject().getString("token");
            return token;
        }
        throw new RuntimeException("Username or password are not correct!");
    }
}
