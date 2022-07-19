package com.ag04.notifications.kubernetes.service;


import kong.unirest.UnirestException;

public interface KubernetesService {
    String applyYaml(String yaml) throws UnirestException;
}
