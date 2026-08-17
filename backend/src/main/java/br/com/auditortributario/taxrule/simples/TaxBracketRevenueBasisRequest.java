package br.com.auditortributario.taxrule.simples;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public record TaxBracketRevenueBasisRequest(
        LocalDate openingDate,
        YearMonth assessmentPeriod,
        List<MonthlyRevenue> monthlyRevenues) {

    public TaxBracketRevenueBasisRequest {
        Objects.requireNonNull(
                openingDate,
                "A data de abertura não pode ser nula.");

        Objects.requireNonNull(
                assessmentPeriod,
                "O período de apuração não pode ser nulo.");

        Objects.requireNonNull(
                monthlyRevenues,
                "O histórico de receitas não pode ser nulo.");

        monthlyRevenues = List.copyOf(
                monthlyRevenues);
    }
}