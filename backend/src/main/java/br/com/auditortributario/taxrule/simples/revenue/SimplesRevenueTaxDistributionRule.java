package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record SimplesRevenueTaxDistributionRule(
        SimplesRevenueTaxRoute route,
        int bracketNumber,
        List<ComponentShare> shares) {

    public SimplesRevenueTaxDistributionRule {
        Objects.requireNonNull(
                route,
                "A rota tributária não pode ser nula.");

        Objects.requireNonNull(
                shares,
                "As participações tributárias não podem ser nulas.");

        if (bracketNumber < 1 || bracketNumber > 6) {
            throw new IllegalArgumentException(
                    "A faixa deve estar entre 1 e 6.");
        }

        shares = List.copyOf(
                shares);

        if (shares.isEmpty()) {
            throw new IllegalArgumentException(
                    "A regra deve possuir componentes tributários.");
        }

        validateShares(
                shares);
    }

    public ComponentShare find(
            TaxComponent component) {
        Objects.requireNonNull(
                component,
                "O componente pesquisado não pode ser nulo.");

        return shares
                .stream()
                .filter(
                        share -> share.component() == component)
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "O componente "
                                        + component
                                        + " não pertence à repartição."));
    }

    private static void validateShares(
            List<ComponentShare> shares) {
        Set<TaxComponent> components = new HashSet<>();

        BigDecimal total = BigDecimal.ZERO;

        for (ComponentShare share : shares) {
            Objects.requireNonNull(
                    share,
                    "A repartição não pode conter item nulo.");

            if (!components.add(
                    share.component())) {
                throw new IllegalArgumentException(
                        "O componente "
                                + share.component()
                                + " aparece mais de uma vez.");
            }

            total = total.add(
                    share.distributionRate());
        }

        if (total.compareTo(
                BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException(
                    "Os percentuais de repartição devem totalizar 100%.");
        }
    }

    public record ComponentShare(
            TaxComponent component,
            BigDecimal distributionRate) {

        public ComponentShare {
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
}