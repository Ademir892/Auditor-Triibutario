package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxComponentTreatment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SimplesRevenueTaxAdjustmentResult(
        RevenueEntry revenue,
        SimplesRevenueTaxAdjustmentStatus status,
        BigDecimal originalSimplesAmount,
        Optional<BigDecimal> adjustedSimplesAmount,
        List<SimplesAdjustedTaxComponent> components,
        List<RevenueTaxComponentTreatment> pendingTreatments) {

    public SimplesRevenueTaxAdjustmentResult {
        Objects.requireNonNull(
                revenue,
                "A receita não pode ser nula.");

        Objects.requireNonNull(
                status,
                "O status do ajuste não pode ser nulo.");

        Objects.requireNonNull(
                originalSimplesAmount,
                "O valor original do Simples não pode ser nulo.");

        Objects.requireNonNull(
                adjustedSimplesAmount,
                "O valor ajustado opcional não pode ser nulo.");

        Objects.requireNonNull(
                components,
                "Os componentes não podem ser nulos.");

        Objects.requireNonNull(
                pendingTreatments,
                "Os tratamentos pendentes não podem ser nulos.");

        components = List.copyOf(
                components);

        pendingTreatments = List.copyOf(
                pendingTreatments);

        if (status == SimplesRevenueTaxAdjustmentStatus.REQUIRES_ADDITIONAL_RULES
                && adjustedSimplesAmount.isPresent()) {

            throw new IllegalArgumentException(
                    "Um resultado pendente de regras adicionais "
                            + "não pode declarar valor final ajustado.");
        }
    }

    public Optional<BigDecimal> reductionAmount() {
        return adjustedSimplesAmount.map(
                adjusted -> originalSimplesAmount.subtract(
                        adjusted));
    }

    public boolean isFinal() {
        return adjustedSimplesAmount.isPresent();
    }
}