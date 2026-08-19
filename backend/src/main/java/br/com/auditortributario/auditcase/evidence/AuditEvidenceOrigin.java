package br.com.auditortributario.auditcase.evidence;

public enum AuditEvidenceOrigin {

    MANUAL_ENTRY(
            "Informação digitada manualmente"),

    DOCUMENT_TEXT(
            "Texto extraído do documento"),

    OCR(
            "Reconhecimento óptico de caracteres"),

    SYSTEM_CALCULATION(
            "Calculado pelo sistema"),

    EXTERNAL_INTEGRATION(
            "Obtido por integração externa");

    private final String displayName;

    AuditEvidenceOrigin(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}