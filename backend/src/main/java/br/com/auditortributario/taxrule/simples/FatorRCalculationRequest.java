package br.com.auditortributario.taxrule.simples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public record FatorRCalculationRequest(
        LocalDate openingDate,
        YearMonth assessmentPeriod,
        BigDecimal payrollBase,
        BigDecimal revenueBase) {

    public FatorRCalculationRequest {
        Objects.requireNonNull(
                openingDate,
                "A data de abertura não pode ser nula.");

        Objects.requireNonNull(
                assessmentPeriod,
                "O período de apuração não pode ser nulo.");

        Objects.requireNonNull(
                payrollBase,
                "A base de folha não pode ser nula.");

        Objects.requireNonNull(
                revenueBase,
                "A base de receita não pode ser nula.");

        if (payrollBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A base de folha não pode ser negativa.");
        }

        if (revenueBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A base de receita não pode ser negativa.");
        }
    }
}