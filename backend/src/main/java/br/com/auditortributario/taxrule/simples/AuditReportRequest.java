package br.com.auditortributario.taxrule.simples;

import java.util.Objects;

public record AuditReportRequest(
        ConsolidatedAuditResult consolidatedAuditResult) {

    public AuditReportRequest {
        Objects.requireNonNull(
                consolidatedAuditResult,
                "O resultado consolidado da auditoria não pode ser nulo.");
    }
}