package br.com.auditortributario.taxrule.simples;

public record ConsolidatedAuditFinding(
        String code,
        ConsolidatedAuditSeverity severity,
        String category,
        String message,
        String recommendation) {

    public ConsolidatedAuditFinding {
        code = requireText(
                code,
                "code");

        if (severity == null) {
            throw new NullPointerException(
                    "A severidade não pode ser nula.");
        }

        category = requireText(
                category,
                "category");

        message = requireText(
                message,
                "message");

        recommendation = requireText(
                recommendation,
                "recommendation");
    }

    private static String requireText(
            String value,
            String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " não pode ser nulo ou vazio.");
        }

        return value;
    }
}