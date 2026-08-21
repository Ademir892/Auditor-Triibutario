package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record SimplesSublimitEvaluationResult(
        BigDecimal accumulatedRevenue,
        BigDecimal sublimit,
        BigDecimal twentyPercentThreshold,
        BigDecimal excessAmount,
        SimplesSublimitStatus status,
        TaxDecision decision) {

    public SimplesSublimitEvaluationResult {
        Objects.requireNonNull(
                accumulatedRevenue,
                "A receita acumulada não pode ser nula.");

        Objects.requireNonNull(
                sublimit,
                "O sublimite não pode ser nulo.");

        Objects.requireNonNull(
                twentyPercentThreshold,
                "O limite acrescido de 20% não pode ser nulo.");

        Objects.requireNonNull(
                excessAmount,
                "O valor excedente não pode ser nulo.");

        Objects.requireNonNull(
                status,
                "O status do sublimite não pode ser nulo.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");
    }

    public boolean isExceeded() {
        return status.isExceeded();
    }

    public boolean isExceededOverTwentyPercent() {
        return status.isExceededOverTwentyPercent();
    }

    public BigDecimal excessAmountForDisplay() {
        return excessAmount.setScale(
                2,
                RoundingMode.HALF_UP);
    }
}