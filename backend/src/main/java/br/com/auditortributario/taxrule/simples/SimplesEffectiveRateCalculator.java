package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public final class SimplesEffectiveRateCalculator {

    private static final BigDecimal ONE = BigDecimal.ONE;

    private static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL128;

    private static final String RULE_CODE = "SIMPLES_EFFECTIVE_RATE";

    public SimplesEffectiveRateResult calculate(
            SimplesTaxBracketSelectionResult bracketSelectionResult) {
        Objects.requireNonNull(
                bracketSelectionResult,
                "O resultado da seleção da faixa não pode ser nulo.");

        BigDecimal originalRevenueBasis = bracketSelectionResult
                .revenueBasisResult()
                .revenueBasis();

        BigDecimal calculationRevenueBasis = determineCalculationRevenueBasis(
                originalRevenueBasis);

        SimplesTaxBracket bracket = bracketSelectionResult.bracket();

        BigDecimal effectiveRate = calculateEffectiveRate(
                calculationRevenueBasis,
                bracket);

        TaxDecision decision = createDecision(
                bracketSelectionResult,
                originalRevenueBasis,
                calculationRevenueBasis,
                effectiveRate);

        return new SimplesEffectiveRateResult(
                bracketSelectionResult,
                calculationRevenueBasis,
                effectiveRate,
                decision);
    }

    private BigDecimal determineCalculationRevenueBasis(
            BigDecimal revenueBasis) {
        if (revenueBasis.compareTo(BigDecimal.ZERO) == 0) {
            return ONE;
        }

        return revenueBasis;
    }

    private BigDecimal calculateEffectiveRate(
            BigDecimal revenueBasis,
            SimplesTaxBracket bracket) {
        BigDecimal nominalTax = revenueBasis.multiply(
                bracket.nominalRate(),
                CALCULATION_CONTEXT);

        BigDecimal adjustedTax = nominalTax.subtract(
                bracket.deduction(),
                CALCULATION_CONTEXT);

        BigDecimal effectiveRate = adjustedTax.divide(
                revenueBasis,
                CALCULATION_CONTEXT);

        if (effectiveRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "A alíquota efetiva calculada não pode ser negativa.");
        }

        return effectiveRate;
    }

    private TaxDecision createDecision(
            SimplesTaxBracketSelectionResult bracketSelectionResult,
            BigDecimal originalRevenueBasis,
            BigDecimal calculationRevenueBasis,
            BigDecimal effectiveRate) {
        SimplesTaxBracket bracket = bracketSelectionResult.bracket();

        TaxBracketRevenueBasisResult revenueBasisResult = bracketSelectionResult.revenueBasisResult();

        String description = "Cálculo da alíquota efetiva do "
                + bracketSelectionResult
                        .fatorRResult()
                        .annex()
                        .getDisplayName()
                + ", faixa "
                + bracket.number()
                + ".";

        String input = revenueBasisResult
                .basisType()
                .getCode()
                + " = "
                + originalRevenueBasis.toPlainString()
                + "; base utilizada na fórmula = "
                + calculationRevenueBasis.toPlainString()
                + "; alíquota nominal = "
                + bracket.nominalRate().toPlainString()
                + "; parcela a deduzir = "
                + bracket.deduction().toPlainString()
                + ".";

        String condition = createCondition(
                originalRevenueBasis,
                calculationRevenueBasis);

        String result = "Alíquota efetiva = "
                + effectiveRate.toPlainString()
                + "; percentual aproximado para exibição = "
                + effectiveRate
                        .movePointRight(2)
                        .toPlainString()
                + "%.";

        return new TaxDecision(
                RULE_CODE,
                bracketSelectionResult
                        .taxTable()
                        .version(),
                description,
                input,
                condition,
                result,
                bracketSelectionResult
                        .taxTable()
                        .legalReference());
    }

    private String createCondition(
            BigDecimal originalRevenueBasis,
            BigDecimal calculationRevenueBasis) {
        if (originalRevenueBasis.compareTo(BigDecimal.ZERO) == 0) {
            return "A receita acumulada utilizada na apuração "
                    + "é igual a zero; para determinação da "
                    + "alíquota efetiva, a base foi considerada "
                    + "igual a "
                    + calculationRevenueBasis.toPlainString()
                    + ".";
        }

        return "A base de receita é maior que zero; "
                + "foi utilizada diretamente na fórmula "
                + "da alíquota efetiva.";
    }
}