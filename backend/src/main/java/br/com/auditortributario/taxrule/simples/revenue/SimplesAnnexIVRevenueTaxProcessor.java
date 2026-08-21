package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.RevenueActivityType;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;

import java.math.BigDecimal;
import java.util.Objects;

public final class SimplesAnnexIVRevenueTaxProcessor {

    private static final String RULE_VERSION = "SIMPLES-ANNEX-IV-2018-2026";

    private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, "
            + "art. 13, VI, e art. 18, § 5º-C; "
            + "Resolução CGSN nº 140/2018.";

    private final SimplesRevenueClassifier classifier;

    private final SimplesRevenueTaxCalculator taxCalculator;

    private final SimplesRevenueTaxCompositionAdjuster compositionAdjuster;

    public SimplesAnnexIVRevenueTaxProcessor() {
        this.classifier = new SimplesRevenueClassifier();

        this.taxCalculator = new SimplesRevenueTaxCalculator();

        this.compositionAdjuster = new SimplesRevenueTaxCompositionAdjuster();
    }

    public SimplesAnnexIVRevenueTaxResult process(
            RevenueEntry revenue,
            BigDecimal revenueBasis,
            SimplesServiceTaxRule serviceTaxRule) {
        Objects.requireNonNull(
                revenue,
                "A receita não pode ser nula.");

        Objects.requireNonNull(
                revenueBasis,
                "A base de enquadramento não pode ser nula.");

        Objects.requireNonNull(
                serviceTaxRule,
                "A regra tributária do serviço não pode ser nula.");

        validateRevenue(
                revenue,
                serviceTaxRule);

        SimplesRevenueClassificationResult classification = classifier.classify(
                revenue,
                serviceTaxRule);

        validateClassification(
                classification);

        SimplesRevenueTaxCalculationResult baseCalculation = taxCalculator.calculate(
                classification,
                revenueBasis);

        SimplesRevenueTaxAdjustmentResult adjustment = compositionAdjuster.adjust(
                revenue,
                baseCalculation.composition());

        TaxDecision decision = createDecision(
                revenue,
                revenueBasis,
                serviceTaxRule,
                baseCalculation,
                adjustment);

        return new SimplesAnnexIVRevenueTaxResult(
                classification,
                serviceTaxRule,
                baseCalculation,
                adjustment,
                decision);
    }

    private void validateRevenue(
            RevenueEntry revenue,
            SimplesServiceTaxRule serviceTaxRule) {
        if (revenue.activityType() != RevenueActivityType.SERVICE) {

            throw new IllegalArgumentException(
                    "O processador do Anexo IV aceita somente "
                            + "receitas classificadas como SERVICE.");
        }

        if (revenue.subjectToFatorR()) {
            throw new IllegalArgumentException(
                    "Uma receita sujeita ao Fator R deve ser "
                            + "processada pelo fluxo dos Anexos III/V.");
        }

        if (serviceTaxRule.getRoute() != SimplesRevenueTaxRoute.ANNEX_IV) {

            throw new IllegalArgumentException(
                    "A regra tributária informada não corresponde "
                            + "ao Anexo IV.");
        }
    }

    private void validateClassification(
            SimplesRevenueClassificationResult classification) {
        if (!classification.isResolved()) {
            throw new IllegalStateException(
                    "A classificação do serviço do Anexo IV "
                            + "deveria estar resolvida.");
        }

        SimplesRevenueTaxRoute route = classification
                .route()
                .orElseThrow();

        if (route != SimplesRevenueTaxRoute.ANNEX_IV) {
            throw new IllegalStateException(
                    "A classificação do serviço deveria produzir "
                            + "a rota Anexo IV.");
        }
    }

    private TaxDecision createDecision(
            RevenueEntry revenue,
            BigDecimal revenueBasis,
            SimplesServiceTaxRule serviceTaxRule,
            SimplesRevenueTaxCalculationResult baseCalculation,
            SimplesRevenueTaxAdjustmentResult adjustment) {
        String finalAmount = adjustment
                .adjustedSimplesAmount()
                .map(
                        BigDecimal::toPlainString)
                .orElse(
                        "PENDENTE");

        return new TaxDecision(
                "SIMPLES_ANNEX_IV_REVENUE_PROCESSING",
                RULE_VERSION,
                "Processamento tributário de receita de serviço "
                        + "enquadrada no Anexo IV.",
                "Competência="
                        + revenue.competence()
                        + "; Receita="
                        + revenue.amount()
                        + "; Base="
                        + revenueBasis
                        + "; RegraServico="
                        + serviceTaxRule,
                "Anexo=IV"
                        + "; Faixa="
                        + baseCalculation
                                .bracket()
                                .number()
                        + "; AliquotaEfetiva="
                        + baseCalculation
                                .effectiveRate()
                                .toPlainString()
                        + "; CPPForaDoDAS=true"
                        + "; StatusAjuste="
                        + adjustment.status(),
                "ValorBrutoDAS="
                        + baseCalculation
                                .rawTaxAmount()
                                .toPlainString()
                        + "; ValorFinalDAS="
                        + finalAmount
                        + "; CPP=OBRIGACAO_EXTERNA",
                LEGAL_REFERENCE);
    }
}