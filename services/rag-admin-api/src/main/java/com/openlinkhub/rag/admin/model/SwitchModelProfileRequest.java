package com.openlinkhub.rag.admin.model;

import jakarta.validation.constraints.NotBlank;

public final class SwitchModelProfileRequest {

    @NotBlank
    private String modelProfileId;

    public SwitchModelProfileRequest() {
    }

    public String modelProfileId() {
        return modelProfileId;
    }

    public String getModelProfileId() {
        return modelProfileId;
    }

    public void setModelProfileId(String modelProfileId) {
        this.modelProfileId = modelProfileId;
    }
}
