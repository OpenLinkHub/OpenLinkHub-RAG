package com.openlinkhub.rag.admin.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.Instant;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public final class ModelProfile {

    private final String id;
    private final String name;
    private final String llmBinding;
    private final String llmModel;
    private final String embeddingBinding;
    private final String embeddingModel;
    private final Integer embeddingDim;
    private final String rerankBinding;
    private final String rerankModel;
    private final String status;
    private final Instant createdAt;

    public ModelProfile(
            String id,
            String name,
            String llmBinding,
            String llmModel,
            String embeddingBinding,
            String embeddingModel,
            Integer embeddingDim,
            String rerankBinding,
            String rerankModel,
            String status,
            Instant createdAt
    ) {
        this.id = id;
        this.name = name;
        this.llmBinding = llmBinding;
        this.llmModel = llmModel;
        this.embeddingBinding = embeddingBinding;
        this.embeddingModel = embeddingModel;
        this.embeddingDim = embeddingDim;
        this.rerankBinding = rerankBinding;
        this.rerankModel = rerankModel;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String llmBinding() {
        return llmBinding;
    }

    public String llmModel() {
        return llmModel;
    }

    public String embeddingBinding() {
        return embeddingBinding;
    }

    public String embeddingModel() {
        return embeddingModel;
    }

    public Integer embeddingDim() {
        return embeddingDim;
    }

    public String rerankBinding() {
        return rerankBinding;
    }

    public String rerankModel() {
        return rerankModel;
    }

    public String status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
