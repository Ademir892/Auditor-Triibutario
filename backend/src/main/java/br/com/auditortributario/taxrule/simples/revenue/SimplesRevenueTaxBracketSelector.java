package br.com.auditortributario.taxrule.simples.revenue;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

public final class SimplesRevenueTaxBracketSelector {

    private final SimplesRevenueTaxTableRegistry registry;

    public SimplesRevenueTaxBracketSelector() {
        this.registry = new SimplesRevenueTaxTableRegistry();
    }

    public SimplesRevenueTaxBracket select(
            SimplesRevenueTaxRoute route,
            YearMonth competence,
            BigDecimal revenueBasis) {
        Objects.requireNonNull(
                route,
                "A rota tributária não pode ser nula.");

        Objects.requireNonNull(
                competence,
                "A competência não pode ser nula.");

        Objects.requireNonNull(
                revenueBasis,
                "A base de receita não pode ser nula.");

        if (revenueBasis.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A base de receita não pode ser negativa.");
        }

        if (route != SimplesRevenueTaxRoute.ANNEX_I
                && route != SimplesRevenueTaxRoute.ANNEX_II) {

            throw new IllegalArgumentException(
                    "Este seletor atualmente suporta "
                            + "apenas Anexo I e Anexo II.");
        }

        SimplesRevenueTaxTable table = registry.find(
                route,
                competence);

        /*
         * Assim como no motor já existente, uma base zero
         * continua pertencendo à primeira faixa.
         */
        return table
                .brackets()
                .stream()
                .filter(
                        bracket -> revenueBasis.compareTo(
                                bracket.maximumRevenue()) <= 0)
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "A base de receita "
                                        + revenueBasis
                                        + " excede o limite atualmente "
                                        + "modelado do Simples Nacional."));
    }
}