package br.com.auditortributario.taxrule.simples.composition;

import br.com.auditortributario.taxrule.domain.TaxComponent;

import java.math.BigDecimal;
import java.util.Objects;

public record SimplesTaxDistributionShare(
        TaxComponent component,
        BigDecimal distributionRate) {

    public SimplesTaxDistributionShare {
        Objects.requireNonNull(
                component,
                "O componente tributário não pode ser nulo.");

        Objects.requireNonNull(
                distributionRate,
                "O percentual de repartição não pode ser nulo.");

        if (distributionRate.compareTo(
                BigDecimal.ZERO) < 0
                || distributionRate.compareTo(
                        BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                    "O percentual de repartição deve estar entre 0 e 1.");
        }
    }
}