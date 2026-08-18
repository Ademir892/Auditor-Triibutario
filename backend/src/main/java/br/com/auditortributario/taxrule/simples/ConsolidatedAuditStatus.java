package br.com.auditortributario.taxrule.simples;

public enum ConsolidatedAuditStatus {

    COMPATIBLE(
            "Auditoria compatível"),

    REVIEW_REQUIRED(
            "Revisão adicional recomendada"),

    DIVERGENT(
            "Possível divergência");

    private final String displayName;

    ConsolidatedAuditStatus(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}