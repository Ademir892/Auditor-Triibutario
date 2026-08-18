package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.util.List;
import java.util.Objects;

public record ConsolidatedAuditResult(
        GuideAmountAuditResult amountAuditResult,
        GuideStructureAuditResult structureAuditResult,
        List<ConsolidatedAuditFinding> findings,
        ConsolidatedAuditStatus status,
        ConsolidatedAuditSeverity severity,
        ConsolidatedAuditCause principalCause,
        String executiveSummary,
        List<String> recommendedChecks,
        TaxDecision decision) {

    public ConsolidatedAuditResult {
        Objects.requireNonNull(
                amountAuditResult,
                "A auditoria de valor não pode ser nula.");

        Objects.requireNonNull(
                structureAuditResult,
                "A auditoria estrutural não pode ser nula.");

        Objects.requireNonNull(
                findings,
                "Os achados consolidados não podem ser nulos.");

        Objects.requireNonNull(
                status,
                "O status consolidado não pode ser nulo.");

        Objects.requireNonNull(
                severity,
                "A severidade consolidada não pode ser nula.");

        Objects.requireNonNull(
                principalCause,
                "A principal hipótese não pode ser nula.");

        if (executiveSummary == null || executiveSummary.isBlank()) {
            throw new IllegalArgumentException(
                    "O resumo executivo não pode ser nulo ou vazio.");
        }

        Objects.requireNonNull(
                recommendedChecks,
                "As verificações recomendadas não podem ser nulas.");

        Objects.requireNonNull(
                decision,
                "A decisão consolidada não pode ser nula.");

        findings = List.copyOf(
                findings);

        recommendedChecks = List.copyOf(
                recommendedChecks);
    }

    public boolean hasDivergences() {
        return status == ConsolidatedAuditStatus.DIVERGENT;
    }
}