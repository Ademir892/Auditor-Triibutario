package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public record SimplesOpeningYearSublimitResult(
        LocalDate openingDate,
        BigDecimal annualSublimit,
        BigDecimal monthlyReference,
        int monthsConsidered,
        BigDecimal proportionalizedSublimit,
        TaxDecision decision) {

    public SimplesOpeningYearSublimitResult {
        Objects.requireNonNull(
                openingDate,
                "A data de abertura não pode ser nula.");

        Objects.requireNonNull(
                annualSublimit,
                "O sublimite anual não pode ser nulo.");

        Objects.requireNonNull(
                monthlyReference,
                "A referência mensal do sublimite não pode ser nula.");

        Objects.requireNonNull(
                proportionalizedSublimit,
                "O sublimite proporcional não pode ser nulo.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        if (annualSublimit.compareTo(
                BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O sublimite anual deve ser maior que zero.");
        }

        if (monthlyReference.compareTo(
                BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "A referência mensal do sublimite "
                            + "deve ser maior que zero.");
        }

        if (monthsConsidered < 1
                || monthsConsidered > 12) {

            throw new IllegalArgumentException(
                    "A quantidade de meses considerada "
                            + "deve estar entre 1 e 12.");
        }

        if (proportionalizedSublimit.compareTo(
                BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O sublimite proporcional deve ser maior que zero.");
        }
    }

    public boolean coversFullCalendarYear() {
        return monthsConsidered == 12;
    }

    public BigDecimal proportionalizedSublimitForDisplay() {
        return proportionalizedSublimit.setScale(
                2,
                RoundingMode.HALF_UP);
    }
}