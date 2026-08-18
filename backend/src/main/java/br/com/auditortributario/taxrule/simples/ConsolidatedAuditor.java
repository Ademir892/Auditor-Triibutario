package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class ConsolidatedAuditor {

    private static final String RULE_CODE = "SIMPLES_CONSOLIDATED_AUDIT";

    private static final String RULE_VERSION = "2026.1";

    private static final String INTERNAL_REFERENCE = "Consolidação interna das auditorias de valor "
            + "e estrutura tributária. As hipóteses, "
            + "severidades e recomendações são critérios "
            + "técnicos do software e não constituem "
            + "classificação fiscal oficial.";

    public ConsolidatedAuditResult audit(
            ConsolidatedAuditRequest request) {
        Objects.requireNonNull(
                request,
                "A requisição consolidada não pode ser nula.");

        List<ConsolidatedAuditFinding> findings = new ArrayList<>();

        addAmountFinding(
                request.amountAuditResult(),
                findings);

        addStructureFindings(
                request.structureAuditResult(),
                findings);

        findings.sort(
                Comparator
                        .comparingInt(
                                (ConsolidatedAuditFinding finding) -> finding
                                        .severity()
                                        .getWeight())
                        .reversed()
                        .thenComparing(
                                ConsolidatedAuditFinding::code));

        ConsolidatedAuditStatus status = determineStatus(
                request);

        ConsolidatedAuditSeverity severity = determineSeverity(
                findings);

        ConsolidatedAuditCause principalCause = determinePrincipalCause(
                request,
                findings);

        String executiveSummary = createExecutiveSummary(
                request,
                status,
                severity,
                principalCause,
                findings);

        List<String> recommendedChecks = createRecommendedChecks(
                findings);

        TaxDecision decision = createDecision(
                request,
                findings,
                status,
                severity,
                principalCause);

        return new ConsolidatedAuditResult(
                request.amountAuditResult(),
                request.structureAuditResult(),
                findings,
                status,
                severity,
                principalCause,
                executiveSummary,
                recommendedChecks,
                decision);
    }

    private void addAmountFinding(
            GuideAmountAuditResult amountAudit,
            List<ConsolidatedAuditFinding> findings) {
        switch (amountAudit.status()) {
            case EXACT_MATCH,
                    WITHIN_TOLERANCE -> {
                // Nenhum achado relevante.
            }

            case DIVERGENT -> findings.add(
                    new ConsolidatedAuditFinding(
                            "AMOUNT_DIVERGENCE",
                            ConsolidatedAuditSeverity.MEDIUM,
                            "Valor",
                            createAmountDivergenceMessage(
                                    amountAudit),
                            "Conferir receita tributável da competência, "
                                    + "alíquota efetiva aplicada e eventuais "
                                    + "valores adicionados à apuração."));

            case REQUIRES_ADDITIONAL_CONTEXT -> findings.add(
                    new ConsolidatedAuditFinding(
                            "AMOUNT_REQUIRES_CONTEXT",
                            ConsolidatedAuditSeverity.LOW,
                            "Valor",
                            "A comparação isolada do valor não é "
                                    + "conclusiva porque existem regras "
                                    + "de diferimento a considerar.",
                            "Verificar competências anteriores e valores "
                                    + "diferidos antes de concluir pela "
                                    + "existência de divergência."));
        }
    }

    private String createAmountDivergenceMessage(
            GuideAmountAuditResult amountAudit) {
        String direction;

        if (amountAudit.guideIsHigherThanExpected()) {
            direction = "O valor informado é superior ao esperado.";
        } else if (amountAudit.guideIsLowerThanExpected()) {
            direction = "O valor informado é inferior ao esperado.";
        } else {
            direction = "O valor informado diverge do esperado.";
        }

        return direction
                + " Esperado = "
                + amountAudit.expectedAmount().toPlainString()
                + "; informado = "
                + amountAudit.guideAmount().toPlainString()
                + "; diferença absoluta = "
                + amountAudit
                        .absoluteDifference()
                        .toPlainString()
                + ".";
    }

    private void addStructureFindings(
            GuideStructureAuditResult structureAudit,
            List<ConsolidatedAuditFinding> findings) {
        for (GuideStructureAuditFinding finding : structureAudit.findings()) {

            findings.add(
                    new ConsolidatedAuditFinding(
                            finding.code(),
                            mapSeverity(
                                    finding.severity()),
                            finding.field(),
                            finding.message(),
                            recommendationFor(
                                    finding.code())));
        }
    }

    private ConsolidatedAuditSeverity mapSeverity(
            GuideStructureAuditSeverity severity) {
        return switch (severity) {
            case NONE ->
                ConsolidatedAuditSeverity.NONE;

            case LOW ->
                ConsolidatedAuditSeverity.LOW;

            case MEDIUM ->
                ConsolidatedAuditSeverity.MEDIUM;

            case HIGH ->
                ConsolidatedAuditSeverity.HIGH;
        };
    }

    private String recommendationFor(
            String code) {
        return switch (code) {
            case "FATOR_R_MISMATCH" ->
                "Conferir folha, pró-labore, encargos, receita "
                        + "e competências utilizadas no Fator R.";

            case "ANNEX_MISMATCH" ->
                "Conferir o enquadramento da atividade e o resultado "
                        + "do Fator R utilizado na apuração.";

            case "BRACKET_MISMATCH" ->
                "Conferir RBT12 ou RBT12p, histórico de receitas "
                        + "e tabela tributária aplicada.";

            case "EFFECTIVE_RATE_MISMATCH" ->
                "Conferir alíquota nominal, parcela a deduzir, "
                        + "base de receita e precisão da fórmula.";

            case "FATOR_R_NOT_REPORTED" ->
                "Obter a memória de cálculo do Fator R "
                        + "para completar a conferência.";

            case "ANNEX_NOT_REPORTED" ->
                "Obter o anexo utilizado na apuração.";

            case "BRACKET_NOT_REPORTED" ->
                "Obter a faixa tributária utilizada na apuração.";

            case "EFFECTIVE_RATE_NOT_REPORTED" ->
                "Obter a alíquota efetiva utilizada na apuração.";

            default ->
                "Revisar o dado informado e a memória de cálculo "
                        + "correspondente.";
        };
    }

    private ConsolidatedAuditStatus determineStatus(
            ConsolidatedAuditRequest request) {
        if (request.amountAuditResult().status() == GuideAmountAuditStatus.DIVERGENT
                || request.structureAuditResult().status() == GuideStructureAuditStatus.DIVERGENT) {

            return ConsolidatedAuditStatus.DIVERGENT;
        }

        if (request.amountAuditResult().status() == GuideAmountAuditStatus.REQUIRES_ADDITIONAL_CONTEXT
                || request.structureAuditResult().status() == GuideStructureAuditStatus.PARTIALLY_COMPATIBLE
                || request.structureAuditResult().status() == GuideStructureAuditStatus.INSUFFICIENT_DATA) {

            return ConsolidatedAuditStatus.REVIEW_REQUIRED;
        }

        return ConsolidatedAuditStatus.COMPATIBLE;
    }

    private ConsolidatedAuditSeverity determineSeverity(
            List<ConsolidatedAuditFinding> findings) {
        ConsolidatedAuditSeverity severity = ConsolidatedAuditSeverity.NONE;

        for (ConsolidatedAuditFinding finding : findings) {
            severity = ConsolidatedAuditSeverity.highest(
                    severity,
                    finding.severity());
        }

        return severity;
    }

    private ConsolidatedAuditCause determinePrincipalCause(
            ConsolidatedAuditRequest request,
            List<ConsolidatedAuditFinding> findings) {
        if (containsCode(
                findings,
                "FATOR_R_MISMATCH")
                || containsCode(
                        findings,
                        "ANNEX_MISMATCH")) {
            return ConsolidatedAuditCause.FACTOR_R_OR_ANNEX;
        }

        if (containsCode(
                findings,
                "BRACKET_MISMATCH")) {
            return ConsolidatedAuditCause.REVENUE_BASIS_OR_BRACKET;
        }

        if (containsCode(
                findings,
                "EFFECTIVE_RATE_MISMATCH")) {
            return ConsolidatedAuditCause.EFFECTIVE_RATE;
        }

        if (containsCode(
                findings,
                "AMOUNT_DIVERGENCE")) {
            return ConsolidatedAuditCause.AMOUNT_ONLY;
        }

        if (containsCode(
                findings,
                "AMOUNT_REQUIRES_CONTEXT")) {
            return ConsolidatedAuditCause.DEFERRED_AMOUNT;
        }

        if (request.structureAuditResult().status() == GuideStructureAuditStatus.INSUFFICIENT_DATA
                || request.structureAuditResult().status() == GuideStructureAuditStatus.PARTIALLY_COMPATIBLE) {

            return ConsolidatedAuditCause.INSUFFICIENT_DATA;
        }

        return ConsolidatedAuditCause.NONE;
    }

    private boolean containsCode(
            List<ConsolidatedAuditFinding> findings,
            String code) {
        return findings.stream()
                .anyMatch(
                        finding -> finding.code().equals(code));
    }

    private String createExecutiveSummary(
            ConsolidatedAuditRequest request,
            ConsolidatedAuditStatus status,
            ConsolidatedAuditSeverity severity,
            ConsolidatedAuditCause principalCause,
            List<ConsolidatedAuditFinding> findings) {
        if (status == ConsolidatedAuditStatus.COMPATIBLE) {
            return "Os valores e os dados estruturais disponíveis "
                    + "são compatíveis com o cálculo produzido "
                    + "pelo motor tributário.";
        }

        if (status == ConsolidatedAuditStatus.REVIEW_REQUIRED) {
            return "A auditoria não encontrou divergência conclusiva, "
                    + "mas existem informações ausentes ou contexto "
                    + "adicional necessário para concluir a conferência.";
        }

        return "Foram encontrados "
                + findings.size()
                + " achado(s) na auditoria. "
                + "A severidade geral foi classificada como "
                + severity.getDisplayName()
                + ". Principal hipótese: "
                + principalCause.getDisplayName()
                + ". Valor esperado = "
                + request
                        .amountAuditResult()
                        .expectedAmount()
                        .toPlainString()
                + "; valor informado = "
                + request
                        .amountAuditResult()
                        .guideAmount()
                        .toPlainString()
                + "; diferença = "
                + request
                        .amountAuditResult()
                        .absoluteDifference()
                        .toPlainString()
                + ".";
    }

    private List<String> createRecommendedChecks(
            List<ConsolidatedAuditFinding> findings) {
        Set<String> recommendations = new LinkedHashSet<>();

        for (ConsolidatedAuditFinding finding : findings) {
            recommendations.add(
                    finding.recommendation());
        }

        return List.copyOf(
                recommendations);
    }

    private TaxDecision createDecision(
            ConsolidatedAuditRequest request,
            List<ConsolidatedAuditFinding> findings,
            ConsolidatedAuditStatus status,
            ConsolidatedAuditSeverity severity,
            ConsolidatedAuditCause principalCause) {
        String description = "Consolidação das auditorias de valor "
                + "e estrutura tributária.";

        String input = "Auditoria de valor = "
                + request
                        .amountAuditResult()
                        .status()
                        .getDisplayName()
                + "; auditoria estrutural = "
                + request
                        .structureAuditResult()
                        .status()
                        .getDisplayName()
                + "; quantidade de achados = "
                + findings.size()
                + ".";

        String condition;

        if (findings.isEmpty()) {
            condition = "Nenhum achado relevante foi identificado.";
        } else {
            condition = findings.stream()
                    .map(
                            finding -> finding.code()
                                    + " ["
                                    + finding
                                            .severity()
                                            .getDisplayName()
                                    + "]")
                    .collect(
                            Collectors.joining(" | "));
        }

        String result = "Status = "
                + status.getDisplayName()
                + "; severidade = "
                + severity.getDisplayName()
                + "; principal hipótese = "
                + principalCause.getDisplayName()
                + ".";

        return new TaxDecision(
                RULE_CODE,
                RULE_VERSION,
                description,
                input,
                condition,
                result,
                INTERNAL_REFERENCE);
    }
}