package com.openlinkhub.rag.admin.kb;

import jakarta.validation.constraints.NotBlank;

public final class KnowledgeBaseRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    private String description;

    @NotBlank
    private String endpointId;

    private String defaultQueryMode;

    public KnowledgeBaseRequest() {
    }

    public String name() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String code() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String description() {
        return description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String endpointId() {
        return endpointId;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public String defaultQueryMode() {
        return defaultQueryMode;
    }

    public String getDefaultQueryMode() {
        return defaultQueryMode;
    }

    public void setDefaultQueryMode(String defaultQueryMode) {
        this.defaultQueryMode = defaultQueryMode;
    }
}
