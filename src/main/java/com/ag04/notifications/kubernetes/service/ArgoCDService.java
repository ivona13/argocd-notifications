package com.ag04.notifications.kubernetes.service;

import com.ag04.notifications.kubernetes.Property;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ArgoCDService {
    List<String> listApplications();

    String subscribeApplication(String applicationName, String serviceName, List<String> triggers, String subscriptionValue);

    void updateArgoProperty(Property property, String value);
}
