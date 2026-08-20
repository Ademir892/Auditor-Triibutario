package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

public record SimplesGoodsRevenueTaxResult(
        SimplesRevenueClassificationResult classification,
        SimplesRevenueTaxCalculationResult baseCalculation,
        SimplesRevenueTaxAdjustmentResult adjustment,
        TaxDecision decision) {

    public SimplesGoodsRevenueTaxResult {
        Objects.requireNonNull(
                classification,
                "A classificação não pode ser nula.");

        Objects.requireNonNull(
                baseCalculation,
                "O cálculo-base não pode ser nulo.");

        Objects.requireNonNull(
                adjustment,
                "O ajuste tributário não pode ser nulo.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        if (!classification.isResolved()) {
            throw new IllegalArgumentException(
                    "O resultado final exige classificação resolvida.");
        }

        if (!classification
                .revenue()
                .equals(
                        baseCalculation.revenue())) {

            throw new IllegalArgumentException(
                    "A classificação e o cálculo-base "
                            + "devem pertencer à mesma receita.");
        }

        if (!baseCalculation
                .revenue()
                .equals(
                        adjustment.revenue())) {

            throw new IllegalArgumentException(
                    "O cálculo-base e o ajuste "
                            + "devem pertencer à mesma receita.");
        }
    }

    public Optional<BigDecimal> finalTaxAmount() {
        return adjustment.adjustedSimplesAmount();
    }

    public Optional<BigDecimal> finalTaxAmountForDisplay() {
        return finalTaxAmount()
                .map(
                        amount -> amount.setScale(
                                2,
                                RoundingMode.HALF_UP));
    }

    public Optional<BigDecimal> reductionAmountForDisplay() {
        return adjustment
                .reductionAmount()
                .map(
                        amount -> amount.setScale(
                                2,
                                RoundingMode.HALF_UP));
    }

    public boolean isFinal() {
        return adjustment.isFinal();
    }

    public boolean requiresAdditionalRules() {
        return adjustment.status() == SimplesRevenueTaxAdjustmentStatus.REQUIRES_ADDITIONAL_RULES;
    }

    public boolean hasExternalObligation() {
        return adjustment.status() == SimplesRevenueTaxAdjustmentStatus.APPLIED_WITH_EXTERNAL_OBLIGATION;
    }
}