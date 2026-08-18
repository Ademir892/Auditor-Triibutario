package br.com.auditortributario.taxrule.simples;

import java.math.BigDecimal;
import java.util.Objects;

public record ConsolidatedAuditRequest(
        GuideAmountAuditResult amountAuditResult,
        GuideStructureAuditResult structureAuditResult) {

    public ConsolidatedAuditRequest {
        Objects.requireNonNull(
                amountAuditResult,
                "A auditoria de valor não pode ser nula.");

        Objects.requireNonNull(
                structureAuditResult,
                "A auditoria estrutural não pode ser nula.");

        BigDecimal amountExpected = amountAuditResult.expectedAmount();

        BigDecimal structureExpected = structureAuditResult
                .estimatedTaxResult()
                .estimatedTaxAmount();

        if (amountExpected.compareTo(structureExpected) != 0) {
            throw new IllegalArgumentException(
                    "As auditorias consolidadas devem pertencer "
                            + "ao mesmo cálculo tributário.");
        }
    }
}