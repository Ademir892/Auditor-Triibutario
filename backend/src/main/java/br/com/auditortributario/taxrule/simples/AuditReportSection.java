package br.com.auditortributario.taxrule.simples;

public record AuditReportSection(
        String code,
        String title,
        String content) {

    public AuditReportSection {
        code = requireText(
                code,
                "O código da seção não pode ser nulo ou vazio.");

        title = requireText(
                title,
                "O título da seção não pode ser nulo ou vazio.");

        content = requireText(
                content,
                "O conteúdo da seção não pode ser nulo ou vazio.");
    }

    private static String requireText(
            String value,
            String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    message);
        }

        return value;
    }
}