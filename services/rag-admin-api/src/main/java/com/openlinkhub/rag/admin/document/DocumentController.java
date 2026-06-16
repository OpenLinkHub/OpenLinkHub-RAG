package com.openlinkhub.rag.admin.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.openlinkhub.rag.admin.auth.AuthService;
import com.openlinkhub.rag.admin.auth.Permission;
import com.openlinkhub.rag.admin.common.ApiResponse;
import com.openlinkhub.rag.admin.kb.KnowledgeBase;
import com.openlinkhub.rag.admin.kb.KnowledgeBaseService;
import com.openlinkhub.rag.admin.kb.LightRagEndpoint;
import com.openlinkhub.rag.admin.lightrag.LightRagClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/knowledge-bases/{knowledgeBaseId}")
public class DocumentController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final LightRagClient lightRagClient;
    private final AuthService authService;

    public DocumentController(
            KnowledgeBaseService knowledgeBaseService,
            LightRagClient lightRagClient,
            AuthService authService
    ) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.lightRagClient = lightRagClient;
        this.authService = authService;
    }

    @GetMapping("/documents")
    public ApiResponse<JsonNode> listDocuments(@PathVariable String knowledgeBaseId) {
        return ApiResponse.ok(lightRagClient.listDocuments(endpointOf(knowledgeBaseId)));
    }

    @PostMapping("/documents")
    public ApiResponse<JsonNode> documents(
            @PathVariable String knowledgeBaseId,
            @RequestBody Map<String, Object> request
    ) {
        return ApiResponse.ok(lightRagClient.documents(endpointOf(knowledgeBaseId), request));
    }

    @GetMapping("/documents/status-counts")
    public ApiResponse<JsonNode> statusCounts(@PathVariable String knowledgeBaseId) {
        return ApiResponse.ok(lightRagClient.statusCounts(endpointOf(knowledgeBaseId)));
    }

    @GetMapping("/documents/track-status/{trackId}")
    public ApiResponse<JsonNode> trackStatus(
            @PathVariable String knowledgeBaseId,
            @PathVariable String trackId
    ) {
        return ApiResponse.ok(lightRagClient.trackStatus(endpointOf(knowledgeBaseId), trackId));
    }

    @PostMapping("/documents/upload")
    public ApiResponse<JsonNode> upload(
            @PathVariable String knowledgeBaseId,
            @RequestParam("file") MultipartFile file
    ) {
        authService.require(Permission.DOC_UPLOAD);
        return ApiResponse.ok(lightRagClient.upload(endpointOf(knowledgeBaseId), file));
    }

    @PostMapping("/documents/text")
    public ApiResponse<JsonNode> insertText(
            @PathVariable String knowledgeBaseId,
            @RequestBody Map<String, Object> request
    ) {
        authService.require(Permission.DOC_UPLOAD);
        return ApiResponse.ok(lightRagClient.insertText(endpointOf(knowledgeBaseId), request));
    }

    @PostMapping("/documents/texts")
    public ApiResponse<JsonNode> insertTexts(
            @PathVariable String knowledgeBaseId,
            @RequestBody Map<String, Object> request
    ) {
        authService.require(Permission.DOC_UPLOAD);
        return ApiResponse.ok(lightRagClient.insertTexts(endpointOf(knowledgeBaseId), request));
    }

    @DeleteMapping("/documents")
    public ApiResponse<JsonNode> clearDocuments(@PathVariable String knowledgeBaseId) {
        authService.require(Permission.DOC_DELETE);
        return ApiResponse.ok(lightRagClient.clearDocuments(endpointOf(knowledgeBaseId)));
    }

    @DeleteMapping("/documents/batch")
    public ApiResponse<JsonNode> deleteDocuments(
            @PathVariable String knowledgeBaseId,
            @RequestBody Map<String, Object> request
    ) {
        authService.require(Permission.DOC_DELETE);
        return ApiResponse.ok(lightRagClient.deleteDocuments(endpointOf(knowledgeBaseId), request));
    }

    @DeleteMapping("/documents/{documentId}")
    public ApiResponse<JsonNode> deleteDocument(
            @PathVariable String knowledgeBaseId,
            @PathVariable String documentId,
            @RequestParam(defaultValue = "false") boolean deleteFile,
            @RequestParam(defaultValue = "false") boolean deleteLlmCache
    ) {
        authService.require(Permission.DOC_DELETE);
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("doc_ids", Collections.singletonList(documentId));
        request.put("delete_file", deleteFile);
        request.put("delete_llm_cache", deleteLlmCache);
        return ApiResponse.ok(lightRagClient.deleteDocuments(endpointOf(knowledgeBaseId), request));
    }

    @PostMapping("/documents/scan")
    public ApiResponse<JsonNode> scan(@PathVariable String knowledgeBaseId) {
        authService.require(Permission.DOC_UPLOAD);
        return ApiResponse.ok(lightRagClient.scan(endpointOf(knowledgeBaseId)));
    }

    @PostMapping("/documents/reprocess-failed")
    public ApiResponse<JsonNode> reprocessFailed(@PathVariable String knowledgeBaseId) {
        authService.require(Permission.DOC_REPROCESS);
        return ApiResponse.ok(lightRagClient.reprocessFailed(endpointOf(knowledgeBaseId)));
    }

    @PostMapping("/documents/cancel-pipeline")
    public ApiResponse<JsonNode> cancelPipeline(@PathVariable String knowledgeBaseId) {
        authService.require(Permission.DOC_REPROCESS);
        return ApiResponse.ok(lightRagClient.cancelPipeline(endpointOf(knowledgeBaseId)));
    }

    @PostMapping("/documents/clear-cache")
    public ApiResponse<JsonNode> clearCache(@PathVariable String knowledgeBaseId) {
        authService.require(Permission.DOC_REPROCESS);
        return ApiResponse.ok(lightRagClient.clearCache(endpointOf(knowledgeBaseId)));
    }

    @GetMapping("/pipeline-status")
    public ApiResponse<JsonNode> pipelineStatus(@PathVariable String knowledgeBaseId) {
        return ApiResponse.ok(lightRagClient.pipelineStatus(endpointOf(knowledgeBaseId)));
    }

    private LightRagEndpoint endpointOf(String knowledgeBaseId) {
        KnowledgeBase kb = knowledgeBaseService.getKnowledgeBase(knowledgeBaseId);
        return knowledgeBaseService.getEndpoint(kb.endpointId());
    }
}
