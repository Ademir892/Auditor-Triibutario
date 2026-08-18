package br.com.auditortributario.api.simples.audit;

import br.com.auditortributario.taxrule.simples.ConsolidatedAuditFinding;
import br.com.auditortributario.taxrule.simples.ConsolidatedAuditResult;
import br.com.auditortributario.taxrule.simples.GuideAmountAuditResult;
import br.com.auditortributario.taxrule.simples.GuideStructureAuditResult;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record SimplesAuditResponse(
        YearMonth assessmentPeriod,
        String status,
        String statusLabel,
        String severity,
        String severityLabel,
        PrincipalCauseResponse principalCause,
        AmountAuditResponse amount,
        StructureAuditResponse structure,
        List<FindingResponse> findings,
        String executiveSummary,
        List<String> recommendedChecks) {

    public static SimplesAuditResponse from(
            ConsolidatedAuditResult result) {
        GuideAmountAuditResult amountAudit = result.amountAuditResult();

        GuideStructureAuditResult structureAudit = result.structureAuditResult();

        YearMonth assessmentPeriod = structureAudit
                .estimatedTaxResult()
                .taxableRevenue()
                .period();

        List<FindingResponse> findings = result.findings()
                .stream()
                .map(
                        FindingResponse::from)
                .toList();

        return new SimplesAuditResponse(
                assessmentPeriod,

                result.status().name(),
                result.status().getDisplayName(),

                result.severity().name(),
                result.severity().getDisplayName(),

                new PrincipalCauseResponse(
                        result.principalCause().name(),
                        result
                                .principalCause()
                                .getDisplayName()),

                new AmountAuditResponse(
                        amountAudit.status().name(),
                        amountAudit
                                .status()
                                .getDisplayName(),
                        amountAudit.expectedAmount(),
                        amountAudit.guideAmount(),
                        amountAudit.signedDifference(),
                        amountAudit.absoluteDifference(),
                        amountAudit
                                .percentageDifference()
                                .orElse(null),
                        amountAudit.tolerance(),
                        amountAudit
                                .guideIsHigherThanExpected(),
                        amountAudit
                                .guideIsLowerThanExpected()),

                new StructureAuditResponse(
                        structureAudit.status().name(),
                        structureAudit
                                .status()
                                .getDisplayName(),
                        structureAudit.severity().name(),
                        structureAudit
                                .severity()
                                .getDisplayName()),

                findings,

                result.executiveSummary(),

                result.recommendedChecks());
    }

    public record PrincipalCauseResponse(
            String code,
            String label) {
    }

    public record AmountAuditResponse(
            String status,
            String statusLabel,
            BigDecimal expectedAmount,
            BigDecimal reportedAmount,
            BigDecimal signedDifference,
            BigDecimal absoluteDifference,
            BigDecimal percentageDifference,
            BigDecimal tolerance,
            boolean reportedAmountHigherThanExpected,
            boolean reportedAmountLowerThanExpected) {
    }

    public record StructureAuditResponse(
            String status,
            String statusLabel,
            String severity,
            String severityLabel) {
    }

    public record FindingResponse(
            String code,
            String severity,
            String severityLabel,
            String category,
            String message,
            String recommendation) {

        public static FindingResponse from(
                ConsolidatedAuditFinding finding) {
            return new FindingResponse(
                    finding.code(),
                    finding.severity().name(),
                    finding
                            .severity()
                            .getDisplayName(),
                    finding.category(),
                    finding.message(),
                    finding.recommendation());
        }
    }
}