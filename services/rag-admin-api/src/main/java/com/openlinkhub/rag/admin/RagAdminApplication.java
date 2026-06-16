package com.openlinkhub.rag.admin;

import com.openlinkhub.rag.admin.config.RagAdminProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RagAdminProperties.class)
public class RagAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagAdminApplication.class, args);
    }
}
