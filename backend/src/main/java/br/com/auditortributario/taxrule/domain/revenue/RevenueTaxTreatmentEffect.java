package br.com.auditortributario.taxrule.domain.revenue;

public enum RevenueTaxTreatmentEffect {

    EXCLUDE_COMPONENT(
            "Excluir componente da tributação comum"),

    REQUIRE_SPECIAL_CALCULATION(
            "Exigir cálculo tributário específico"),

    REDUCE_COMPONENT(
            "Aplicar redução ao componente"),

    WITHHOLD_COMPONENT(
            "Componente sujeito à retenção");

    private final String displayName;

    RevenueTaxTreatmentEffect(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}