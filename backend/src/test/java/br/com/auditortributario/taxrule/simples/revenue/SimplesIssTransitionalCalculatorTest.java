package br.com.auditortributario.taxrule.simples.revenue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesIssTransitionalCalculatorTest {

    private static final BigDecimal SUBLIMIT = new BigDecimal("3600000.00");

    private static final YearMonth COMPETENCE = YearMonth.of(2026, 8);

    private final SimplesSublimitEvaluator sublimitEvaluator = new SimplesSublimitEvaluator();

    private final SimplesSublimitTemporalEffectCalculator temporalCalculator = new SimplesSublimitTemporalEffectCalculator();

    private final SimplesIssSublimitEffectCalculator issEffectCalculator = new SimplesIssSublimitEffectCalculator();

    private final SimplesSublimitMonthlyExcessCalculator monthlyExcessCalculator = new SimplesSublimitMonthlyExcessCalculator();

    private final SimplesIssTransitionalCalculator calculator = new SimplesIssTransitionalCalculator();

    @Test
    void shouldLimitAnnexIIITransitionalIssToFivePercent() {
        TestContext context = createContext(
                "3700000.00",
                "200000.00");

        SimplesIssTransitionalCalculationResult result = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_III,
                SUBLIMIT,
                context.issEffect(),
                context.monthlyExcess());

        assertEquals(
                5,
                result.referenceBracketNumber());

        assertEquals(
                0,
                new BigDecimal("0.1751").compareTo(
                        result.referenceEffectiveRate()));

        assertEquals(
                0,
                new BigDecimal("0.0586585").compareTo(
                        result.rawIssEffectiveRate()));

        assertEquals(
                0,
                new BigDecimal("0.05").compareTo(
                        result.issEffectiveRate()));

        assertTrue(
                result.limitedToFivePercent());

        assertEquals(
                new BigDecimal("5000.00"),
                result.issAmountForDisplay());
    }

    @Test
    void shouldLimitAnnexIVTransitionalIssToFivePercent() {
        TestContext context = createContext(
                "3700000.00",
                "200000.00");

        SimplesIssTransitionalCalculationResult result = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_IV,
                SUBLIMIT,
                context.issEffect(),
                context.monthlyExcess());

        assertEquals(
                0,
                new BigDecimal("0.16895").compareTo(
                        result.referenceEffectiveRate()));

        assertEquals(
                0,
                new BigDecimal("0.0675800").compareTo(
                        result.rawIssEffectiveRate()));

        assertEquals(
                0,
                new BigDecimal("0.05").compareTo(
                        result.issEffectiveRate()));

        assertTrue(
                result.limitedToFivePercent());

        assertEquals(
                new BigDecimal("5000.00"),
                result.issAmountForDisplay());
    }

    @Test
    void shouldKeepAnnexVBelowFivePercentCap() {
        TestContext context = createContext(
                "3700000.00",
                "200000.00");

        SimplesIssTransitionalCalculationResult result = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_V,
                SUBLIMIT,
                context.issEffect(),
                context.monthlyExcess());

        assertEquals(
                0,
                new BigDecimal("0.21275").compareTo(
                        result.referenceEffectiveRate()));

        assertEquals(
                0,
                new BigDecimal("0.04999625").compareTo(
                        result.issEffectiveRate()));

        assertFalse(
                result.limitedToFivePercent());

        assertEquals(
                new BigDecimal("4999.63"),
                result.issAmountForDisplay());
    }

    @Test
    void shouldApplyIssOnlyToMonthlyExcessPortion() {
        TestContext context = createContext(
                "3650000.00",
                "200000.00");

        SimplesIssTransitionalCalculationResult result = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_IV,
                SUBLIMIT,
                context.issEffect(),
                context.monthlyExcess());

        assertEquals(
                0,
                new BigDecimal("50000.00").compareTo(
                        context
                                .monthlyExcess()
                                .excessMonthlyRevenue()));

        assertEquals(
                new BigDecimal("2500.00"),
                result.issAmountForDisplay());
    }

    private TestContext createContext(
            String accumulatedRevenue,
            String monthlyRevenue) {
        SimplesSublimitEvaluationResult evaluation = sublimitEvaluator.evaluate(
                new BigDecimal(
                        accumulatedRevenue),
                SUBLIMIT);

        SimplesSublimitTemporalEffectResult temporalEffect = temporalCalculator.calculate(
                evaluation,
                LocalDate.of(
                        2020,
                        1,
                        1),
                COMPETENCE);

        SimplesIssSublimitEffectResult issEffect = issEffectCalculator.calculate(
                temporalEffect,
                COMPETENCE);

        SimplesSublimitMonthlyExcessResult monthlyExcess = monthlyExcessCalculator.calculate(
                evaluation,
                new BigDecimal(
                        monthlyRevenue));

        return new TestContext(
                issEffect,
                monthlyExcess);
    }

    private record TestContext(
            SimplesIssSublimitEffectResult issEffect,
            SimplesSublimitMonthlyExcessResult monthlyExcess) {
    }
}