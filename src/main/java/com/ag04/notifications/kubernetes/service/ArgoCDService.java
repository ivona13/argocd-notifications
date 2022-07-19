package com.ag04.notifications.kubernetes.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ArgoCDService {
    List<String> listApplications();

    String subscribeApplication(String applicationName, String webhookName);
}
