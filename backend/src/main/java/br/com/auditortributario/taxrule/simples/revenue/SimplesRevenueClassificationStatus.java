package br.com.auditortributario.taxrule.simples.revenue;

public enum SimplesRevenueClassificationStatus {

    RESOLVED(
            "Classificação resolvida"),

    REQUIRES_FATOR_R(
            "Necessita cálculo do Fator R"),

    REQUIRES_SERVICE_RULE(
            "Necessita classificação específica do serviço"),

    REQUIRES_MANUAL_CLASSIFICATION(
            "Necessita classificação adicional");

    private final String displayName;

    SimplesRevenueClassificationStatus(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}