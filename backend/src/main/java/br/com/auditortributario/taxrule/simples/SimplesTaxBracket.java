package br.com.auditortributario.taxrule.simples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record SimplesTaxBracket(
        int number,
        BigDecimal maximumRevenue,
        BigDecimal nominalRate,
        BigDecimal deduction) {

    public SimplesTaxBracket {
        if (number <= 0) {
            throw new IllegalArgumentException(
                    "O número da faixa deve ser maior que zero.");
        }

        Objects.requireNonNull(
                maximumRevenue,
                "O limite máximo de receita não pode ser nulo.");

        Objects.requireNonNull(
                nominalRate,
                "A alíquota nominal não pode ser nula.");

        Objects.requireNonNull(
                deduction,
                "A parcela a deduzir não pode ser nula.");

        if (maximumRevenue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O limite máximo de receita deve ser maior que zero.");
        }

        if (nominalRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "A alíquota nominal deve ser maior que zero.");
        }

        if (nominalRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "A alíquota nominal não pode ser superior a 100%.");
        }

        if (deduction.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A parcela a deduzir não pode ser negativa.");
        }
    }

    public BigDecimal nominalRateAsPercentage() {
        return nominalRate
                .movePointRight(2)
                .setScale(2, RoundingMode.HALF_UP);
    }
}