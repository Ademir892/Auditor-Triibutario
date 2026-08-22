package br.com.auditortributario.taxrule.simples.revenue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesOpeningYearSublimitCalculatorTest {

    private static final BigDecimal ANNUAL_SUBLIMIT = new BigDecimal(
            "3600000.00");

    private final SimplesOpeningYearSublimitCalculator calculator = new SimplesOpeningYearSublimitCalculator();

    private final SimplesSublimitEvaluator evaluator = new SimplesSublimitEvaluator();

    @Test
    void shouldUseTwelveMonthsForJanuaryOpening() {
        SimplesOpeningYearSublimitResult result = calculator.calculate(
                LocalDate.of(
                        2026,
                        1,
                        1),
                ANNUAL_SUBLIMIT);

        assertEquals(
                12,
                result.monthsConsidered());

        assertTrue(
                result.coversFullCalendarYear());

        assertEquals(
                0,
                new BigDecimal(
                        "300000.00").compareTo(
                                result.monthlyReference()));

        assertEquals(
                0,
                new BigDecimal(
                        "3600000.00").compareTo(
                                result.proportionalizedSublimit()));
    }

    @Test
    void shouldCountJanuaryFractionAsFullMonth() {
        SimplesOpeningYearSublimitResult result = calculator.calculate(
                LocalDate.of(
                        2026,
                        1,
                        31),
                ANNUAL_SUBLIMIT);

        assertEquals(
                12,
                result.monthsConsidered());

        assertEquals(
                0,
                new BigDecimal(
                        "3600000.00").compareTo(
                                result.proportionalizedSublimit()));
    }

    @Test
    void shouldCalculateElevenMonthsForFebruaryOpening() {
        SimplesOpeningYearSublimitResult result = calculator.calculate(
                LocalDate.of(
                        2026,
                        2,
                        15),
                ANNUAL_SUBLIMIT);

        assertEquals(
                11,
                result.monthsConsidered());

        assertFalse(
                result.coversFullCalendarYear());

        assertEquals(
                0,
                new BigDecimal(
                        "3300000.00").compareTo(
                                result.proportionalizedSublimit()));
    }

    @Test
    void shouldCountFebruaryFractionAsFullMonth() {
        SimplesOpeningYearSublimitResult result = calculator.calculate(
                LocalDate.of(
                        2026,
                        2,
                        28),
                ANNUAL_SUBLIMIT);

        assertEquals(
                11,
                result.monthsConsidered());

        assertEquals(
                0,
                new BigDecimal(
                        "3300000.00").compareTo(
                                result.proportionalizedSublimit()));
    }

    @Test
    void shouldCalculateOneMonthForDecemberOpening() {
        SimplesOpeningYearSublimitResult result = calculator.calculate(
                LocalDate.of(
                        2026,
                        12,
                        31),
                ANNUAL_SUBLIMIT);

        assertEquals(
                1,
                result.monthsConsidered());

        assertEquals(
                0,
                new BigDecimal(
                        "300000.00").compareTo(
                                result.proportionalizedSublimit()));
    }

    @Test
    void shouldUseProportionalizedSublimitInExistingEvaluator() {
        SimplesOpeningYearSublimitResult proportionalized = calculator.calculate(
                LocalDate.of(
                        2026,
                        2,
                        15),
                ANNUAL_SUBLIMIT);

        SimplesSublimitEvaluationResult within = evaluator.evaluate(
                new BigDecimal(
                        "3300000.00"),
                proportionalized
                        .proportionalizedSublimit());

        SimplesSublimitEvaluationResult exceeded = evaluator.evaluate(
                new BigDecimal(
                        "3300000.01"),
                proportionalized
                        .proportionalizedSublimit());

        SimplesSublimitEvaluationResult exceededOverTwentyPercent = evaluator.evaluate(
                new BigDecimal(
                        "3960000.01"),
                proportionalized
                        .proportionalizedSublimit());

        assertEquals(
                SimplesSublimitStatus.WITHIN_SUBLIMIT,
                within.status());

        assertEquals(
                SimplesSublimitStatus.EXCEEDED_UP_TO_TWENTY_PERCENT,
                exceeded.status());

        assertEquals(
                SimplesSublimitStatus.EXCEEDED_OVER_TWENTY_PERCENT,
                exceededOverTwentyPercent.status());
    }

    @Test
    void shouldRejectZeroAnnualSublimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        LocalDate.of(
                                2026,
                                2,
                                15),
                        BigDecimal.ZERO));
    }

    @Test
    void shouldRejectNegativeAnnualSublimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        LocalDate.of(
                                2026,
                                2,
                                15),
                        new BigDecimal(
                                "-0.01")));
    }
}