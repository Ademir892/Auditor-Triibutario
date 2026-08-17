package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record SimplesEffectiveRateResult(
        SimplesTaxBracketSelectionResult bracketSelectionResult,
        BigDecimal calculationRevenueBasis,
        BigDecimal effectiveRate,
        TaxDecision decision) {

    public SimplesEffectiveRateResult {
        Objects.requireNonNull(
                bracketSelectionResult,
                "O resultado da seleção da faixa não pode ser nulo.");

        Objects.requireNonNull(
                calculationRevenueBasis,
                "A base utilizada no cálculo não pode ser nula.");

        Objects.requireNonNull(
                effectiveRate,
                "A alíquota efetiva não pode ser nula.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        if (calculationRevenueBasis.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "A base utilizada no cálculo deve ser maior que zero.");
        }

        if (effectiveRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A alíquota efetiva não pode ser negativa.");
        }
    }

    public BigDecimal effectiveRateAsPercentage() {
        return effectiveRate
                .movePointRight(2)
                .setScale(
                        5,
                        RoundingMode.HALF_UP);
    }
}