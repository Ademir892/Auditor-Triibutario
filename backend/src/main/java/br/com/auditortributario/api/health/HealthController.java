package br.com.auditortributario.api.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public final class HealthController {

    private static final String APPLICATION_NAME = "auditor-tributario";

    @GetMapping
    public HealthResponse health() {
        return new HealthResponse(
                "UP",
                APPLICATION_NAME);
    }
}