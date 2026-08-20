package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;

import java.util.Objects;
import java.util.Optional;

public record SimplesRevenueClassificationResult(
        RevenueEntry revenue,
        SimplesRevenueClassificationStatus status,
        Optional<SimplesRevenueTaxRoute> route,
        String explanation,
        TaxDecision decision) {

    public SimplesRevenueClassificationResult {
        Objects.requireNonNull(
                revenue,
                "A receita não pode ser nula.");

        Objects.requireNonNull(
                status,
                "O status da classificação não pode ser nulo.");

        Objects.requireNonNull(
                route,
                "A rota tributária opcional não pode ser nula.");

        Objects.requireNonNull(
                explanation,
                "A explicação não pode ser nula.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        explanation = explanation.trim();

        if (explanation.isBlank()) {
            throw new IllegalArgumentException(
                    "A explicação não pode estar vazia.");
        }

        if (status == SimplesRevenueClassificationStatus.RESOLVED
                && route.isEmpty()) {

            throw new IllegalArgumentException(
                    "Uma classificação resolvida deve possuir rota tributária.");
        }

        if (status != SimplesRevenueClassificationStatus.RESOLVED
                && route.isPresent()) {

            throw new IllegalArgumentException(
                    "Uma classificação pendente "
                            + "não pode possuir rota tributária definitiva.");
        }
    }

    public boolean isResolved() {
        return status == SimplesRevenueClassificationStatus.RESOLVED;
    }

    public boolean requiresAdditionalInformation() {
        return !isResolved();
    }
}