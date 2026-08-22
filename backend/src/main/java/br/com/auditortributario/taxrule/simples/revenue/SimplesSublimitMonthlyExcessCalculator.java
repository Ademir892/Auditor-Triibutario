package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public final class SimplesSublimitMonthlyExcessCalculator {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private static final String RULE_CODE = "SIMPLES_SUBLIMIT_MONTHLY_EXCESS";

    private static final String RULE_VERSION = "LC123-CGSN140-2018-2026";

    private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, art. 18, "
            + "§§ 16, 16-A, 17 e 17-A; "
            + "Resolução CGSN nº 140/2018, art. 24, § 2º.";

    public SimplesSublimitMonthlyExcessResult calculate(
            SimplesSublimitEvaluationResult evaluationResult,
            BigDecimal monthlyRevenue) {
        Objects.requireNonNull(
                evaluationResult,
                "O resultado da avaliação do sublimite não pode ser nulo.");

        Objects.requireNonNull(
                monthlyRevenue,
                "A receita mensal não pode ser nula.");

        if (monthlyRevenue.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A receita mensal não pode ser negativa.");
        }

        if (evaluationResult
                .accumulatedRevenue()
                .compareTo(
                        monthlyRevenue) < 0) {

            throw new IllegalArgumentException(
                    "A receita acumulada deve incluir a receita "
                            + "da competência avaliada e, portanto, "
                            + "não pode ser inferior à receita mensal.");
        }

        BigDecimal excessMonthlyRevenue = calculateExcessMonthlyRevenue(
                evaluationResult,
                monthlyRevenue);

        BigDecimal revenueWithinSublimit = monthlyRevenue.subtract(
                excessMonthlyRevenue,
                MATH_CONTEXT);

        BigDecimal excessRatio = calculateExcessRatio(
                monthlyRevenue,
                excessMonthlyRevenue);

        TaxDecision decision = createDecision(
                evaluationResult,
                monthlyRevenue,
                revenueWithinSublimit,
                excessMonthlyRevenue,
                excessRatio);

        return new SimplesSublimitMonthlyExcessResult(
                evaluationResult,
                monthlyRevenue,
                revenueWithinSublimit,
                excessMonthlyRevenue,
                excessRatio,
                decision);
    }

    private BigDecimal calculateExcessMonthlyRevenue(
            SimplesSublimitEvaluationResult evaluationResult,
            BigDecimal monthlyRevenue) {
        if (!evaluationResult.isExceeded()) {
            return BigDecimal.ZERO;
        }

        BigDecimal cumulativeExcess = evaluationResult.excessAmount();

        if (cumulativeExcess.compareTo(
                monthlyRevenue) >= 0) {
            return monthlyRevenue;
        }

        return cumulativeExcess;
    }

    private BigDecimal calculateExcessRatio(
            BigDecimal monthlyRevenue,
            BigDecimal excessMonthlyRevenue) {
        if (monthlyRevenue.compareTo(
                BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return excessMonthlyRevenue.divide(
                monthlyRevenue,
                MATH_CONTEXT);
    }

    private TaxDecision createDecision(
            SimplesSublimitEvaluationResult evaluationResult,
            BigDecimal monthlyRevenue,
            BigDecimal revenueWithinSublimit,
            BigDecimal excessMonthlyRevenue,
            BigDecimal excessRatio) {
        return new TaxDecision(
                RULE_CODE,
                RULE_VERSION,
                "Determinação da parcela da receita bruta mensal "
                        + "que excedeu o sublimite do Simples Nacional.",
                "ReceitaAcumulada="
                        + evaluationResult
                                .accumulatedRevenue()
                                .toPlainString()
                        + "; Sublimite="
                        + evaluationResult
                                .sublimit()
                                .toPlainString()
                        + "; ReceitaMensal="
                        + monthlyRevenue.toPlainString(),
                "ReceitaDentroSublimite="
                        + revenueWithinSublimit.toPlainString()
                        + "; ReceitaMensalExcedente="
                        + excessMonthlyRevenue.toPlainString(),
                "RazaoExcedente="
                        + excessRatio.toPlainString()
                        + "; StatusSublimite="
                        + evaluationResult.status(),
                LEGAL_REFERENCE);
    }
}