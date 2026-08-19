package br.com.auditortributario.api.simples.audit;

import br.com.auditortributario.application.simples.audit.SimplesAuditReportResult;
import br.com.auditortributario.taxrule.simples.AuditReport;
import br.com.auditortributario.taxrule.simples.AuditReportSection;

import java.time.YearMonth;
import java.util.List;

public record SimplesAuditReportResponse(
        YearMonth assessmentPeriod,
        String status,
        String severity,
        String principalCause,
        String title,
        String executiveSummary,
        List<SectionResponse> sections,
        List<String> references,
        String markdown) {

    public SimplesAuditReportResponse {
        sections = List.copyOf(
                sections);

        references = List.copyOf(
                references);
    }

    public static SimplesAuditReportResponse from(
            SimplesAuditReportResult result) {
        AuditReport report = result.report();

        List<SectionResponse> sections = report.sections()
                .stream()
                .map(
                        SectionResponse::from)
                .toList();

        return new SimplesAuditReportResponse(
                report.assessmentPeriod(),

                result.auditResult()
                        .status()
                        .name(),

                result.auditResult()
                        .severity()
                        .name(),

                result.auditResult()
                        .principalCause()
                        .name(),

                report.title(),

                report.executiveSummary(),

                sections,

                report.references(),

                result.markdown());
    }

    public record SectionResponse(
            String code,
            String title,
            String content) {

        public static SectionResponse from(
                AuditReportSection section) {
            return new SectionResponse(
                    section.code(),
                    section.title(),
                    section.content());
        }
    }
}