package br.com.auditortributario.taxrule.domain.revenue;

public enum RevenueOrigin {

    MANUAL_ENTRY(
            "Informada manualmente"),

    DOCUMENT_EVIDENCE(
            "Obtida a partir de documento ou evidência"),

    EXTERNAL_IMPORT(
            "Importada de fonte externa"),

    SYSTEM_DERIVED(
            "Derivada pelo sistema");

    private final String displayName;

    RevenueOrigin(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}