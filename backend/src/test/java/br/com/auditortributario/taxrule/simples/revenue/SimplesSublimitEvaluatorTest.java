package br.com.auditortributario.taxrule.simples.revenue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesSublimitEvaluatorTest {

    private static final BigDecimal SUBLIMIT_2026 = new BigDecimal(
            "3600000.00");

    private final SimplesSublimitEvaluator evaluator = new SimplesSublimitEvaluator();

    @Test
    void shouldRemainWithinSublimit() {
        SimplesSublimitEvaluationResult result = evaluator.evaluate(
                new BigDecimal(
                        "3500000.00"),
                SUBLIMIT_2026);

        assertEquals(
                SimplesSublimitStatus.WITHIN_SUBLIMIT,
                result.status());

        assertFalse(
                result.isExceeded());

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.excessAmount()));
    }

    @Test
    void shouldRemainWithinSublimitAtExactBoundary() {
        SimplesSublimitEvaluationResult result = evaluator.evaluate(
                new BigDecimal(
                        "3600000.00"),
                SUBLIMIT_2026);

        assertEquals(
                SimplesSublimitStatus.WITHIN_SUBLIMIT,
                result.status());

        assertFalse(
                result.isExceeded());
    }

    @Test
    void shouldIdentifyExcessUpToTwentyPercent() {
        SimplesSublimitEvaluationResult result = evaluator.evaluate(
                new BigDecimal(
                        "4000000.00"),
                SUBLIMIT_2026);

        assertEquals(
                SimplesSublimitStatus.EXCEEDED_UP_TO_TWENTY_PERCENT,
                result.status());

        assertTrue(
                result.isExceeded());

        assertFalse(
                result.isExceededOverTwentyPercent());

        assertEquals(
                0,
                new BigDecimal(
                        "400000.00").compareTo(
                                result.excessAmount()));
    }

    @Test
    void shouldTreatExactTwentyPercentBoundaryAsUpToTwentyPercent() {
        SimplesSublimitEvaluationResult result = evaluator.evaluate(
                new BigDecimal(
                        "4320000.00"),
                SUBLIMIT_2026);

        assertEquals(
                SimplesSublimitStatus.EXCEEDED_UP_TO_TWENTY_PERCENT,
                result.status());

        assertEquals(
                0,
                new BigDecimal(
                        "4320000.0000").compareTo(
                                result.twentyPercentThreshold()));
    }

    @Test
    void shouldIdentifyExcessOverTwentyPercent() {
        SimplesSublimitEvaluationResult result = evaluator.evaluate(
                new BigDecimal(
                        "4320000.01"),
                SUBLIMIT_2026);

        assertEquals(
                SimplesSublimitStatus.EXCEEDED_OVER_TWENTY_PERCENT,
                result.status());

        assertTrue(
                result.isExceeded());

        assertTrue(
                result.isExceededOverTwentyPercent());

        assertEquals(
                0,
                new BigDecimal(
                        "720000.01").compareTo(
                                result.excessAmount()));
    }

    @Test
    void shouldRejectNegativeAccumulatedRevenue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> evaluator.evaluate(
                        new BigDecimal(
                                "-0.01"),
                        SUBLIMIT_2026));
    }

    @Test
    void shouldRejectZeroSublimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> evaluator.evaluate(
                        new BigDecimal(
                                "100000.00"),
                        BigDecimal.ZERO));
    }
}