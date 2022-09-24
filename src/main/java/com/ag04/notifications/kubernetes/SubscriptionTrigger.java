package com.ag04.notifications.kubernetes;

public enum SubscriptionTrigger {
    ON_CREATED("on-created"),
    ON_DELETED("on-deleted"),
    ON_DEPLOYED("on-deployed"),
    ON_HEALTH_DEGRADED("on-health-degraded"),
    ON_SYNC_FAILED("on-sync-failed"),
    ON_SYNC_RUNNING("on-sync-running"),
    ON_SYNC_STATUS_UNKNOWN("on-sync-status-unknown"),
    ON_SYNC_SUCCEEDED("on-sync-succeeded");

    private final String triggerValue;

    SubscriptionTrigger(String triggerValue) {
        this.triggerValue = triggerValue;
    }

    public String getTriggerValue() {
        return triggerValue;
    }
}
