package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.YearMonth;
import java.util.Objects;

public final class SimplesIcmsTransitionalCalculator {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private static final BigDecimal SUBLIMIT_1800000 = new BigDecimal("1800000.00");

    private static final BigDecimal SUBLIMIT_3600000 = new BigDecimal("3600000.00");

    private static final String RULE_CODE = "SIMPLES_ICMS_TRANSITIONAL_CALCULATION";

    private static final String RULE_VERSION = "LC123-CGSN140-2018-2026";

    private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, art. 18, "
            + "§§ 16, 16-A, 17 e 17-A; "
            + "Resolução CGSN nº 140/2018, art. 24.";

    private final SimplesRevenueTaxBracketSelector bracketSelector;

    private final SimplesRevenueTaxDistributionRegistry distributionRegistry;

    public SimplesIcmsTransitionalCalculator() {
        this.bracketSelector = new SimplesRevenueTaxBracketSelector();

        this.distributionRegistry = new SimplesRevenueTaxDistributionRegistry();
    }

    public SimplesIcmsTransitionalCalculationResult calculate(
            SimplesRevenueTaxRoute route,
            BigDecimal annualSublimit,
            SimplesIcmsSublimitEffectResult icmsEffect,
            SimplesSublimitMonthlyExcessResult monthlyExcess) {
        Objects.requireNonNull(
                route,
                "A rota tributária não pode ser nula.");

        Objects.requireNonNull(
                annualSublimit,
                "O sublimite anual não pode ser nulo.");

        Objects.requireNonNull(
                icmsEffect,
                "O efeito do sublimite sobre o ICMS não pode ser nulo.");

        Objects.requireNonNull(
                monthlyExcess,
                "O resultado do excesso mensal não pode ser nulo.");

        validateRoute(
                route);

        validateAnnualSublimit(
                annualSublimit);

        validateTransitionalState(
                icmsEffect);

        validateEvaluationCompatibility(
                icmsEffect,
                monthlyExcess);

        YearMonth competence = icmsEffect.assessmentPeriod();

        int referenceBracketNumber = determineReferenceBracketNumber(
                annualSublimit);

        SimplesRevenueTaxBracket referenceBracket = bracketSelector.select(
                route,
                competence,
                annualSublimit);

        if (referenceBracket.number() != referenceBracketNumber) {

            throw new IllegalStateException(
                    "A faixa encontrada para o sublimite anual "
                            + "não corresponde à faixa legal "
                            + "de referência do cálculo transitório.");
        }

        BigDecimal referenceEffectiveRate = calculateReferenceEffectiveRate(
                annualSublimit,
                referenceBracket);

        SimplesRevenueTaxDistributionRule distributionRule = distributionRegistry.find(
                route,
                referenceBracketNumber,
                competence);

        BigDecimal icmsDistributionRate = findIcmsDistributionRate(
                distributionRule);

        BigDecimal icmsEffectiveRate = referenceEffectiveRate.multiply(
                icmsDistributionRate,
                MATH_CONTEXT);

        BigDecimal icmsAmount = monthlyExcess
                .excessMonthlyRevenue()
                .multiply(
                        icmsEffectiveRate,
                        MATH_CONTEXT);

        TaxDecision decision = createDecision(
                route,
                competence,
                annualSublimit,
                referenceBracketNumber,
                referenceEffectiveRate,
                icmsDistributionRate,
                icmsEffectiveRate,
                monthlyExcess,
                icmsAmount);

        return new SimplesIcmsTransitionalCalculationResult(
                route,
                competence,
                annualSublimit,
                referenceBracketNumber,
                referenceEffectiveRate,
                icmsDistributionRate,
                icmsEffectiveRate,
                monthlyExcess,
                icmsAmount,
                decision);
    }

    private void validateRoute(
            SimplesRevenueTaxRoute route) {
        if (route != SimplesRevenueTaxRoute.ANNEX_I
                && route != SimplesRevenueTaxRoute.ANNEX_II) {

            throw new IllegalArgumentException(
                    "O cálculo transitório do ICMS suporta "
                            + "somente os Anexos I e II.");
        }
    }

    private void validateAnnualSublimit(
            BigDecimal annualSublimit) {
        if (annualSublimit.compareTo(
                SUBLIMIT_1800000) != 0
                && annualSublimit.compareTo(
                        SUBLIMIT_3600000) != 0) {

            throw new IllegalArgumentException(
                    "O cálculo transitório atualmente suporta "
                            + "os sublimites anuais de "
                            + "R$ 1.800.000,00 e R$ 3.600.000,00.");
        }
    }

    private void validateTransitionalState(
            SimplesIcmsSublimitEffectResult icmsEffect) {
        if (!icmsEffect.requiresTransitionalCalculation()) {
            throw new IllegalArgumentException(
                    "O cálculo transitório do ICMS somente pode "
                            + "ser executado quando o status for "
                            + "IN_DAS_TRANSITIONAL.");
        }
    }

    private void validateEvaluationCompatibility(
            SimplesIcmsSublimitEffectResult icmsEffect,
            SimplesSublimitMonthlyExcessResult monthlyExcess) {
        SimplesSublimitEvaluationResult temporalEvaluation = icmsEffect
                .temporalEffect()
                .evaluationResult();

        if (!temporalEvaluation.equals(
                monthlyExcess.evaluationResult())) {
            throw new IllegalArgumentException(
                    "O efeito temporal do ICMS e o excesso mensal "
                            + "devem pertencer à mesma avaliação "
                            + "de sublimite.");
        }
    }

    private int determineReferenceBracketNumber(
            BigDecimal annualSublimit) {
        if (annualSublimit.compareTo(
                SUBLIMIT_1800000) == 0) {
            return 4;
        }

        return 5;
    }

    private BigDecimal calculateReferenceEffectiveRate(
            BigDecimal annualSublimit,
            SimplesRevenueTaxBracket referenceBracket) {
        BigDecimal numerator = annualSublimit
                .multiply(
                        referenceBracket.nominalRate(),
                        MATH_CONTEXT)
                .subtract(
                        referenceBracket.deduction(),
                        MATH_CONTEXT);

        BigDecimal effectiveRate = numerator.divide(
                annualSublimit,
                MATH_CONTEXT);

        if (effectiveRate.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "A alíquota efetiva de referência "
                            + "do ICMS não pode ser negativa.");
        }

        return effectiveRate;
    }

    private BigDecimal findIcmsDistributionRate(
            SimplesRevenueTaxDistributionRule distributionRule) {
        return distributionRule
                .shares()
                .stream()
                .filter(
                        share -> share.component() == TaxComponent.ICMS)
                .map(
                        SimplesRevenueTaxDistributionRule.ComponentShare::distributionRate)
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "A faixa tributária de referência "
                                        + "não possui percentual "
                                        + "de repartição para ICMS."));
    }

    private TaxDecision createDecision(
            SimplesRevenueTaxRoute route,
            YearMonth competence,
            BigDecimal annualSublimit,
            int referenceBracketNumber,
            BigDecimal referenceEffectiveRate,
            BigDecimal icmsDistributionRate,
            BigDecimal icmsEffectiveRate,
            SimplesSublimitMonthlyExcessResult monthlyExcess,
            BigDecimal icmsAmount) {
        return new TaxDecision(
                RULE_CODE,
                RULE_VERSION,
                "Cálculo transitório do ICMS incidente "
                        + "sobre a parcela da receita mensal "
                        + "que excedeu o sublimite.",
                "Competencia="
                        + competence
                        + "; Rota="
                        + route
                        + "; SublimiteAnual="
                        + annualSublimit.toPlainString()
                        + "; ReceitaMensalExcedente="
                        + monthlyExcess
                                .excessMonthlyRevenue()
                                .toPlainString(),
                "FaixaReferencia="
                        + referenceBracketNumber
                        + "; AliquotaEfetivaReferencia="
                        + referenceEffectiveRate.toPlainString()
                        + "; PercentualReparticaoICMS="
                        + icmsDistributionRate.toPlainString(),
                "PercentualEfetivoICMS="
                        + icmsEffectiveRate.toPlainString()
                        + "; ValorICMS="
                        + icmsAmount.toPlainString(),
                LEGAL_REFERENCE);
    }
}