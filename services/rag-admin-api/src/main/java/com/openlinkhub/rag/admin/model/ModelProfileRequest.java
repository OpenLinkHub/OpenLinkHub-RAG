package com.openlinkhub.rag.admin.model;

import jakarta.validation.constraints.NotBlank;

public final class ModelProfileRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String llmBinding;

    @NotBlank
    private String llmModel;

    @NotBlank
    private String embeddingBinding;

    @NotBlank
    private String embeddingModel;

    private Integer embeddingDim;
    private String rerankBinding;
    private String rerankModel;

    public ModelProfileRequest() {
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String llmBinding() {
        return llmBinding;
    }

    public void setLlmBinding(String llmBinding) {
        this.llmBinding = llmBinding;
    }

    public String llmModel() {
        return llmModel;
    }

    public void setLlmModel(String llmModel) {
        this.llmModel = llmModel;
    }

    public String embeddingBinding() {
        return embeddingBinding;
    }

    public void setEmbeddingBinding(String embeddingBinding) {
        this.embeddingBinding = embeddingBinding;
    }

    public String embeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public Integer embeddingDim() {
        return embeddingDim;
    }

    public void setEmbeddingDim(Integer embeddingDim) {
        this.embeddingDim = embeddingDim;
    }

    public String rerankBinding() {
        return rerankBinding;
    }

    public void setRerankBinding(String rerankBinding) {
        this.rerankBinding = rerankBinding;
    }

    public String rerankModel() {
        return rerankModel;
    }

    public void setRerankModel(String rerankModel) {
        this.rerankModel = rerankModel;
    }
}
