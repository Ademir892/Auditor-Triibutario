package br.com.auditortributario.taxrule.simples.composition;

import br.com.auditortributario.taxrule.domain.TaxComponent;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record SimplesTaxDistributionRule(
        int bracketNumber,
        List<SimplesTaxDistributionShare> shares,
        Optional<SimplesIssCapRule> issCapRule) {

    public SimplesTaxDistributionRule {
        if (bracketNumber < 1
                || bracketNumber > 6) {

            throw new IllegalArgumentException(
                    "A faixa deve estar entre 1 e 6.");
        }

        Objects.requireNonNull(
                shares,
                "A repartição não pode ser nula.");

        Objects.requireNonNull(
                issCapRule,
                "A regra opcional do ISS não pode ser nula.");

        shares = List.copyOf(
                shares);

        if (shares.isEmpty()) {
            throw new IllegalArgumentException(
                    "A repartição deve possuir componentes.");
        }

        validateShares(
                shares);
    }

    public SimplesTaxDistributionShare find(
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
                                        + " não faz parte da repartição."));
    }

    private static void validateShares(
            List<SimplesTaxDistributionShare> shares) {
        Set<TaxComponent> components = new HashSet<>();

        BigDecimal total = BigDecimal.ZERO;

        for (SimplesTaxDistributionShare share : shares) {
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
                    "Os percentuais de repartição "
                            + "devem totalizar 100%.");
        }
    }
}