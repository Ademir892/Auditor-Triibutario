package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.TaxComponentAllocation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SimplesRevenueTaxAllocationCalculator {

        private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

        private static final BigDecimal ANNEX_IV_ISS_CAP_TRIGGER = new BigDecimal("0.125");

        private static final BigDecimal ISS_MAXIMUM_EFFECTIVE_RATE = new BigDecimal("0.05");

        private static final List<FederalRedistributionShare> ANNEX_IV_FEDERAL_REDISTRIBUTION = List.of(
                        new FederalRedistributionShare(
                                        TaxComponent.IRPJ,
                                        new BigDecimal("0.3133")),
                        new FederalRedistributionShare(
                                        TaxComponent.CSLL,
                                        new BigDecimal("0.3200")),
                        new FederalRedistributionShare(
                                        TaxComponent.COFINS,
                                        new BigDecimal("0.3013")),
                        new FederalRedistributionShare(
                                        TaxComponent.PIS_PASEP,
                                        new BigDecimal("0.0654")));

        private final SimplesRevenueTaxDistributionRegistry distributionRegistry;

        public SimplesRevenueTaxAllocationCalculator() {
                this(
                                new SimplesRevenueTaxDistributionRegistry());
        }

        SimplesRevenueTaxAllocationCalculator(
                        SimplesRevenueTaxDistributionRegistry distributionRegistry) {
                this.distributionRegistry = Objects.requireNonNull(
                                distributionRegistry,
                                "O registro de repartição tributária "
                                                + "não pode ser nulo.");
        }

        public List<TaxComponentAllocation> calculate(
                        SimplesRevenueTaxRoute route,
                        int bracketNumber,
                        YearMonth competence,
                        BigDecimal revenueAmount,
                        BigDecimal totalEffectiveRate) {
                Objects.requireNonNull(
                                route,
                                "A rota tributária não pode ser nula.");

                Objects.requireNonNull(
                                competence,
                                "A competência não pode ser nula.");

                Objects.requireNonNull(
                                revenueAmount,
                                "O valor da receita não pode ser nulo.");

                Objects.requireNonNull(
                                totalEffectiveRate,
                                "A alíquota efetiva não pode ser nula.");

                if (revenueAmount.compareTo(
                                BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                        "O valor da receita não pode ser negativo.");
                }

                if (totalEffectiveRate.compareTo(
                                BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                        "A alíquota efetiva não pode ser negativa.");
                }

                SimplesRevenueTaxDistributionRule rule = distributionRegistry.find(
                                route,
                                bracketNumber,
                                competence);

                if (requiresAnnexIVIssCap(
                                route,
                                bracketNumber,
                                totalEffectiveRate)) {
                        return createAnnexIVCappedAllocations(
                                        revenueAmount,
                                        totalEffectiveRate);
                }

                return createStandardAllocations(
                                revenueAmount,
                                totalEffectiveRate,
                                rule);
        }

        public String distributionVersion() {
                return distributionRegistry.version();
        }

        private boolean requiresAnnexIVIssCap(
                        SimplesRevenueTaxRoute route,
                        int bracketNumber,
                        BigDecimal totalEffectiveRate) {
                return route == SimplesRevenueTaxRoute.ANNEX_IV
                                && bracketNumber == 5
                                && totalEffectiveRate.compareTo(
                                                ANNEX_IV_ISS_CAP_TRIGGER) > 0;
        }

        private List<TaxComponentAllocation> createStandardAllocations(
                        BigDecimal revenueAmount,
                        BigDecimal totalEffectiveRate,
                        SimplesRevenueTaxDistributionRule rule) {
                List<TaxComponentAllocation> allocations = new ArrayList<>();

                for (SimplesRevenueTaxDistributionRule.ComponentShare share : rule.shares()) {

                        BigDecimal componentEffectiveRate = totalEffectiveRate.multiply(
                                        share.distributionRate(),
                                        MATH_CONTEXT);

                        BigDecimal componentAmount = revenueAmount.multiply(
                                        componentEffectiveRate,
                                        MATH_CONTEXT);

                        allocations.add(
                                        new TaxComponentAllocation(
                                                        share.component(),
                                                        share.distributionRate(),
                                                        componentEffectiveRate,
                                                        componentAmount));
                }

                return List.copyOf(
                                allocations);
        }

        private List<TaxComponentAllocation> createAnnexIVCappedAllocations(
                        BigDecimal revenueAmount,
                        BigDecimal totalEffectiveRate) {

                List<TaxComponentAllocation> allocations = new ArrayList<>();

                BigDecimal federalEffectiveRate = totalEffectiveRate.subtract(
                                ISS_MAXIMUM_EFFECTIVE_RATE,
                                MATH_CONTEXT);

                for (FederalRedistributionShare redistributionShare : ANNEX_IV_FEDERAL_REDISTRIBUTION) {

                        BigDecimal componentEffectiveRate = federalEffectiveRate.multiply(
                                        redistributionShare.redistributionRate(),
                                        MATH_CONTEXT);

                        BigDecimal finalDistributionRate = componentEffectiveRate.divide(
                                        totalEffectiveRate,
                                        MATH_CONTEXT);

                        BigDecimal componentAmount = revenueAmount.multiply(
                                        componentEffectiveRate,
                                        MATH_CONTEXT);

                        allocations.add(
                                        new TaxComponentAllocation(
                                                        redistributionShare.component(),
                                                        finalDistributionRate,
                                                        componentEffectiveRate,
                                                        componentAmount));
                }

                BigDecimal issDistributionRate = ISS_MAXIMUM_EFFECTIVE_RATE.divide(
                                totalEffectiveRate,
                                MATH_CONTEXT);

                BigDecimal issAmount = revenueAmount.multiply(
                                ISS_MAXIMUM_EFFECTIVE_RATE,
                                MATH_CONTEXT);

                allocations.add(
                                new TaxComponentAllocation(
                                                TaxComponent.ISS,
                                                issDistributionRate,
                                                ISS_MAXIMUM_EFFECTIVE_RATE,
                                                issAmount));

                return List.copyOf(
                                allocations);
        }

        private record FederalRedistributionShare(
                        TaxComponent component,
                        BigDecimal redistributionRate) {

                private FederalRedistributionShare {
                        Objects.requireNonNull(
                                        component,
                                        "O componente tributário não pode ser nulo.");

                        Objects.requireNonNull(
                                        redistributionRate,
                                        "O percentual de redistribuição "
                                                        + "não pode ser nulo.");

                        if (redistributionRate.compareTo(
                                        BigDecimal.ZERO) < 0
                                        || redistributionRate.compareTo(
                                                        BigDecimal.ONE) > 0) {

                                throw new IllegalArgumentException(
                                                "O percentual de redistribuição "
                                                                + "deve estar entre zero e um.");
                        }
                }
        }
}