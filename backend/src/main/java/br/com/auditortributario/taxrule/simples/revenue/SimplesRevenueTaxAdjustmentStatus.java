package br.com.auditortributario.taxrule.simples.revenue;

public enum SimplesRevenueTaxAdjustmentStatus {

    NO_ADJUSTMENT(
            "Nenhum ajuste necessário"),

    APPLIED(
            "Ajustes aplicados"),

    APPLIED_WITH_EXTERNAL_OBLIGATION(
            "Ajustes aplicados com obrigação externa ao Simples"),

    REQUIRES_ADDITIONAL_RULES(
            "Necessita regras adicionais para concluir o cálculo");

    private final String displayName;

    SimplesRevenueTaxAdjustmentStatus(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}