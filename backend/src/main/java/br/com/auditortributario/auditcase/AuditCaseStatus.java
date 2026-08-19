package br.com.auditortributario.auditcase;

public enum AuditCaseStatus {

    CREATED(
            "Criado"),

    IN_PROGRESS(
            "Em análise"),

    COMPLETED(
            "Concluído"),

    REQUIRES_INFORMATION(
            "Aguardando informações"),

    CANCELLED(
            "Cancelado");

    private final String displayName;

    AuditCaseStatus(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}