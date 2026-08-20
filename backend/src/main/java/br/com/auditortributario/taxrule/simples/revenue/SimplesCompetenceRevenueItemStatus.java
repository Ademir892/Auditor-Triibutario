package br.com.auditortributario.taxrule.simples.revenue;

public enum SimplesCompetenceRevenueItemStatus {

    COMPLETED(
            "Processamento concluído"),

    COMPLETED_WITH_EXTERNAL_OBLIGATION(
            "Processamento concluído com obrigação externa ao Simples"),

    REQUIRES_CLASSIFICATION(
            "Necessita classificação tributária adicional"),

    REQUIRES_ADDITIONAL_RULES(
            "Necessita regras tributárias adicionais"),

    UNSUPPORTED_ROUTE(
            "Rota tributária ainda não suportada por este processador");

    private final String displayName;

    SimplesCompetenceRevenueItemStatus(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isFinal() {
        return this == COMPLETED
                || this == COMPLETED_WITH_EXTERNAL_OBLIGATION;
    }
}