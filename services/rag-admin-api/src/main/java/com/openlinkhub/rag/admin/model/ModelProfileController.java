package com.openlinkhub.rag.admin.model;

import com.openlinkhub.rag.admin.auth.AuthService;
import com.openlinkhub.rag.admin.auth.Permission;
import com.openlinkhub.rag.admin.common.ApiResponse;
import com.openlinkhub.rag.admin.kb.KnowledgeBase;
import com.openlinkhub.rag.admin.kb.KnowledgeBaseService;
import jakarta.validation.Valid;
import com.openlinkhub.rag.admin.model.SwitchModelProfileRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/admin")
public class ModelProfileController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final AuthService authService;

    public ModelProfileController(KnowledgeBaseService knowledgeBaseService, AuthService authService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.authService = authService;
    }

    @GetMapping("/model-profiles")
    public ApiResponse<Collection<ModelProfile>> listModelProfiles() {
        return ApiResponse.ok(knowledgeBaseService.listModelProfiles());
    }

    @PostMapping("/model-profiles")
    public ApiResponse<ModelProfile> createModelProfile(@Valid @RequestBody ModelProfileRequest request) {
        authService.require(Permission.MODEL_MANAGE);
        return ApiResponse.ok(knowledgeBaseService.createModelProfile(request));
    }

    @PostMapping("/knowledge-bases/{id}/model-profile")
    public ApiResponse<KnowledgeBase> switchModelProfile(
            @PathVariable String id,
            @Valid @RequestBody SwitchModelProfileRequest request
    ) {
        authService.require(Permission.MODEL_MANAGE);
        return ApiResponse.ok(knowledgeBaseService.switchModelProfile(id, request.modelProfileId()));
    }
}
