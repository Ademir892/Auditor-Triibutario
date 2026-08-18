package br.com.auditortributario.taxrule.simples;

public enum GuideAmountAuditStatus {

    EXACT_MATCH(
            "Valor exatamente compatível"),

    WITHIN_TOLERANCE(
            "Diferença dentro da tolerância"),

    DIVERGENT(
            "Possível divergência"),

    REQUIRES_ADDITIONAL_CONTEXT(
            "Necessita contexto adicional");

    private final String displayName;

    GuideAmountAuditStatus(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}