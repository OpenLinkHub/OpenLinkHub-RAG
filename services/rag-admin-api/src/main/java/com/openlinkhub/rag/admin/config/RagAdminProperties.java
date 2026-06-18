package com.openlinkhub.rag.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "rag-admin")
public class RagAdminProperties {

    private SeedAdmin seedAdmin = new SeedAdmin("admin", "", "系统管理员");
    private DefaultLightRag defaultLightRag = new DefaultLightRag("Local LightRAG", "http://127.0.0.1:9621", null, "", "");
    private Security security = new Security();
    private Cors cors = new Cors(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:5175"
    ));

    public SeedAdmin seedAdmin() {
        return seedAdmin;
    }

    public void setSeedAdmin(SeedAdmin seedAdmin) {
        if (seedAdmin != null) {
            this.seedAdmin = seedAdmin;
        }
    }

    public DefaultLightRag defaultLightRag() {
        return defaultLightRag;
    }

    public void setDefaultLightRag(DefaultLightRag defaultLightRag) {
        if (defaultLightRag != null) {
            this.defaultLightRag = defaultLightRag;
        }
    }

    public Security security() {
        return security;
    }

    public void setSecurity(Security security) {
        if (security != null) {
            this.security = security;
        }
    }

    public Cors cors() {
        return cors;
    }

    public void setCors(Cors cors) {
        if (cors != null) {
            this.cors = cors;
        }
    }

    public static final class SeedAdmin {

        private String username;
        private String password;
        private String displayName;

        public SeedAdmin() {
        }

        public SeedAdmin(String username, String password, String displayName) {
            this.username = username;
            this.password = password;
            this.displayName = displayName;
        }

        public String username() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String password() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String displayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }

    public static final class DefaultLightRag {

        private String name;
        private String baseUrl;
        private String apiKey;
        private String username = "";
        private String password = "";

        public DefaultLightRag() {
        }

        public DefaultLightRag(String name, String baseUrl, String apiKey, String username, String password) {
            this.name = name;
            this.baseUrl = baseUrl;
            this.apiKey = apiKey;
            this.username = username == null ? "" : username;
            this.password = password == null ? "" : password;
        }

        public String name() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String baseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String apiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String username() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username == null ? "" : username;
        }

        public String password() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password == null ? "" : password;
        }
    }

    public static final class Cors {

        private List<String> allowedOrigins;

        public Cors() {
        }

        public Cors(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public List<String> allowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static final class Security {

        private String jwtSecret = "openlinkhub-rag-admin-change-me-in-production";
        private long jwtExpirationMs = 86400000L;

        public String jwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public long jwtExpirationMs() {
            return jwtExpirationMs;
        }

        public void setJwtExpirationMs(long jwtExpirationMs) {
            this.jwtExpirationMs = jwtExpirationMs;
        }
    }
}
