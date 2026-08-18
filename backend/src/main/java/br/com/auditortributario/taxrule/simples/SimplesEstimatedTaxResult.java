package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.util.Objects;

public record SimplesEstimatedTaxResult(
        MonthlyRevenue taxableRevenue,
        SimplesEffectiveRateResult effectiveRateResult,
        BigDecimal rawTaxAmount,
        BigDecimal estimatedTaxAmount,
        SimplesEstimatedTaxStatus status,
        TaxDecision decision) {

    public SimplesEstimatedTaxResult {
        Objects.requireNonNull(
                taxableRevenue,
                "A receita tributável não pode ser nula.");

        Objects.requireNonNull(
                effectiveRateResult,
                "O resultado da alíquota efetiva não pode ser nulo.");

        Objects.requireNonNull(
                rawTaxAmount,
                "O valor bruto calculado não pode ser nulo.");

        Objects.requireNonNull(
                estimatedTaxAmount,
                "O valor estimado não pode ser nulo.");

        Objects.requireNonNull(
                status,
                "A situação do valor estimado não pode ser nula.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        if (rawTaxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor bruto calculado não pode ser negativo.");
        }

        if (estimatedTaxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor estimado não pode ser negativo.");
        }
    }
}