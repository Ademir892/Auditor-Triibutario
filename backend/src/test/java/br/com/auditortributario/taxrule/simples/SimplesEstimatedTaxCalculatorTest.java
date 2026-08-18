package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesEstimatedTaxCalculatorTest {

    private final FatorRCalculator fatorRCalculator = new FatorRCalculator();

    private final TaxBracketRevenueBasisCalculator revenueCalculator = new TaxBracketRevenueBasisCalculator();

    private final SimplesTaxBracketSelector bracketSelector = new SimplesTaxBracketSelector();

    private final SimplesEffectiveRateCalculator effectiveRateCalculator = new SimplesEffectiveRateCalculator();

    private final SimplesEstimatedTaxCalculator taxCalculator = new SimplesEstimatedTaxCalculator();

    @Test
    void shouldCalculateEstimatedTaxForAnnexIII() {
        SimplesEffectiveRateResult effectiveRateResult = createStandardEffectiveRate(
                "150000.00",
                "500000.00");

        SimplesEstimatedTaxResult result = taxCalculator.calculate(
                new SimplesEstimatedTaxRequest(
                        revenue(
                                2026,
                                1,
                                "10000.00"),
                        effectiveRateResult));

        assertEquals(
                SimplesAnnex.ANEXO_III,
                result.effectiveRateResult()
                        .bracketSelectionResult()
                        .fatorRResult()
                        .annex());

        assertEquals(
                new BigDecimal("0.09972"),
                result.effectiveRateResult()
                        .effectiveRate());

        assertEquals(
                new BigDecimal("997.2000000"),
                result.rawTaxAmount());

        assertEquals(
                new BigDecimal("997.20"),
                result.estimatedTaxAmount());

        assertEquals(
                SimplesEstimatedTaxStatus.PAYABLE,
                result.status());
    }

    @Test
    void shouldCalculateEstimatedTaxForAnnexV() {
        SimplesEffectiveRateResult effectiveRateResult = createStandardEffectiveRate(
                "125000.00",
                "500000.00");

        SimplesEstimatedTaxResult result = taxCalculator.calculate(
                new SimplesEstimatedTaxRequest(
                        revenue(
                                2026,
                                1,
                                "10000.00"),
                        effectiveRateResult));

        assertEquals(
                SimplesAnnex.ANEXO_V,
                result.effectiveRateResult()
                        .bracketSelectionResult()
                        .fatorRResult()
                        .annex());

        assertEquals(
                new BigDecimal("0.1752"),
                result.effectiveRateResult()
                        .effectiveRate());

        assertEquals(
                new BigDecimal("1752.000000"),
                result.rawTaxAmount());

        assertEquals(
                new BigDecimal("1752.00"),
                result.estimatedTaxAmount());

        assertEquals(
                SimplesEstimatedTaxStatus.PAYABLE,
                result.status());
    }

    @Test
    void shouldPreserveRawCalculationBeforeMoneyRounding() {
        SimplesEffectiveRateResult effectiveRateResult = createStandardEffectiveRate(
                "150000.00",
                "500000.00");

        SimplesEstimatedTaxResult result = taxCalculator.calculate(
                new SimplesEstimatedTaxRequest(
                        revenue(
                                2026,
                                1,
                                "12345.67"),
                        effectiveRateResult));

        assertEquals(
                new BigDecimal("1231.1102124"),
                result.rawTaxAmount());

        assertEquals(
                new BigDecimal("1231.11"),
                result.estimatedTaxAmount());
    }

    @Test
    void shouldDeferAmountBelowMinimumDasValue() {
        SimplesEffectiveRateResult effectiveRateResult = createOpeningMonthEffectiveRate(
                "30.00",
                "100.00");

        SimplesEstimatedTaxResult result = taxCalculator.calculate(
                new SimplesEstimatedTaxRequest(
                        revenue(
                                2026,
                                2,
                                "100.00"),
                        effectiveRateResult));

        assertEquals(
                new BigDecimal("0.06"),
                result.effectiveRateResult()
                        .effectiveRate());

        assertEquals(
                new BigDecimal("6.00"),
                result.estimatedTaxAmount());

        assertEquals(
                SimplesEstimatedTaxStatus.DEFERRED_BELOW_MINIMUM,
                result.status());

        assertTrue(
                result.decision()
                        .condition()
                        .contains("inferior"));

        assertTrue(
                result.decision()
                        .condition()
                        .contains("10.00"));
    }

    @Test
    void shouldReturnNoTaxDueWhenCurrentRevenueIsZero() {
        SimplesEffectiveRateResult effectiveRateResult = createOpeningMonthEffectiveRate(
                "10.00",
                "0.00");

        SimplesEstimatedTaxResult result = taxCalculator.calculate(
                new SimplesEstimatedTaxRequest(
                        revenue(
                                2026,
                                2,
                                "0.00"),
                        effectiveRateResult));

        assertEquals(
                BigDecimal.ZERO.setScale(2),
                result.estimatedTaxAmount());

        assertEquals(
                SimplesEstimatedTaxStatus.NO_TAX_DUE,
                result.status());
    }

    @Test
    void shouldRejectRevenueFromDifferentAssessmentPeriod() {
        SimplesEffectiveRateResult effectiveRateResult = createStandardEffectiveRate(
                "150000.00",
                "500000.00");

        SimplesEstimatedTaxRequest request = new SimplesEstimatedTaxRequest(
                revenue(
                        2026,
                        2,
                        "10000.00"),
                effectiveRateResult);

        assertThrows(
                IllegalArgumentException.class,
                () -> taxCalculator.calculate(request));
    }

    @Test
    void shouldCreateAuditableEstimatedTaxDecision() {
        SimplesEffectiveRateResult effectiveRateResult = createStandardEffectiveRate(
                "150000.00",
                "500000.00");

        SimplesEstimatedTaxResult result = taxCalculator.calculate(
                new SimplesEstimatedTaxRequest(
                        revenue(
                                2026,
                                1,
                                "10000.00"),
                        effectiveRateResult));

        assertEquals(
                "SIMPLES_ESTIMATED_TAX_AMOUNT",
                result.decision()
                        .ruleCode());

        assertTrue(
                result.decision()
                        .input()
                        .contains("10000.00"));

        assertTrue(
                result.decision()
                        .input()
                        .contains("0.09972"));

        assertTrue(
                result.decision()
                        .input()
                        .contains("Anexo III"));

        assertTrue(
                result.decision()
                        .result()
                        .contains("997.20"));
    }

    @Test
    void shouldRejectNullRequest() {
        assertThrows(
                NullPointerException.class,
                () -> taxCalculator.calculate(null));
    }

    private SimplesEffectiveRateResult createOpeningMonthEffectiveRate(
            String payroll,
            String currentRevenue) {
        FatorRCalculationResult fatorRResult = fatorRCalculator.calculateOpeningMonth(
                new BigDecimal(payroll),
                new BigDecimal(currentRevenue));

        TaxBracketRevenueBasisResult revenueResult = revenueCalculator.calculate(
                new TaxBracketRevenueBasisRequest(
                        LocalDate.of(2026, 2, 10),
                        YearMonth.of(2026, 2),
                        List.of(
                                revenue(
                                        2026,
                                        2,
                                        currentRevenue))));

        SimplesTaxBracketSelectionResult selectionResult = bracketSelector.select(
                new SimplesTaxBracketSelectionRequest(
                        YearMonth.of(2026, 2),
                        fatorRResult,
                        revenueResult));

        return effectiveRateCalculator.calculate(
                selectionResult);
    }

    private SimplesEffectiveRateResult createStandardEffectiveRate(
            String payroll,
            String revenueBasis) {
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

        return effectiveRateCalculator.calculate(
                selectionResult);
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