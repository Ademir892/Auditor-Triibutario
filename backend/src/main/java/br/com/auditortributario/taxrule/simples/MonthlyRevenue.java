package br.com.auditortributario.taxrule.simples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Objects;

public record MonthlyRevenue(
        YearMonth period,
        BigDecimal amount) {

    public MonthlyRevenue {
        Objects.requireNonNull(
                period,
                "A competência da receita não pode ser nula.");

        Objects.requireNonNull(
                amount,
                "O valor da receita não pode ser nulo.");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor da receita não pode ser negativo.");
        }

        try {
            amount = amount.setScale(
                    2,
                    RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "O valor da receita não pode possuir "
                            + "frações inferiores a um centavo.",
                    exception);
        }
    }
}