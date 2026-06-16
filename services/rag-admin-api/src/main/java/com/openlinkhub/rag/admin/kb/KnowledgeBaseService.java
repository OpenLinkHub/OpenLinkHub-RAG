package com.openlinkhub.rag.admin.kb;

import com.openlinkhub.rag.admin.auth.AuthContext;
import com.openlinkhub.rag.admin.common.AdminException;
import com.openlinkhub.rag.admin.config.RagAdminProperties;
import com.openlinkhub.rag.admin.model.ModelProfile;
import com.openlinkhub.rag.admin.model.ModelProfileRequest;
import com.openlinkhub.rag.admin.common.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KnowledgeBaseService {

    private final Map<String, LightRagEndpoint> endpoints = new ConcurrentHashMap<>();
    private final Map<String, KnowledgeBase> knowledgeBases = new ConcurrentHashMap<>();
    private final Map<String, ModelProfile> modelProfiles = new ConcurrentHashMap<>();

    public KnowledgeBaseService(RagAdminProperties properties) {
        RagAdminProperties.DefaultLightRag defaultLightRag = properties.defaultLightRag();
        LightRagEndpoint endpoint = new LightRagEndpoint(
                "endpoint-local",
                defaultLightRag.name(),
                defaultLightRag.baseUrl(),
                defaultLightRag.apiKey(),
                defaultLightRag.username(),
                defaultLightRag.password(),
                "UNKNOWN",
                null
        );
        endpoints.put(endpoint.id(), endpoint);

        ModelProfile profile = new ModelProfile(
                "model-default",
                "Default LightRAG Runtime",
                "openai",
                "configured-by-lightrag",
                "openai",
                "configured-by-lightrag",
                null,
                "",
                "",
                "ACTIVE",
                Instant.now()
        );
        modelProfiles.put(profile.id(), profile);

        KnowledgeBase kb = new KnowledgeBase(
                "kb-default",
                "默认知识库",
                "default",
                "绑定本机 LightRAG 实例的默认知识库。",
                endpoint.id(),
                "u-admin",
                "ACTIVE",
                "mix",
                profile.id(),
                false,
                Instant.now(),
                Instant.now()
        );
        knowledgeBases.put(kb.id(), kb);
    }

    public Collection<KnowledgeBase> listKnowledgeBases() {
        return knowledgeBases.values();
    }

    public KnowledgeBase getKnowledgeBase(String id) {
        KnowledgeBase kb = knowledgeBases.get(id);
        if (kb == null) {
            throw new AdminException(HttpStatus.NOT_FOUND, "Knowledge base not found: " + id);
        }
        return kb;
    }

    public KnowledgeBase createKnowledgeBase(KnowledgeBaseRequest request) {
        ensureEndpoint(request.endpointId());
        String id = "kb-" + UUID.randomUUID();
        Instant now = Instant.now();
        KnowledgeBase kb = new KnowledgeBase(
                id,
                request.name(),
                request.code(),
                request.description(),
                request.endpointId(),
                AuthContext.currentUser().id(),
                "ACTIVE",
                request.defaultQueryMode() == null || StringUtils.isBlank(request.defaultQueryMode()) ? "mix" : request.defaultQueryMode(),
                "model-default",
                false,
                now,
                now
        );
        knowledgeBases.put(id, kb);
        return kb;
    }

    public Collection<LightRagEndpoint> listEndpoints() {
        return endpoints.values();
    }

    public LightRagEndpoint getEndpoint(String id) {
        LightRagEndpoint endpoint = endpoints.get(id);
        if (endpoint == null) {
            throw new AdminException(HttpStatus.NOT_FOUND, "LightRAG endpoint not found: " + id);
        }
        return endpoint;
    }

    public LightRagEndpoint updateEndpointHealth(String id, String status) {
        LightRagEndpoint endpoint = getEndpoint(id).withHealth(status, Instant.now());
        endpoints.put(id, endpoint);
        return endpoint;
    }

    public Collection<ModelProfile> listModelProfiles() {
        return modelProfiles.values();
    }

    public ModelProfile createModelProfile(ModelProfileRequest request) {
        ModelProfile profile = new ModelProfile(
                "model-" + UUID.randomUUID(),
                request.name(),
                request.llmBinding(),
                request.llmModel(),
                request.embeddingBinding(),
                request.embeddingModel(),
                request.embeddingDim(),
                request.rerankBinding(),
                request.rerankModel(),
                "ACTIVE",
                Instant.now()
        );
        modelProfiles.put(profile.id(), profile);
        return profile;
    }

    public KnowledgeBase switchModelProfile(String knowledgeBaseId, String modelProfileId) {
        if (!modelProfiles.containsKey(modelProfileId)) {
            throw new AdminException(HttpStatus.NOT_FOUND, "Model profile not found: " + modelProfileId);
        }
        KnowledgeBase updated = getKnowledgeBase(knowledgeBaseId).withModelProfile(modelProfileId);
        knowledgeBases.put(knowledgeBaseId, updated);
        return updated;
    }

    private void ensureEndpoint(String endpointId) {
        if (!endpoints.containsKey(endpointId)) {
            throw new AdminException(HttpStatus.BAD_REQUEST, "Unknown LightRAG endpoint: " + endpointId);
        }
    }
}
