package br.com.auditortributario.taxrule.simples.revenue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimplesSublimitSegregatedRevenueIntegrationTest {

    private static final BigDecimal SUBLIMIT = new BigDecimal("3600000.00");

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesSublimitEvaluator sublimitEvaluator = new SimplesSublimitEvaluator();

    private final SimplesSublimitTemporalEffectCalculator temporalCalculator = new SimplesSublimitTemporalEffectCalculator();

    private final SimplesSublimitMonthlyExcessCalculator monthlyExcessCalculator = new SimplesSublimitMonthlyExcessCalculator();

    private final SimplesIcmsSublimitEffectCalculator icmsEffectCalculator = new SimplesIcmsSublimitEffectCalculator();

    private final SimplesIssSublimitEffectCalculator issEffectCalculator = new SimplesIssSublimitEffectCalculator();

    private final SimplesIcmsTransitionalCalculator icmsCalculator = new SimplesIcmsTransitionalCalculator();

    private final SimplesIssTransitionalCalculator issCalculator = new SimplesIssTransitionalCalculator();

    @Test
    void shouldDistributeMonthlyExcessAcrossSegregatedIcmsRevenues() {
        SharedContext context = createSharedContext();

        SimplesIcmsTransitionalCalculationResult commerce = icmsCalculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_I,
                SUBLIMIT,
                context.icmsEffect(),
                context.monthlyExcess(),
                new BigDecimal("120000.00"));

        SimplesIcmsTransitionalCalculationResult industry = icmsCalculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_II,
                SUBLIMIT,
                context.icmsEffect(),
                context.monthlyExcess(),
                new BigDecimal("80000.00"));

        assertEquals(
                0,
                new BigDecimal("0.5").compareTo(
                        context
                                .monthlyExcess()
                                .excessRatio()));

        assertEquals(
                new BigDecimal("60000.00"),
                commerce
                        .segregatedExcessRevenueForDisplay());

        assertEquals(
                new BigDecimal("40000.00"),
                industry
                        .segregatedExcessRevenueForDisplay());

        BigDecimal totalSegregatedExcess = commerce
                .segregatedExcessRevenue()
                .add(
                        industry
                                .segregatedExcessRevenue());

        assertEquals(
                0,
                new BigDecimal("100000.00").compareTo(
                        totalSegregatedExcess));

        assertEquals(
                0,
                context
                        .monthlyExcess()
                        .excessMonthlyRevenue()
                        .compareTo(
                                totalSegregatedExcess));

        assertEquals(
                new BigDecimal("2386.88"),
                commerce.icmsAmountForDisplay());

        assertEquals(
                new BigDecimal("1577.60"),
                industry.icmsAmountForDisplay());
    }

    @Test
    void shouldDistributeMonthlyExcessAcrossSegregatedIssRevenues() {
        SharedContext context = createSharedContext();

        SimplesIssTransitionalCalculationResult annexIII = issCalculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_III,
                SUBLIMIT,
                context.issEffect(),
                context.monthlyExcess(),
                new BigDecimal("120000.00"));

        SimplesIssTransitionalCalculationResult annexV = issCalculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_V,
                SUBLIMIT,
                context.issEffect(),
                context.monthlyExcess(),
                new BigDecimal("80000.00"));

        assertEquals(
                new BigDecimal("60000.00"),
                annexIII
                        .segregatedExcessRevenueForDisplay());

        assertEquals(
                new BigDecimal("40000.00"),
                annexV
                        .segregatedExcessRevenueForDisplay());

        BigDecimal totalSegregatedExcess = annexIII
                .segregatedExcessRevenue()
                .add(
                        annexV
                                .segregatedExcessRevenue());

        assertEquals(
                0,
                new BigDecimal("100000.00").compareTo(
                        totalSegregatedExcess));

        assertEquals(
                new BigDecimal("3000.00"),
                annexIII.issAmountForDisplay());

        assertEquals(
                new BigDecimal("1999.85"),
                annexV.issAmountForDisplay());
    }

    @Test
    void shouldKeepPreviousSingleRevenueOverloadCompatible() {
        SharedContext context = createSharedContext();

        SimplesIcmsTransitionalCalculationResult result = icmsCalculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_I,
                SUBLIMIT,
                context.icmsEffect(),
                context.monthlyExcess());

        assertEquals(
                0,
                new BigDecimal("200000.00").compareTo(
                        result.segregatedRevenueAmount()));

        assertEquals(
                new BigDecimal("100000.00"),
                result
                        .segregatedExcessRevenueForDisplay());

        assertEquals(
                new BigDecimal("3978.13"),
                result.icmsAmountForDisplay());
    }

    @Test
    void shouldRejectSegregatedRevenueGreaterThanMonthlyTotal() {
        SharedContext context = createSharedContext();

        assertThrows(
                IllegalArgumentException.class,
                () -> icmsCalculator.calculate(
                        SimplesRevenueTaxRoute.ANNEX_I,
                        SUBLIMIT,
                        context.icmsEffect(),
                        context.monthlyExcess(),
                        new BigDecimal("200000.01")));
    }

    private SharedContext createSharedContext() {
        SimplesSublimitEvaluationResult evaluation = sublimitEvaluator.evaluate(
                new BigDecimal("3700000.00"),
                SUBLIMIT);

        SimplesSublimitTemporalEffectResult temporalEffect = temporalCalculator.calculate(
                evaluation,
                LocalDate.of(
                        2020,
                        1,
                        1),
                COMPETENCE);

        SimplesSublimitMonthlyExcessResult monthlyExcess = monthlyExcessCalculator.calculate(
                evaluation,
                new BigDecimal("200000.00"));

        SimplesIcmsSublimitEffectResult icmsEffect = icmsEffectCalculator.calculate(
                temporalEffect,
                COMPETENCE);

        SimplesIssSublimitEffectResult issEffect = issEffectCalculator.calculate(
                temporalEffect,
                COMPETENCE);

        return new SharedContext(
                monthlyExcess,
                icmsEffect,
                issEffect);
    }

    private record SharedContext(
            SimplesSublimitMonthlyExcessResult monthlyExcess,
            SimplesIcmsSublimitEffectResult icmsEffect,
            SimplesIssSublimitEffectResult issEffect) {
    }
}