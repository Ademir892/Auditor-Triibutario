package br.com.auditortributario.taxrule.simples.revenue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesSublimitTemporalEffectCalculatorTest {

    private static final BigDecimal SUBLIMIT = new BigDecimal(
            "3600000.00");

    private final SimplesSublimitEvaluator evaluator = new SimplesSublimitEvaluator();

    private final SimplesSublimitTemporalEffectCalculator calculator = new SimplesSublimitTemporalEffectCalculator();

    @Test
    void shouldProduceNoImpedimentWhenSublimitWasNotExceeded() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "3500000.00"),
                SUBLIMIT);

        SimplesSublimitTemporalEffectResult result = calculator.calculate(
                evaluation,
                LocalDate.of(
                        2020,
                        3,
                        10),
                YearMonth.of(
                        2026,
                        8));

        assertEquals(
                SimplesSublimitEffectTiming.NO_IMPEDIMENT,
                result.timing());

        assertFalse(
                result.hasImpediment());

        assertTrue(
                result.impedimentStartPeriod().isEmpty());
    }

    @Test
    void shouldApplyImpedimentNextYearWhenEstablishedCompanyExceedsUpToTwentyPercent() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "4000000.00"),
                SUBLIMIT);

        SimplesSublimitTemporalEffectResult result = calculator.calculate(
                evaluation,
                LocalDate.of(
                        2020,
                        3,
                        10),
                YearMonth.of(
                        2026,
                        8));

        assertEquals(
                SimplesSublimitEffectTiming.NEXT_CALENDAR_YEAR,
                result.timing());

        assertEquals(
                YearMonth.of(
                        2027,
                        1),
                result
                        .impedimentStartPeriod()
                        .orElseThrow());

        assertFalse(
                result.isRetroactive());
    }

    @Test
    void shouldApplyImpedimentNextMonthWhenEstablishedCompanyExceedsOverTwentyPercent() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "4320000.01"),
                SUBLIMIT);

        SimplesSublimitTemporalEffectResult result = calculator.calculate(
                evaluation,
                LocalDate.of(
                        2020,
                        3,
                        10),
                YearMonth.of(
                        2026,
                        8));

        assertEquals(
                SimplesSublimitEffectTiming.NEXT_MONTH,
                result.timing());

        assertEquals(
                YearMonth.of(
                        2026,
                        9),
                result
                        .impedimentStartPeriod()
                        .orElseThrow());

        assertFalse(
                result.isRetroactive());
    }

    @Test
    void shouldApplyImpedimentNextYearWhenOpeningYearExceedsUpToTwentyPercent() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "4000000.00"),
                SUBLIMIT);

        SimplesSublimitTemporalEffectResult result = calculator.calculate(
                evaluation,
                LocalDate.of(
                        2026,
                        2,
                        15),
                YearMonth.of(
                        2026,
                        8));

        assertTrue(
                result.isOpeningYear());

        assertEquals(
                SimplesSublimitEffectTiming.NEXT_CALENDAR_YEAR,
                result.timing());

        assertEquals(
                YearMonth.of(
                        2027,
                        1),
                result
                        .impedimentStartPeriod()
                        .orElseThrow());
    }

    @Test
    void shouldApplyRetroactiveImpedimentWhenOpeningYearExceedsOverTwentyPercent() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "4320000.01"),
                SUBLIMIT);

        SimplesSublimitTemporalEffectResult result = calculator.calculate(
                evaluation,
                LocalDate.of(
                        2026,
                        2,
                        15),
                YearMonth.of(
                        2026,
                        8));

        assertTrue(
                result.isOpeningYear());

        assertTrue(
                result.isRetroactive());

        assertEquals(
                SimplesSublimitEffectTiming.RETROACTIVE_TO_OPENING,
                result.timing());

        assertEquals(
                YearMonth.of(
                        2026,
                        2),
                result
                        .impedimentStartPeriod()
                        .orElseThrow());
    }

    @Test
    void shouldTreatExactTwentyPercentAsNextCalendarYear() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "4320000.00"),
                SUBLIMIT);

        SimplesSublimitTemporalEffectResult result = calculator.calculate(
                evaluation,
                LocalDate.of(
                        2020,
                        1,
                        1),
                YearMonth.of(
                        2026,
                        12));

        assertEquals(
                SimplesSublimitStatus.EXCEEDED_UP_TO_TWENTY_PERCENT,
                evaluation.status());

        assertEquals(
                SimplesSublimitEffectTiming.NEXT_CALENDAR_YEAR,
                result.timing());

        assertEquals(
                YearMonth.of(
                        2027,
                        1),
                result
                        .impedimentStartPeriod()
                        .orElseThrow());
    }

    @Test
    void shouldRejectEvaluationPeriodBeforeOpening() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "100000.00"),
                SUBLIMIT);

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        evaluation,
                        LocalDate.of(
                                2026,
                                8,
                                10),
                        YearMonth.of(
                                2026,
                                7)));
    }
}