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

                if (!isSupportedRoute(
                                route)) {
                        throw new IllegalArgumentException(
                                        "Este seletor atualmente suporta "
                                                        + "Anexo I, Anexo II e Anexo IV.");
                }

                SimplesRevenueTaxTable table = registry.find(
                                route,
                                competence);

                /*
                 * Assim como no motor já existente,
                 * uma base zero permanece na primeira faixa.
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

        private boolean isSupportedRoute(
                        SimplesRevenueTaxRoute route) {
                return route == SimplesRevenueTaxRoute.ANNEX_I
                                || route == SimplesRevenueTaxRoute.ANNEX_II
                                || route == SimplesRevenueTaxRoute.ANNEX_IV;
        }
}