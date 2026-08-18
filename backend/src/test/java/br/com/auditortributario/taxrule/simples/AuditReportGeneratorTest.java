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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditReportGeneratorTest {

    private final FatorRCalculator fatorRCalculator = new FatorRCalculator();

    private final TaxBracketRevenueBasisCalculator revenueCalculator = new TaxBracketRevenueBasisCalculator();

    private final SimplesTaxBracketSelector bracketSelector = new SimplesTaxBracketSelector();

    private final SimplesEffectiveRateCalculator effectiveRateCalculator = new SimplesEffectiveRateCalculator();

    private final SimplesEstimatedTaxCalculator taxCalculator = new SimplesEstimatedTaxCalculator();

    private final GuideAmountAuditor amountAuditor = new GuideAmountAuditor();

    private final GuideStructureAuditor structureAuditor = new GuideStructureAuditor();

    private final ConsolidatedAuditor consolidatedAuditor = new ConsolidatedAuditor();

    private final AuditReportGenerator reportGenerator = new AuditReportGenerator();

    private final AuditReportMarkdownRenderer markdownRenderer = new AuditReportMarkdownRenderer();

    @Test
    void shouldGenerateCompleteAuditReport() {
        ConsolidatedAuditResult consolidated = createDivergentAudit();

        AuditReport report = reportGenerator.generate(
                new AuditReportRequest(
                        consolidated));

        assertEquals(
                "Relatório de Auditoria Tributária",
                report.title());

        assertEquals(
                YearMonth.of(
                        2026,
                        1),
                report.assessmentPeriod());

        assertEquals(
                6,
                report.sections().size());

        assertTrue(
                report
                        .findSection("SUMMARY")
                        .isPresent());

        assertTrue(
                report
                        .findSection("TAX_FRAMEWORK")
                        .isPresent());

        assertTrue(
                report
                        .findSection("AMOUNT_COMPARISON")
                        .isPresent());

        assertTrue(
                report
                        .findSection("FINDINGS")
                        .isPresent());

        assertTrue(
                report
                        .findSection("RECOMMENDATIONS")
                        .isPresent());

        assertTrue(
                report
                        .findSection("TRACEABILITY")
                        .isPresent());

        assertFalse(
                report.references().isEmpty());
    }

    @Test
    void shouldIncludeTaxFrameworkMemory() {
        AuditReport report = reportGenerator.generate(
                new AuditReportRequest(
                        createDivergentAudit()));

        String framework = report
                .findSection(
                        "TAX_FRAMEWORK")
                .orElseThrow()
                .content();

        assertTrue(
                framework.contains(
                        "Fator R: 30%"));

        assertTrue(
                framework.contains(
                        "Anexo: Anexo III"));

        assertTrue(
                framework.contains(
                        "Receita para enquadramento: R$ 500.000,00"));

        assertTrue(
                framework.contains(
                        "Faixa: 3"));

        assertTrue(
                framework.contains(
                        "Alíquota nominal: 13,5%"));

        assertTrue(
                framework.contains(
                        "Parcela a deduzir: R$ 17.640,00"));

        assertTrue(
                framework.contains(
                        "Alíquota efetiva: 9,972%"));

        assertTrue(
                framework.contains(
                        "Valor estimado: R$ 997,20"));
    }

    @Test
    void shouldIncludePrioritizedFindings() {
        AuditReport report = reportGenerator.generate(
                new AuditReportRequest(
                        createDivergentAudit()));

        String findings = report
                .findSection(
                        "FINDINGS")
                .orElseThrow()
                .content();

        assertTrue(
                findings.contains(
                        "FATOR_R_MISMATCH"));

        assertTrue(
                findings.contains(
                        "ANNEX_MISMATCH"));

        assertTrue(
                findings.contains(
                        "AMOUNT_DIVERGENCE"));

        assertTrue(
                findings.contains(
                        "EFFECTIVE_RATE_MISMATCH"));
    }

    @Test
    void shouldRenderReportAsMarkdown() {
        AuditReport report = reportGenerator.generate(
                new AuditReportRequest(
                        createDivergentAudit()));

        String markdown = markdownRenderer.render(
                report);

        assertTrue(
                markdown.startsWith(
                        "# Relatório de Auditoria Tributária"));

        assertTrue(
                markdown.contains(
                        "**Competência:** 01/2026"));

        assertTrue(
                markdown.contains(
                        "## Resumo da auditoria"));

        assertTrue(
                markdown.contains(
                        "## Memória de enquadramento"));

        assertTrue(
                markdown.contains(
                        "## Achados da auditoria"));

        assertTrue(
                markdown.contains(
                        "## Verificações recomendadas"));

        assertTrue(
                markdown.contains(
                        "## Referências e critérios"));

        assertTrue(
                markdown.contains(
                        "R$ 754,80"));
    }

    @Test
    void shouldGenerateCompatibleReportWithoutFindings() {
        SimplesEstimatedTaxResult estimatedResult = createEstimatedResult();

        GuideAmountAuditResult amountAudit = amountAuditor.audit(
                new GuideAmountAuditRequest(
                        estimatedResult,
                        new BigDecimal("997.20")));

        GuideStructureAuditResult structureAudit = structureAuditor.audit(
                new GuideStructureAuditRequest(
                        estimatedResult,
                        Optional.of(
                                new BigDecimal("0.30")),
                        Optional.of(
                                SimplesAnnex.ANEXO_III),
                        Optional.of(3),
                        Optional.of(
                                new BigDecimal("0.09972"))));

        ConsolidatedAuditResult consolidated = consolidatedAuditor.audit(
                new ConsolidatedAuditRequest(
                        amountAudit,
                        structureAudit));

        AuditReport report = reportGenerator.generate(
                new AuditReportRequest(
                        consolidated));

        String findings = report
                .findSection(
                        "FINDINGS")
                .orElseThrow()
                .content();

        assertEquals(
                "Nenhum achado relevante foi identificado.",
                findings);

        assertEquals(
                ConsolidatedAuditStatus.COMPATIBLE,
                consolidated.status());
    }

    @Test
    void shouldFormatValuesDeterministically() {
        assertEquals(
                "R$ 1.752,00",
                AuditReportValueFormatter
                        .formatMoney(
                                new BigDecimal("1752.00")));

        assertEquals(
                "R$ 500.000,00",
                AuditReportValueFormatter
                        .formatMoney(
                                new BigDecimal("500000.00")));

        assertEquals(
                "9,972%",
                AuditReportValueFormatter
                        .formatPercentage(
                                new BigDecimal("0.09972")));

        assertEquals(
                "01/2026",
                AuditReportValueFormatter
                        .formatPeriod(
                                YearMonth.of(
                                        2026,
                                        1)));
    }

    @Test
    void shouldRejectNullReportRequest() {
        assertThrows(
                NullPointerException.class,
                () -> reportGenerator.generate(null));
    }

    @Test
    void shouldRejectNullReportInMarkdownRenderer() {
        assertThrows(
                NullPointerException.class,
                () -> markdownRenderer.render(null));
    }

    private ConsolidatedAuditResult createDivergentAudit() {
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

        return consolidatedAuditor.audit(
                new ConsolidatedAuditRequest(
                        amountAudit,
                        structureAudit));
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