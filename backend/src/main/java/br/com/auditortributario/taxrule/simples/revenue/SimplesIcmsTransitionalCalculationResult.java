package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Objects;

public record SimplesIcmsTransitionalCalculationResult(
        SimplesRevenueTaxRoute route,
        YearMonth competence,
        BigDecimal annualSublimit,
        int referenceBracketNumber,
        BigDecimal referenceEffectiveRate,
        BigDecimal icmsDistributionRate,
        BigDecimal icmsEffectiveRate,
        SimplesSublimitMonthlyExcessResult monthlyExcess,
        BigDecimal icmsAmount,
        TaxDecision decision) {

    public SimplesIcmsTransitionalCalculationResult {
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
                icmsDistributionRate,
                "O percentual de repartição do ICMS não pode ser nulo.");

        Objects.requireNonNull(
                icmsEffectiveRate,
                "O percentual efetivo do ICMS não pode ser nulo.");

        Objects.requireNonNull(
                monthlyExcess,
                "O resultado do excesso mensal não pode ser nulo.");

        Objects.requireNonNull(
                icmsAmount,
                "O valor do ICMS não pode ser nulo.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        if (route != SimplesRevenueTaxRoute.ANNEX_I
                && route != SimplesRevenueTaxRoute.ANNEX_II) {

            throw new IllegalArgumentException(
                    "O cálculo transitório do ICMS suporta "
                            + "somente os Anexos I e II.");
        }

        if (annualSublimit.compareTo(
                BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O sublimite anual deve ser maior que zero.");
        }

        if (referenceBracketNumber < 1
                || referenceBracketNumber > 6) {

            throw new IllegalArgumentException(
                    "A faixa de referência deve estar entre 1 e 6.");
        }

        if (referenceEffectiveRate.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A alíquota efetiva de referência "
                            + "não pode ser negativa.");
        }

        if (icmsDistributionRate.compareTo(
                BigDecimal.ZERO) < 0
                || icmsDistributionRate.compareTo(
                        BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                    "O percentual de repartição do ICMS "
                            + "deve estar entre zero e um.");
        }

        if (icmsEffectiveRate.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O percentual efetivo do ICMS "
                            + "não pode ser negativo.");
        }

        if (icmsAmount.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor do ICMS não pode ser negativo.");
        }
    }

    public BigDecimal icmsAmountForDisplay() {
        return icmsAmount.setScale(
                2,
                RoundingMode.HALF_UP);
    }

    public BigDecimal icmsEffectivePercentageForDisplay() {
        return icmsEffectiveRate
                .multiply(
                        new BigDecimal("100"))
                .setScale(
                        6,
                        RoundingMode.HALF_UP);
    }
}