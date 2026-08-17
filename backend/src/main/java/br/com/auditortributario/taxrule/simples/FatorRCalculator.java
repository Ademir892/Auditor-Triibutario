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
                return calculate(
                                fs12,
                                rbt12,
                                FatorRCalculationBasis.STANDARD_12_MONTHS);
        }

        public FatorRCalculationResult calculateOpeningMonth(
                        BigDecimal fspa,
                        BigDecimal rpa) {
                validateAmount(fspa, "FSPA");
                validateAmount(rpa, "RPA");

                if (isZero(fspa) && isZero(rpa)) {
                        throw new IllegalArgumentException(
                                        "No mês de abertura, FSPA e RPA não podem ser "
                                                        + "simultaneamente iguais a zero para apuração "
                                                        + "do Fator R.");
                }

                return calculate(
                                fspa,
                                rpa,
                                FatorRCalculationBasis.OPENING_MONTH);
        }

        public FatorRCalculationResult calculateUnderThirteenMonths(
                        BigDecimal accumulatedPayroll,
                        BigDecimal accumulatedRevenue) {
                return calculate(
                                accumulatedPayroll,
                                accumulatedRevenue,
                                FatorRCalculationBasis.UNDER_13_MONTHS);
        }

        private FatorRCalculationResult calculate(
                        BigDecimal payrollBase,
                        BigDecimal revenueBase,
                        FatorRCalculationBasis calculationBasis) {
                validateAmount(
                                payrollBase,
                                calculationBasis.getPayrollCode());

                validateAmount(
                                revenueBase,
                                calculationBasis.getRevenueCode());

                CalculationOutcome outcome = calculateRawFactor(
                                payrollBase,
                                revenueBase,
                                calculationBasis);

                FatorR fatorR = new FatorR(
                                outcome.rawFactor());

                SimplesAnnex annex = fatorR.getApplicableAnnex();

                TaxDecision decision = createDecision(
                                payrollBase,
                                revenueBase,
                                outcome,
                                fatorR,
                                annex,
                                calculationBasis);

                return new FatorRCalculationResult(
                                payrollBase,
                                revenueBase,
                                outcome.rawFactor(),
                                fatorR,
                                annex,
                                calculationBasis,
                                decision);
        }

        private CalculationOutcome calculateRawFactor(
                        BigDecimal payrollBase,
                        BigDecimal revenueBase,
                        FatorRCalculationBasis calculationBasis) {
                boolean payrollIsZero = isZero(payrollBase);

                boolean revenueIsZero = isZero(revenueBase);

                if (calculationBasis == FatorRCalculationBasis.OPENING_MONTH) {
                        return calculateOpeningMonthRawFactor(
                                        payrollBase,
                                        revenueBase,
                                        payrollIsZero,
                                        revenueIsZero);
                }

                if (payrollIsZero && revenueIsZero) {
                        return new CalculationOutcome(
                                        ONE_PERCENT,
                                        "A folha e a receita utilizadas na apuração "
                                                        + "são iguais a zero. "
                                                        + "A regra determina Fator R igual a 0,01.");
                }

                if (payrollIsZero) {
                        return new CalculationOutcome(
                                        ONE_PERCENT,
                                        "A folha utilizada na apuração é igual a zero "
                                                        + "e a receita é maior que zero. "
                                                        + "A regra determina Fator R igual a 0,01.");
                }

                if (revenueIsZero) {
                        return new CalculationOutcome(
                                        TWENTY_EIGHT_PERCENT,
                                        "A folha utilizada na apuração é maior que zero "
                                                        + "e a receita é igual a zero. "
                                                        + "A regra determina Fator R igual a 0,28.");
                }

                return divide(
                                payrollBase,
                                revenueBase,
                                "O Fator R foi calculado pela divisão entre "
                                                + calculationBasis.getPayrollCode()
                                                + " e "
                                                + calculationBasis.getRevenueCode()
                                                + ".");
        }

        private CalculationOutcome calculateOpeningMonthRawFactor(
                        BigDecimal fspa,
                        BigDecimal rpa,
                        boolean fspaIsZero,
                        boolean rpaIsZero) {
                if (fspaIsZero && rpaIsZero) {
                        throw new IllegalArgumentException(
                                        "Não há base para apuração do Fator R "
                                                        + "no mês de abertura.");
                }

                if (fspaIsZero) {
                        return new CalculationOutcome(
                                        ONE_PERCENT,
                                        "No mês de abertura, a FSPA é igual a zero "
                                                        + "e a RPA é maior que zero. "
                                                        + "A regra determina Fator R igual a 0,01.");
                }

                if (rpaIsZero) {
                        return new CalculationOutcome(
                                        TWENTY_EIGHT_PERCENT,
                                        "No mês de abertura, a FSPA é maior que zero "
                                                        + "e a RPA é igual a zero. "
                                                        + "A regra determina Fator R igual a 0,28.");
                }

                return divide(
                                fspa,
                                rpa,
                                "No mês de abertura, o Fator R foi calculado "
                                                + "pela divisão entre FSPA e RPA.");
        }

        private CalculationOutcome divide(
                        BigDecimal payrollBase,
                        BigDecimal revenueBase,
                        String explanation) {
                BigDecimal rawFactor = payrollBase.divide(
                                revenueBase,
                                CALCULATION_SCALE,
                                RoundingMode.DOWN);

                return new CalculationOutcome(
                                rawFactor,
                                explanation);
        }

        private TaxDecision createDecision(
                        BigDecimal payrollBase,
                        BigDecimal revenueBase,
                        CalculationOutcome outcome,
                        FatorR fatorR,
                        SimplesAnnex annex,
                        FatorRCalculationBasis calculationBasis) {
                String input = "Base de cálculo = "
                                + calculationBasis.getDisplayName()
                                + "; "
                                + calculationBasis.getPayrollCode()
                                + " = "
                                + payrollBase.toPlainString()
                                + "; "
                                + calculationBasis.getRevenueCode()
                                + " = "
                                + revenueBase.toPlainString()
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

                String description = calculationBasis.getDisplayName()
                                + ". "
                                + outcome.explanation();

                return new TaxDecision(
                                RULE_CODE,
                                RULE_VERSION,
                                description,
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

        private boolean isZero(BigDecimal value) {
                return value.compareTo(BigDecimal.ZERO) == 0;
        }

        private record CalculationOutcome(
                        BigDecimal rawFactor,
                        String explanation) {
        }
}