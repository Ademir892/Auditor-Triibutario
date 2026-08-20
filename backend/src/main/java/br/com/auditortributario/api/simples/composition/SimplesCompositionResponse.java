package br.com.auditortributario.api.simples.composition;

import br.com.auditortributario.application.simples.composition.SimplesCompositionResult;
import br.com.auditortributario.taxrule.domain.TaxComponentAllocation;
import br.com.auditortributario.taxrule.simples.composition.SimplesTaxCompositionResult;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record SimplesCompositionResponse(
        YearMonth assessmentPeriod,
        BigDecimal taxableRevenue,
        BigDecimal totalEffectiveRate,
        BigDecimal totalEffectiveRatePercentage,
        BigDecimal totalTaxAmount,
        String taxStatus,
        String annex,
        String annexLabel,
        int bracketNumber,
        String distributionTableVersion,
        boolean issCapApplied,
        boolean fullyAllocated,
        BigDecimal unallocatedAmount,
        List<ComponentResponse> components) {

    public SimplesCompositionResponse {
        components = List.copyOf(
                components);
    }

    public static SimplesCompositionResponse from(
            SimplesCompositionResult result) {
        SimplesTaxCompositionResult compositionResult = result.compositionResult();

        var estimated = result.estimatedTaxResult();

        var effectiveRate = estimated.effectiveRateResult();

        var selection = effectiveRate.bracketSelectionResult();

        var annex = selection
                .fatorRResult()
                .annex();

        List<ComponentResponse> components = compositionResult
                .composition()
                .allocations()
                .stream()
                .map(
                        ComponentResponse::from)
                .toList();

        return new SimplesCompositionResponse(
                estimated
                        .taxableRevenue()
                        .period(),

                estimated
                        .taxableRevenue()
                        .amount(),

                effectiveRate
                        .effectiveRate(),

                effectiveRate
                        .effectiveRateAsPercentage(),

                estimated
                        .estimatedTaxAmount(),

                estimated
                        .status()
                        .name(),

                annex.name(),

                annex.getDisplayName(),

                selection
                        .bracket()
                        .number(),

                compositionResult
                        .tableVersion(),

                compositionResult
                        .issCapApplied(),

                compositionResult
                        .composition()
                        .isFullyAllocated(),

                compositionResult
                        .composition()
                        .unallocatedAmount(),

                components);
    }

    public record ComponentResponse(
            String code,
            String name,
            String description,
            String jurisdiction,
            String jurisdictionLabel,
            BigDecimal distributionRate,
            BigDecimal distributionRatePercentage,
            BigDecimal effectiveRate,
            BigDecimal effectiveRatePercentage,
            BigDecimal amount) {

        public static ComponentResponse from(
                TaxComponentAllocation allocation) {
            var component = allocation.component();

            return new ComponentResponse(
                    component.name(),

                    component.getDisplayName(),

                    component.getDescription(),

                    component
                            .getJurisdiction()
                            .name(),

                    component
                            .getJurisdiction()
                            .getDisplayName(),

                    allocation
                            .distributionRate(),

                    allocation
                            .distributionRateAsPercentage(),

                    allocation
                            .effectiveRate(),

                    allocation
                            .effectiveRateAsPercentage(),

                    allocation
                            .amountForDisplay());
        }
    }
}