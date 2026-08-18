package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideStructureAuditorTest {

    private final FatorRCalculator fatorRCalculator = new FatorRCalculator();

    private final TaxBracketRevenueBasisCalculator revenueCalculator = new TaxBracketRevenueBasisCalculator();

    private final SimplesTaxBracketSelector bracketSelector = new SimplesTaxBracketSelector();

    private final SimplesEffectiveRateCalculator effectiveRateCalculator = new SimplesEffectiveRateCalculator();

    private final SimplesEstimatedTaxCalculator taxCalculator = new SimplesEstimatedTaxCalculator();

    private final GuideStructureAuditor auditor = new GuideStructureAuditor();

    @Test
    void shouldIdentifyFullyCompatibleStructure() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideStructureAuditResult result = auditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.30")),
                        Optional.of(
                                SimplesAnnex.ANEXO_III),
                        Optional.of(3),
                        Optional.of(
                                new BigDecimal("0.09972"))));

        assertEquals(
                GuideStructureAuditStatus.COMPATIBLE,
                result.status());

        assertEquals(
                GuideStructureAuditSeverity.NONE,
                result.severity());

        assertTrue(
                result.findings().isEmpty());

        assertFalse(
                result.hasDivergences());
    }

    @Test
    void shouldIdentifyFatorRMismatch() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideStructureAuditResult result = auditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.25")),
                        Optional.of(
                                SimplesAnnex.ANEXO_III),
                        Optional.of(3),
                        Optional.of(
                                new BigDecimal("0.09972"))));

        assertEquals(
                GuideStructureAuditStatus.DIVERGENT,
                result.status());

        assertEquals(
                GuideStructureAuditSeverity.HIGH,
                result.severity());

        assertTrue(
                result.findings()
                        .stream()
                        .anyMatch(finding -> finding.code().equals(
                                "FATOR_R_MISMATCH")));
    }

    @Test
    void shouldIdentifyAnnexMismatch() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideStructureAuditResult result = auditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.30")),
                        Optional.of(
                                SimplesAnnex.ANEXO_V),
                        Optional.of(3),
                        Optional.of(
                                new BigDecimal("0.09972"))));

        assertEquals(
                GuideStructureAuditStatus.DIVERGENT,
                result.status());

        assertEquals(
                GuideStructureAuditSeverity.HIGH,
                result.severity());

        assertTrue(
                result.findings()
                        .stream()
                        .anyMatch(finding -> finding.code().equals(
                                "ANNEX_MISMATCH")));
    }

    @Test
    void shouldIdentifyBracketMismatch() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideStructureAuditResult result = auditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.30")),
                        Optional.of(
                                SimplesAnnex.ANEXO_III),
                        Optional.of(4),
                        Optional.of(
                                new BigDecimal("0.09972"))));

        assertEquals(
                GuideStructureAuditStatus.DIVERGENT,
                result.status());

        assertEquals(
                GuideStructureAuditSeverity.HIGH,
                result.severity());

        assertTrue(
                result.findings()
                        .stream()
                        .anyMatch(finding -> finding.code().equals(
                                "BRACKET_MISMATCH")));
    }

    @Test
    void shouldIdentifyEffectiveRateMismatch() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideStructureAuditResult result = auditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.30")),
                        Optional.of(
                                SimplesAnnex.ANEXO_III),
                        Optional.of(3),
                        Optional.of(
                                new BigDecimal("0.10000"))));

        assertEquals(
                GuideStructureAuditStatus.DIVERGENT,
                result.status());

        assertEquals(
                GuideStructureAuditSeverity.MEDIUM,
                result.severity());

        assertTrue(
                result.findings()
                        .stream()
                        .anyMatch(finding -> finding.code().equals(
                                "EFFECTIVE_RATE_MISMATCH")));
    }

    @Test
    void shouldRespectEffectiveRateTolerance() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideStructureAuditResult result = auditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.30")),
                        Optional.of(
                                SimplesAnnex.ANEXO_III),
                        Optional.of(3),
                        Optional.of(
                                new BigDecimal("0.10000")),
                        new BigDecimal("0.00100")));

        assertEquals(
                GuideStructureAuditStatus.COMPATIBLE,
                result.status());

        assertTrue(
                result.findings().isEmpty());
    }

    @Test
    void shouldReturnPartialCompatibilityWhenSomeFieldsAreMissing() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideStructureAuditResult result = auditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.empty(),
                        Optional.of(
                                SimplesAnnex.ANEXO_III),
                        Optional.empty(),
                        Optional.empty()));

        assertEquals(
                GuideStructureAuditStatus.PARTIALLY_COMPATIBLE,
                result.status());

        assertEquals(
                GuideStructureAuditSeverity.LOW,
                result.severity());

        assertEquals(
                3,
                result.findings().size());
    }

    @Test
    void shouldReturnInsufficientDataWhenNothingWasReported() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideStructureAuditResult result = auditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertEquals(
                GuideStructureAuditStatus.INSUFFICIENT_DATA,
                result.status());

        assertEquals(
                GuideStructureAuditSeverity.LOW,
                result.severity());

        assertEquals(
                4,
                result.findings().size());
    }

    @Test
    void shouldIdentifyMultipleStructuralDivergences() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideStructureAuditResult result = auditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.25")),
                        Optional.of(
                                SimplesAnnex.ANEXO_V),
                        Optional.of(3),
                        Optional.of(
                                new BigDecimal("0.1752"))));

        assertEquals(
                GuideStructureAuditStatus.DIVERGENT,
                result.status());

        assertEquals(
                GuideStructureAuditSeverity.HIGH,
                result.severity());

        assertEquals(
                3,
                result.findings().size());

        assertTrue(
                result.decision()
                        .condition()
                        .contains("FATOR_R_MISMATCH"));

        assertTrue(
                result.decision()
                        .condition()
                        .contains("ANNEX_MISMATCH"));

        assertTrue(
                result.decision()
                        .condition()
                        .contains("EFFECTIVE_RATE_MISMATCH"));
    }

    private SimplesEstimatedTaxResult createEstimatedResult() {
        BigDecimal revenueBasis = new BigDecimal("500000.00");

        FatorRCalculationResult fatorRResult = fatorRCalculator.calculate(
                new BigDecimal("150000.00"),
                revenueBasis);

        TaxBracketRevenueBasisResult revenueResult = revenueCalculator.calculate(
                new TaxBracketRevenueBasisRequest(
                        LocalDate.of(2024, 1, 10),
                        YearMonth.of(2026, 1),
                        createTwelveMonthHistory(
                                revenueBasis)));

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
                                "10000.00"),
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