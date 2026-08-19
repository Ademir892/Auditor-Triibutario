package br.com.auditortributario.auditcase.document;

public enum AuditDocumentSource {

    USER_UPLOAD(
            "Enviado pelo usuário"),

    MANUAL_REGISTRATION(
            "Registrado manualmente"),

    SYSTEM_GENERATED(
            "Gerado pelo sistema"),

    EXTERNAL_IMPORT(
            "Importado de fonte externa"),

    EXTERNAL_INTEGRATION(
            "Obtido por integração externa");

    private final String displayName;

    AuditDocumentSource(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}