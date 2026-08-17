package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaxBracketRevenueBasisCalculatorTest {

    private final TaxBracketRevenueBasisCalculator calculator = new TaxBracketRevenueBasisCalculator();

    @Test
    void shouldCalculateRbt12pInFirstMonth() {
        TaxBracketRevenueBasisRequest request = new TaxBracketRevenueBasisRequest(
                LocalDate.of(2026, 2, 10),
                YearMonth.of(2026, 2),
                List.of(
                        revenue(
                                2026,
                                2,
                                "10000.00")));

        TaxBracketRevenueBasisResult result = calculator.calculate(request);

        assertEquals(
                TaxBracketRevenueBasisType.RBT12_PROPORTIONALIZED,
                result.basisType());

        assertEquals(
                new BigDecimal("120000.00"),
                result.revenueBasis());

        assertEquals(
                1,
                result.revenuesUsed().size());
    }

    @Test
    void shouldReproduceOfficialFourthMonthExample() {
        TaxBracketRevenueBasisRequest request = new TaxBracketRevenueBasisRequest(
                LocalDate.of(2026, 2, 10),
                YearMonth.of(2026, 5),
                List.of(
                        revenue(
                                2026,
                                2,
                                "10000.00"),
                        revenue(
                                2026,
                                3,
                                "0.00"),
                        revenue(
                                2026,
                                4,
                                "590000.00"),
                        revenue(
                                2026,
                                5,
                                "50000.00")));

        TaxBracketRevenueBasisResult result = calculator.calculate(request);

        assertEquals(
                TaxBracketRevenueBasisType.RBT12_PROPORTIONALIZED,
                result.basisType());

        assertEquals(
                new BigDecimal("2400000.00"),
                result.revenueBasis());

        assertEquals(
                3,
                result.revenuesUsed().size());

        assertEquals(
                YearMonth.of(2026, 2),
                result.revenuesUsed()
                        .get(0)
                        .period());

        assertEquals(
                YearMonth.of(2026, 4),
                result.revenuesUsed()
                        .get(2)
                        .period());
    }

    @Test
    void shouldNotUseCurrentMonthRevenueFromSecondToTwelfthMonth() {
        TaxBracketRevenueBasisRequest request = new TaxBracketRevenueBasisRequest(
                LocalDate.of(2026, 2, 10),
                YearMonth.of(2026, 5),
                List.of(
                        revenue(
                                2026,
                                2,
                                "10000.00"),
                        revenue(
                                2026,
                                3,
                                "0.00"),
                        revenue(
                                2026,
                                4,
                                "590000.00"),
                        revenue(
                                2026,
                                5,
                                "999999.00")));

        TaxBracketRevenueBasisResult result = calculator.calculate(request);

        assertEquals(
                new BigDecimal("2400000.00"),
                result.revenueBasis());

        assertTrue(
                result.revenuesUsed()
                        .stream()
                        .noneMatch(revenue -> revenue.period().equals(
                                YearMonth.of(
                                        2026,
                                        5))));
    }

    @Test
    void shouldUseRbt12pInTwelfthMonth() {
        TaxBracketRevenueBasisRequest request = new TaxBracketRevenueBasisRequest(
                LocalDate.of(2026, 1, 15),
                YearMonth.of(2026, 12),
                List.of(
                        revenue(2026, 1, "10000.00"),
                        revenue(2026, 2, "10000.00"),
                        revenue(2026, 3, "10000.00"),
                        revenue(2026, 4, "10000.00"),
                        revenue(2026, 5, "10000.00"),
                        revenue(2026, 6, "10000.00"),
                        revenue(2026, 7, "10000.00"),
                        revenue(2026, 8, "10000.00"),
                        revenue(2026, 9, "10000.00"),
                        revenue(2026, 10, "10000.00"),
                        revenue(2026, 11, "10000.00")));

        TaxBracketRevenueBasisResult result = calculator.calculate(request);

        assertEquals(
                TaxBracketRevenueBasisType.RBT12_PROPORTIONALIZED,
                result.basisType());

        assertEquals(
                new BigDecimal("120000.00"),
                result.revenueBasis());

        assertEquals(
                11,
                result.revenuesUsed().size());
    }

    @Test
    void shouldUseStandardRbt12FromThirteenthMonth() {
        TaxBracketRevenueBasisRequest request = new TaxBracketRevenueBasisRequest(
                LocalDate.of(2025, 1, 15),
                YearMonth.of(2026, 1),
                List.of(
                        revenue(2025, 1, "10000.00"),
                        revenue(2025, 2, "20000.00"),
                        revenue(2025, 3, "30000.00"),
                        revenue(2025, 4, "40000.00"),
                        revenue(2025, 5, "50000.00"),
                        revenue(2025, 6, "60000.00"),
                        revenue(2025, 7, "70000.00"),
                        revenue(2025, 8, "80000.00"),
                        revenue(2025, 9, "90000.00"),
                        revenue(2025, 10, "100000.00"),
                        revenue(2025, 11, "110000.00"),
                        revenue(2025, 12, "120000.00")));

        TaxBracketRevenueBasisResult result = calculator.calculate(request);

        assertEquals(
                TaxBracketRevenueBasisType.RBT12,
                result.basisType());

        assertEquals(
                new BigDecimal("780000.00"),
                result.revenueBasis());

        assertEquals(
                12,
                result.revenuesUsed().size());
    }

    @Test
    void shouldRejectMissingRevenueMonth() {
        TaxBracketRevenueBasisRequest request = new TaxBracketRevenueBasisRequest(
                LocalDate.of(2026, 2, 10),
                YearMonth.of(2026, 5),
                List.of(
                        revenue(
                                2026,
                                2,
                                "10000.00"),
                        revenue(
                                2026,
                                4,
                                "590000.00")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(request));

        assertTrue(
                exception.getMessage()
                        .contains("2026-03"));

        assertTrue(
                exception.getMessage()
                        .contains("0,00"));
    }

    @Test
    void shouldRejectDuplicateRevenuePeriod() {
        TaxBracketRevenueBasisRequest request = new TaxBracketRevenueBasisRequest(
                LocalDate.of(2026, 2, 10),
                YearMonth.of(2026, 3),
                List.of(
                        revenue(
                                2026,
                                2,
                                "10000.00"),
                        revenue(
                                2026,
                                2,
                                "20000.00")));

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(request));
    }

    @Test
    void shouldRejectRevenueBeforeCompanyOpening() {
        TaxBracketRevenueBasisRequest request = new TaxBracketRevenueBasisRequest(
                LocalDate.of(2026, 2, 10),
                YearMonth.of(2026, 3),
                List.of(
                        revenue(
                                2026,
                                1,
                                "10000.00"),
                        revenue(
                                2026,
                                2,
                                "20000.00")));

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(request));
    }

    @Test
    void shouldRejectRevenueAfterAssessmentPeriod() {
        TaxBracketRevenueBasisRequest request = new TaxBracketRevenueBasisRequest(
                LocalDate.of(2026, 2, 10),
                YearMonth.of(2026, 3),
                List.of(
                        revenue(
                                2026,
                                2,
                                "10000.00"),
                        revenue(
                                2026,
                                4,
                                "20000.00")));

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(request));
    }

    @Test
    void shouldRejectAssessmentPeriodBeforeOpening() {
        TaxBracketRevenueBasisRequest request = new TaxBracketRevenueBasisRequest(
                LocalDate.of(2026, 5, 10),
                YearMonth.of(2026, 4),
                List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(request));
    }

    @Test
    void shouldRejectAssessmentPeriodOutsideValidatedRuleVersion() {
        TaxBracketRevenueBasisRequest request = new TaxBracketRevenueBasisRequest(
                LocalDate.of(2026, 1, 10),
                YearMonth.of(2027, 1),
                List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(request));
    }

    @Test
    void shouldCreateAuditableTaxDecision() {
        TaxBracketRevenueBasisRequest request = new TaxBracketRevenueBasisRequest(
                LocalDate.of(2026, 2, 10),
                YearMonth.of(2026, 5),
                List.of(
                        revenue(
                                2026,
                                2,
                                "10000.00"),
                        revenue(
                                2026,
                                3,
                                "0.00"),
                        revenue(
                                2026,
                                4,
                                "590000.00")));

        TaxBracketRevenueBasisResult result = calculator.calculate(request);

        assertEquals(
                "SIMPLES_TAX_BRACKET_REVENUE_BASIS",
                result.decision().ruleCode());

        assertEquals(
                "2018-2026.1",
                result.decision().ruleVersion());

        assertTrue(
                result.decision()
                        .result()
                        .contains("2400000.00"));

        assertTrue(
                result.decision()
                        .condition()
                        .contains("RBT12p"));

        assertTrue(
                result.decision()
                        .input()
                        .contains("2026-03 = 0.00"));
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