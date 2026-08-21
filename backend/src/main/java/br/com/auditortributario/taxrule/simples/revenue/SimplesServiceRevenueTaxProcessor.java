package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.simples.FatorRCalculationResult;
import br.com.auditortributario.taxrule.simples.MonthlyRevenue;
import br.com.auditortributario.taxrule.simples.SimplesAnnex;
import br.com.auditortributario.taxrule.simples.SimplesEffectiveRateCalculator;
import br.com.auditortributario.taxrule.simples.SimplesEffectiveRateResult;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxCalculator;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxRequest;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxResult;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracketSelectionRequest;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracketSelectionResult;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracketSelector;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisResult;
import br.com.auditortributario.taxrule.simples.composition.SimplesTaxCompositionCalculator;
import br.com.auditortributario.taxrule.simples.composition.SimplesTaxCompositionResult;

import java.math.BigDecimal;
import java.util.Objects;

public final class SimplesServiceRevenueTaxProcessor
        implements SimplesServiceRevenueTaxEngine {

    private static final String RULE_VERSION = "SIMPLES-SERVICE-REVENUE-2018-2026";

    private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, art. 18; "
            + "Resolução CGSN nº 140/2018.";

    private final SimplesTaxBracketSelector bracketSelector;

    private final SimplesEffectiveRateCalculator effectiveRateCalculator;

    private final SimplesEstimatedTaxCalculator estimatedTaxCalculator;

    private final SimplesTaxCompositionCalculator compositionCalculator;

    private final SimplesRevenueTaxCompositionAdjuster compositionAdjuster;

    public SimplesServiceRevenueTaxProcessor() {
        this.bracketSelector = new SimplesTaxBracketSelector();

        this.effectiveRateCalculator = new SimplesEffectiveRateCalculator();

        this.estimatedTaxCalculator = new SimplesEstimatedTaxCalculator();

        this.compositionCalculator = new SimplesTaxCompositionCalculator();

        this.compositionAdjuster = new SimplesRevenueTaxCompositionAdjuster();
    }

    @Override
    public SimplesServiceRevenueTaxResult process(
            RevenueEntry revenue,
            TaxBracketRevenueBasisResult revenueBasisResult,
            FatorRCalculationResult fatorRResult,
            SimplesRevenueClassificationResult classification) {
        Objects.requireNonNull(
                revenue,
                "A receita não pode ser nula.");

        Objects.requireNonNull(
                revenueBasisResult,
                "O resultado da base de enquadramento não pode ser nulo.");

        Objects.requireNonNull(
                fatorRResult,
                "O resultado do Fator R não pode ser nulo.");

        Objects.requireNonNull(
                classification,
                "A classificação não pode ser nula.");

        validateClassification(
                revenue,
                fatorRResult,
                classification);

        SimplesTaxBracketSelectionResult bracketSelectionResult = bracketSelector.select(
                new SimplesTaxBracketSelectionRequest(
                        revenue.competence(),
                        fatorRResult,
                        revenueBasisResult));

        SimplesEffectiveRateResult effectiveRateResult = effectiveRateCalculator.calculate(
                bracketSelectionResult);

        MonthlyRevenue taxableRevenue = new MonthlyRevenue(
                revenue.competence(),
                revenue.amount());

        SimplesEstimatedTaxResult estimatedTaxResult = estimatedTaxCalculator.calculate(
                new SimplesEstimatedTaxRequest(
                        taxableRevenue,
                        effectiveRateResult));

        SimplesTaxCompositionResult taxCompositionResult = compositionCalculator.calculate(
                estimatedTaxResult);

        SimplesRevenueTaxAdjustmentResult adjustment = compositionAdjuster.adjust(
                revenue,
                taxCompositionResult.composition());

        TaxDecision decision = createDecision(
                revenue,
                revenueBasisResult,
                fatorRResult,
                bracketSelectionResult,
                effectiveRateResult,
                estimatedTaxResult,
                taxCompositionResult,
                adjustment);

        return new SimplesServiceRevenueTaxResult(
                classification,
                bracketSelectionResult,
                effectiveRateResult,
                estimatedTaxResult,
                taxCompositionResult,
                adjustment,
                decision);
    }

    private void validateClassification(
            RevenueEntry revenue,
            FatorRCalculationResult fatorRResult,
            SimplesRevenueClassificationResult classification) {
        if (!classification.isResolved()) {
            throw new IllegalArgumentException(
                    "A receita de serviço precisa possuir "
                            + "classificação resolvida.");
        }

        if (!classification
                .revenue()
                .equals(
                        revenue)) {

            throw new IllegalArgumentException(
                    "A classificação deve pertencer "
                            + "à mesma receita processada.");
        }

        SimplesRevenueTaxRoute route = classification
                .route()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "A classificação resolvida "
                                        + "deve possuir rota tributária."));

        SimplesRevenueTaxRoute expectedRoute = routeFor(
                fatorRResult.annex());

        if (route != expectedRoute) {
            throw new IllegalArgumentException(
                    "A rota da classificação "
                            + route
                            + " não corresponde ao anexo definido "
                            + "pelo Fator R: "
                            + expectedRoute
                            + ".");
        }
    }

    private SimplesRevenueTaxRoute routeFor(
            SimplesAnnex annex) {
        Objects.requireNonNull(
                annex,
                "O anexo não pode ser nulo.");

        return switch (annex) {
            case ANEXO_III ->
                SimplesRevenueTaxRoute.ANNEX_III;

            case ANEXO_V ->
                SimplesRevenueTaxRoute.ANNEX_V;
        };
    }

    private TaxDecision createDecision(
            RevenueEntry revenue,
            TaxBracketRevenueBasisResult revenueBasisResult,
            FatorRCalculationResult fatorRResult,
            SimplesTaxBracketSelectionResult bracketSelectionResult,
            SimplesEffectiveRateResult effectiveRateResult,
            SimplesEstimatedTaxResult estimatedTaxResult,
            SimplesTaxCompositionResult taxCompositionResult,
            SimplesRevenueTaxAdjustmentResult adjustment) {
        String finalAmount = adjustment
                .adjustedSimplesAmount()
                .map(
                        BigDecimal::toPlainString)
                .orElse(
                        "PENDENTE");

        return new TaxDecision(
                "SIMPLES_SERVICE_REVENUE_PROCESSING",
                RULE_VERSION,
                "Processamento completo de receita de serviço "
                        + "sujeita ao Fator R.",
                "Competência="
                        + revenue.competence()
                        + "; Receita="
                        + revenue.amount()
                        + "; FatorR="
                        + fatorRResult
                                .fatorR()
                                .value()
                                .toPlainString()
                        + "; Base="
                        + revenueBasisResult
                                .revenueBasis()
                                .toPlainString()
                        + "; TipoBase="
                        + revenueBasisResult
                                .basisType()
                                .getCode(),
                "Anexo="
                        + fatorRResult
                                .annex()
                                .getDisplayName()
                        + "; Faixa="
                        + bracketSelectionResult
                                .bracket()
                                .number()
                        + "; AliquotaEfetiva="
                        + effectiveRateResult
                                .effectiveRate()
                                .toPlainString()
                        + "; ISSCap="
                        + taxCompositionResult.issCapApplied()
                        + "; StatusAjuste="
                        + adjustment.status(),
                "ValorOriginal="
                        + estimatedTaxResult
                                .rawTaxAmount()
                                .toPlainString()
                        + "; ValorFinal="
                        + finalAmount,
                LEGAL_REFERENCE);
    }
}