package br.com.auditortributario.taxrule.simples.revenue;

public enum SimplesCompetenceRevenueProcessingStatus {

    EMPTY(
            "Competência sem receitas"
    ),

    COMPLETED(
            "Competência processada integralmente"
    ),

    PARTIALLY_PROCESSED(
            "Competência processada parcialmente"
    ),

    REQUIRES_ADDITIONAL_INFORMATION(
            "Competência depende de informações ou regras adicionais"
    );

    private final String displayName;

    SimplesCompetenceRevenueProcessingStatus(
            String displayName
    ) {
        this.displayName =
                displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}