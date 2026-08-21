package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.revenue.CompetenceRevenue;
import br.com.auditortributario.taxrule.domain.revenue.RevenueActivityType;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueOrigin;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesCompetenceAnnexIVIntegrationTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesCompetenceRevenueTaxProcessor processor = new SimplesCompetenceRevenueTaxProcessor();

    @Test
    void shouldProcessAnnexIVInsideCompetence() {
        RevenueEntry legalService = serviceRevenue(
                "10000.00",
                "Serviços advocatícios");

        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        legalService));

        SimplesCompetenceTaxContext context = SimplesCompetenceTaxContext
                .withoutFatorR(
                        new BigDecimal(
                                "300000.00"))
                .withServiceTaxRule(
                        legalService,
                        SimplesServiceTaxRule.ANNEX_IV_LEGAL_SERVICES);

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                context);

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.COMPLETED,
                result.status());

        assertTrue(
                result.isFinal());

        assertTrue(
                result.hasExternalObligations());

        assertEquals(
                new BigDecimal(
                        "630.00"),
                result
                        .finalTaxAmount()
                        .orElseThrow()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP));

        SimplesCompetenceRevenueItemResult item = result
                .items()
                .getFirst();

        assertEquals(
                SimplesCompetenceRevenueItemStatus.COMPLETED_WITH_EXTERNAL_OBLIGATION,
                item.status());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_IV,
                item
                        .classification()
                        .route()
                        .orElseThrow());

        assertTrue(
                item
                        .taxResult()
                        .orElseThrow() instanceof SimplesAnnexIVRevenueTaxResult);

        assertTrue(
                hasComponent(
                        result,
                        TaxComponent.ISS));

        assertFalse(
                hasComponent(
                        result,
                        TaxComponent.CPP));
    }

    @Test
    void shouldConsolidateAnnexesIAndIIAndIVInSameCompetence() {
        RevenueEntry commerce = standardRevenue(
                "10000.00",
                RevenueActivityType.COMMERCE,
                "Receita comercial");

        RevenueEntry industry = standardRevenue(
                "5000.00",
                RevenueActivityType.INDUSTRY,
                "Receita industrial");

        RevenueEntry legalService = serviceRevenue(
                "5000.00",
                "Serviços advocatícios");

        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        commerce,
                        industry,
                        legalService));

        SimplesCompetenceTaxContext context = SimplesCompetenceTaxContext
                .withoutFatorR(
                        new BigDecimal(
                                "300000.00"))
                .withServiceTaxRule(
                        legalService,
                        SimplesServiceTaxRule.ANNEX_IV_LEGAL_SERVICES);

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                context);

        /*
         * Anexo I
         *
         * (300.000 × 7,30% - 5.940) / 300.000
         * = 5,32%
         *
         * 10.000 × 5,32%
         * = 532,00
         *
         *
         * Anexo II
         *
         * (300.000 × 7,80% - 5.940) / 300.000
         * = 5,82%
         *
         * 5.000 × 5,82%
         * = 291,00
         *
         *
         * Anexo IV
         *
         * (300.000 × 9,00% - 8.100) / 300.000
         * = 6,30%
         *
         * 5.000 × 6,30%
         * = 315,00
         *
         *
         * Total DAS
         *
         * 532,00 + 291,00 + 315,00
         * = 1.138,00
         */

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.COMPLETED,
                result.status());

        assertTrue(
                result.isFinal());

        assertTrue(
                result.hasExternalObligations());

        assertEquals(
                new BigDecimal(
                        "1138.00"),
                result
                        .finalTaxAmount()
                        .orElseThrow()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP));

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_I,
                result
                        .items()
                        .get(0)
                        .classification()
                        .route()
                        .orElseThrow());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_II,
                result
                        .items()
                        .get(1)
                        .classification()
                        .route()
                        .orElseThrow());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_IV,
                result
                        .items()
                        .get(2)
                        .classification()
                        .route()
                        .orElseThrow());

        assertEquals(
                SimplesCompetenceRevenueItemStatus.COMPLETED_WITH_EXTERNAL_OBLIGATION,
                result
                        .items()
                        .get(2)
                        .status());

        assertTrue(
                hasComponent(
                        result,
                        TaxComponent.ICMS));

        assertTrue(
                hasComponent(
                        result,
                        TaxComponent.IPI));

        assertTrue(
                hasComponent(
                        result,
                        TaxComponent.ISS));
    }

    private RevenueEntry standardRevenue(
            String amount,
            RevenueActivityType activityType,
            String description) {
        return RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        amount),
                activityType,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                description);
    }

    private RevenueEntry serviceRevenue(
            String amount,
            String description) {
        return RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        amount),
                RevenueActivityType.SERVICE,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                description);
    }

    private boolean hasComponent(
            SimplesCompetenceRevenueTaxResult result,
            TaxComponent component) {
        return result
                .componentTotals()
                .stream()
                .anyMatch(
                        total -> total.component() == component);
    }
}