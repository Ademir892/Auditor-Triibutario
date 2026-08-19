package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class AuditReportGenerator {

    private static final String REPORT_TITLE = "Relatório de Auditoria Tributária";

    public AuditReport generate(
            AuditReportRequest request) {
        Objects.requireNonNull(
                request,
                "A requisição do relatório não pode ser nula.");

        ConsolidatedAuditResult consolidated = request.consolidatedAuditResult();

        SimplesEstimatedTaxResult estimated = consolidated
                .structureAuditResult()
                .estimatedTaxResult();

        SimplesEffectiveRateResult effectiveRate = estimated.effectiveRateResult();

        SimplesTaxBracketSelectionResult bracketSelection = effectiveRate.bracketSelectionResult();

        FatorRCalculationResult fatorR = bracketSelection.fatorRResult();

        TaxBracketRevenueBasisResult revenueBasis = bracketSelection.revenueBasisResult();

        GuideAmountAuditResult amountAudit = consolidated.amountAuditResult();

        YearMonth assessmentPeriod = estimated
                .taxableRevenue()
                .period();

        List<AuditReportSection> sections = new ArrayList<>();

        sections.add(
                createSummarySection(
                        consolidated,
                        amountAudit));

        sections.add(
                createTaxFrameworkSection(
                        fatorR,
                        revenueBasis,
                        bracketSelection,
                        effectiveRate,
                        estimated));

        sections.add(
                createAmountComparisonSection(
                        amountAudit));

        sections.add(
                createFindingsSection(
                        consolidated));

        sections.add(
                createRecommendationsSection(
                        consolidated));

        sections.add(
                createTraceabilitySection(
                        fatorR,
                        revenueBasis,
                        bracketSelection,
                        effectiveRate,
                        estimated,
                        consolidated));

        List<String> references = collectReferences(
                fatorR,
                revenueBasis,
                bracketSelection,
                effectiveRate,
                estimated,
                consolidated);

        return new AuditReport(
                REPORT_TITLE,
                assessmentPeriod,
                consolidated.executiveSummary(),
                sections,
                references);
    }

    private AuditReportSection createSummarySection(
            ConsolidatedAuditResult consolidated,
            GuideAmountAuditResult amountAudit) {
        String content = "Status: "
                + consolidated
                        .status()
                        .getDisplayName()
                + "\n"
                + "Severidade: "
                + consolidated
                        .severity()
                        .getDisplayName()
                + "\n"
                + "Principal hipótese: "
                + consolidated
                        .principalCause()
                        .getDisplayName()
                + "\n"
                + "Valor esperado: "
                + AuditReportValueFormatter
                        .formatMoney(
                                amountAudit.expectedAmount())
                + "\n"
                + "Valor informado: "
                + AuditReportValueFormatter
                        .formatMoney(
                                amountAudit.guideAmount())
                + "\n"
                + "Diferença: "
                + AuditReportValueFormatter
                        .formatMoney(
                                amountAudit.absoluteDifference());

        return new AuditReportSection(
                "SUMMARY",
                "Resumo da auditoria",
                content);
    }

    private AuditReportSection createTaxFrameworkSection(
            FatorRCalculationResult fatorR,
            TaxBracketRevenueBasisResult revenueBasis,
            SimplesTaxBracketSelectionResult bracketSelection,
            SimplesEffectiveRateResult effectiveRate,
            SimplesEstimatedTaxResult estimated) {
        SimplesTaxBracket bracket = bracketSelection.bracket();

        String content = "Base temporal do Fator R: "
                + fatorR
                        .calculationBasis()
                        .getDisplayName()
                + "\n"
                + "Fator R: "
                + AuditReportValueFormatter
                        .formatPercentage(
                                fatorR
                                        .fatorR()
                                        .value())
                + "\n"
                + "Anexo: "
                + fatorR
                        .annex()
                        .getDisplayName()
                + "\n"
                + "Base de receita: "
                + revenueBasis
                        .basisType()
                        .getCode()
                + "\n"
                + "Receita para enquadramento: "
                + AuditReportValueFormatter
                        .formatMoney(
                                revenueBasis
                                        .revenueBasis())
                + "\n"
                + "Faixa: "
                + bracket.number()
                + "\n"
                + "Alíquota nominal: "
                + AuditReportValueFormatter
                        .formatPercentage(
                                bracket.nominalRate())
                + "\n"
                + "Parcela a deduzir: "
                + AuditReportValueFormatter
                        .formatMoney(
                                bracket.deduction())
                + "\n"
                + "Alíquota efetiva: "
                + AuditReportValueFormatter
                        .formatPercentage(
                                effectiveRate
                                        .effectiveRate())
                + "\n"
                + "Receita tributável da competência: "
                + AuditReportValueFormatter
                        .formatMoney(
                                estimated
                                        .taxableRevenue()
                                        .amount())
                + "\n"
                + "Valor estimado: "
                + AuditReportValueFormatter
                        .formatMoney(
                                estimated
                                        .estimatedTaxAmount());

        return new AuditReportSection(
                "TAX_FRAMEWORK",
                "Memória de enquadramento",
                content);
    }

    private AuditReportSection createAmountComparisonSection(
            GuideAmountAuditResult amountAudit) {
        String direction = determineAmountDirection(
                amountAudit);

        String percentage = amountAudit
                .percentageDifferenceForDisplay()
                .map(
                        value -> value
                                .toPlainString()
                                .replace(
                                        '.',
                                        ',')
                                + "%")
                .orElse(
                        "Não disponível");

        String content = "Status: "
                + amountAudit
                        .status()
                        .getDisplayName()
                + "\n"
                + "Valor esperado: "
                + AuditReportValueFormatter
                        .formatMoney(
                                amountAudit.expectedAmount())
                + "\n"
                + "Valor informado: "
                + AuditReportValueFormatter
                        .formatMoney(
                                amountAudit.guideAmount())
                + "\n"
                + "Diferença absoluta: "
                + AuditReportValueFormatter
                        .formatMoney(
                                amountAudit.absoluteDifference())
                + "\n"
                + "Diferença percentual: "
                + percentage
                + "\n"
                + "Direção: "
                + direction
                + "\n"
                + "Tolerância técnica: "
                + AuditReportValueFormatter
                        .formatMoney(
                                amountAudit.tolerance());

        return new AuditReportSection(
                "AMOUNT_COMPARISON",
                "Comparação de valores",
                content);
    }

    private String determineAmountDirection(
            GuideAmountAuditResult amountAudit) {
        if (amountAudit.guideIsHigherThanExpected()) {
            return "Valor informado acima do esperado";
        }

        if (amountAudit.guideIsLowerThanExpected()) {
            return "Valor informado abaixo do esperado";
        }

        return "Valores equivalentes";
    }

    private AuditReportSection createFindingsSection(
            ConsolidatedAuditResult consolidated) {
        if (consolidated.findings().isEmpty()) {
            return new AuditReportSection(
                    "FINDINGS",
                    "Achados da auditoria",
                    "Nenhum achado relevante foi identificado.");
        }

        String content = consolidated
                .findings()
                .stream()
                .map(
                        finding -> formatFinding(
                                finding))
                .collect(
                        Collectors.joining(
                                "\n\n"));

        return new AuditReportSection(
                "FINDINGS",
                "Achados da auditoria",
                content);
    }

    private String formatFinding(
            ConsolidatedAuditFinding finding) {
        return "Código: "
                + finding.code()
                + "\n"
                + "Categoria: "
                + finding.category()
                + "\n"
                + "Severidade: "
                + finding
                        .severity()
                        .getDisplayName()
                + "\n"
                + "Achado: "
                + finding.message()
                + "\n"
                + "Recomendação: "
                + finding.recommendation();
    }

    private AuditReportSection createRecommendationsSection(
            ConsolidatedAuditResult consolidated) {
        if (consolidated.recommendedChecks().isEmpty()) {
            return new AuditReportSection(
                    "RECOMMENDATIONS",
                    "Verificações recomendadas",
                    "Nenhuma verificação adicional foi recomendada.");
        }

        StringBuilder content = new StringBuilder();

        for (int index = 0; index < consolidated.recommendedChecks().size(); index++) {

            if (index > 0) {
                content.append('\n');
            }

            content.append(
                    index + 1);

            content.append(". ");

            content.append(
                    consolidated
                            .recommendedChecks()
                            .get(index));
        }

        return new AuditReportSection(
                "RECOMMENDATIONS",
                "Verificações recomendadas",
                content.toString());
    }

    private AuditReportSection createTraceabilitySection(
            FatorRCalculationResult fatorR,
            TaxBracketRevenueBasisResult revenueBasis,
            SimplesTaxBracketSelectionResult bracketSelection,
            SimplesEffectiveRateResult effectiveRate,
            SimplesEstimatedTaxResult estimated,
            ConsolidatedAuditResult consolidated) {
        List<TaxDecision> decisions = collectDecisions(
                fatorR,
                revenueBasis,
                bracketSelection,
                effectiveRate,
                estimated,
                consolidated);

        String content = decisions
                .stream()
                .map(
                        decision -> decision.ruleCode()
                                + " | versão "
                                + decision.ruleVersion()
                                + " | "
                                + decision.description())
                .collect(
                        Collectors.joining(
                                "\n"));

        return new AuditReportSection(
                "TRACEABILITY",
                "Rastreabilidade das decisões",
                content);
    }

    private List<String> collectReferences(
            FatorRCalculationResult fatorR,
            TaxBracketRevenueBasisResult revenueBasis,
            SimplesTaxBracketSelectionResult bracketSelection,
            SimplesEffectiveRateResult effectiveRate,
            SimplesEstimatedTaxResult estimated,
            ConsolidatedAuditResult consolidated) {
        Set<String> references = new LinkedHashSet<>();

        for (TaxDecision decision : collectDecisions(
                fatorR,
                revenueBasis,
                bracketSelection,
                effectiveRate,
                estimated,
                consolidated)) {

            if (decision.legalReference() != null
                    && !decision.legalReference().isBlank()) {

                references.add(
                        decision.legalReference());
            }
        }

        return List.copyOf(
                references);
    }

    private List<TaxDecision> collectDecisions(
            FatorRCalculationResult fatorR,
            TaxBracketRevenueBasisResult revenueBasis,
            SimplesTaxBracketSelectionResult bracketSelection,
            SimplesEffectiveRateResult effectiveRate,
            SimplesEstimatedTaxResult estimated,
            ConsolidatedAuditResult consolidated) {
        return List.of(
                fatorR.decision(),
                revenueBasis.decision(),
                bracketSelection.decision(),
                effectiveRate.decision(),
                estimated.decision(),
                consolidated
                        .amountAuditResult()
                        .decision(),
                consolidated
                        .structureAuditResult()
                        .decision(),
                consolidated.decision());
    }
}