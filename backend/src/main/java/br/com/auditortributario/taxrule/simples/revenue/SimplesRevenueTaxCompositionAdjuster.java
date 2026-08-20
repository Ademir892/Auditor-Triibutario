package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponentAllocation;
import br.com.auditortributario.taxrule.domain.TaxCompositionResult;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxComponentTreatment;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxTreatmentEffect;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SimplesRevenueTaxCompositionAdjuster {

    private final SimplesRevenueTaxTreatmentResolver treatmentResolver;

    public SimplesRevenueTaxCompositionAdjuster() {
        this.treatmentResolver = new SimplesRevenueTaxTreatmentResolver();
    }

    public SimplesRevenueTaxAdjustmentResult adjust(
            RevenueEntry revenue,
            TaxCompositionResult originalComposition) {
        Objects.requireNonNull(
                revenue,
                "A receita não pode ser nula.");

        Objects.requireNonNull(
                originalComposition,
                "A composição original não pode ser nula.");

        List<RevenueTaxComponentTreatment> treatments = treatmentResolver.resolve(
                revenue);

        if (treatments.isEmpty()) {
            return noAdjustment(
                    revenue,
                    originalComposition);
        }

        List<RevenueTaxComponentTreatment> pendingTreatments = treatments
                .stream()
                .filter(
                        this::requiresAdditionalRule)
                .toList();

        boolean hasExternalObligation = treatments
                .stream()
                .anyMatch(
                        treatment -> treatment.effect() == RevenueTaxTreatmentEffect.REQUIRE_EXTERNAL_CALCULATION);

        List<SimplesAdjustedTaxComponent> adjustedComponents = new ArrayList<>();

        for (TaxComponentAllocation allocation : originalComposition.allocations()) {

            List<RevenueTaxComponentTreatment> componentTreatments = treatments
                    .stream()
                    .filter(
                            treatment -> treatment.component() == allocation.component())
                    .toList();

            boolean exclude = componentTreatments
                    .stream()
                    .anyMatch(
                            this::excludesFromSimples);

            BigDecimal adjustedEffectiveRate = exclude
                    ? BigDecimal.ZERO
                    : allocation.effectiveRate();

            BigDecimal adjustedAmount = exclude
                    ? BigDecimal.ZERO
                    : allocation.amount();

            adjustedComponents.add(
                    new SimplesAdjustedTaxComponent(
                            allocation.component(),
                            allocation.effectiveRate(),
                            adjustedEffectiveRate,
                            allocation.amount(),
                            adjustedAmount,
                            componentTreatments));
        }

        if (!pendingTreatments.isEmpty()) {
            return new SimplesRevenueTaxAdjustmentResult(
                    revenue,
                    SimplesRevenueTaxAdjustmentStatus.REQUIRES_ADDITIONAL_RULES,
                    originalComposition.totalTaxAmount(),
                    Optional.empty(),
                    adjustedComponents,
                    pendingTreatments);
        }

        BigDecimal adjustedTotal = adjustedComponents
                .stream()
                .map(
                        SimplesAdjustedTaxComponent::adjustedAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        SimplesRevenueTaxAdjustmentStatus status = hasExternalObligation
                ? SimplesRevenueTaxAdjustmentStatus.APPLIED_WITH_EXTERNAL_OBLIGATION
                : SimplesRevenueTaxAdjustmentStatus.APPLIED;

        return new SimplesRevenueTaxAdjustmentResult(
                revenue,
                status,
                originalComposition.totalTaxAmount(),
                Optional.of(
                        adjustedTotal),
                adjustedComponents,
                List.of());
    }

    private SimplesRevenueTaxAdjustmentResult noAdjustment(
            RevenueEntry revenue,
            TaxCompositionResult originalComposition) {
        List<SimplesAdjustedTaxComponent> components = originalComposition
                .allocations()
                .stream()
                .map(
                        allocation -> new SimplesAdjustedTaxComponent(
                                allocation.component(),
                                allocation.effectiveRate(),
                                allocation.effectiveRate(),
                                allocation.amount(),
                                allocation.amount(),
                                List.of()))
                .toList();

        return new SimplesRevenueTaxAdjustmentResult(
                revenue,
                SimplesRevenueTaxAdjustmentStatus.NO_ADJUSTMENT,
                originalComposition.totalTaxAmount(),
                Optional.of(
                        originalComposition.totalTaxAmount()),
                components,
                List.of());
    }

    private boolean excludesFromSimples(
            RevenueTaxComponentTreatment treatment) {
        return treatment.effect() == RevenueTaxTreatmentEffect.EXCLUDE_COMPONENT
                || treatment.effect() == RevenueTaxTreatmentEffect.WITHHOLD_COMPONENT;
    }

    private boolean requiresAdditionalRule(
            RevenueTaxComponentTreatment treatment) {
        return treatment.effect() == RevenueTaxTreatmentEffect.REQUIRE_SPECIAL_CALCULATION
                || treatment.effect() == RevenueTaxTreatmentEffect.REDUCE_COMPONENT;
    }
}