package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.util.List;
import java.util.Objects;

public record GuideStructureAuditResult(
        SimplesEstimatedTaxResult estimatedTaxResult,
        List<GuideStructureAuditFinding> findings,
        GuideStructureAuditStatus status,
        GuideStructureAuditSeverity severity,
        TaxDecision decision) {

    public GuideStructureAuditResult {
        Objects.requireNonNull(
                estimatedTaxResult,
                "O resultado estimado não pode ser nulo.");

        Objects.requireNonNull(
                findings,
                "Os achados da auditoria não podem ser nulos.");

        Objects.requireNonNull(
                status,
                "O status da auditoria não pode ser nulo.");

        Objects.requireNonNull(
                severity,
                "A severidade da auditoria não pode ser nula.");

        Objects.requireNonNull(
                decision,
                "A decisão de auditoria não pode ser nula.");

        findings = List.copyOf(
                findings);
    }

    public boolean hasDivergences() {
        return status == GuideStructureAuditStatus.DIVERGENT;
    }
}