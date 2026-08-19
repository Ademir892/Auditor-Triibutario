package br.com.auditortributario.auditcase;

public enum AuditCompetenceStatus {

    PENDING(
            "Pendente"),

    IN_PROGRESS(
            "Em análise"),

    COMPLETED(
            "Concluída"),

    REQUIRES_INFORMATION(
            "Aguardando informações");

    private final String displayName;

    AuditCompetenceStatus(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}