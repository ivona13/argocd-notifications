package com.ag04.notifications.kubernetes.service;
import com.ag04.notifications.kubernetes.EnvironmentVariable;
import org.springframework.http.ResponseEntity;

public interface KubernetesService {
    ResponseEntity applyYaml(String yamlFilePath);

    void updateK8sProperty(EnvironmentVariable env, String value);

    String getK8sProperty(EnvironmentVariable env);
}
