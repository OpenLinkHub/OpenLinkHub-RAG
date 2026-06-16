package com.openlinkhub.rag.admin.kb;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.Instant;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public final class KnowledgeBase {

    private final String id;
    private final String name;
    private final String code;
    private final String description;
    private final String endpointId;
    private final String ownerUserId;
    private final String status;
    private final String defaultQueryMode;
    private final String modelProfileId;
    private final boolean pendingRestart;
    private final Instant createdAt;
    private final Instant updatedAt;

    public KnowledgeBase(
            String id,
            String name,
            String code,
            String description,
            String endpointId,
            String ownerUserId,
            String status,
            String defaultQueryMode,
            String modelProfileId,
            boolean pendingRestart,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.endpointId = endpointId;
        this.ownerUserId = ownerUserId;
        this.status = status;
        this.defaultQueryMode = defaultQueryMode;
        this.modelProfileId = modelProfileId;
        this.pendingRestart = pendingRestart;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public KnowledgeBase withModelProfile(String nextModelProfileId) {
        return new KnowledgeBase(
                id,
                name,
                code,
                description,
                endpointId,
                ownerUserId,
                status,
                defaultQueryMode,
                nextModelProfileId,
                true,
                createdAt,
                Instant.now()
        );
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    public String endpointId() {
        return endpointId;
    }

    public String ownerUserId() {
        return ownerUserId;
    }

    public String status() {
        return status;
    }

    public String defaultQueryMode() {
        return defaultQueryMode;
    }

    public String modelProfileId() {
        return modelProfileId;
    }

    public boolean pendingRestart() {
        return pendingRestart;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
