package com.openlinkhub.rag.admin.kb;

import com.openlinkhub.rag.admin.auth.AuthService;
import com.openlinkhub.rag.admin.auth.Permission;
import com.openlinkhub.rag.admin.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/admin")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final AuthService authService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService, AuthService authService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.authService = authService;
    }

    @GetMapping("/knowledge-bases")
    public ApiResponse<Collection<KnowledgeBase>> listKnowledgeBases() {
        return ApiResponse.ok(knowledgeBaseService.listKnowledgeBases());
    }

    @PostMapping("/knowledge-bases")
    public ApiResponse<KnowledgeBase> createKnowledgeBase(@Valid @RequestBody KnowledgeBaseRequest request) {
        authService.require(Permission.KB_CREATE);
        return ApiResponse.ok(knowledgeBaseService.createKnowledgeBase(request));
    }

    @GetMapping("/knowledge-bases/{id}")
    public ApiResponse<KnowledgeBase> getKnowledgeBase(@PathVariable String id) {
        return ApiResponse.ok(knowledgeBaseService.getKnowledgeBase(id));
    }

    @GetMapping("/endpoints")
    public ApiResponse<Collection<LightRagEndpoint>> listEndpoints() {
        return ApiResponse.ok(knowledgeBaseService.listEndpoints());
    }
}
