package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public final class SimplesSublimitEvaluator {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private static final BigDecimal TWENTY_PERCENT = new BigDecimal("0.20");

    private static final String RULE_CODE = "SIMPLES_SUBLIMIT_EVALUATION";

    private static final String RULE_VERSION = "LC123-CGSN140-2018-2026";

    private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, arts. 19 e 20; "
            + "Resolução CGSN nº 140/2018, arts. 9º e 12.";

    public SimplesSublimitEvaluationResult evaluate(
            BigDecimal accumulatedRevenue,
            BigDecimal sublimit) {
        Objects.requireNonNull(
                accumulatedRevenue,
                "A receita acumulada não pode ser nula.");

        Objects.requireNonNull(
                sublimit,
                "O sublimite não pode ser nulo.");

        if (accumulatedRevenue.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A receita acumulada não pode ser negativa.");
        }

        if (sublimit.compareTo(
                BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O sublimite deve ser maior que zero.");
        }

        BigDecimal twentyPercentAmount = sublimit.multiply(
                TWENTY_PERCENT,
                MATH_CONTEXT);

        BigDecimal twentyPercentThreshold = sublimit.add(
                twentyPercentAmount,
                MATH_CONTEXT);

        BigDecimal excessAmount = accumulatedRevenue.subtract(
                sublimit,
                MATH_CONTEXT);

        if (excessAmount.compareTo(
                BigDecimal.ZERO) < 0) {
            excessAmount = BigDecimal.ZERO;
        }

        SimplesSublimitStatus status = determineStatus(
                accumulatedRevenue,
                sublimit,
                twentyPercentThreshold);

        TaxDecision decision = createDecision(
                accumulatedRevenue,
                sublimit,
                twentyPercentThreshold,
                excessAmount,
                status);

        return new SimplesSublimitEvaluationResult(
                accumulatedRevenue,
                sublimit,
                twentyPercentThreshold,
                excessAmount,
                status,
                decision);
    }

    private SimplesSublimitStatus determineStatus(
            BigDecimal accumulatedRevenue,
            BigDecimal sublimit,
            BigDecimal twentyPercentThreshold) {
        if (accumulatedRevenue.compareTo(
                sublimit) <= 0) {
            return SimplesSublimitStatus.WITHIN_SUBLIMIT;
        }

        if (accumulatedRevenue.compareTo(
                twentyPercentThreshold) <= 0) {
            return SimplesSublimitStatus.EXCEEDED_UP_TO_TWENTY_PERCENT;
        }

        return SimplesSublimitStatus.EXCEEDED_OVER_TWENTY_PERCENT;
    }

    private TaxDecision createDecision(
            BigDecimal accumulatedRevenue,
            BigDecimal sublimit,
            BigDecimal twentyPercentThreshold,
            BigDecimal excessAmount,
            SimplesSublimitStatus status) {
        return new TaxDecision(
                RULE_CODE,
                RULE_VERSION,
                "Avaliação do sublimite de receita bruta "
                        + "para ICMS e ISS no Simples Nacional.",
                "ReceitaAcumulada="
                        + accumulatedRevenue.toPlainString()
                        + "; Sublimite="
                        + sublimit.toPlainString(),
                "LimiteComVintePorCento="
                        + twentyPercentThreshold.toPlainString(),
                "Status="
                        + status
                        + "; Excesso="
                        + excessAmount.toPlainString(),
                LEGAL_REFERENCE);
    }
}