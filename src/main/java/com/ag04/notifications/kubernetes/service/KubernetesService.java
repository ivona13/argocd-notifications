package com.ag04.notifications.kubernetes.service;


import com.ag04.notifications.kubernetes.EnvironmentVariable;
import kong.unirest.UnirestException;

public interface KubernetesService {
    String applyYaml(String yaml) throws UnirestException;

    void updateK8sProperty(EnvironmentVariable env, String value);
}
