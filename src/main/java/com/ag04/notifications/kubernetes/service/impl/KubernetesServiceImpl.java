package com.ag04.notifications.kubernetes.service.impl;

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

    String token;

    String kubeUrl;

    public KubernetesServiceImpl(@Value("${kubernetes.kube-api}") String kubeApi, @Value("${kubernetes.token}") String token) {
        this.kubeApi = kubeApi;
        this.token = token;
        this.kubeUrl = kubeApi + "/api/v1/";
    }

    @Override
    public String applyYaml(String yaml) throws UnirestException {
        Unirest.config().verifySsl(false);
        HttpResponse<JsonNode> jsonResponse = Unirest.put(kubeUrl + "namespaces/argocd/configmaps/argocd-notifications-cm")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/yaml")
                .body(yaml)
                .asJson();

        return jsonResponse.getStatusText();
    }
}
