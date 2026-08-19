package br.com.auditortributario.taxrule.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record TaxCompositionResult(
        BigDecimal totalTaxAmount,
        List<TaxComponentAllocation> allocations) {

    public TaxCompositionResult {
        Objects.requireNonNull(
                totalTaxAmount,
                "O valor total do tributo não pode ser nulo.");

        Objects.requireNonNull(
                allocations,
                "A composição tributária não pode ser nula.");

        if (totalTaxAmount.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "O valor total do tributo não pode ser negativo.");
        }

        if (allocations.stream().anyMatch(
                Objects::isNull)) {

            throw new IllegalArgumentException(
                    "A composição não pode conter itens nulos.");
        }

        validateDuplicateComponents(
                allocations);

        BigDecimal allocatedAmount = calculateAllocatedAmount(
                allocations);

        if (allocatedAmount.compareTo(
                totalTaxAmount) > 0) {

            throw new IllegalArgumentException(
                    "A soma dos componentes tributários "
                            + "não pode exceder o valor total.");
        }

        allocations = List.copyOf(
                allocations);
    }

    public BigDecimal allocatedAmount() {
        return calculateAllocatedAmount(
                allocations);
    }

    public BigDecimal unallocatedAmount() {
        return totalTaxAmount
                .subtract(
                        allocatedAmount());
    }

    public boolean isFullyAllocated() {
        return unallocatedAmount()
                .compareTo(
                        BigDecimal.ZERO) == 0;
    }

    public Optional<TaxComponentAllocation> find(
            TaxComponent component) {
        Objects.requireNonNull(
                component,
                "O componente pesquisado não pode ser nulo.");

        return allocations
                .stream()
                .filter(
                        allocation -> allocation
                                .component()
                                .equals(
                                        component))
                .findFirst();
    }

    public BigDecimal totalTaxAmountForDisplay() {
        return totalTaxAmount.setScale(
                2,
                RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateAllocatedAmount(
            List<TaxComponentAllocation> allocations) {
        return allocations
                .stream()
                .map(
                        TaxComponentAllocation::amount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);
    }

    private static void validateDuplicateComponents(
            List<TaxComponentAllocation> allocations) {
        Set<TaxComponent> components = new HashSet<>();

        for (TaxComponentAllocation allocation : allocations) {
            if (!components.add(
                    allocation.component())) {
                throw new IllegalArgumentException(
                        "O componente tributário "
                                + allocation.component()
                                + " aparece mais de uma vez.");
            }
        }
    }
}