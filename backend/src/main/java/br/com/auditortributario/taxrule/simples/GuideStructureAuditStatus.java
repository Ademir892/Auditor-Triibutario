package br.com.auditortributario.taxrule.simples;

public enum GuideStructureAuditStatus {

    COMPATIBLE(
            "Estrutura compatível"),

    PARTIALLY_COMPATIBLE(
            "Estrutura parcialmente conferida"),

    DIVERGENT(
            "Possível divergência estrutural"),

    INSUFFICIENT_DATA(
            "Dados insuficientes para auditoria estrutural");

    private final String displayName;

    GuideStructureAuditStatus(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}