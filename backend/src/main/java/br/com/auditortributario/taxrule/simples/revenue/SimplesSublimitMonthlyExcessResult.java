package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record SimplesSublimitMonthlyExcessResult(
        SimplesSublimitEvaluationResult evaluationResult,
        BigDecimal monthlyRevenue,
        BigDecimal revenueWithinSublimit,
        BigDecimal excessMonthlyRevenue,
        BigDecimal excessRatio,
        TaxDecision decision) {

    public SimplesSublimitMonthlyExcessResult {
        Objects.requireNonNull(
                evaluationResult,
                "O resultado da avaliação do sublimite não pode ser nulo.");

        Objects.requireNonNull(
                monthlyRevenue,
                "A receita mensal não pode ser nula.");

        Objects.requireNonNull(
                revenueWithinSublimit,
                "A parcela da receita dentro do sublimite não pode ser nula.");

        Objects.requireNonNull(
                excessMonthlyRevenue,
                "A parcela mensal excedente não pode ser nula.");

        Objects.requireNonNull(
                excessRatio,
                "A razão de excesso não pode ser nula.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        if (monthlyRevenue.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A receita mensal não pode ser negativa.");
        }

        if (revenueWithinSublimit.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A parcela da receita dentro do sublimite "
                            + "não pode ser negativa.");
        }

        if (excessMonthlyRevenue.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A parcela mensal excedente não pode ser negativa.");
        }

        if (excessRatio.compareTo(
                BigDecimal.ZERO) < 0
                || excessRatio.compareTo(
                        BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                    "A razão de excesso deve estar entre zero e um.");
        }

        BigDecimal reconstructedMonthlyRevenue = revenueWithinSublimit.add(
                excessMonthlyRevenue);

        if (reconstructedMonthlyRevenue.compareTo(
                monthlyRevenue) != 0) {
            throw new IllegalArgumentException(
                    "A soma das parcelas mensal normal e excedente "
                            + "deve corresponder à receita mensal.");
        }
    }

    public boolean hasExcessPortion() {
        return excessMonthlyRevenue.compareTo(
                BigDecimal.ZERO) > 0;
    }

    public boolean isEntireMonthlyRevenueAboveSublimit() {
        return monthlyRevenue.compareTo(
                BigDecimal.ZERO) > 0
                && excessMonthlyRevenue.compareTo(
                        monthlyRevenue) == 0;
    }

    public BigDecimal excessMonthlyRevenueForDisplay() {
        return excessMonthlyRevenue.setScale(
                2,
                RoundingMode.HALF_UP);
    }

    public BigDecimal excessPercentageForDisplay() {
        return excessRatio
                .multiply(
                        new BigDecimal("100"))
                .setScale(
                        2,
                        RoundingMode.HALF_UP);
    }
}