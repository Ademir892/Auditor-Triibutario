package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;

import java.math.BigDecimal;
import java.util.Objects;

public final class SimplesGoodsRevenueTaxProcessor {

    private static final String RULE_VERSION = "SIMPLES-GOODS-REVENUE-2018-2026";

    private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, art. 18; "
            + "Resolução CGSN nº 140/2018, art. 25.";

    private final SimplesRevenueClassifier classifier;

    private final SimplesRevenueTaxCalculator taxCalculator;

    private final SimplesRevenueTaxCompositionAdjuster compositionAdjuster;

    public SimplesGoodsRevenueTaxProcessor() {
        this.classifier = new SimplesRevenueClassifier();

        this.taxCalculator = new SimplesRevenueTaxCalculator();

        this.compositionAdjuster = new SimplesRevenueTaxCompositionAdjuster();
    }

    public SimplesGoodsRevenueTaxResult process(
            RevenueEntry revenue,
            BigDecimal revenueBasis) {
        Objects.requireNonNull(
                revenue,
                "A receita não pode ser nula.");

        Objects.requireNonNull(
                revenueBasis,
                "A base de receita não pode ser nula.");

        SimplesRevenueClassificationResult classification = classifier.classify(
                revenue);

        if (!classification.isResolved()) {
            throw new IllegalArgumentException(
                    "A receita não possui classificação tributária "
                            + "resolvida para este processamento.");
        }

        SimplesRevenueTaxRoute route = classification
                .route()
                .orElseThrow();

        validateSupportedRoute(
                route);

        SimplesRevenueTaxCalculationResult baseCalculation = taxCalculator.calculate(
                classification,
                revenueBasis);

        SimplesRevenueTaxAdjustmentResult adjustment = compositionAdjuster.adjust(
                revenue,
                baseCalculation.composition());

        TaxDecision decision = createDecision(
                revenue,
                revenueBasis,
                baseCalculation,
                adjustment);

        return new SimplesGoodsRevenueTaxResult(
                classification,
                baseCalculation,
                adjustment,
                decision);
    }

    private void validateSupportedRoute(
            SimplesRevenueTaxRoute route) {
        if (route != SimplesRevenueTaxRoute.ANNEX_I
                && route != SimplesRevenueTaxRoute.ANNEX_II) {

            throw new IllegalArgumentException(
                    "Este processador atualmente suporta apenas "
                            + "receitas classificadas nos Anexos I e II.");
        }
    }

    private TaxDecision createDecision(
            RevenueEntry revenue,
            BigDecimal revenueBasis,
            SimplesRevenueTaxCalculationResult baseCalculation,
            SimplesRevenueTaxAdjustmentResult adjustment) {
        String finalAmount = adjustment
                .adjustedSimplesAmount()
                .map(
                        BigDecimal::toPlainString)
                .orElse(
                        "PENDENTE");

        return new TaxDecision(
                "SIMPLES_GOODS_REVENUE_PROCESSING",
                RULE_VERSION,
                "Processamento completo de receita segregada "
                        + "de comércio ou indústria no Simples Nacional.",
                "Competência="
                        + revenue.competence()
                        + "; Atividade="
                        + revenue.activityType()
                        + "; Receita="
                        + revenue.amount()
                        + "; Base="
                        + revenueBasis
                        + "; Tratamentos="
                        + revenue.treatments(),
                "Rota="
                        + baseCalculation.route()
                        + "; Faixa="
                        + baseCalculation.bracket().number()
                        + "; StatusAjuste="
                        + adjustment.status(),
                "ValorOriginal="
                        + baseCalculation.rawTaxAmount()
                        + "; ValorFinal="
                        + finalAmount,
                LEGAL_REFERENCE);
    }
}