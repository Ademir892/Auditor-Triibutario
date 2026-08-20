package br.com.auditortributario.taxrule.simples.composition;

import br.com.auditortributario.taxrule.domain.TaxComponent;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public record SimplesIssCapRule(
        BigDecimal maximumEffectiveRate,
        Map<TaxComponent, BigDecimal> redistributionRates) {

    public SimplesIssCapRule {
        Objects.requireNonNull(
                maximumEffectiveRate,
                "O limite efetivo do ISS não pode ser nulo.");

        Objects.requireNonNull(
                redistributionRates,
                "Os percentuais de redistribuição não podem ser nulos.");

        if (maximumEffectiveRate.compareTo(
                BigDecimal.ZERO) <= 0
                || maximumEffectiveRate.compareTo(
                        BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                    "O limite efetivo do ISS deve estar entre 0 e 1.");
        }

        redistributionRates = Map.copyOf(
                redistributionRates);

        validateRedistributionRates(
                redistributionRates);
    }

    private static void validateRedistributionRates(
            Map<TaxComponent, BigDecimal> rates) {
        if (rates.isEmpty()) {
            throw new IllegalArgumentException(
                    "A redistribuição do ISS deve possuir componentes.");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<TaxComponent, BigDecimal> entry : rates.entrySet()) {

            Objects.requireNonNull(
                    entry.getKey(),
                    "O componente da redistribuição não pode ser nulo.");

            BigDecimal rate = Objects.requireNonNull(
                    entry.getValue(),
                    "O percentual de redistribuição não pode ser nulo.");

            if (entry.getKey() == TaxComponent.ISS) {

                throw new IllegalArgumentException(
                        "O ISS não pode receber a própria redistribuição.");
            }

            if (rate.compareTo(
                    BigDecimal.ZERO) < 0
                    || rate.compareTo(
                            BigDecimal.ONE) > 0) {

                throw new IllegalArgumentException(
                        "Os percentuais de redistribuição "
                                + "devem estar entre 0 e 1.");
            }

            total = total.add(
                    rate);
        }

        if (total.compareTo(
                BigDecimal.ONE) != 0) {

            throw new IllegalArgumentException(
                    "Os percentuais de redistribuição "
                            + "devem totalizar 100%.");
        }
    }
}