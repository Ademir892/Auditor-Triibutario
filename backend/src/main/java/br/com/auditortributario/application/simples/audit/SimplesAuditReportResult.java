package br.com.auditortributario.application.simples.audit;

import br.com.auditortributario.taxrule.simples.AuditReport;
import br.com.auditortributario.taxrule.simples.ConsolidatedAuditResult;

import java.util.Objects;

public record SimplesAuditReportResult(
        ConsolidatedAuditResult auditResult,
        AuditReport report,
        String markdown) {

    public SimplesAuditReportResult {
        Objects.requireNonNull(
                auditResult,
                "O resultado da auditoria não pode ser nulo.");

        Objects.requireNonNull(
                report,
                "O relatório não pode ser nulo.");

        Objects.requireNonNull(
                markdown,
                "O Markdown do relatório não pode ser nulo.");
    }
}