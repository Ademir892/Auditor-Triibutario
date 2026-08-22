package br.com.auditortributario.taxrule.simples.revenue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesSublimitMonthlyExcessCalculatorTest {

    private static final BigDecimal SUBLIMIT = new BigDecimal(
            "3600000.00");

    private final SimplesSublimitEvaluator evaluator = new SimplesSublimitEvaluator();

    private final SimplesSublimitMonthlyExcessCalculator calculator = new SimplesSublimitMonthlyExcessCalculator();

    @Test
    void shouldProduceNoMonthlyExcessWhenAccumulatedRevenueIsBelowSublimit() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "3500000.00"),
                SUBLIMIT);

        SimplesSublimitMonthlyExcessResult result = calculator.calculate(
                evaluation,
                new BigDecimal(
                        "100000.00"));

        assertFalse(
                result.hasExcessPortion());

        assertFalse(
                result.isEntireMonthlyRevenueAboveSublimit());

        assertEquals(
                0,
                new BigDecimal(
                        "100000.00").compareTo(
                                result.revenueWithinSublimit()));

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.excessMonthlyRevenue()));

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.excessRatio()));
    }

    @Test
    void shouldProduceNoMonthlyExcessAtExactSublimitBoundary() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "3600000.00"),
                SUBLIMIT);

        SimplesSublimitMonthlyExcessResult result = calculator.calculate(
                evaluation,
                new BigDecimal(
                        "100000.00"));

        assertFalse(
                result.hasExcessPortion());

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.excessMonthlyRevenue()));
    }

    @Test
    void shouldSplitCrossingMonthIntoNormalAndExcessPortions() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "3700000.00"),
                SUBLIMIT);

        SimplesSublimitMonthlyExcessResult result = calculator.calculate(
                evaluation,
                new BigDecimal(
                        "200000.00"));

        assertTrue(
                result.hasExcessPortion());

        assertFalse(
                result.isEntireMonthlyRevenueAboveSublimit());

        assertEquals(
                0,
                new BigDecimal(
                        "100000.00").compareTo(
                                result.revenueWithinSublimit()));

        assertEquals(
                0,
                new BigDecimal(
                        "100000.00").compareTo(
                                result.excessMonthlyRevenue()));

        assertEquals(
                0,
                new BigDecimal(
                        "0.5").compareTo(
                                result.excessRatio()));

        assertEquals(
                new BigDecimal(
                        "50.00"),
                result.excessPercentageForDisplay());
    }

    @Test
    void shouldTreatEntireMonthAsExcessWhenSublimitWasAlreadyExceededBeforeMonth() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "4000000.00"),
                SUBLIMIT);

        SimplesSublimitMonthlyExcessResult result = calculator.calculate(
                evaluation,
                new BigDecimal(
                        "100000.00"));

        assertTrue(
                result.hasExcessPortion());

        assertTrue(
                result.isEntireMonthlyRevenueAboveSublimit());

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.revenueWithinSublimit()));

        assertEquals(
                0,
                new BigDecimal(
                        "100000.00").compareTo(
                                result.excessMonthlyRevenue()));

        assertEquals(
                0,
                BigDecimal.ONE.compareTo(
                        result.excessRatio()));
    }

    @Test
    void shouldWorkWithProportionalizedOpeningYearSublimit() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "3400000.00"),
                new BigDecimal(
                        "3300000.00"));

        SimplesSublimitMonthlyExcessResult result = calculator.calculate(
                evaluation,
                new BigDecimal(
                        "200000.00"));

        assertEquals(
                0,
                new BigDecimal(
                        "100000.00").compareTo(
                                result.revenueWithinSublimit()));

        assertEquals(
                0,
                new BigDecimal(
                        "100000.00").compareTo(
                                result.excessMonthlyRevenue()));

        assertEquals(
                0,
                new BigDecimal(
                        "0.5").compareTo(
                                result.excessRatio()));
    }

    @Test
    void shouldReturnZeroRatioForZeroMonthlyRevenue() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "3500000.00"),
                SUBLIMIT);

        SimplesSublimitMonthlyExcessResult result = calculator.calculate(
                evaluation,
                BigDecimal.ZERO);

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.revenueWithinSublimit()));

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.excessMonthlyRevenue()));

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.excessRatio()));
    }

    @Test
    void shouldRejectMonthlyRevenueGreaterThanAccumulatedRevenue() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "100000.00"),
                SUBLIMIT);

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        evaluation,
                        new BigDecimal(
                                "100000.01")));
    }

    @Test
    void shouldRejectNegativeMonthlyRevenue() {
        SimplesSublimitEvaluationResult evaluation = evaluator.evaluate(
                new BigDecimal(
                        "3500000.00"),
                SUBLIMIT);

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        evaluation,
                        new BigDecimal(
                                "-0.01")));
    }
}