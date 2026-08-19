package br.com.auditortributario.auditcase.document;

public enum AuditDocumentStatus {

    REGISTERED(
            "Registrado"),

    AVAILABLE(
            "Disponível"),

    PROCESSING(
            "Em processamento"),

    PROCESSED(
            "Processado"),

    FAILED(
            "Falha no processamento");

    private final String displayName;

    AuditDocumentStatus(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}