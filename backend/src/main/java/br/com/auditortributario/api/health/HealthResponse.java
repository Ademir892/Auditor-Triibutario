package br.com.auditortributario.api.health;

public record HealthResponse(
        String status,
        String application) {
}