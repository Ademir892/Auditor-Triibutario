package br.com.auditortributario.taxrule.simples.revenue;

import java.util.Objects;

public enum SimplesServiceTaxRule {

    ANNEX_IV_CONSTRUCTION_ENGINEERING(
            SimplesRevenueTaxRoute.ANNEX_IV,
            "Construção de imóveis, obras de engenharia, "
                    + "subempreitada, projetos, paisagismo "
                    + "e decoração de interiores"),

    ANNEX_IV_SECURITY_CLEANING_CONSERVATION(
            SimplesRevenueTaxRoute.ANNEX_IV,
            "Vigilância, limpeza ou conservação"),

    ANNEX_IV_LEGAL_SERVICES(
            SimplesRevenueTaxRoute.ANNEX_IV,
            "Serviços advocatícios");

    private final SimplesRevenueTaxRoute route;

    private final String displayName;

    SimplesServiceTaxRule(
            SimplesRevenueTaxRoute route,
            String displayName) {
        this.route = Objects.requireNonNull(
                route,
                "A rota tributária não pode ser nula.");

        this.displayName = Objects.requireNonNull(
                displayName,
                "A descrição da regra não pode ser nula.");
    }

    public SimplesRevenueTaxRoute getRoute() {
        return route;
    }

    public String getDisplayName() {
        return displayName;
    }
}