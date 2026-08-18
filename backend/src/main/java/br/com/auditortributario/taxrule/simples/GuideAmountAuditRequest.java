package br.com.auditortributario.taxrule.simples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record GuideAmountAuditRequest(
        SimplesEstimatedTaxResult estimatedTaxResult,
        BigDecimal guideAmount,
        BigDecimal tolerance) {

    public static final BigDecimal DEFAULT_TOLERANCE = new BigDecimal("0.05");

    public GuideAmountAuditRequest(
            SimplesEstimatedTaxResult estimatedTaxResult,
            BigDecimal guideAmount) {
        this(
                estimatedTaxResult,
                guideAmount,
                DEFAULT_TOLERANCE);
    }

    public GuideAmountAuditRequest {
        Objects.requireNonNull(
                estimatedTaxResult,
                "O resultado estimado não pode ser nulo.");

        Objects.requireNonNull(
                guideAmount,
                "O valor informado na guia não pode ser nulo.");

        Objects.requireNonNull(
                tolerance,
                "A tolerância não pode ser nula.");

        if (guideAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor informado na guia não pode ser negativo.");
        }

        if (tolerance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A tolerância não pode ser negativa.");
        }

        try {
            guideAmount = guideAmount.setScale(
                    2,
                    RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "O valor informado na guia deve possuir "
                            + "no máximo duas casas decimais.",
                    exception);
        }

        try {
            tolerance = tolerance.setScale(
                    2,
                    RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "A tolerância deve possuir "
                            + "no máximo duas casas decimais.",
                    exception);
        }
    }
}