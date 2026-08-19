package br.com.auditortributario.api.simples.audit;

import br.com.auditortributario.application.simples.audit.SimplesAuditReportResult;
import br.com.auditortributario.application.simples.audit.SimplesAuditReportService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simples/audit")
public final class SimplesAuditReportController {

    private final SimplesAuditReportService reportService;

    public SimplesAuditReportController(
            SimplesAuditReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/report")
    public SimplesAuditReportResponse generateReport(
            @Valid @RequestBody SimplesAuditRequest request) {
        SimplesAuditReportResult result = reportService.generate(
                request.toCommand());

        return SimplesAuditReportResponse.from(
                result);
    }
}