package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FatorRCalculatorTest {

        private final FatorRCalculator calculator = new FatorRCalculator();

        @Test
        void shouldCalculateFatorRUsingFs12AndRbt12() {
                FatorRCalculationResult result = calculator.calculate(
                                new BigDecimal("72000.00"),
                                new BigDecimal("286000.00"));

                assertEquals(
                                new BigDecimal("0.2517482517"),
                                result.rawFactor());

                assertEquals(
                                new BigDecimal("0.25"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_V,
                                result.annex());

                assertEquals(
                                FatorRCalculationBasis.STANDARD_12_MONTHS,
                                result.calculationBasis());
        }

        @Test
        void shouldUseAnnexIIIWhenFatorRIsExactlyTwentyEightPercent() {
                FatorRCalculationResult result = calculator.calculate(
                                new BigDecimal("84000.00"),
                                new BigDecimal("300000.00"));

                assertEquals(
                                new BigDecimal("0.28"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_III,
                                result.annex());
        }

        @Test
        void shouldUseAnnexIIIWhenFatorRIsAboveTwentyEightPercent() {
                FatorRCalculationResult result = calculator.calculate(
                                new BigDecimal("90000.00"),
                                new BigDecimal("300000.00"));

                assertEquals(
                                new BigDecimal("0.30"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_III,
                                result.annex());
        }

        @Test
        void shouldReturnOnePercentWhenFs12AndRbt12AreZero() {
                FatorRCalculationResult result = calculator.calculate(
                                BigDecimal.ZERO,
                                BigDecimal.ZERO);

                assertEquals(
                                new BigDecimal("0.01"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_V,
                                result.annex());
        }

        @Test
        void shouldReturnOnePercentWhenFs12IsZeroAndRbt12IsPositive() {
                FatorRCalculationResult result = calculator.calculate(
                                BigDecimal.ZERO,
                                new BigDecimal("100000.00"));

                assertEquals(
                                new BigDecimal("0.01"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_V,
                                result.annex());
        }

        @Test
        void shouldReturnTwentyEightPercentWhenFs12IsPositiveAndRbt12IsZero() {
                FatorRCalculationResult result = calculator.calculate(
                                new BigDecimal("10000.00"),
                                BigDecimal.ZERO);

                assertEquals(
                                new BigDecimal("0.28"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_III,
                                result.annex());
        }

        @Test
        void shouldTruncateCalculatedFatorRWithoutRounding() {
                FatorRCalculationResult result = calculator.calculate(
                                new BigDecimal("2774.00"),
                                new BigDecimal("10000.00"));

                assertEquals(
                                new BigDecimal("0.2774000000"),
                                result.rawFactor());

                assertEquals(
                                new BigDecimal("0.27"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_V,
                                result.annex());
        }

        @Test
        void shouldCreateTaxDecisionExplainingTheApplicableAnnex() {
                FatorRCalculationResult result = calculator.calculate(
                                new BigDecimal("72000.00"),
                                new BigDecimal("286000.00"));

                assertEquals(
                                "SIMPLES_FATOR_R",
                                result.decision().ruleCode());

                assertEquals(
                                "2026.1",
                                result.decision().ruleVersion());

                assertTrue(
                                result.decision()
                                                .condition()
                                                .contains("menor que 0,28"));

                assertTrue(
                                result.decision()
                                                .result()
                                                .contains("Anexo V"));

                assertTrue(
                                result.decision()
                                                .legalReference()
                                                .contains("art. 26"));
        }

        @Test
        void shouldCalculateFatorRUsingFspaAndRpaInOpeningMonth() {
                FatorRCalculationResult result = calculator.calculateOpeningMonth(
                                new BigDecimal("5000.00"),
                                new BigDecimal("20000.00"));

                assertEquals(
                                new BigDecimal("0.2500000000"),
                                result.rawFactor());

                assertEquals(
                                new BigDecimal("0.25"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_V,
                                result.annex());

                assertEquals(
                                FatorRCalculationBasis.OPENING_MONTH,
                                result.calculationBasis());
        }

        @Test
        void shouldReturnTwentyEightPercentWhenFspaIsPositiveAndRpaIsZero() {
                FatorRCalculationResult result = calculator.calculateOpeningMonth(
                                new BigDecimal("5000.00"),
                                BigDecimal.ZERO);

                assertEquals(
                                new BigDecimal("0.28"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_III,
                                result.annex());
        }

        @Test
        void shouldReturnOnePercentWhenFspaIsZeroAndRpaIsPositive() {
                FatorRCalculationResult result = calculator.calculateOpeningMonth(
                                BigDecimal.ZERO,
                                new BigDecimal("20000.00"));

                assertEquals(
                                new BigDecimal("0.01"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_V,
                                result.annex());
        }

        @Test
        void shouldRejectOpeningMonthWhenFspaAndRpaAreBothZero() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> calculator.calculateOpeningMonth(
                                                BigDecimal.ZERO,
                                                BigDecimal.ZERO));
        }

        @Test
        void shouldExplainOpeningMonthCalculation() {
                FatorRCalculationResult result = calculator.calculateOpeningMonth(
                                new BigDecimal("5000.00"),
                                new BigDecimal("20000.00"));

                assertTrue(
                                result.decision()
                                                .description()
                                                .contains("Mês de abertura"));

                assertTrue(
                                result.decision()
                                                .input()
                                                .contains("FSPA"));

                assertTrue(
                                result.decision()
                                                .input()
                                                .contains("RPA"));
        }

        @Test
        void shouldCalculateFatorRForCompanyUnderThirteenMonths() {
                FatorRCalculationResult result = calculator.calculateUnderThirteenMonths(
                                new BigDecimal("15000.00"),
                                new BigDecimal("50000.00"));

                assertEquals(
                                new BigDecimal("0.3000000000"),
                                result.rawFactor());

                assertEquals(
                                new BigDecimal("0.30"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_III,
                                result.annex());

                assertEquals(
                                FatorRCalculationBasis.UNDER_13_MONTHS,
                                result.calculationBasis());
        }

        @Test
        void shouldUseAccumulatedValuesForCompanyUnderThirteenMonths() {
                FatorRCalculationResult result = calculator.calculateUnderThirteenMonths(
                                new BigDecimal("10000.00"),
                                new BigDecimal("40000.00"));

                assertEquals(
                                new BigDecimal("0.25"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_V,
                                result.annex());

                assertTrue(
                                result.decision()
                                                .description()
                                                .contains("menos de 13 meses"));
        }

        @Test
        void shouldReturnOnePercentWhenAccumulatedPayrollAndRevenueAreZero() {
                FatorRCalculationResult result = calculator.calculateUnderThirteenMonths(
                                BigDecimal.ZERO,
                                BigDecimal.ZERO);

                assertEquals(
                                new BigDecimal("0.01"),
                                result.fatorR().value());

                assertEquals(
                                SimplesAnnex.ANEXO_V,
                                result.annex());
        }

        @Test
        void shouldRejectNegativeFs12() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> calculator.calculate(
                                                new BigDecimal("-1.00"),
                                                new BigDecimal("100000.00")));
        }

        @Test
        void shouldRejectNegativeRbt12() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> calculator.calculate(
                                                new BigDecimal("10000.00"),
                                                new BigDecimal("-1.00")));
        }

        @Test
        void shouldRejectNullFs12() {
                assertThrows(
                                NullPointerException.class,
                                () -> calculator.calculate(
                                                null,
                                                new BigDecimal("100000.00")));
        }

        @Test
        void shouldRejectNullRbt12() {
                assertThrows(
                                NullPointerException.class,
                                () -> calculator.calculate(
                                                new BigDecimal("10000.00"),
                                                null));
        }
}