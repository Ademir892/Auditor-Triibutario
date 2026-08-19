package br.com.auditortributario.application.simples.audit;

import br.com.auditortributario.taxrule.simples.AuditReport;
import br.com.auditortributario.taxrule.simples.AuditReportGenerator;
import br.com.auditortributario.taxrule.simples.AuditReportMarkdownRenderer;
import br.com.auditortributario.taxrule.simples.AuditReportRequest;
import br.com.auditortributario.taxrule.simples.ConsolidatedAuditResult;

import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public final class SimplesAuditReportService {

    private final SimplesAuditService auditService;

    private final AuditReportGenerator reportGenerator;

    private final AuditReportMarkdownRenderer markdownRenderer;

    public SimplesAuditReportService(
            SimplesAuditService auditService) {
        this.auditService = Objects.requireNonNull(
                auditService,
                "O serviço de auditoria não pode ser nulo.");

        this.reportGenerator = new AuditReportGenerator();

        this.markdownRenderer = new AuditReportMarkdownRenderer();
    }

    public SimplesAuditReportResult generate(
            SimplesAuditCommand command) {
        Objects.requireNonNull(
                command,
                "O comando de auditoria não pode ser nulo.");

        ConsolidatedAuditResult auditResult = auditService.audit(
                command);

        AuditReport report = reportGenerator.generate(
                new AuditReportRequest(
                        auditResult));

        String markdown = markdownRenderer.render(
                report);

        return new SimplesAuditReportResult(
                auditResult,
                report,
                markdown);
    }
}