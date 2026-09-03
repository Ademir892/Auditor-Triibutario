package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.TaxComponentAllocation;
import br.com.auditortributario.taxrule.domain.TaxCompositionResult;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimplesRevenueTaxAllocationCalculatorTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesRevenueTaxAllocationCalculator calculator = new SimplesRevenueTaxAllocationCalculator();

    @Test
    void shouldUseStandardAnnexIVDistributionBelowIssCapTrigger() {
        List<TaxComponentAllocation> allocations = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_IV,
                5,
                COMPETENCE,
                new BigDecimal("10000.00"),
                new BigDecimal("0.1200"));

        TaxComponentAllocation iss = find(
                allocations,
                TaxComponent.ISS);

        assertEquals(
                0,
                new BigDecimal("0.04800")
                        .compareTo(
                                iss.effectiveRate()));

        assertEquals(
                new BigDecimal("480.00"),
                iss
                        .amount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP));
    }

    @Test
    void shouldCapAnnexIVIssAtFivePercent() {
        List<TaxComponentAllocation> allocations = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_IV,
                5,
                COMPETENCE,
                new BigDecimal("10000.00"),
                new BigDecimal("0.12811"));

        TaxComponentAllocation iss = find(
                allocations,
                TaxComponent.ISS);

        assertEquals(
                0,
                new BigDecimal("0.05")
                        .compareTo(
                                iss.effectiveRate()));

        assertEquals(
                new BigDecimal("500.00"),
                iss
                        .amount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP));
    }

    @Test
    void shouldRedistributeAnnexIVIssExcessToFederalComponents() {
        List<TaxComponentAllocation> allocations = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_IV,
                5,
                COMPETENCE,
                new BigDecimal("10000.00"),
                new BigDecimal("0.12811"));

        assertAmount(
                allocations,
                TaxComponent.IRPJ,
                "244.72");

        assertAmount(
                allocations,
                TaxComponent.CSLL,
                "249.95");

        assertAmount(
                allocations,
                TaxComponent.COFINS,
                "235.35");

        assertAmount(
                allocations,
                TaxComponent.PIS_PASEP,
                "51.08");

        assertAmount(
                allocations,
                TaxComponent.ISS,
                "500.00");
    }

    @Test
    void shouldPreserveTotalTaxAmountAfterAnnexIVRedistribution() {
        List<TaxComponentAllocation> allocations = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_IV,
                5,
                COMPETENCE,
                new BigDecimal("10000.00"),
                new BigDecimal("0.12811"));

        BigDecimal total = allocations
                .stream()
                .map(
                        TaxComponentAllocation::amount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        assertEquals(
                new BigDecimal("1281.10"),
                total.setScale(
                        2,
                        RoundingMode.HALF_UP));
    }

    @Test
    void shouldKeepAnnexINormalDistributionUnchanged() {
        List<TaxComponentAllocation> allocations = calculator.calculate(
                SimplesRevenueTaxRoute.ANNEX_I,
                2,
                COMPETENCE,
                new BigDecimal("10000.00"),
                new BigDecimal("0.0532"));

        TaxComponentAllocation icms = find(
                allocations,
                TaxComponent.ICMS);

        assertEquals(
                new BigDecimal("180.88"),
                icms
                        .amount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP));
    }

    @Test
    void shouldFullyAllocateAnnexIFourthBracketWithoutPrecisionRemainder() {
        assertFullyAllocated(
                SimplesRevenueTaxRoute.ANNEX_I,
                4,
                new BigDecimal("1700000.00"),
                new BigDecimal("0.1075882352941176470588235294117647"));
    }

    @Test
    void shouldFullyAllocateAnnexIFifthBracketWithoutPrecisionRemainder() {
        assertFullyAllocated(
                SimplesRevenueTaxRoute.ANNEX_I,
                5,
                new BigDecimal("3500000.00"),
                new BigDecimal("0.1128"));
    }

    private void assertFullyAllocated(
            SimplesRevenueTaxRoute route,
            int bracket,
            BigDecimal revenue,
            BigDecimal effectiveRate) {
        List<TaxComponentAllocation> allocations = calculator.calculate(
                route,
                bracket,
                COMPETENCE,
                revenue,
                effectiveRate);

        TaxCompositionResult composition = new TaxCompositionResult(
                revenue.multiply(effectiveRate, java.math.MathContext.DECIMAL128),
                allocations);

        assertEquals(0, composition.unallocatedAmount().compareTo(BigDecimal.ZERO));
    }

    private void assertAmount(
            List<TaxComponentAllocation> allocations,
            TaxComponent component,
            String expectedAmount) {
        TaxComponentAllocation allocation = find(
                allocations,
                component);

        assertEquals(
                new BigDecimal(
                        expectedAmount),
                allocation
                        .amount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP));
    }

    private TaxComponentAllocation find(
            List<TaxComponentAllocation> allocations,
            TaxComponent component) {
        return allocations
                .stream()
                .filter(
                        allocation -> allocation.component() == component)
                .findFirst()
                .orElseThrow();
    }
}
