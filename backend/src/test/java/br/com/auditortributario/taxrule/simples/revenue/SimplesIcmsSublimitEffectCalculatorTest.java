package br.com.auditortributario.taxrule.simples.revenue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesIcmsSublimitEffectCalculatorTest {

    private static final BigDecimal SUBLIMIT = new BigDecimal(
            "3600000.00");

    private final SimplesSublimitEvaluator sublimitEvaluator = new SimplesSublimitEvaluator();

    private final SimplesSublimitTemporalEffectCalculator temporalCalculator = new SimplesSublimitTemporalEffectCalculator();

    private final SimplesIcmsSublimitEffectCalculator calculator = new SimplesIcmsSublimitEffectCalculator();

    @Test
    void shouldKeepIcmsInStandardDasWhenSublimitWasNotExceeded() {
        SimplesSublimitTemporalEffectResult temporalEffect = temporalEffect(
                "3500000.00",
                LocalDate.of(
                        2020,
                        1,
                        1),
                YearMonth.of(
                        2026,
                        8));

        SimplesIcmsSublimitEffectResult result = calculator.calculate(
                temporalEffect,
                YearMonth.of(
                        2026,
                        8));

        assertEquals(
                SimplesIcmsSublimitCollectionStatus.IN_DAS_STANDARD,
                result.status());

        assertTrue(
                result.isInsideDas());

        assertFalse(
                result.requiresTransitionalCalculation());

        assertFalse(
                result.isOutsideDas());
    }

    @Test
    void shouldUseTransitionalCalculationInMonthOfExcessOverTwentyPercent() {
        SimplesSublimitTemporalEffectResult temporalEffect = temporalEffect(
                "4320000.01",
                LocalDate.of(
                        2020,
                        1,
                        1),
                YearMonth.of(
                        2026,
                        8));

        SimplesIcmsSublimitEffectResult result = calculator.calculate(
                temporalEffect,
                YearMonth.of(
                        2026,
                        8));

        assertEquals(
                SimplesIcmsSublimitCollectionStatus.IN_DAS_TRANSITIONAL,
                result.status());

        assertTrue(
                result.isInsideDas());

        assertTrue(
                result.requiresTransitionalCalculation());
    }

    @Test
    void shouldMoveIcmsOutsideDasInMonthFollowingExcessOverTwentyPercent() {
        SimplesSublimitTemporalEffectResult temporalEffect = temporalEffect(
                "4320000.01",
                LocalDate.of(
                        2020,
                        1,
                        1),
                YearMonth.of(
                        2026,
                        8));

        SimplesIcmsSublimitEffectResult result = calculator.calculate(
                temporalEffect,
                YearMonth.of(
                        2026,
                        9));

        assertEquals(
                SimplesIcmsSublimitCollectionStatus.OUTSIDE_DAS,
                result.status());

        assertTrue(
                result.isOutsideDas());

        assertFalse(
                result.isInsideDas());
    }

    @Test
    void shouldKeepTransitionalIcmsUntilEndOfYearForExcessUpToTwentyPercent() {
        SimplesSublimitTemporalEffectResult temporalEffect = temporalEffect(
                "4000000.00",
                LocalDate.of(
                        2020,
                        1,
                        1),
                YearMonth.of(
                        2025,
                        8));

        SimplesIcmsSublimitEffectResult result = calculator.calculate(
                temporalEffect,
                YearMonth.of(
                        2025,
                        12));

        assertEquals(
                SimplesIcmsSublimitCollectionStatus.IN_DAS_TRANSITIONAL,
                result.status());

        assertTrue(
                result.requiresTransitionalCalculation());
    }

    @Test
    void shouldMoveIcmsOutsideDasNextYearForExcessUpToTwentyPercent() {
        SimplesSublimitTemporalEffectResult temporalEffect = temporalEffect(
                "4000000.00",
                LocalDate.of(
                        2020,
                        1,
                        1),
                YearMonth.of(
                        2025,
                        8));

        SimplesIcmsSublimitEffectResult result = calculator.calculate(
                temporalEffect,
                YearMonth.of(
                        2026,
                        1));

        assertEquals(
                SimplesIcmsSublimitCollectionStatus.OUTSIDE_DAS,
                result.status());

        assertTrue(
                result.isOutsideDas());
    }

    @Test
    void shouldKeepStandardIcmsBeforeExcessDetectionPeriod() {
        SimplesSublimitTemporalEffectResult temporalEffect = temporalEffect(
                "4320000.01",
                LocalDate.of(
                        2020,
                        1,
                        1),
                YearMonth.of(
                        2026,
                        8));

        SimplesIcmsSublimitEffectResult result = calculator.calculate(
                temporalEffect,
                YearMonth.of(
                        2026,
                        7));

        assertEquals(
                SimplesIcmsSublimitCollectionStatus.IN_DAS_STANDARD,
                result.status());
    }

    @Test
    void shouldApplyOpeningYearExcessOverTwentyPercentRetroactively() {
        SimplesSublimitEvaluationResult evaluation = sublimitEvaluator.evaluate(
                new BigDecimal(
                        "3960000.01"),
                new BigDecimal(
                        "3300000.00"));

        SimplesSublimitTemporalEffectResult temporalEffect = temporalCalculator.calculate(
                evaluation,
                LocalDate.of(
                        2026,
                        2,
                        15),
                YearMonth.of(
                        2026,
                        8));

        SimplesIcmsSublimitEffectResult result = calculator.calculate(
                temporalEffect,
                YearMonth.of(
                        2026,
                        2));

        assertEquals(
                SimplesIcmsSublimitCollectionStatus.OUTSIDE_DAS,
                result.status());

        assertTrue(
                result.isOutsideDas());
    }

    private SimplesSublimitTemporalEffectResult temporalEffect(
            String accumulatedRevenue,
            LocalDate openingDate,
            YearMonth evaluationPeriod) {
        SimplesSublimitEvaluationResult evaluation = sublimitEvaluator.evaluate(
                new BigDecimal(
                        accumulatedRevenue),
                SUBLIMIT);

        return temporalCalculator.calculate(
                evaluation,
                openingDate,
                evaluationPeriod);
    }
}