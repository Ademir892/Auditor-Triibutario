package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.simples.SimplesEffectiveRateResult;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxResult;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracketSelectionResult;
import br.com.auditortributario.taxrule.simples.composition.SimplesTaxCompositionResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SimplesServiceRevenueTaxResult(
        SimplesRevenueClassificationResult classification,
        SimplesTaxBracketSelectionResult bracketSelectionResult,
        SimplesEffectiveRateResult effectiveRateResult,
        SimplesEstimatedTaxResult estimatedTaxResult,
        SimplesTaxCompositionResult taxCompositionResult,
        SimplesRevenueTaxAdjustmentResult adjustment,
        TaxDecision decision) implements SimplesRevenueTaxProcessingResult {

    public SimplesServiceRevenueTaxResult {
        Objects.requireNonNull(
                classification,
                "A classificação não pode ser nula.");

        Objects.requireNonNull(
                bracketSelectionResult,
                "O resultado da seleção da faixa não pode ser nulo.");

        Objects.requireNonNull(
                effectiveRateResult,
                "O resultado da alíquota efetiva não pode ser nulo.");

        Objects.requireNonNull(
                estimatedTaxResult,
                "O resultado tributário estimado não pode ser nulo.");

        Objects.requireNonNull(
                taxCompositionResult,
                "A composição tributária não pode ser nula.");

        Objects.requireNonNull(
                adjustment,
                "O ajuste tributário não pode ser nulo.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        if (!classification.isResolved()) {
            throw new IllegalArgumentException(
                    "O resultado de serviço exige "
                            + "classificação tributária resolvida.");
        }

        if (classification.route().isEmpty()) {
            throw new IllegalArgumentException(
                    "O resultado de serviço exige rota tributária.");
        }

        SimplesRevenueTaxRoute route = classification
                .route()
                .orElseThrow();

        if (route != SimplesRevenueTaxRoute.ANNEX_III
                && route != SimplesRevenueTaxRoute.ANNEX_V) {

            throw new IllegalArgumentException(
                    "O resultado de serviço suporta somente "
                            + "Anexo III ou Anexo V.");
        }

        if (!classification
                .revenue()
                .equals(
                        adjustment.revenue())) {

            throw new IllegalArgumentException(
                    "A classificação e o ajuste devem "
                            + "pertencer à mesma receita.");
        }
    }

    @Override
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

    @Override
    public boolean isFinal() {
        return adjustment.isFinal();
    }

    @Override
    public boolean hasExternalObligation() {
        return adjustment.status() == SimplesRevenueTaxAdjustmentStatus.APPLIED_WITH_EXTERNAL_OBLIGATION;
    }

    @Override
    public List<SimplesAdjustedTaxComponent> adjustedComponents() {
        return adjustment.components();
    }
}