package br.com.auditortributario.taxrule.simples.revenue;

import java.math.BigDecimal;
import java.util.Objects;

public record SimplesRevenueTaxBracket(
        SimplesRevenueTaxRoute route,
        int number,
        BigDecimal maximumRevenue,
        BigDecimal nominalRate,
        BigDecimal deduction) {

    public SimplesRevenueTaxBracket {
        Objects.requireNonNull(
                route,
                "A rota tributária não pode ser nula.");

        Objects.requireNonNull(
                maximumRevenue,
                "O limite máximo de receita não pode ser nulo.");

        Objects.requireNonNull(
                nominalRate,
                "A alíquota nominal não pode ser nula.");

        Objects.requireNonNull(
                deduction,
                "A parcela a deduzir não pode ser nula.");

        if (number < 1 || number > 6) {
            throw new IllegalArgumentException(
                    "O número da faixa deve estar entre 1 e 6.");
        }

        if (maximumRevenue.compareTo(
                BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O limite máximo de receita deve ser positivo.");
        }

        if (nominalRate.compareTo(
                BigDecimal.ZERO) <= 0
                || nominalRate.compareTo(
                        BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                    "A alíquota nominal deve estar entre 0 e 1.");
        }

        if (deduction.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A parcela a deduzir não pode ser negativa.");
        }
    }

    public BigDecimal nominalRateAsPercentage() {
        return nominalRate
                .multiply(
                        BigDecimal.valueOf(
                                100))
                .stripTrailingZeros();
    }
}