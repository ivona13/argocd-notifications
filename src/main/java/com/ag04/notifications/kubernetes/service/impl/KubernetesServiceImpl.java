package com.ag04.notifications.kubernetes.service.impl;

import com.ag04.notifications.kubernetes.EnvironmentVariable;
import com.ag04.notifications.kubernetes.service.KubernetesService;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KubernetesServiceImpl implements KubernetesService {

    String kubeApi;

    String kubeToken;

    String kubeUrl;

    public KubernetesServiceImpl(@Value("${kubernetes.kube-api}") String kubeApi, @Value("${kubernetes.token}") String kubeToken) {
        this.kubeApi = kubeApi;
        this.kubeToken = kubeToken;
        this.kubeUrl = kubeApi + "/api/v1/";
    }

    @Override
    public String applyYaml(String yaml) throws UnirestException {
        Unirest.config().verifySsl(false);
        HttpResponse<JsonNode> configMapExists = Unirest.get(kubeUrl + "namespaces/argocd/configmaps/argocd-notifications-cm")
                .header("Authorization", "Bearer " + kubeToken)
                .asJson();

        HttpResponse<JsonNode> jsonResponse;
        if (configMapExists.getStatus() == 404) {
            jsonResponse = Unirest.post(kubeUrl + "namespaces/argocd/configmaps")
                    .header("Authorization", "Bearer " + kubeToken)
                    .header("Content-Type", "application/yaml")
                    .body(yaml)
                    .asJson();
        } else {
            jsonResponse = Unirest.put(kubeUrl + "namespaces/argocd/configmaps/argocd-notifications-cm")
                    .header("Authorization", "Bearer " + kubeToken)
                    .header("Content-Type", "application/yaml")
                    .body(yaml)
                    .asJson();
        }

        return jsonResponse.getStatusText();
    }

    @Override
    public void updateK8sProperty(EnvironmentVariable env, String value) {
        switch (env) {
            case KUBE_URL:
                this.kubeUrl = value;
            case KUBE_TOKEN:
                this.kubeToken = value;
            default:
                break;
        }
    }
}
