package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SimplesAnnexIVRevenueTaxResult(
        SimplesRevenueClassificationResult classification,
        SimplesServiceTaxRule serviceTaxRule,
        SimplesRevenueTaxCalculationResult baseCalculation,
        SimplesRevenueTaxAdjustmentResult adjustment,
        TaxDecision decision) implements SimplesRevenueTaxProcessingResult {

    public SimplesAnnexIVRevenueTaxResult {
        Objects.requireNonNull(
                classification,
                "A classificação tributária não pode ser nula.");

        Objects.requireNonNull(
                serviceTaxRule,
                "A regra tributária do serviço não pode ser nula.");

        Objects.requireNonNull(
                baseCalculation,
                "O cálculo tributário base não pode ser nulo.");

        Objects.requireNonNull(
                adjustment,
                "O ajuste tributário não pode ser nulo.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        if (!classification.isResolved()) {
            throw new IllegalArgumentException(
                    "O resultado do Anexo IV exige "
                            + "classificação tributária resolvida.");
        }

        SimplesRevenueTaxRoute route = classification
                .route()
                .orElseThrow();

        if (route != SimplesRevenueTaxRoute.ANNEX_IV) {
            throw new IllegalArgumentException(
                    "O resultado suporta somente receitas "
                            + "classificadas no Anexo IV.");
        }

        if (serviceTaxRule.getRoute() != SimplesRevenueTaxRoute.ANNEX_IV) {

            throw new IllegalArgumentException(
                    "A regra tributária informada não direciona "
                            + "a receita ao Anexo IV.");
        }

        if (baseCalculation.route() != SimplesRevenueTaxRoute.ANNEX_IV) {

            throw new IllegalArgumentException(
                    "O cálculo base precisa pertencer ao Anexo IV.");
        }

        if (!classification
                .revenue()
                .equals(
                        baseCalculation.revenue())) {

            throw new IllegalArgumentException(
                    "A classificação e o cálculo base devem "
                            + "pertencer à mesma receita.");
        }

        if (!classification
                .revenue()
                .equals(
                        adjustment.revenue())) {

            throw new IllegalArgumentException(
                    "A classificação e o ajuste tributário devem "
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

    @Override
    public boolean isFinal() {
        return adjustment.isFinal();
    }

    @Override
    public boolean hasExternalObligation() {
        /*
         * No Anexo IV, a CPP não integra o DAS.
         *
         * Portanto, independentemente de outros tratamentos
         * tributários da receita, existe obrigação previdenciária
         * estrutural fora do valor calculado por este resultado.
         */
        return true;
    }

    public boolean hasCppOutsideDas() {
        return true;
    }

    @Override
    public List<SimplesAdjustedTaxComponent> adjustedComponents() {
        return adjustment.components();
    }
}