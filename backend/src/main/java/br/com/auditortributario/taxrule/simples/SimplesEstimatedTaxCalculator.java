package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Objects;

public final class SimplesEstimatedTaxCalculator {

    private static final BigDecimal MINIMUM_DAS_AMOUNT = new BigDecimal("10.00");

    private static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL128;

    private static final String RULE_CODE = "SIMPLES_ESTIMATED_TAX_AMOUNT";

    private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, art. 18; "
            + "Resolução CGSN nº 140/2018, art. 44; "
            + "Manual do PGDAS-D e DEFIS.";

    public SimplesEstimatedTaxResult calculate(
            SimplesEstimatedTaxRequest request) {
        Objects.requireNonNull(
                request,
                "A requisição de cálculo não pode ser nula.");

        validateAssessmentPeriod(
                request);

        BigDecimal effectiveRate = request.effectiveRateResult()
                .effectiveRate();

        BigDecimal taxableRevenue = request.taxableRevenue()
                .amount();

        BigDecimal rawTaxAmount = taxableRevenue.multiply(
                effectiveRate,
                CALCULATION_CONTEXT);

        BigDecimal estimatedTaxAmount = rawTaxAmount.setScale(
                2,
                RoundingMode.HALF_UP);

        SimplesEstimatedTaxStatus status = determineStatus(
                estimatedTaxAmount);

        TaxDecision decision = createDecision(
                request,
                rawTaxAmount,
                estimatedTaxAmount,
                status);

        return new SimplesEstimatedTaxResult(
                request.taxableRevenue(),
                request.effectiveRateResult(),
                rawTaxAmount,
                estimatedTaxAmount,
                status,
                decision);
    }

    private void validateAssessmentPeriod(
            SimplesEstimatedTaxRequest request) {
        YearMonth revenuePeriod = request.taxableRevenue()
                .period();

        YearMonth calculationPeriod = request.effectiveRateResult()
                .bracketSelectionResult()
                .assessmentPeriod();

        if (!revenuePeriod.equals(calculationPeriod)) {
            throw new IllegalArgumentException(
                    "A competência da receita tributável "
                            + revenuePeriod
                            + " não corresponde à competência "
                            + "do cálculo tributário "
                            + calculationPeriod
                            + ".");
        }
    }

    private SimplesEstimatedTaxStatus determineStatus(
            BigDecimal estimatedTaxAmount) {
        if (estimatedTaxAmount.compareTo(BigDecimal.ZERO) == 0) {
            return SimplesEstimatedTaxStatus.NO_TAX_DUE;
        }

        if (estimatedTaxAmount.compareTo(
                MINIMUM_DAS_AMOUNT) < 0) {
            return SimplesEstimatedTaxStatus.DEFERRED_BELOW_MINIMUM;
        }

        return SimplesEstimatedTaxStatus.PAYABLE;
    }

    private TaxDecision createDecision(
            SimplesEstimatedTaxRequest request,
            BigDecimal rawTaxAmount,
            BigDecimal estimatedTaxAmount,
            SimplesEstimatedTaxStatus status) {
        SimplesEffectiveRateResult effectiveRateResult = request.effectiveRateResult();

        BigDecimal effectiveRate = effectiveRateResult.effectiveRate();

        String description = "Estimativa do valor devido no Simples Nacional "
                + "para a receita tributável da competência "
                + request.taxableRevenue().period()
                + ".";

        String input = "Receita tributável do período = "
                + request.taxableRevenue()
                        .amount()
                        .toPlainString()
                + "; alíquota efetiva = "
                + effectiveRate.toPlainString()
                + "; anexo = "
                + effectiveRateResult
                        .bracketSelectionResult()
                        .fatorRResult()
                        .annex()
                        .getDisplayName()
                + "; faixa = "
                + effectiveRateResult
                        .bracketSelectionResult()
                        .bracket()
                        .number()
                + ".";

        String condition = createCondition(
                estimatedTaxAmount,
                status);

        String result = "Valor matemático calculado = "
                + rawTaxAmount.toPlainString()
                + "; valor estimado = "
                + estimatedTaxAmount.toPlainString()
                + "; situação = "
                + status.getDisplayName()
                + ".";

        return new TaxDecision(
                RULE_CODE,
                effectiveRateResult
                        .bracketSelectionResult()
                        .taxTable()
                        .version(),
                description,
                input,
                condition,
                result,
                LEGAL_REFERENCE);
    }

    private String createCondition(
            BigDecimal estimatedTaxAmount,
            SimplesEstimatedTaxStatus status) {
        return switch (status) {
            case NO_TAX_DUE ->
                "O valor estimado é igual a zero.";

            case DEFERRED_BELOW_MINIMUM ->
                "O valor estimado de "
                        + estimatedTaxAmount.toPlainString()
                        + " é inferior ao mínimo de "
                        + MINIMUM_DAS_AMOUNT.toPlainString()
                        + " para emissão de DAS.";

            case PAYABLE ->
                "O valor estimado de "
                        + estimatedTaxAmount.toPlainString()
                        + " é igual ou superior ao mínimo de "
                        + MINIMUM_DAS_AMOUNT.toPlainString()
                        + ".";
        };
    }
}