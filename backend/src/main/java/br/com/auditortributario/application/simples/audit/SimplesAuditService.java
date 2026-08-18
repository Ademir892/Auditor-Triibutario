package br.com.auditortributario.application.simples.audit;

import br.com.auditortributario.application.simples.SimplesCalculationService;
import br.com.auditortributario.taxrule.simples.ConsolidatedAuditRequest;
import br.com.auditortributario.taxrule.simples.ConsolidatedAuditResult;
import br.com.auditortributario.taxrule.simples.ConsolidatedAuditor;
import br.com.auditortributario.taxrule.simples.GuideAmountAuditRequest;
import br.com.auditortributario.taxrule.simples.GuideAmountAuditResult;
import br.com.auditortributario.taxrule.simples.GuideAmountAuditor;
import br.com.auditortributario.taxrule.simples.GuideStructureAuditRequest;
import br.com.auditortributario.taxrule.simples.GuideStructureAuditResult;
import br.com.auditortributario.taxrule.simples.GuideStructureAuditor;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxResult;

import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public final class SimplesAuditService {

    private final SimplesCalculationService calculationService;

    private final GuideAmountAuditor amountAuditor;

    private final GuideStructureAuditor structureAuditor;

    private final ConsolidatedAuditor consolidatedAuditor;

    public SimplesAuditService(
            SimplesCalculationService calculationService) {
        this.calculationService = Objects.requireNonNull(
                calculationService,
                "O serviço de cálculo não pode ser nulo.");

        this.amountAuditor = new GuideAmountAuditor();

        this.structureAuditor = new GuideStructureAuditor();

        this.consolidatedAuditor = new ConsolidatedAuditor();
    }

    public ConsolidatedAuditResult audit(
            SimplesAuditCommand command) {
        Objects.requireNonNull(
                command,
                "O comando de auditoria não pode ser nulo.");

        SimplesEstimatedTaxResult estimatedTaxResult = calculationService.calculate(
                command.calculation());

        GuideAmountAuditResult amountAuditResult = amountAuditor.audit(
                new GuideAmountAuditRequest(
                        estimatedTaxResult,
                        command.guideAmount()));

        GuideStructureAuditResult structureAuditResult = structureAuditor.audit(
                new GuideStructureAuditRequest(
                        estimatedTaxResult,
                        command.reportedFatorR(),
                        command.reportedAnnex(),
                        command.reportedBracketNumber(),
                        command.reportedEffectiveRate()));

        return consolidatedAuditor.audit(
                new ConsolidatedAuditRequest(
                        amountAuditResult,
                        structureAuditResult));
    }
}