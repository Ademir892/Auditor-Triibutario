package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxComponentTreatment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record SimplesAdjustedTaxComponent(
        TaxComponent component,
        BigDecimal originalEffectiveRate,
        BigDecimal adjustedEffectiveRate,
        BigDecimal originalAmount,
        BigDecimal adjustedAmount,
        List<RevenueTaxComponentTreatment> appliedTreatments) {

    public SimplesAdjustedTaxComponent {
        Objects.requireNonNull(
                component,
                "O componente tributário não pode ser nulo.");

        Objects.requireNonNull(
                originalEffectiveRate,
                "A alíquota efetiva original não pode ser nula.");

        Objects.requireNonNull(
                adjustedEffectiveRate,
                "A alíquota efetiva ajustada não pode ser nula.");

        Objects.requireNonNull(
                originalAmount,
                "O valor original não pode ser nulo.");

        Objects.requireNonNull(
                adjustedAmount,
                "O valor ajustado não pode ser nulo.");

        Objects.requireNonNull(
                appliedTreatments,
                "Os tratamentos aplicados não podem ser nulos.");

        if (originalEffectiveRate.compareTo(
                BigDecimal.ZERO) < 0
                || adjustedEffectiveRate.compareTo(
                        BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "As alíquotas efetivas não podem ser negativas.");
        }

        if (originalAmount.compareTo(
                BigDecimal.ZERO) < 0
                || adjustedAmount.compareTo(
                        BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Os valores tributários não podem ser negativos.");
        }

        appliedTreatments = List.copyOf(
                appliedTreatments);
    }

    public BigDecimal reductionAmount() {
        return originalAmount.subtract(
                adjustedAmount);
    }

    public boolean wasAdjusted() {
        return originalAmount.compareTo(
                adjustedAmount) != 0
                || originalEffectiveRate.compareTo(
                        adjustedEffectiveRate) != 0;
    }
}