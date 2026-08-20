package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.TaxComponentAllocation;
import br.com.auditortributario.taxrule.domain.TaxCompositionResult;
import br.com.auditortributario.taxrule.domain.revenue.RevenueActivityType;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueOrigin;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxTreatment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SimplesRevenueTaxCompositionAdjusterSafetyTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesRevenueTaxCompositionAdjuster adjuster = new SimplesRevenueTaxCompositionAdjuster();

    @Test
    void shouldRejectPartiallyAllocatedComposition() {
        RevenueEntry revenue = RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        "10000.00"),
                RevenueActivityType.COMMERCE,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "Receita comercial");

        TaxCompositionResult partialComposition = new TaxCompositionResult(
                new BigDecimal(
                        "1000.00"),
                List.of(
                        allocation(
                                TaxComponent.IRPJ,
                                "1.00",
                                "0.01",
                                "100.00")));

        assertThrows(
                IllegalArgumentException.class,
                () -> adjuster.adjust(
                        revenue,
                        partialComposition));
    }

    @Test
    void shouldRejectTreatmentForComponentAbsentFromComposition() {
        RevenueEntry revenue = RevenueEntry.create(
                COMPETENCE,
                new BigDecimal(
                        "10000.00"),
                RevenueActivityType.COMMERCE,
                false,
                Set.of(
                        RevenueTaxTreatment.ISS_WITHHELD),
                RevenueOrigin.MANUAL_ENTRY,
                "Receita comercial marcada incorretamente "
                        + "como ISS retido");

        TaxCompositionResult composition = new TaxCompositionResult(
                new BigDecimal(
                        "100.00"),
                List.of(
                        allocation(
                                TaxComponent.ICMS,
                                "1.00",
                                "0.01",
                                "100.00")));

        assertThrows(
                IllegalArgumentException.class,
                () -> adjuster.adjust(
                        revenue,
                        composition));
    }

    private TaxComponentAllocation allocation(
            TaxComponent component,
            String distributionRate,
            String effectiveRate,
            String amount) {
        return new TaxComponentAllocation(
                component,
                new BigDecimal(
                        distributionRate),
                new BigDecimal(
                        effectiveRate),
                new BigDecimal(
                        amount));
    }
}