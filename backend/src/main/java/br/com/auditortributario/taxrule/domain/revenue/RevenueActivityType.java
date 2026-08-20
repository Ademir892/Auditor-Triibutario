package br.com.auditortributario.taxrule.domain.revenue;

public enum RevenueActivityType {

    SERVICE(
            "Serviço"),

    COMMERCE(
            "Comércio"),

    INDUSTRY(
            "Indústria"),

    OTHER(
            "Outra atividade");

    private final String displayName;

    RevenueActivityType(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}