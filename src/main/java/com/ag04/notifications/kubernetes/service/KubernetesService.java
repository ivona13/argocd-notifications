package com.ag04.notifications.kubernetes.service;


import com.ag04.notifications.kubernetes.Property;
import kong.unirest.UnirestException;

public interface KubernetesService {
    String applyYaml(String yaml) throws UnirestException;

    void updateK8sProperty(Property property, String value);
}
