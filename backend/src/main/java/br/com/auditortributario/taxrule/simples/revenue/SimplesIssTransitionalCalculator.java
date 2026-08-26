package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.YearMonth;
import java.util.Objects;

public final class SimplesIssTransitionalCalculator {

        private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

        private static final BigDecimal SUBLIMIT_1800000 = new BigDecimal("1800000.00");

        private static final BigDecimal SUBLIMIT_3600000 = new BigDecimal("3600000.00");

        private static final BigDecimal ISS_MAXIMUM_EFFECTIVE_RATE = new BigDecimal("0.05");

        private static final String RULE_CODE = "SIMPLES_ISS_TRANSITIONAL_CALCULATION";

        private static final String RULE_VERSION = "LC123-CGSN140-2018-2026";

        private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, art. 18; "
                        + "Resolução CGSN nº 140/2018, "
                        + "arts. 21 e 24.";

        /*
         * Compatibilidade com o cenário anterior de uma única
         * receita segregada na competência.
         */
        public SimplesIssTransitionalCalculationResult calculate(
                        SimplesRevenueTaxRoute route,
                        BigDecimal annualSublimit,
                        SimplesIssSublimitEffectResult issEffect,
                        SimplesSublimitMonthlyExcessResult monthlyExcess) {
                return calculate(
                                route,
                                annualSublimit,
                                issEffect,
                                monthlyExcess,
                                monthlyExcess.monthlyRevenue());
        }

        /*
         * Método destinado ao processamento consolidado de
         * múltiplas receitas segregadas.
         */
        public SimplesIssTransitionalCalculationResult calculate(
                        SimplesRevenueTaxRoute route,
                        BigDecimal annualSublimit,
                        SimplesIssSublimitEffectResult issEffect,
                        SimplesSublimitMonthlyExcessResult monthlyExcess,
                        BigDecimal segregatedRevenueAmount) {
                Objects.requireNonNull(
                                route,
                                "A rota tributária não pode ser nula.");

                Objects.requireNonNull(
                                annualSublimit,
                                "O sublimite anual não pode ser nulo.");

                Objects.requireNonNull(
                                issEffect,
                                "O efeito do sublimite sobre o ISS não pode ser nulo.");

                Objects.requireNonNull(
                                monthlyExcess,
                                "O resultado do excesso mensal não pode ser nulo.");

                Objects.requireNonNull(
                                segregatedRevenueAmount,
                                "A receita segregada não pode ser nula.");

                validateRoute(
                                route);

                validateAnnualSublimit(
                                annualSublimit);

                validateTransitionalState(
                                issEffect);

                validateEvaluationCompatibility(
                                issEffect,
                                monthlyExcess);

                validateSegregatedRevenue(
                                segregatedRevenueAmount,
                                monthlyExcess);

                IssReference reference = referenceFor(
                                route,
                                annualSublimit);

                BigDecimal referenceEffectiveRate = calculateReferenceEffectiveRate(
                                annualSublimit,
                                reference.nominalRate(),
                                reference.deduction());

                BigDecimal rawIssEffectiveRate = referenceEffectiveRate.multiply(
                                reference.issDistributionRate(),
                                MATH_CONTEXT);

                boolean limitedToFivePercent = rawIssEffectiveRate.compareTo(
                                ISS_MAXIMUM_EFFECTIVE_RATE) > 0;

                BigDecimal issEffectiveRate = limitedToFivePercent
                                ? ISS_MAXIMUM_EFFECTIVE_RATE
                                : rawIssEffectiveRate;

                BigDecimal segregatedExcessRevenue = segregatedRevenueAmount.multiply(
                                monthlyExcess.excessRatio(),
                                MATH_CONTEXT);

                BigDecimal issAmount = segregatedExcessRevenue.multiply(
                                issEffectiveRate,
                                MATH_CONTEXT);

                TaxDecision decision = createDecision(
                                route,
                                issEffect.assessmentPeriod(),
                                annualSublimit,
                                reference,
                                referenceEffectiveRate,
                                rawIssEffectiveRate,
                                issEffectiveRate,
                                limitedToFivePercent,
                                monthlyExcess,
                                segregatedRevenueAmount,
                                segregatedExcessRevenue,
                                issAmount);

                return new SimplesIssTransitionalCalculationResult(
                                route,
                                issEffect.assessmentPeriod(),
                                annualSublimit,
                                reference.bracketNumber(),
                                referenceEffectiveRate,
                                reference.issDistributionRate(),
                                rawIssEffectiveRate,
                                issEffectiveRate,
                                limitedToFivePercent,
                                monthlyExcess,
                                segregatedRevenueAmount,
                                segregatedExcessRevenue,
                                issAmount,
                                decision);
        }

        private void validateRoute(
                        SimplesRevenueTaxRoute route) {
                if (route != SimplesRevenueTaxRoute.ANNEX_III
                                && route != SimplesRevenueTaxRoute.ANNEX_IV
                                && route != SimplesRevenueTaxRoute.ANNEX_V) {

                        throw new IllegalArgumentException(
                                        "O cálculo transitório do ISS suporta "
                                                        + "somente os Anexos III, IV e V.");
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
                        SimplesIssSublimitEffectResult issEffect) {
                if (!issEffect.requiresTransitionalCalculation()) {
                        throw new IllegalArgumentException(
                                        "O cálculo transitório do ISS somente pode "
                                                        + "ser executado quando o status for "
                                                        + "IN_DAS_TRANSITIONAL.");
                }
        }

        private void validateEvaluationCompatibility(
                        SimplesIssSublimitEffectResult issEffect,
                        SimplesSublimitMonthlyExcessResult monthlyExcess) {
                SimplesSublimitEvaluationResult temporalEvaluation = issEffect
                                .temporalEffect()
                                .evaluationResult();

                if (!temporalEvaluation.equals(
                                monthlyExcess.evaluationResult())) {
                        throw new IllegalArgumentException(
                                        "O efeito temporal do ISS e o excesso mensal "
                                                        + "devem pertencer à mesma avaliação "
                                                        + "de sublimite.");
                }
        }

        private void validateSegregatedRevenue(
                        BigDecimal segregatedRevenueAmount,
                        SimplesSublimitMonthlyExcessResult monthlyExcess) {
                if (segregatedRevenueAmount.compareTo(
                                BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                        "A receita segregada não pode ser negativa.");
                }

                if (segregatedRevenueAmount.compareTo(
                                monthlyExcess.monthlyRevenue()) > 0) {
                        throw new IllegalArgumentException(
                                        "A receita segregada não pode superar "
                                                        + "a receita total da competência.");
                }
        }

        private IssReference referenceFor(
                        SimplesRevenueTaxRoute route,
                        BigDecimal annualSublimit) {
                boolean lowerSublimit = annualSublimit.compareTo(
                                SUBLIMIT_1800000) == 0;

                return switch (route) {

                        case ANNEX_III ->
                                lowerSublimit
                                                ? new IssReference(
                                                                4,
                                                                new BigDecimal("0.1600"),
                                                                new BigDecimal("35640.00"),
                                                                new BigDecimal("0.3250"))
                                                : new IssReference(
                                                                5,
                                                                new BigDecimal("0.2100"),
                                                                new BigDecimal("125640.00"),
                                                                new BigDecimal("0.3350"));

                        case ANNEX_IV ->
                                lowerSublimit
                                                ? new IssReference(
                                                                4,
                                                                new BigDecimal("0.1400"),
                                                                new BigDecimal("39780.00"),
                                                                new BigDecimal("0.4000"))
                                                : new IssReference(
                                                                5,
                                                                new BigDecimal("0.2200"),
                                                                new BigDecimal("183780.00"),
                                                                new BigDecimal("0.4000"));

                        case ANNEX_V ->
                                lowerSublimit
                                                ? new IssReference(
                                                                4,
                                                                new BigDecimal("0.2050"),
                                                                new BigDecimal("17100.00"),
                                                                new BigDecimal("0.2100"))
                                                : new IssReference(
                                                                5,
                                                                new BigDecimal("0.2300"),
                                                                new BigDecimal("62100.00"),
                                                                new BigDecimal("0.2350"));

                        default ->
                                throw new IllegalArgumentException(
                                                "Rota sem referência transitória de ISS.");
                };
        }

        private BigDecimal calculateReferenceEffectiveRate(
                        BigDecimal annualSublimit,
                        BigDecimal nominalRate,
                        BigDecimal deduction) {
                BigDecimal effectiveRate = annualSublimit
                                .multiply(
                                                nominalRate,
                                                MATH_CONTEXT)
                                .subtract(
                                                deduction,
                                                MATH_CONTEXT)
                                .divide(
                                                annualSublimit,
                                                MATH_CONTEXT);

                if (effectiveRate.compareTo(
                                BigDecimal.ZERO) < 0) {
                        throw new IllegalStateException(
                                        "A alíquota efetiva de referência "
                                                        + "do ISS não pode ser negativa.");
                }

                return effectiveRate;
        }

        private TaxDecision createDecision(
                        SimplesRevenueTaxRoute route,
                        YearMonth competence,
                        BigDecimal annualSublimit,
                        IssReference reference,
                        BigDecimal referenceEffectiveRate,
                        BigDecimal rawIssEffectiveRate,
                        BigDecimal issEffectiveRate,
                        boolean limitedToFivePercent,
                        SimplesSublimitMonthlyExcessResult monthlyExcess,
                        BigDecimal segregatedRevenueAmount,
                        BigDecimal segregatedExcessRevenue,
                        BigDecimal issAmount) {
                return new TaxDecision(
                                RULE_CODE,
                                RULE_VERSION,
                                "Cálculo transitório do ISS incidente "
                                                + "sobre a parcela da receita segregada "
                                                + "que corresponde ao excesso do sublimite.",
                                "Competencia="
                                                + competence
                                                + "; Rota="
                                                + route
                                                + "; SublimiteAnual="
                                                + annualSublimit.toPlainString()
                                                + "; ReceitaMensalTotal="
                                                + monthlyExcess
                                                                .monthlyRevenue()
                                                                .toPlainString()
                                                + "; ReceitaSegregada="
                                                + segregatedRevenueAmount.toPlainString(),
                                "RazaoExcedente="
                                                + monthlyExcess
                                                                .excessRatio()
                                                                .toPlainString()
                                                + "; ReceitaSegregadaExcedente="
                                                + segregatedExcessRevenue.toPlainString()
                                                + "; FaixaReferencia="
                                                + reference.bracketNumber()
                                                + "; AliquotaEfetivaReferencia="
                                                + referenceEffectiveRate.toPlainString()
                                                + "; PercentualReparticaoISS="
                                                + reference
                                                                .issDistributionRate()
                                                                .toPlainString(),
                                "PercentualISSBruto="
                                                + rawIssEffectiveRate.toPlainString()
                                                + "; PercentualISSAplicado="
                                                + issEffectiveRate.toPlainString()
                                                + "; LimitadoCincoPorCento="
                                                + limitedToFivePercent
                                                + "; ValorISS="
                                                + issAmount.toPlainString(),
                                LEGAL_REFERENCE);
        }

        private record IssReference(
                        int bracketNumber,
                        BigDecimal nominalRate,
                        BigDecimal deduction,
                        BigDecimal issDistributionRate) {
        }
}