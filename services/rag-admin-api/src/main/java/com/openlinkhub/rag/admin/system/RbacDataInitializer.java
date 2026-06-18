package com.openlinkhub.rag.admin.system;

import com.openlinkhub.rag.admin.auth.AuthService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RbacDataInitializer implements ApplicationRunner {

    private final AuthService authService;

    public RbacDataInitializer(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void run(ApplicationArguments args) {
        authService.ensureSeedAdmin();
    }
}
