package br.com.auditortributario.api.simples.calculation;

import br.com.auditortributario.application.simples.SimplesCalculationCommand;
import br.com.auditortributario.taxrule.simples.MonthlyRevenue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public record SimplesCalculationRequest(
        @NotNull LocalDate openingDate,

        @NotNull YearMonth assessmentPeriod,

        @NotNull @PositiveOrZero BigDecimal fatorRPayrollBase,

        @NotNull @PositiveOrZero BigDecimal fatorRRevenueBase,

        @NotNull @PositiveOrZero BigDecimal taxableRevenue,

        @NotNull @Valid List<MonthlyRevenueRequest> priorMonthlyRevenues) {

    public SimplesCalculationCommand toCommand() {
        List<MonthlyRevenue> revenues = priorMonthlyRevenues
                .stream()
                .map(
                        revenue -> new MonthlyRevenue(
                                revenue.period(),
                                revenue.amount()))
                .toList();

        return new SimplesCalculationCommand(
                openingDate,
                assessmentPeriod,
                fatorRPayrollBase,
                fatorRRevenueBase,
                taxableRevenue,
                revenues);
    }

    public record MonthlyRevenueRequest(
            @NotNull YearMonth period,

            @NotNull @PositiveOrZero BigDecimal amount) {
    }
}