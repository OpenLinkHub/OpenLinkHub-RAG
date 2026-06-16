package com.openlinkhub.rag.admin.lightrag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openlinkhub.rag.admin.common.AdminException;
import com.openlinkhub.rag.admin.common.StringUtils;
import com.openlinkhub.rag.admin.kb.LightRagEndpoint;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Component
public class LightRagClient {

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 120000;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, String> accessTokens = new ConcurrentHashMap<String, String>();

    public LightRagClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MS);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public JsonNode health(LightRagEndpoint endpoint) {
        return sendJson(endpoint, "GET", "/health", null);
    }

    public JsonNode listDocuments(LightRagEndpoint endpoint) {
        return sendJson(endpoint, "GET", "/documents", null);
    }

    public JsonNode documents(LightRagEndpoint endpoint, Map<String, Object> request) {
        return sendJson(endpoint, "POST", "/documents/paginated", request);
    }

    public JsonNode statusCounts(LightRagEndpoint endpoint) {
        return sendJson(endpoint, "GET", "/documents/status_counts", null);
    }

    public JsonNode pipelineStatus(LightRagEndpoint endpoint) {
        return sendJson(endpoint, "GET", "/documents/pipeline_status", null);
    }

    public JsonNode trackStatus(LightRagEndpoint endpoint, String trackId) {
        return sendJson(endpoint, "GET", "/documents/track_status/" + trackId, null);
    }

    public JsonNode insertText(LightRagEndpoint endpoint, Map<String, Object> request) {
        return sendJson(endpoint, "POST", "/documents/text", request);
    }

    public JsonNode insertTexts(LightRagEndpoint endpoint, Map<String, Object> request) {
        return sendJson(endpoint, "POST", "/documents/texts", request);
    }

    public JsonNode scan(LightRagEndpoint endpoint) {
        return sendJson(endpoint, "POST", "/documents/scan", null);
    }

    public JsonNode cancelPipeline(LightRagEndpoint endpoint) {
        return sendJson(endpoint, "POST", "/documents/cancel_pipeline", null);
    }

    public JsonNode clearCache(LightRagEndpoint endpoint) {
        return sendJson(endpoint, "POST", "/documents/clear_cache", new HashMap<String, Object>());
    }

    public JsonNode reprocessFailed(LightRagEndpoint endpoint) {
        return sendJson(endpoint, "POST", "/documents/reprocess_failed", null);
    }

    public JsonNode clearDocuments(LightRagEndpoint endpoint) {
        return sendJson(endpoint, "DELETE", "/documents", null);
    }

    public JsonNode upload(LightRagEndpoint endpoint, MultipartFile file) {
        return upload(endpoint, file, true);
    }

    private JsonNode upload(LightRagEndpoint endpoint, MultipartFile file, boolean allowRetry) {
        try {
            final String filename = file.getOriginalFilename();
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
            body.add("file", resource);

            HttpHeaders headers = buildHeaders(endpoint);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint.baseUrl() + "/documents/upload",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            if (response.getStatusCode().value() == 401 && allowRetry) {
                invalidateToken(endpoint);
                return upload(endpoint, file, false);
            }
            return parseResponse(response);
        } catch (IOException exception) {
            throw new AdminException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Failed to upload file to LightRAG: " + exception.getMessage());
        } catch (RestClientException exception) {
            throw new AdminException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Failed to upload file to LightRAG: " + exception.getMessage());
        }
    }

    public JsonNode deleteDocuments(LightRagEndpoint endpoint, Map<String, Object> request) {
        return sendJson(endpoint, "DELETE", "/documents/delete_document", request);
    }

    public JsonNode query(LightRagEndpoint endpoint, Map<String, Object> request) {
        return sendJson(endpoint, "POST", "/query", request);
    }

    public Stream<String> queryStream(LightRagEndpoint endpoint, Map<String, Object> request) {
        return queryStream(endpoint, request, true);
    }

    private Stream<String> queryStream(LightRagEndpoint endpoint, Map<String, Object> request, boolean allowRetry) {
        try {
            final String payload = objectMapper.writeValueAsString(request);
            return restTemplate.execute(
                    endpoint.baseUrl() + "/query/stream",
                    HttpMethod.POST,
                    httpRequest -> {
                        HttpHeaders headers = buildHeaders(endpoint);
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.setAccept(Arrays.asList(
                                MediaType.parseMediaType("application/x-ndjson"),
                                MediaType.APPLICATION_JSON
                        ));
                        headers.forEach((name, values) -> httpRequest.getHeaders().addAll(name, values));
                        httpRequest.getBody().write(payload.getBytes(StandardCharsets.UTF_8));
                    },
                    response -> {
                        int status = response.getRawStatusCode();
                        if (status == 401 && allowRetry) {
                            invalidateToken(endpoint);
                            return queryStream(endpoint, request, false);
                        }
                        if (status < 200 || status >= 300) {
                            throw new AdminException(
                                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                                    "LightRAG stream query failed with status " + status
                            );
                        }
                        final BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8)
                        );
                        return reader.lines().onClose(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    reader.close();
                                } catch (IOException ignored) {
                                    // ignore close errors for streaming response
                                }
                            }
                        });
                    }
            );
        } catch (IOException exception) {
            throw new AdminException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Failed to query LightRAG stream: " + exception.getMessage());
        } catch (RestClientException exception) {
            throw new AdminException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Failed to query LightRAG stream: " + exception.getMessage());
        }
    }

    private JsonNode sendJson(LightRagEndpoint endpoint, String method, String path, Object body) {
        return sendJson(endpoint, method, path, body, true);
    }

    private JsonNode sendJson(LightRagEndpoint endpoint, String method, String path, Object body, boolean allowRetry) {
        try {
            HttpHeaders headers = buildHeaders(endpoint);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity;
            if (body == null) {
                entity = new HttpEntity<String>(headers);
            } else {
                headers.setContentType(MediaType.APPLICATION_JSON);
                entity = new HttpEntity<String>(objectMapper.writeValueAsString(body), headers);
            }
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint.baseUrl() + path,
                    HttpMethod.valueOf(method),
                    entity,
                    String.class
            );
            if (response.getStatusCode().value() == 401 && allowRetry) {
                invalidateToken(endpoint);
                return sendJson(endpoint, method, path, body, false);
            }
            return parseResponse(response);
        } catch (IOException exception) {
            throw new AdminException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Failed to call LightRAG: " + exception.getMessage());
        } catch (RestClientException exception) {
            throw new AdminException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Failed to call LightRAG: " + exception.getMessage());
        }
    }

    private HttpHeaders buildHeaders(LightRagEndpoint endpoint) {
        HttpHeaders headers = new HttpHeaders();
        if (!StringUtils.isBlank(endpoint.apiKey())) {
            headers.set("X-API-Key", endpoint.apiKey());
            return headers;
        }
        String token = resolveAccessToken(endpoint);
        if (!StringUtils.isBlank(token)) {
            headers.set("Authorization", "Bearer " + token);
        }
        return headers;
    }

    private String resolveAccessToken(LightRagEndpoint endpoint) {
        if (StringUtils.isBlank(endpoint.username())) {
            return accessTokens.get(endpoint.id());
        }
        String cached = accessTokens.get(endpoint.id());
        if (cached != null) {
            return cached;
        }
        String token = login(endpoint);
        accessTokens.put(endpoint.id(), token);
        return token;
    }

    private String login(LightRagEndpoint endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
        form.add("username", endpoint.username());
        form.add("password", endpoint.password());
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(form, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint.baseUrl() + "/login", entity, String.class);
            if (response.getStatusCode().value() < 200 || response.getStatusCode().value() >= 300) {
                throw new AdminException(
                        org.springframework.http.HttpStatus.BAD_GATEWAY,
                        "LightRAG login failed with HTTP " + response.getStatusCode().value() + ": " + response.getBody()
                );
            }
            JsonNode payload = objectMapper.readTree(response.getBody());
            JsonNode tokenNode = payload.get("access_token");
            String token = tokenNode == null || tokenNode.isNull() ? null : tokenNode.asText();
            if (StringUtils.isBlank(token)) {
                throw new AdminException(org.springframework.http.HttpStatus.BAD_GATEWAY, "LightRAG login response missing access_token.");
            }
            return token;
        } catch (IOException exception) {
            throw new AdminException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Failed to login to LightRAG: " + exception.getMessage());
        } catch (RestClientException exception) {
            throw new AdminException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Failed to login to LightRAG: " + exception.getMessage());
        }
    }

    private void invalidateToken(LightRagEndpoint endpoint) {
        accessTokens.remove(endpoint.id());
    }

    private JsonNode parseResponse(ResponseEntity<String> response) throws IOException {
        if (response.getStatusCode().value() < 200 || response.getStatusCode().value() >= 300) {
            throw new AdminException(
                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "LightRAG returned HTTP " + response.getStatusCode().value() + ": " + response.getBody()
            );
        }
        String body = response.getBody();
        if (StringUtils.isBlank(body)) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(body);
    }
}
