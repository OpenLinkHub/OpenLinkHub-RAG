package com.openlinkhub.rag.admin.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.openlinkhub.rag.admin.auth.AuthService;
import com.openlinkhub.rag.admin.auth.Permission;
import com.openlinkhub.rag.admin.common.ApiResponse;
import com.openlinkhub.rag.admin.kb.KnowledgeBase;
import com.openlinkhub.rag.admin.kb.KnowledgeBaseService;
import com.openlinkhub.rag.admin.lightrag.LightRagClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/admin/knowledge-bases/{knowledgeBaseId}")
public class QueryController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final LightRagClient lightRagClient;
    private final AuthService authService;

    public QueryController(KnowledgeBaseService knowledgeBaseService, LightRagClient lightRagClient, AuthService authService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.lightRagClient = lightRagClient;
        this.authService = authService;
    }

    @PostMapping("/query")
    public ApiResponse<JsonNode> query(@PathVariable String knowledgeBaseId, @RequestBody Map<String, Object> request) {
        authService.require(Permission.QUERY_EXECUTE);
        KnowledgeBase kb = knowledgeBaseService.getKnowledgeBase(knowledgeBaseId);
        return ApiResponse.ok(lightRagClient.query(knowledgeBaseService.getEndpoint(kb.endpointId()), withDefaults(kb, request, false)));
    }

    @PostMapping(value = "/query/stream", produces = "application/x-ndjson")
    public StreamingResponseBody queryStream(@PathVariable String knowledgeBaseId, @RequestBody Map<String, Object> request) {
        authService.require(Permission.QUERY_EXECUTE);
        KnowledgeBase kb = knowledgeBaseService.getKnowledgeBase(knowledgeBaseId);
        Stream<String> lines = lightRagClient.queryStream(knowledgeBaseService.getEndpoint(kb.endpointId()), withDefaults(kb, request, true));
        return outputStream -> {
            try (lines) {
                lines.forEach(line -> {
                    try {
                        outputStream.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    } catch (java.io.IOException exception) {
                        throw new RuntimeException(exception);
                    }
                });
            }
        };
    }

    private Map<String, Object> withDefaults(KnowledgeBase kb, Map<String, Object> request, boolean stream) {
        Map<String, Object> payload = new LinkedHashMap<>(request);
        payload.putIfAbsent("mode", kb.defaultQueryMode());
        payload.putIfAbsent("include_references", true);
        payload.put("stream", stream);
        return payload;
    }
}
