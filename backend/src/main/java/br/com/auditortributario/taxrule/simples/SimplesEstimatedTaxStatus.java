package br.com.auditortributario.taxrule.simples;

public enum SimplesEstimatedTaxStatus {

    PAYABLE(
            "Valor apto para recolhimento"),

    DEFERRED_BELOW_MINIMUM(
            "Valor inferior ao mínimo para emissão de DAS"),

    NO_TAX_DUE(
            "Sem valor devido no período");

    private final String displayName;

    SimplesEstimatedTaxStatus(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}