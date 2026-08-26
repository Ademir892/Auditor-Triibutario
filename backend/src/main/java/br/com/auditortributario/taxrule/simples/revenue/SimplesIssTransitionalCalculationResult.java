package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Objects;

public record SimplesIssTransitionalCalculationResult(
                SimplesRevenueTaxRoute route,
                YearMonth competence,
                BigDecimal annualSublimit,
                int referenceBracketNumber,
                BigDecimal referenceEffectiveRate,
                BigDecimal issDistributionRate,
                BigDecimal rawIssEffectiveRate,
                BigDecimal issEffectiveRate,
                boolean limitedToFivePercent,
                SimplesSublimitMonthlyExcessResult monthlyExcess,
                BigDecimal segregatedRevenueAmount,
                BigDecimal segregatedExcessRevenue,
                BigDecimal issAmount,
                TaxDecision decision) {

        public SimplesIssTransitionalCalculationResult {
                Objects.requireNonNull(
                                route,
                                "A rota tributária não pode ser nula.");

                Objects.requireNonNull(
                                competence,
                                "A competência não pode ser nula.");

                Objects.requireNonNull(
                                annualSublimit,
                                "O sublimite anual não pode ser nulo.");

                Objects.requireNonNull(
                                referenceEffectiveRate,
                                "A alíquota efetiva de referência não pode ser nula.");

                Objects.requireNonNull(
                                issDistributionRate,
                                "O percentual de repartição do ISS não pode ser nulo.");

                Objects.requireNonNull(
                                rawIssEffectiveRate,
                                "O percentual bruto do ISS não pode ser nulo.");

                Objects.requireNonNull(
                                issEffectiveRate,
                                "O percentual efetivo do ISS não pode ser nulo.");

                Objects.requireNonNull(
                                monthlyExcess,
                                "O resultado do excesso mensal não pode ser nulo.");

                Objects.requireNonNull(
                                segregatedRevenueAmount,
                                "A receita segregada não pode ser nula.");

                Objects.requireNonNull(
                                segregatedExcessRevenue,
                                "A parcela excedente da receita segregada não pode ser nula.");

                Objects.requireNonNull(
                                issAmount,
                                "O valor do ISS não pode ser nulo.");

                Objects.requireNonNull(
                                decision,
                                "A decisão tributária não pode ser nula.");

                if (route != SimplesRevenueTaxRoute.ANNEX_III
                                && route != SimplesRevenueTaxRoute.ANNEX_IV
                                && route != SimplesRevenueTaxRoute.ANNEX_V) {

                        throw new IllegalArgumentException(
                                        "O cálculo transitório do ISS suporta "
                                                        + "somente os Anexos III, IV e V.");
                }

                if (annualSublimit.compareTo(
                                BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException(
                                        "O sublimite anual deve ser maior que zero.");
                }

                if (segregatedRevenueAmount.compareTo(
                                BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                        "A receita segregada não pode ser negativa.");
                }

                if (segregatedRevenueAmount.compareTo(
                                monthlyExcess.monthlyRevenue()) > 0) {
                        throw new IllegalArgumentException(
                                        "A receita segregada não pode superar "
                                                        + "a receita total da competência.");
                }

                if (segregatedExcessRevenue.compareTo(
                                BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                        "A parcela excedente da receita segregada "
                                                        + "não pode ser negativa.");
                }

                if (segregatedExcessRevenue.compareTo(
                                segregatedRevenueAmount) > 0) {
                        throw new IllegalArgumentException(
                                        "A parcela excedente não pode superar "
                                                        + "a própria receita segregada.");
                }

                if (segregatedExcessRevenue.compareTo(
                                monthlyExcess.excessMonthlyRevenue()) > 0) {
                        throw new IllegalArgumentException(
                                        "A parcela excedente da receita segregada "
                                                        + "não pode superar o excesso mensal total.");
                }

                if (issEffectiveRate.compareTo(
                                new BigDecimal("0.05")) > 0) {
                        throw new IllegalArgumentException(
                                        "O percentual efetivo do ISS "
                                                        + "não pode superar 5%.");
                }

                if (issAmount.compareTo(
                                BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                        "O valor do ISS não pode ser negativo.");
                }
        }

        public BigDecimal segregatedExcessRevenueForDisplay() {
                return segregatedExcessRevenue.setScale(
                                2,
                                RoundingMode.HALF_UP);
        }

        public BigDecimal issAmountForDisplay() {
                return issAmount.setScale(
                                2,
                                RoundingMode.HALF_UP);
        }

        public BigDecimal issEffectivePercentageForDisplay() {
                return issEffectiveRate
                                .multiply(
                                                new BigDecimal("100"))
                                .setScale(
                                                6,
                                                RoundingMode.HALF_UP);
        }
}