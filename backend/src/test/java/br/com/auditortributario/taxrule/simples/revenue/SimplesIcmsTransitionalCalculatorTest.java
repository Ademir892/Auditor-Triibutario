package br.com.auditortributario.taxrule.simples.revenue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimplesIcmsTransitionalCalculatorTest {

    private static final BigDecimal SUBLIMIT = new BigDecimal(
            "3600000.00");

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesSublimitEvaluator sublimitEvaluator = new SimplesSublimitEvaluator();

    private final SimplesSublimitTemporalEffectCalculator temporalEffectCalculator = new SimplesSublimitTemporalEffectCalculator();

    private final SimplesIcmsSublimitEffectCalculator icmsEffectCalculator = new SimplesIcmsSublimitEffectCalculator();

    private final SimplesSublimitMonthlyExcessCalculator monthlyExcessCalculator = new SimplesSublimitMonthlyExcessCalculator();

    private final SimplesIcmsTransitionalCalculator calculator = new SimplesIcmsTransitionalCalculator();

    @Test
    void shouldCalculateAnnexITransitionalIcmsUsingFifthBracket() {
        TestContext context = createTransitionalContext(
                "3700000.00",
                "200000.00");

        SimplesIcmsTransitionalCalculationResult result = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_I,
                SUBLIMIT,
                context.icmsEffect(),
                context.monthlyExcess());

        assertEquals(
                5,
                result.referenceBracketNumber());

        assertEquals(
                0,
                new BigDecimal(
                        "0.11875").compareTo(
                                result.referenceEffectiveRate()));

        assertEquals(
                0,
                new BigDecimal(
                        "0.3350").compareTo(
                                result.icmsDistributionRate()));

        assertEquals(
                0,
                new BigDecimal(
                        "0.039781250").compareTo(
                                result.icmsEffectiveRate()));

        assertEquals(
                new BigDecimal(
                        "3978.13"),
                result.icmsAmountForDisplay());
    }

    @Test
    void shouldCalculateAnnexIITransitionalIcmsUsingFifthBracket() {
        TestContext context = createTransitionalContext(
                "3700000.00",
                "200000.00");

        SimplesIcmsTransitionalCalculationResult result = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_II,
                SUBLIMIT,
                context.icmsEffect(),
                context.monthlyExcess());

        assertEquals(
                5,
                result.referenceBracketNumber());

        assertEquals(
                0,
                new BigDecimal(
                        "0.12325").compareTo(
                                result.referenceEffectiveRate()));

        assertEquals(
                0,
                new BigDecimal(
                        "0.3200").compareTo(
                                result.icmsDistributionRate()));

        assertEquals(
                0,
                new BigDecimal(
                        "0.03944000").compareTo(
                                result.icmsEffectiveRate()));

        assertEquals(
                new BigDecimal(
                        "3944.00"),
                result.icmsAmountForDisplay());
    }

    @Test
    void shouldApplyIcmsOnlyToExcessMonthlyRevenue() {
        TestContext context = createTransitionalContext(
                "3650000.00",
                "200000.00");

        SimplesIcmsTransitionalCalculationResult result = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_I,
                SUBLIMIT,
                context.icmsEffect(),
                context.monthlyExcess());

        assertEquals(
                0,
                new BigDecimal(
                        "50000.00").compareTo(
                                context
                                        .monthlyExcess()
                                        .excessMonthlyRevenue()));

        assertEquals(
                new BigDecimal(
                        "1989.06"),
                result.icmsAmountForDisplay());
    }

    @Test
    void shouldCalculateZeroIcmsWhenTransitionalMonthHasZeroRevenue() {
        SimplesSublimitEvaluationResult evaluation = sublimitEvaluator.evaluate(
                new BigDecimal(
                        "4000000.00"),
                SUBLIMIT);

        SimplesSublimitTemporalEffectResult temporalEffect = temporalEffectCalculator.calculate(
                evaluation,
                LocalDate.of(
                        2020,
                        1,
                        1),
                YearMonth.of(
                        2026,
                        7));

        SimplesIcmsSublimitEffectResult icmsEffect = icmsEffectCalculator.calculate(
                temporalEffect,
                YearMonth.of(
                        2026,
                        8));

        SimplesSublimitMonthlyExcessResult monthlyExcess = monthlyExcessCalculator.calculate(
                evaluation,
                BigDecimal.ZERO);

        SimplesIcmsTransitionalCalculationResult result = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_I,
                SUBLIMIT,
                icmsEffect,
                monthlyExcess);

        assertEquals(
                new BigDecimal(
                        "0.00"),
                result.icmsAmountForDisplay());
    }

    @Test
    void shouldRejectCalculationWhenIcmsIsAlreadyOutsideDas() {
        SimplesSublimitEvaluationResult evaluation = sublimitEvaluator.evaluate(
                new BigDecimal(
                        "4320000.01"),
                SUBLIMIT);

        SimplesSublimitTemporalEffectResult temporalEffect = temporalEffectCalculator.calculate(
                evaluation,
                LocalDate.of(
                        2020,
                        1,
                        1),
                COMPETENCE);

        SimplesIcmsSublimitEffectResult outsideDas = icmsEffectCalculator.calculate(
                temporalEffect,
                YearMonth.of(
                        2026,
                        9));

        SimplesSublimitMonthlyExcessResult monthlyExcess = monthlyExcessCalculator.calculate(
                evaluation,
                new BigDecimal(
                        "200000.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        SimplesRevenueTaxRoute.ANNEX_I,
                        SUBLIMIT,
                        outsideDas,
                        monthlyExcess));
    }

    @Test
    void shouldRejectServiceAnnexForIcmsCalculation() {
        TestContext context = createTransitionalContext(
                "3700000.00",
                "200000.00");

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        SimplesRevenueTaxRoute.ANNEX_IV,
                        SUBLIMIT,
                        context.icmsEffect(),
                        context.monthlyExcess()));
    }

    @Test
    void shouldRejectUnsupportedAnnualSublimit() {
        TestContext context = createTransitionalContext(
                "3700000.00",
                "200000.00");

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        SimplesRevenueTaxRoute.ANNEX_I,
                        new BigDecimal(
                                "2500000.00"),
                        context.icmsEffect(),
                        context.monthlyExcess()));
    }

    private TestContext createTransitionalContext(
            String accumulatedRevenue,
            String monthlyRevenue) {
        SimplesSublimitEvaluationResult evaluation = sublimitEvaluator.evaluate(
                new BigDecimal(
                        accumulatedRevenue),
                SUBLIMIT);

        SimplesSublimitTemporalEffectResult temporalEffect = temporalEffectCalculator.calculate(
                evaluation,
                LocalDate.of(
                        2020,
                        1,
                        1),
                COMPETENCE);

        SimplesIcmsSublimitEffectResult icmsEffect = icmsEffectCalculator.calculate(
                temporalEffect,
                COMPETENCE);

        SimplesSublimitMonthlyExcessResult monthlyExcess = monthlyExcessCalculator.calculate(
                evaluation,
                new BigDecimal(
                        monthlyRevenue));

        return new TestContext(
                icmsEffect,
                monthlyExcess);
    }

    private record TestContext(
            SimplesIcmsSublimitEffectResult icmsEffect,
            SimplesSublimitMonthlyExcessResult monthlyExcess) {
    }
}