package br.com.auditortributario.taxrule.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record TaxComponentAllocation(
                TaxComponent component,
                BigDecimal distributionRate,
                BigDecimal effectiveRate,
                BigDecimal amount) {

        public TaxComponentAllocation {
                Objects.requireNonNull(
                                component,
                                "O componente tributário não pode ser nulo.");

                Objects.requireNonNull(
                                distributionRate,
                                "O percentual de repartição não pode ser nulo.");

                Objects.requireNonNull(
                                effectiveRate,
                                "A alíquota efetiva do tributo não pode ser nula.");

                Objects.requireNonNull(
                                amount,
                                "O valor do componente tributário não pode ser nulo.");

                validateRate(
                                distributionRate,
                                "O percentual de repartição");

                validateRate(
                                effectiveRate,
                                "A alíquota efetiva do tributo");

                if (amount.compareTo(
                                BigDecimal.ZERO) < 0) {

                        throw new IllegalArgumentException(
                                        "O valor do componente tributário "
                                                        + "não pode ser negativo.");
                }
        }

        public BigDecimal distributionRateAsPercentage() {
                return distributionRate
                                .multiply(
                                                BigDecimal.valueOf(
                                                                100))
                                .stripTrailingZeros();
        }

        public BigDecimal effectiveRateAsPercentage() {
                return effectiveRate
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

        private static void validateRate(
                        BigDecimal value,
                        String field) {
                if (value.compareTo(
                                BigDecimal.ZERO) < 0
                                || value.compareTo(
                                                BigDecimal.ONE) > 0) {

                        throw new IllegalArgumentException(
                                        field
                                                        + " deve estar entre 0 e 1.");
                }
        }
}