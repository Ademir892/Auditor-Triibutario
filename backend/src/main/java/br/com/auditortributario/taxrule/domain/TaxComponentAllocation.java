package br.com.auditortributario.taxrule.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record TaxComponentAllocation(
        TaxComponent component,
        BigDecimal allocationRate,
        BigDecimal amount) {

    public TaxComponentAllocation {
        Objects.requireNonNull(
                component,
                "O componente tributário não pode ser nulo.");

        Objects.requireNonNull(
                allocationRate,
                "O percentual de repartição não pode ser nulo.");

        Objects.requireNonNull(
                amount,
                "O valor do componente tributário não pode ser nulo.");

        if (allocationRate.compareTo(
                BigDecimal.ZERO) < 0
                || allocationRate.compareTo(
                        BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                    "O percentual de repartição deve estar entre 0 e 1.");
        }

        if (amount.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "O valor do componente tributário "
                            + "não pode ser negativo.");
        }
    }

    public BigDecimal allocationRateAsPercentage() {
        return allocationRate
                .multiply(
                        BigDecimal.valueOf(
                                100))
                .stripTrailingZeros();
    }

    public BigDecimal amountForDisplay() {
        return amount.setScale(
                2,
                RoundingMode.HALF_UP);
    }
}