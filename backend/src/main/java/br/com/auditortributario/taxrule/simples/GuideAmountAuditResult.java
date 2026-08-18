package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

public record GuideAmountAuditResult(
        BigDecimal expectedAmount,
        BigDecimal guideAmount,
        BigDecimal signedDifference,
        BigDecimal absoluteDifference,
        Optional<BigDecimal> percentageDifference,
        BigDecimal tolerance,
        GuideAmountAuditStatus status,
        TaxDecision decision) {

    public GuideAmountAuditResult {
        Objects.requireNonNull(
                expectedAmount,
                "O valor esperado não pode ser nulo.");

        Objects.requireNonNull(
                guideAmount,
                "O valor da guia não pode ser nulo.");

        Objects.requireNonNull(
                signedDifference,
                "A diferença não pode ser nula.");

        Objects.requireNonNull(
                absoluteDifference,
                "A diferença absoluta não pode ser nula.");

        Objects.requireNonNull(
                percentageDifference,
                "A diferença percentual não pode ser nula.");

        Objects.requireNonNull(
                tolerance,
                "A tolerância não pode ser nula.");

        Objects.requireNonNull(
                status,
                "O status da auditoria não pode ser nulo.");

        Objects.requireNonNull(
                decision,
                "A decisão de auditoria não pode ser nula.");
    }

    public boolean guideIsHigherThanExpected() {
        return signedDifference.compareTo(
                BigDecimal.ZERO) > 0;
    }

    public boolean guideIsLowerThanExpected() {
        return signedDifference.compareTo(
                BigDecimal.ZERO) < 0;
    }

    public Optional<BigDecimal> percentageDifferenceForDisplay() {
        return percentageDifference.map(
                percentage -> percentage.setScale(
                        2,
                        RoundingMode.HALF_UP));
    }
}