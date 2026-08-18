package br.com.auditortributario.api.simples.audit;

import br.com.auditortributario.application.simples.audit.SimplesAuditService;
import br.com.auditortributario.taxrule.simples.ConsolidatedAuditResult;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simples")
public final class SimplesAuditController {

    private final SimplesAuditService auditService;

    public SimplesAuditController(
            SimplesAuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping("/audit")
    public SimplesAuditResponse audit(
            @Valid @RequestBody SimplesAuditRequest request) {
        ConsolidatedAuditResult result = auditService.audit(
                request.toCommand());

        return SimplesAuditResponse.from(
                result);
    }
}