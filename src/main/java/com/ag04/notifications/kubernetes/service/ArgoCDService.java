package com.ag04.notifications.kubernetes.service;

import com.ag04.notifications.kubernetes.EnvironmentVariable;
import com.ag04.notifications.kubernetes.SubscriptionTrigger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ArgoCDService {
    List<String> listApplications();

    ResponseEntity subscribeApplication(String applicationName, String serviceName, List<SubscriptionTrigger> triggers, String subscriptionValue);

    void updateArgoProperty(EnvironmentVariable env, String value);

    String getArgoProperty(EnvironmentVariable env);
}
