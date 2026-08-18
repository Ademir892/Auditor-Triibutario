package br.com.auditortributario.taxrule.simples;

public record GuideStructureAuditFinding(
        String code,
        GuideStructureAuditSeverity severity,
        String field,
        String expectedValue,
        String reportedValue,
        String message) {

    public GuideStructureAuditFinding {
        code = requireText(
                code,
                "code");

        if (severity == null) {
            throw new NullPointerException(
                    "A severidade não pode ser nula.");
        }

        field = requireText(
                field,
                "field");

        expectedValue = requireText(
                expectedValue,
                "expectedValue");

        reportedValue = requireText(
                reportedValue,
                "reportedValue");

        message = requireText(
                message,
                "message");
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