package com.openlinkhub.rag.admin.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.openlinkhub.rag.admin.common.ApiResponse;
import com.openlinkhub.rag.admin.kb.KnowledgeBaseService;
import com.openlinkhub.rag.admin.kb.LightRagEndpoint;
import com.openlinkhub.rag.admin.lightrag.LightRagClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/system")
public class SystemController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final LightRagClient lightRagClient;

    public SystemController(KnowledgeBaseService knowledgeBaseService, LightRagClient lightRagClient) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.lightRagClient = lightRagClient;
    }

    @GetMapping("/endpoints/{endpointId}/health")
    public ApiResponse<JsonNode> health(@PathVariable String endpointId) {
        LightRagEndpoint endpoint = knowledgeBaseService.getEndpoint(endpointId);
        JsonNode health = lightRagClient.health(endpoint);
        knowledgeBaseService.updateEndpointHealth(endpointId, "UP");
        return ApiResponse.ok(health);
    }
}
