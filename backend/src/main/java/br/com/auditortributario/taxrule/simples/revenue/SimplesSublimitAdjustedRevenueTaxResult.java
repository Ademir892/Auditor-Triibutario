package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SimplesSublimitAdjustedRevenueTaxResult(
        SimplesRevenueTaxProcessingResult originalResult,
        SimplesSublimitTaxTreatment treatment,
        List<SimplesAdjustedTaxComponent> adjustedComponents,
        Optional<BigDecimal> finalTaxAmount,
        boolean finalResult,
        boolean externalObligation,
        TaxDecision decision)
        implements SimplesRevenueTaxProcessingResult {

    public SimplesSublimitAdjustedRevenueTaxResult {

        Objects.requireNonNull(
                originalResult,
                "O resultado tributário original não pode ser nulo.");

        Objects.requireNonNull(
                treatment,
                "O tratamento do sublimite não pode ser nulo.");

        Objects.requireNonNull(
                adjustedComponents,
                "Os componentes tributários ajustados não podem ser nulos.");

        Objects.requireNonNull(
                finalTaxAmount,
                "O valor final opcional não pode ser nulo.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        adjustedComponents = List.copyOf(
                adjustedComponents);

        if (finalResult
                && finalTaxAmount.isEmpty()) {

            throw new IllegalArgumentException(
                    "Um resultado final precisa possuir "
                            + "valor tributário final.");
        }

        if (treatment.hasExternalObligation()
                && !externalObligation) {

            throw new IllegalArgumentException(
                    "O tratamento fora do DAS precisa "
                            + "indicar obrigação externa.");
        }
    }

    @Override
    public SimplesRevenueClassificationResult classification() {
        return originalResult.classification();
    }

    @Override
    public boolean isFinal() {
        return finalResult;
    }

    @Override
    public boolean hasExternalObligation() {
        return externalObligation;
    }
}