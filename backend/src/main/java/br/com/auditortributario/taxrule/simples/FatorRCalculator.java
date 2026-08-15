package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class FatorRCalculator {

    private static final BigDecimal ONE_PERCENT = new BigDecimal("0.01");

    private static final BigDecimal TWENTY_EIGHT_PERCENT = new BigDecimal("0.28");

    private static final int CALCULATION_SCALE = 10;

    private static final String RULE_CODE = "SIMPLES_FATOR_R";

    private static final String RULE_VERSION = "2026.1";

    private static final String LEGAL_REFERENCE = "Resolução CGSN nº 140/2018, art. 26; "
            + "Manual do PGDAS-D e DEFIS, item 8.2.1.";

    public FatorRCalculationResult calculate(
            BigDecimal fs12,
            BigDecimal rbt12) {
        validateAmount(fs12, "FS12");
        validateAmount(rbt12, "RBT12");

        CalculationOutcome outcome = calculateRawFactor(
                fs12,
                rbt12);

        FatorR fatorR = new FatorR(outcome.rawFactor());

        SimplesAnnex annex = fatorR.getApplicableAnnex();

        TaxDecision decision = createDecision(
                fs12,
                rbt12,
                outcome,
                fatorR,
                annex);

        return new FatorRCalculationResult(
                fs12,
                rbt12,
                outcome.rawFactor(),
                fatorR,
                annex,
                decision);
    }

    private CalculationOutcome calculateRawFactor(
            BigDecimal fs12,
            BigDecimal rbt12) {
        boolean fs12IsZero = fs12.compareTo(BigDecimal.ZERO) == 0;

        boolean rbt12IsZero = rbt12.compareTo(BigDecimal.ZERO) == 0;

        if (fs12IsZero && rbt12IsZero) {
            return new CalculationOutcome(
                    ONE_PERCENT,
                    "FS12 e RBT12 são iguais a zero. "
                            + "A regra determina Fator R igual a 0,01.");
        }

        if (fs12IsZero) {
            return new CalculationOutcome(
                    ONE_PERCENT,
                    "FS12 é igual a zero e RBT12 é maior que zero. "
                            + "A regra determina Fator R igual a 0,01.");
        }

        if (rbt12IsZero) {
            return new CalculationOutcome(
                    TWENTY_EIGHT_PERCENT,
                    "FS12 é maior que zero e RBT12 é igual a zero. "
                            + "A regra determina Fator R igual a 0,28.");
        }

        BigDecimal rawFactor = fs12.divide(
                rbt12,
                CALCULATION_SCALE,
                RoundingMode.DOWN);

        return new CalculationOutcome(
                rawFactor,
                "O Fator R foi calculado pela divisão entre FS12 e RBT12.");
    }

    private TaxDecision createDecision(
            BigDecimal fs12,
            BigDecimal rbt12,
            CalculationOutcome outcome,
            FatorR fatorR,
            SimplesAnnex annex) {
        String input = "FS12 = "
                + fs12.toPlainString()
                + "; RBT12 = "
                + rbt12.toPlainString()
                + "; resultado calculado = "
                + outcome.rawFactor().toPlainString()
                + "; Fator R considerado = "
                + fatorR.value().toPlainString();

        String condition;

        if (fatorR.value().compareTo(TWENTY_EIGHT_PERCENT) >= 0) {
            condition = "Fator R considerado maior ou igual a 0,28.";
        } else {
            condition = "Fator R considerado menor que 0,28.";
        }

        String result = "Fator R = "
                + fatorR.value().toPlainString()
                + "; enquadramento = "
                + annex.getDisplayName()
                + ".";

        return new TaxDecision(
                RULE_CODE,
                RULE_VERSION,
                outcome.explanation(),
                input,
                condition,
                result,
                LEGAL_REFERENCE);
    }

    private void validateAmount(
            BigDecimal amount,
            String fieldName) {
        Objects.requireNonNull(
                amount,
                fieldName + " não pode ser nulo.");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    fieldName + " não pode ser negativo.");
        }
    }

    private record CalculationOutcome(
            BigDecimal rawFactor,
            String explanation) {
    }
}