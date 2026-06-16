package com.openlinkhub.rag.admin.kb;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.Instant;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public final class LightRagEndpoint {

    private final String id;
    private final String name;
    private final String baseUrl;
    private final String apiKey;
    private final String username;
    private final String password;
    private final String healthStatus;
    private final Instant lastCheckedAt;

    public LightRagEndpoint(
            String id,
            String name,
            String baseUrl,
            String apiKey,
            String username,
            String password,
            String healthStatus,
            Instant lastCheckedAt
    ) {
        this.id = id;
        this.name = name;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.username = username == null ? "" : username;
        this.password = password == null ? "" : password;
        this.healthStatus = healthStatus;
        this.lastCheckedAt = lastCheckedAt;
    }

    public LightRagEndpoint withHealth(String status, Instant checkedAt) {
        return new LightRagEndpoint(id, name, baseUrl, apiKey, username, password, status, checkedAt);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String apiKey() {
        return apiKey;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String healthStatus() {
        return healthStatus;
    }

    public Instant lastCheckedAt() {
        return lastCheckedAt;
    }
}
