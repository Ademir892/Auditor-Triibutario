package br.com.auditortributario.application.simples;

import br.com.auditortributario.taxrule.simples.MonthlyRevenue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public record SimplesCalculationCommand(
        LocalDate openingDate,
        YearMonth assessmentPeriod,
        BigDecimal fatorRPayrollBase,
        BigDecimal fatorRRevenueBase,
        BigDecimal taxableRevenue,
        List<MonthlyRevenue> priorMonthlyRevenues) {

    public SimplesCalculationCommand {
        Objects.requireNonNull(
                openingDate,
                "A data de abertura não pode ser nula.");

        Objects.requireNonNull(
                assessmentPeriod,
                "A competência não pode ser nula.");

        Objects.requireNonNull(
                fatorRPayrollBase,
                "A base de folha do Fator R não pode ser nula.");

        Objects.requireNonNull(
                fatorRRevenueBase,
                "A base de receita do Fator R não pode ser nula.");

        Objects.requireNonNull(
                taxableRevenue,
                "A receita tributável não pode ser nula.");

        Objects.requireNonNull(
                priorMonthlyRevenues,
                "O histórico de receitas não pode ser nulo.");

        if (fatorRPayrollBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A base de folha do Fator R não pode ser negativa.");
        }

        if (fatorRRevenueBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A base de receita do Fator R não pode ser negativa.");
        }

        if (taxableRevenue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A receita tributável não pode ser negativa.");
        }

        priorMonthlyRevenues = List.copyOf(
                priorMonthlyRevenues);

        for (MonthlyRevenue revenue : priorMonthlyRevenues) {
            Objects.requireNonNull(
                    revenue,
                    "O histórico não pode conter receita nula.");

            if (!revenue.period().isBefore(assessmentPeriod)) {
                throw new IllegalArgumentException(
                        "O histórico anterior não pode conter "
                                + "a competência atual ou futura: "
                                + revenue.period()
                                + ".");
            }
        }
    }
}