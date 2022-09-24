package com.ag04.notifications.kubernetes.service.impl;

import com.ag04.notifications.kubernetes.EnvironmentVariable;
import com.ag04.notifications.kubernetes.service.KubernetesService;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Service
public class KubernetesServiceImpl implements KubernetesService {
    static final String ARGO_CD_NAMESPACE = "argocd";

    String kubeApi;
    String kubeToken;
    String kubeUrl;

    public KubernetesServiceImpl(@Value("${kubernetes.kube-api}") String kubeApi, @Value("${kubernetes.token}") String kubeToken) {
        this.kubeApi = kubeApi;
        this.kubeToken = kubeToken;
        this.kubeUrl = kubeApi + "/api/v1/";
    }

    @Override
    public ResponseEntity applyYaml(String yamlFilePath) {
        Config config = new ConfigBuilder().withMasterUrl(kubeApi)
                .withOauthToken(kubeToken)
                .withTrustCerts(true)
                .build();
        KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();
        try {
            ConfigMap configMap = client.configMaps().load(new FileInputStream(yamlFilePath)).get();
            client.configMaps()
                    .inNamespace(ARGO_CD_NAMESPACE)
                    .createOrReplace(configMap);
            return ResponseEntity.ok("ConfigMap argocd-notifications-cm successfully applied!");
        } catch (FileNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

    @Override
    public String getK8sProperty(EnvironmentVariable env) {
        switch (env) {
            case KUBE_URL:
                return kubeUrl;
            case KUBE_TOKEN:
                return kubeToken;
            default:
                return "";
        }
    }
}
