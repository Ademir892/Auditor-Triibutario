package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideAmountAuditorTest {

    private final FatorRCalculator fatorRCalculator = new FatorRCalculator();

    private final TaxBracketRevenueBasisCalculator revenueCalculator = new TaxBracketRevenueBasisCalculator();

    private final SimplesTaxBracketSelector bracketSelector = new SimplesTaxBracketSelector();

    private final SimplesEffectiveRateCalculator effectiveRateCalculator = new SimplesEffectiveRateCalculator();

    private final SimplesEstimatedTaxCalculator taxCalculator = new SimplesEstimatedTaxCalculator();

    private final GuideAmountAuditor auditor = new GuideAmountAuditor();

    @Test
    void shouldIdentifyExactMatch() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult(
                "150000.00",
                "500000.00",
                "10000.00");

        GuideAmountAuditResult result = auditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("997.20")));

        assertEquals(
                GuideAmountAuditStatus.EXACT_MATCH,
                result.status());

        assertEquals(
                new BigDecimal("0.00"),
                result.absoluteDifference());

        assertFalse(
                result.guideIsHigherThanExpected());

        assertFalse(
                result.guideIsLowerThanExpected());
    }

    @Test
    void shouldIdentifyDifferenceWithinTolerance() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult(
                "150000.00",
                "500000.00",
                "10000.00");

        GuideAmountAuditResult result = auditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("997.23")));

        assertEquals(
                GuideAmountAuditStatus.WITHIN_TOLERANCE,
                result.status());

        assertEquals(
                new BigDecimal("0.03"),
                result.absoluteDifference());

        assertTrue(
                result.guideIsHigherThanExpected());
    }

    @Test
    void shouldIdentifyDivergentGuideAboveExpectedAmount() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult(
                "150000.00",
                "500000.00",
                "10000.00");

        GuideAmountAuditResult result = auditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("1752.00")));

        assertEquals(
                GuideAmountAuditStatus.DIVERGENT,
                result.status());

        assertEquals(
                new BigDecimal("754.80"),
                result.signedDifference());

        assertEquals(
                new BigDecimal("754.80"),
                result.absoluteDifference());

        assertTrue(
                result.guideIsHigherThanExpected());

        assertFalse(
                result.guideIsLowerThanExpected());

        assertTrue(
                result.percentageDifference().isPresent());
    }

    @Test
    void shouldIdentifyDivergentGuideBelowExpectedAmount() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult(
                "125000.00",
                "500000.00",
                "10000.00");

        GuideAmountAuditResult result = auditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("997.20")));

        assertEquals(
                GuideAmountAuditStatus.DIVERGENT,
                result.status());

        assertEquals(
                new BigDecimal("-754.80"),
                result.signedDifference());

        assertEquals(
                new BigDecimal("754.80"),
                result.absoluteDifference());

        assertTrue(
                result.guideIsLowerThanExpected());
    }

    @Test
    void shouldUseCustomTolerance() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult(
                "150000.00",
                "500000.00",
                "10000.00");

        GuideAmountAuditResult result = auditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("997.28"),
                        new BigDecimal("0.10")));

        assertEquals(
                GuideAmountAuditStatus.WITHIN_TOLERANCE,
                result.status());

        assertEquals(
                new BigDecimal("0.08"),
                result.absoluteDifference());
    }

    @Test
    void shouldRequireAdditionalContextForDeferredAmount() {
        SimplesEstimatedTaxResult estimatedResult = createOpeningMonthEstimatedResult(
                "30.00",
                "100.00");

        GuideAmountAuditResult result = auditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("15.00")));

        assertEquals(
                SimplesEstimatedTaxStatus.DEFERRED_BELOW_MINIMUM,
                estimatedResult.status());

        assertEquals(
                GuideAmountAuditStatus.REQUIRES_ADDITIONAL_CONTEXT,
                result.status());

        assertTrue(
                result.decision()
                        .condition()
                        .contains("competências anteriores"));
    }

    @Test
    void shouldHandleZeroExpectedAmountWithoutPercentageDivision() {
        SimplesEstimatedTaxResult estimatedResult = createOpeningMonthEstimatedResult(
                "10.00",
                "0.00");

        GuideAmountAuditResult result = auditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("0.00")));

        assertEquals(
                GuideAmountAuditStatus.EXACT_MATCH,
                result.status());

        assertTrue(
                result.percentageDifference().isEmpty());
    }

    @Test
    void shouldCreateAuditableDecision() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult(
                "150000.00",
                "500000.00",
                "10000.00");

        GuideAmountAuditResult result = auditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("1752.00")));

        assertEquals(
                "SIMPLES_GUIDE_AMOUNT_AUDIT",
                result.decision()
                        .ruleCode());

        assertTrue(
                result.decision()
                        .input()
                        .contains("997.20"));

        assertTrue(
                result.decision()
                        .input()
                        .contains("1752.00"));

        assertTrue(
                result.decision()
                        .result()
                        .contains("754.80"));

        assertTrue(
                result.decision()
                        .condition()
                        .contains("superior"));
    }

    @Test
    void shouldRejectNegativeGuideAmount() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult(
                "150000.00",
                "500000.00",
                "10000.00");

        assertThrows(
                IllegalArgumentException.class,
                () -> new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("-1.00")));
    }

    @Test
    void shouldRejectNegativeTolerance() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult(
                "150000.00",
                "500000.00",
                "10000.00");

        assertThrows(
                IllegalArgumentException.class,
                () -> new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("997.20"),
                        new BigDecimal("-0.01")));
    }

    @Test
    void shouldRejectNullAuditRequest() {
        assertThrows(
                NullPointerException.class,
                () -> auditor.audit(null));
    }

    private SimplesEstimatedTaxResult createEstimatedResult(
            String payroll,
            String revenueBasis,
            String currentRevenue) {
        FatorRCalculationResult fatorRResult = fatorRCalculator.calculate(
                new BigDecimal(payroll),
                new BigDecimal(revenueBasis));

        TaxBracketRevenueBasisResult revenueResult = revenueCalculator.calculate(
                new TaxBracketRevenueBasisRequest(
                        LocalDate.of(2024, 1, 10),
                        YearMonth.of(2026, 1),
                        createTwelveMonthHistory(
                                new BigDecimal(revenueBasis))));

        SimplesTaxBracketSelectionResult selectionResult = bracketSelector.select(
                new SimplesTaxBracketSelectionRequest(
                        YearMonth.of(2026, 1),
                        fatorRResult,
                        revenueResult));

        SimplesEffectiveRateResult effectiveRateResult = effectiveRateCalculator.calculate(
                selectionResult);

        return taxCalculator.calculate(
                new SimplesEstimatedTaxRequest(
                        revenue(
                                2026,
                                1,
                                currentRevenue),
                        effectiveRateResult));
    }

    private SimplesEstimatedTaxResult createOpeningMonthEstimatedResult(
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

        SimplesTaxBracketSelectionResult selectionResult = bracketSelector.select(
                new SimplesTaxBracketSelectionRequest(
                        YearMonth.of(2026, 2),
                        fatorRResult,
                        revenueResult));

        SimplesEffectiveRateResult effectiveRateResult = effectiveRateCalculator.calculate(
                selectionResult);

        return taxCalculator.calculate(
                new SimplesEstimatedTaxRequest(
                        revenue(
                                2026,
                                2,
                                revenue),
                        effectiveRateResult));
    }

    private List<MonthlyRevenue> createTwelveMonthHistory(
            BigDecimal totalRevenue) {
        BigDecimal monthlyRevenue = totalRevenue.divide(
                new BigDecimal("12"),
                2,
                RoundingMode.DOWN);

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