package br.com.auditortributario.taxrule.simples;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public record GuideStructureAuditRequest(
        SimplesEstimatedTaxResult estimatedTaxResult,
        Optional<BigDecimal> reportedFatorR,
        Optional<SimplesAnnex> reportedAnnex,
        Optional<Integer> reportedBracketNumber,
        Optional<BigDecimal> reportedEffectiveRate,
        BigDecimal effectiveRateTolerance) {

    public static final BigDecimal DEFAULT_EFFECTIVE_RATE_TOLERANCE = new BigDecimal("0.00001");

    public GuideStructureAuditRequest(
            SimplesEstimatedTaxResult estimatedTaxResult,
            Optional<BigDecimal> reportedFatorR,
            Optional<SimplesAnnex> reportedAnnex,
            Optional<Integer> reportedBracketNumber,
            Optional<BigDecimal> reportedEffectiveRate) {
        this(
                estimatedTaxResult,
                reportedFatorR,
                reportedAnnex,
                reportedBracketNumber,
                reportedEffectiveRate,
                DEFAULT_EFFECTIVE_RATE_TOLERANCE);
    }

    public GuideStructureAuditRequest {
        Objects.requireNonNull(
                estimatedTaxResult,
                "O resultado estimado não pode ser nulo.");

        Objects.requireNonNull(
                reportedFatorR,
                "O Fator R informado não pode ser nulo.");

        Objects.requireNonNull(
                reportedAnnex,
                "O anexo informado não pode ser nulo.");

        Objects.requireNonNull(
                reportedBracketNumber,
                "A faixa informada não pode ser nula.");

        Objects.requireNonNull(
                reportedEffectiveRate,
                "A alíquota efetiva informada não pode ser nula.");

        Objects.requireNonNull(
                effectiveRateTolerance,
                "A tolerância da alíquota efetiva não pode ser nula.");

        reportedFatorR.ifPresent(
                value -> {
                    if (value.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                "O Fator R informado não pode ser negativo.");
                    }
                });

        reportedBracketNumber.ifPresent(
                value -> {
                    if (value <= 0) {
                        throw new IllegalArgumentException(
                                "A faixa informada deve ser maior que zero.");
                    }
                });

        reportedEffectiveRate.ifPresent(
                value -> {
                    if (value.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                "A alíquota efetiva informada "
                                        + "não pode ser negativa.");
                    }

                    if (value.compareTo(BigDecimal.ONE) > 0) {
                        throw new IllegalArgumentException(
                                "A alíquota efetiva informada "
                                        + "não pode ser superior a 100%.");
                    }
                });

        if (effectiveRateTolerance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A tolerância da alíquota efetiva "
                            + "não pode ser negativa.");
        }
    }
}