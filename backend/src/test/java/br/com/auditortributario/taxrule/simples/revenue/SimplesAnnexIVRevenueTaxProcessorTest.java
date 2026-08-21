package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.revenue.RevenueActivityType;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueOrigin;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesAnnexIVRevenueTaxProcessorTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesAnnexIVRevenueTaxProcessor processor = new SimplesAnnexIVRevenueTaxProcessor();

    @Test
    void shouldProcessAnnexIVRevenueAndFlagCppOutsideDas() {
        RevenueEntry revenue = serviceRevenue(
                "10000.00");

        SimplesAnnexIVRevenueTaxResult result = processor.process(
                revenue,
                new BigDecimal(
                        "300000.00"),
                SimplesServiceTaxRule.ANNEX_IV_LEGAL_SERVICES);

        assertTrue(
                result.classification()
                        .isResolved());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_IV,
                result
                        .classification()
                        .route()
                        .orElseThrow());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_IV,
                result.baseCalculation()
                        .route());

        assertEquals(
                2,
                result
                        .baseCalculation()
                        .bracket()
                        .number());

        assertEquals(
                0,
                new BigDecimal(
                        "0.063").compareTo(
                                result
                                        .baseCalculation()
                                        .effectiveRate()));

        assertEquals(
                new BigDecimal(
                        "630.00"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertTrue(
                result.isFinal());

        assertTrue(
                result.hasExternalObligation());

        assertTrue(
                result.hasCppOutsideDas());

        assertFalse(
                hasComponent(
                        result,
                        TaxComponent.CPP));

        assertTrue(
                hasComponent(
                        result,
                        TaxComponent.ISS));

        assertEquals(
                new BigDecimal(
                        "252.00"),
                adjustedAmount(
                        result,
                        TaxComponent.ISS));
    }

    @Test
    void shouldProcessConstructionServiceThroughSameAnnexIVEngine() {
        RevenueEntry revenue = serviceRevenue(
                "20000.00");

        SimplesAnnexIVRevenueTaxResult result = processor.process(
                revenue,
                new BigDecimal(
                        "300000.00"),
                SimplesServiceTaxRule.ANNEX_IV_CONSTRUCTION_ENGINEERING);

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_IV,
                result
                        .classification()
                        .route()
                        .orElseThrow());

        assertEquals(
                new BigDecimal(
                        "1260.00"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertTrue(
                result.hasCppOutsideDas());
    }

    @Test
    void shouldProcessSecurityCleaningAndConservationThroughSameEngine() {
        RevenueEntry revenue = serviceRevenue(
                "5000.00");

        SimplesAnnexIVRevenueTaxResult result = processor.process(
                revenue,
                new BigDecimal(
                        "300000.00"),
                SimplesServiceTaxRule.ANNEX_IV_SECURITY_CLEANING_CONSERVATION);

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_IV,
                result
                        .classification()
                        .route()
                        .orElseThrow());

        assertEquals(
                new BigDecimal(
                        "315.00"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertTrue(
                result.hasExternalObligation());
    }

    private RevenueEntry serviceRevenue(
            String amount) {
        return RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        amount),
                RevenueActivityType.SERVICE,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "Serviço enquadrado no Anexo IV");
    }

    private boolean hasComponent(
            SimplesAnnexIVRevenueTaxResult result,
            TaxComponent component) {
        return result
                .adjustedComponents()
                .stream()
                .anyMatch(
                        current -> current.component() == component);
    }

    private BigDecimal adjustedAmount(
            SimplesAnnexIVRevenueTaxResult result,
            TaxComponent component) {
        return result
                .adjustedComponents()
                .stream()
                .filter(
                        current -> current.component() == component)
                .findFirst()
                .orElseThrow()
                .adjustedAmount()
                .setScale(
                        2,
                        RoundingMode.HALF_UP);
    }
}