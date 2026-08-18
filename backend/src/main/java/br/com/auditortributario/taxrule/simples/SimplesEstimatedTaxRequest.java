package br.com.auditortributario.taxrule.simples;

import java.util.Objects;

public record SimplesEstimatedTaxRequest(
        MonthlyRevenue taxableRevenue,
        SimplesEffectiveRateResult effectiveRateResult) {

    public SimplesEstimatedTaxRequest {
        Objects.requireNonNull(
                taxableRevenue,
                "A receita tributável do período não pode ser nula.");

        Objects.requireNonNull(
                effectiveRateResult,
                "O resultado da alíquota efetiva não pode ser nulo.");
    }
}