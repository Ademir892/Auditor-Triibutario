package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesEffectiveRateCalculatorTest {

        private final FatorRCalculator fatorRCalculator = new FatorRCalculator();

        private final TaxBracketRevenueBasisCalculator revenueCalculator = new TaxBracketRevenueBasisCalculator();

        private final SimplesTaxBracketSelector bracketSelector = new SimplesTaxBracketSelector();

        private final SimplesEffectiveRateCalculator effectiveRateCalculator = new SimplesEffectiveRateCalculator();

        @Test
        void shouldCalculateAnnexIIIEffectiveRateForSecondBracket() {
                SimplesTaxBracketSelectionResult selectionResult = createFirstMonthSelection(
                                "6000.00",
                                "20000.00");

                SimplesEffectiveRateResult result = effectiveRateCalculator.calculate(
                                selectionResult);

                assertEquals(
                                SimplesAnnex.ANEXO_III,
                                result.bracketSelectionResult()
                                                .fatorRResult()
                                                .annex());

                assertEquals(
                                2,
                                result.bracketSelectionResult()
                                                .bracket()
                                                .number());

                assertEquals(
                                new BigDecimal("240000.00"),
                                result.calculationRevenueBasis());

                assertEquals(
                                0,
                                new BigDecimal("0.073")
                                                .compareTo(result.effectiveRate()));

                assertEquals(
                                new BigDecimal("7.30000"),
                                result.effectiveRateAsPercentage());
        }

        @Test
        void shouldCalculateAnnexVEffectiveRateForSecondBracket() {
                SimplesTaxBracketSelectionResult selectionResult = createFirstMonthSelection(
                                "5000.00",
                                "20000.00");

                SimplesEffectiveRateResult result = effectiveRateCalculator.calculate(
                                selectionResult);

                assertEquals(
                                SimplesAnnex.ANEXO_V,
                                result.bracketSelectionResult()
                                                .fatorRResult()
                                                .annex());

                assertEquals(
                                new BigDecimal("0.16125"),
                                result.effectiveRate());

                assertEquals(
                                new BigDecimal("16.12500"),
                                result.effectiveRateAsPercentage());
        }

        @Test
        void shouldCalculateEffectiveRateUsingOfficialAnnexIIIExample() {
                SimplesTaxBracketSelectionResult selectionResult = createStandardSelection(
                                "150000.00",
                                "500000.00");

                SimplesEffectiveRateResult result = effectiveRateCalculator.calculate(
                                selectionResult);

                assertEquals(
                                SimplesAnnex.ANEXO_III,
                                result.bracketSelectionResult()
                                                .fatorRResult()
                                                .annex());

                assertEquals(
                                3,
                                result.bracketSelectionResult()
                                                .bracket()
                                                .number());

                assertEquals(
                                new BigDecimal("0.09972"),
                                result.effectiveRate());

                assertEquals(
                                new BigDecimal("9.97200"),
                                result.effectiveRateAsPercentage());
        }

        @Test
        void shouldCalculateEffectiveRateUsingOfficialAnnexVExample() {
                SimplesTaxBracketSelectionResult selectionResult = createStandardSelection(
                                "125000.00",
                                "500000.00");

                SimplesEffectiveRateResult result = effectiveRateCalculator.calculate(
                                selectionResult);

                assertEquals(
                                SimplesAnnex.ANEXO_V,
                                result.bracketSelectionResult()
                                                .fatorRResult()
                                                .annex());

                assertEquals(
                                3,
                                result.bracketSelectionResult()
                                                .bracket()
                                                .number());

                assertEquals(
                                new BigDecimal("0.1752"),
                                result.effectiveRate());

                assertEquals(
                                new BigDecimal("17.52000"),
                                result.effectiveRateAsPercentage());
        }

        @Test
        void shouldPreserveMorePrecisionForRepeatingEffectiveRate() {
                SimplesTaxBracketSelectionResult selectionResult = createFirstMonthSelection(
                                "5000.00",
                                "23833.34");

                SimplesEffectiveRateResult result = effectiveRateCalculator.calculate(
                                selectionResult);

                assertTrue(
                                result.effectiveRate()
                                                .scale() > 5);
        }

        @Test
        void shouldUseOneWhenRevenueBasisIsZero() {
                FatorRCalculationResult fatorRResult = fatorRCalculator.calculateOpeningMonth(
                                new BigDecimal("5000.00"),
                                BigDecimal.ZERO);

                TaxBracketRevenueBasisResult revenueResult = revenueCalculator.calculate(
                                new TaxBracketRevenueBasisRequest(
                                                LocalDate.of(2026, 2, 10),
                                                YearMonth.of(2026, 2),
                                                List.of(
                                                                revenue(
                                                                                2026,
                                                                                2,
                                                                                "0.00"))));

                SimplesTaxBracketSelectionResult selectionResult = bracketSelector.select(
                                new SimplesTaxBracketSelectionRequest(
                                                YearMonth.of(2026, 2),
                                                fatorRResult,
                                                revenueResult));

                SimplesEffectiveRateResult result = effectiveRateCalculator.calculate(
                                selectionResult);

                assertEquals(
                                new BigDecimal("1"),
                                result.calculationRevenueBasis());

                assertEquals(
                                new BigDecimal("0.06"),
                                result.effectiveRate());

                assertTrue(
                                result.decision()
                                                .condition()
                                                .contains("igual a zero"));
        }

        @Test
        void shouldCreateAuditableEffectiveRateDecision() {
                SimplesTaxBracketSelectionResult selectionResult = createStandardSelection(
                                "150000.00",
                                "500000.00");

                SimplesEffectiveRateResult result = effectiveRateCalculator.calculate(
                                selectionResult);

                assertEquals(
                                "SIMPLES_EFFECTIVE_RATE",
                                result.decision()
                                                .ruleCode());

                assertTrue(
                                result.decision()
                                                .description()
                                                .contains("Anexo III"));

                assertTrue(
                                result.decision()
                                                .input()
                                                .contains("0.135"));

                assertTrue(
                                result.decision()
                                                .input()
                                                .contains("17640.00"));

                assertTrue(
                                result.decision()
                                                .result()
                                                .contains("0.09972"));
        }

        @Test
        void shouldRejectNullBracketSelectionResult() {
                assertThrows(
                                NullPointerException.class,
                                () -> effectiveRateCalculator.calculate(null));
        }

        private SimplesTaxBracketSelectionResult createFirstMonthSelection(
                        String payroll,
                        String revenue) {
                FatorRCalculationResult fatorRResult = fatorRCalculator.calculateOpeningMonth(
                                new BigDecimal(payroll),
                                new BigDecimal(revenue));

                TaxBracketRevenueBasisResult revenueResult = revenueCalculator.calculate(
                                new TaxBracketRevenueBasisRequest(
                                                LocalDate.of(2026, 2, 10),
                                                YearMonth.of(2026, 2),
                                                List.of(
                                                                revenue(
                                                                                2026,
                                                                                2,
                                                                                revenue))));

                return bracketSelector.select(
                                new SimplesTaxBracketSelectionRequest(
                                                YearMonth.of(2026, 2),
                                                fatorRResult,
                                                revenueResult));
        }

        private SimplesTaxBracketSelectionResult createStandardSelection(
                        String payroll,
                        String revenueBasis) {
                FatorRCalculationResult fatorRResult = fatorRCalculator.calculate(
                                new BigDecimal(payroll),
                                new BigDecimal(revenueBasis));

                List<MonthlyRevenue> revenues = createTwelveMonthHistory(
                                new BigDecimal(revenueBasis));

                TaxBracketRevenueBasisResult revenueResult = revenueCalculator.calculate(
                                new TaxBracketRevenueBasisRequest(
                                                LocalDate.of(2024, 1, 10),
                                                YearMonth.of(2026, 1),
                                                revenues));

                return bracketSelector.select(
                                new SimplesTaxBracketSelectionRequest(
                                                YearMonth.of(2026, 1),
                                                fatorRResult,
                                                revenueResult));
        }

        private List<MonthlyRevenue> createTwelveMonthHistory(
                        BigDecimal totalRevenue) {
                BigDecimal monthlyRevenue = totalRevenue.divide(
                                new BigDecimal("12"),
                                2,
                                java.math.RoundingMode.DOWN);

                BigDecimal accumulated = monthlyRevenue.multiply(
                                new BigDecimal("11"));

                BigDecimal lastMonthRevenue = totalRevenue.subtract(
                                accumulated);

                return List.of(
                                revenue(2025, 1, monthlyRevenue.toPlainString()),
                                revenue(2025, 2, monthlyRevenue.toPlainString()),
                                revenue(2025, 3, monthlyRevenue.toPlainString()),
                                revenue(2025, 4, monthlyRevenue.toPlainString()),
                                revenue(2025, 5, monthlyRevenue.toPlainString()),
                                revenue(2025, 6, monthlyRevenue.toPlainString()),
                                revenue(2025, 7, monthlyRevenue.toPlainString()),
                                revenue(2025, 8, monthlyRevenue.toPlainString()),
                                revenue(2025, 9, monthlyRevenue.toPlainString()),
                                revenue(2025, 10, monthlyRevenue.toPlainString()),
                                revenue(2025, 11, monthlyRevenue.toPlainString()),
                                revenue(2025, 12, lastMonthRevenue.toPlainString()));
        }

        private MonthlyRevenue revenue(
                        int year,
                        int month,
                        String amount) {
                return new MonthlyRevenue(
                                YearMonth.of(
                                                year,
                                                month),
                                new BigDecimal(amount));
        }
}