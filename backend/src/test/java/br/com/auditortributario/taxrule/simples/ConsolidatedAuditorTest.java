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

class ConsolidatedAuditorTest {

    private final FatorRCalculator fatorRCalculator = new FatorRCalculator();

    private final TaxBracketRevenueBasisCalculator revenueCalculator = new TaxBracketRevenueBasisCalculator();

    private final SimplesTaxBracketSelector bracketSelector = new SimplesTaxBracketSelector();

    private final SimplesEffectiveRateCalculator effectiveRateCalculator = new SimplesEffectiveRateCalculator();

    private final SimplesEstimatedTaxCalculator taxCalculator = new SimplesEstimatedTaxCalculator();

    private final GuideAmountAuditor amountAuditor = new GuideAmountAuditor();

    private final GuideStructureAuditor structureAuditor = new GuideStructureAuditor();

    private final ConsolidatedAuditor consolidatedAuditor = new ConsolidatedAuditor();

    @Test
    void shouldIdentifyFullyCompatibleAudit() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideAmountAuditResult amountAudit = amountAuditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("997.20")));

        GuideStructureAuditResult structureAudit = createCompatibleStructureAudit(
                estimatedResult);

        ConsolidatedAuditResult result = consolidatedAuditor.audit(
                new ConsolidatedAuditRequest(
                        amountAudit,
                        structureAudit));

        assertEquals(
                ConsolidatedAuditStatus.COMPATIBLE,
                result.status());

        assertEquals(
                ConsolidatedAuditSeverity.NONE,
                result.severity());

        assertEquals(
                ConsolidatedAuditCause.NONE,
                result.principalCause());

        assertTrue(
                result.findings().isEmpty());

        assertFalse(
                result.hasDivergences());
    }

    @Test
    void shouldConsolidateHighSeverityFatorRAndAnnexDivergence() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideAmountAuditResult amountAudit = amountAuditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("1752.00")));

        GuideStructureAuditResult structureAudit = structureAuditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.25")),
                        Optional.of(
                                SimplesAnnex.ANEXO_V),
                        Optional.of(3),
                        Optional.of(
                                new BigDecimal("0.1752"))));

        ConsolidatedAuditResult result = consolidatedAuditor.audit(
                new ConsolidatedAuditRequest(
                        amountAudit,
                        structureAudit));

        assertEquals(
                ConsolidatedAuditStatus.DIVERGENT,
                result.status());

        assertEquals(
                ConsolidatedAuditSeverity.HIGH,
                result.severity());

        assertEquals(
                ConsolidatedAuditCause.FACTOR_R_OR_ANNEX,
                result.principalCause());

        assertTrue(
                result.hasDivergences());

        assertTrue(
                result.findings()
                        .stream()
                        .anyMatch(
                                finding -> finding.code()
                                        .equals(
                                                "AMOUNT_DIVERGENCE")));

        assertTrue(
                result.findings()
                        .stream()
                        .anyMatch(
                                finding -> finding.code()
                                        .equals(
                                                "FATOR_R_MISMATCH")));

        assertTrue(
                result.findings()
                        .stream()
                        .anyMatch(
                                finding -> finding.code()
                                        .equals(
                                                "ANNEX_MISMATCH")));
    }

    @Test
    void shouldPrioritizeHighSeverityFindings() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideAmountAuditResult amountAudit = amountAuditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("1752.00")));

        GuideStructureAuditResult structureAudit = structureAuditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.25")),
                        Optional.of(
                                SimplesAnnex.ANEXO_V),
                        Optional.of(3),
                        Optional.of(
                                new BigDecimal("0.1752"))));

        ConsolidatedAuditResult result = consolidatedAuditor.audit(
                new ConsolidatedAuditRequest(
                        amountAudit,
                        structureAudit));

        assertEquals(
                ConsolidatedAuditSeverity.HIGH,
                result.findings()
                        .get(0)
                        .severity());

        assertEquals(
                ConsolidatedAuditSeverity.HIGH,
                result.findings()
                        .get(1)
                        .severity());
    }

    @Test
    void shouldIdentifyBracketAsPrincipalCauseWhenApplicable() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideAmountAuditResult amountAudit = amountAuditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("1200.00")));

        GuideStructureAuditResult structureAudit = structureAuditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.30")),
                        Optional.of(
                                SimplesAnnex.ANEXO_III),
                        Optional.of(4),
                        Optional.of(
                                new BigDecimal("0.09972"))));

        ConsolidatedAuditResult result = consolidatedAuditor.audit(
                new ConsolidatedAuditRequest(
                        amountAudit,
                        structureAudit));

        assertEquals(
                ConsolidatedAuditCause.REVENUE_BASIS_OR_BRACKET,
                result.principalCause());

        assertEquals(
                ConsolidatedAuditSeverity.HIGH,
                result.severity());
    }

    @Test
    void shouldIdentifyAmountOnlyWhenStructureIsCompatible() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideAmountAuditResult amountAudit = amountAuditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("1100.00")));

        GuideStructureAuditResult structureAudit = createCompatibleStructureAudit(
                estimatedResult);

        ConsolidatedAuditResult result = consolidatedAuditor.audit(
                new ConsolidatedAuditRequest(
                        amountAudit,
                        structureAudit));

        assertEquals(
                ConsolidatedAuditStatus.DIVERGENT,
                result.status());

        assertEquals(
                ConsolidatedAuditSeverity.MEDIUM,
                result.severity());

        assertEquals(
                ConsolidatedAuditCause.AMOUNT_ONLY,
                result.principalCause());
    }

    @Test
    void shouldRequireReviewWhenStructuralDataIsIncomplete() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideAmountAuditResult amountAudit = amountAuditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("997.20")));

        GuideStructureAuditResult structureAudit = structureAuditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.empty(),
                        Optional.of(
                                SimplesAnnex.ANEXO_III),
                        Optional.empty(),
                        Optional.empty()));

        ConsolidatedAuditResult result = consolidatedAuditor.audit(
                new ConsolidatedAuditRequest(
                        amountAudit,
                        structureAudit));

        assertEquals(
                ConsolidatedAuditStatus.REVIEW_REQUIRED,
                result.status());

        assertEquals(
                ConsolidatedAuditSeverity.LOW,
                result.severity());

        assertEquals(
                ConsolidatedAuditCause.INSUFFICIENT_DATA,
                result.principalCause());

        assertFalse(
                result.recommendedChecks().isEmpty());
    }

    @Test
    void shouldGenerateExecutiveSummaryAndRecommendations() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideAmountAuditResult amountAudit = amountAuditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("1752.00")));

        GuideStructureAuditResult structureAudit = structureAuditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.25")),
                        Optional.of(
                                SimplesAnnex.ANEXO_V),
                        Optional.of(3),
                        Optional.of(
                                new BigDecimal("0.1752"))));

        ConsolidatedAuditResult result = consolidatedAuditor.audit(
                new ConsolidatedAuditRequest(
                        amountAudit,
                        structureAudit));

        assertTrue(
                result.executiveSummary()
                        .contains("Alta"));

        assertTrue(
                result.executiveSummary()
                        .contains("754.80"));

        assertFalse(
                result.recommendedChecks().isEmpty());

        assertEquals(
                "SIMPLES_CONSOLIDATED_AUDIT",
                result.decision()
                        .ruleCode());
    }

    private GuideStructureAuditResult createCompatibleStructureAudit(
            SimplesEstimatedTaxResult estimatedResult) {
        return structureAuditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.30")),
                        Optional.of(
                                SimplesAnnex.ANEXO_III),
                        Optional.of(3),
                        Optional.of(
                                new BigDecimal("0.09972"))));
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