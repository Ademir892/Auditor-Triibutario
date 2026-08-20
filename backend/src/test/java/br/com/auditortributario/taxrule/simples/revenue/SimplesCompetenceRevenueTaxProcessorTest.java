package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.revenue.CompetenceRevenue;
import br.com.auditortributario.taxrule.domain.revenue.RevenueActivityType;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueOrigin;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxTreatment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesCompetenceRevenueTaxProcessorTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesCompetenceRevenueTaxProcessor processor = new SimplesCompetenceRevenueTaxProcessor();

    @Test
    void shouldProcessCommerceAndIndustryInSameCompetence() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        revenue(
                                "10000.00",
                                RevenueActivityType.COMMERCE,
                                Set.of(),
                                "Comércio"),

                        revenue(
                                "5000.00",
                                RevenueActivityType.INDUSTRY,
                                Set.of(),
                                "Indústria")));

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                new BigDecimal(
                        "500000.00"));

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.COMPLETED,
                result.status());

        assertTrue(
                result.isFinal());

        assertEquals(
                2,
                result.processedCount());

        assertEquals(
                0,
                result.pendingCount());

        assertEquals(
                new BigDecimal(
                        "1034.20"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertFalse(
                result.hasExternalObligations());
    }

    @Test
    void shouldProcessDifferentTreatmentsAcrossSameCompetence() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        revenue(
                                "10000.00",
                                RevenueActivityType.COMMERCE,
                                Set.of(),
                                "Comércio normal"),

                        revenue(
                                "10000.00",
                                RevenueActivityType.COMMERCE,
                                Set.of(
                                        RevenueTaxTreatment.MONOPHASIC),
                                "Comércio monofásico"),

                        revenue(
                                "10000.00",
                                RevenueActivityType.COMMERCE,
                                Set.of(
                                        RevenueTaxTreatment.ICMS_ST_SUBSTITUTED),
                                "Comércio com ICMS-ST")));

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                new BigDecimal(
                        "300000.00"));

        /*
         * Cada receita:
         *
         * normal:
         * 532,00
         *
         * monofásica:
         * 532,00 - PIS/COFINS 15,5%
         * = 449,54
         *
         * ICMS-ST substituído:
         * 532,00 - ICMS 34%
         * = 351,12
         *
         * Total:
         * 1.332,66
         */
        assertEquals(
                new BigDecimal(
                        "1332.66"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.COMPLETED,
                result.status());

        assertEquals(
                3,
                result.processedCount());

        assertEquals(
                0,
                result.pendingCount());
    }

    @Test
    void shouldReturnPartialResultWhenServiceStillRequiresClassification() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        revenue(
                                "10000.00",
                                RevenueActivityType.COMMERCE,
                                Set.of(),
                                "Comércio"),

                        revenue(
                                "5000.00",
                                RevenueActivityType.SERVICE,
                                Set.of(),
                                "Serviço ainda não enquadrado")));

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.PARTIALLY_PROCESSED,
                result.status());

        assertFalse(
                result.isFinal());

        assertTrue(
                result
                        .finalTaxAmount()
                        .isEmpty());

        assertEquals(
                new BigDecimal(
                        "532.00"),
                result.processedTaxAmountForDisplay());

        assertEquals(
                1,
                result.processedCount());

        assertEquals(
                1,
                result.pendingCount());

        assertEquals(
                SimplesCompetenceRevenueItemStatus.REQUIRES_CLASSIFICATION,
                result
                        .items()
                        .get(
                                1)
                        .status());
    }

    @Test
    void shouldRequireAdditionalInformationWhenNothingCanBeFinalized() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        revenue(
                                "10000.00",
                                RevenueActivityType.SERVICE,
                                Set.of(),
                                "Serviço não enquadrado")));

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.REQUIRES_ADDITIONAL_INFORMATION,
                result.status());

        assertEquals(
                BigDecimal.ZERO.setScale(
                        2),
                result.processedTaxAmountForDisplay());

        assertFalse(
                result.isFinal());

        assertEquals(
                0,
                result.processedCount());

        assertEquals(
                1,
                result.pendingCount());
    }

    @Test
    void shouldPreserveExternalObligationAtCompetenceLevel() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        revenue(
                                "10000.00",
                                RevenueActivityType.COMMERCE,
                                Set.of(
                                        RevenueTaxTreatment.ICMS_ST_SUBSTITUTE),
                                "Contribuinte substituto")));

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.COMPLETED,
                result.status());

        assertTrue(
                result.isFinal());

        assertTrue(
                result.hasExternalObligations());

        assertEquals(
                SimplesCompetenceRevenueItemStatus.COMPLETED_WITH_EXTERNAL_OBLIGATION,
                result
                        .items()
                        .getFirst()
                        .status());

        assertEquals(
                new BigDecimal(
                        "532.00"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());
    }

    @Test
    void shouldKeepLegacyGenericTaxSubstitutionPending() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        revenue(
                                "10000.00",
                                RevenueActivityType.COMMERCE,
                                Set.of(
                                        RevenueTaxTreatment.TAX_SUBSTITUTION),
                                "ST sem posição definida")));

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.REQUIRES_ADDITIONAL_INFORMATION,
                result.status());

        assertFalse(
                result.isFinal());

        assertEquals(
                SimplesCompetenceRevenueItemStatus.REQUIRES_ADDITIONAL_RULES,
                result
                        .items()
                        .getFirst()
                        .status());
    }

    @Test
    void shouldConsolidateTaxComponentsAcrossDifferentAnnexes() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        revenue(
                                "10000.00",
                                RevenueActivityType.COMMERCE,
                                Set.of(),
                                "Comércio"),

                        revenue(
                                "5000.00",
                                RevenueActivityType.INDUSTRY,
                                Set.of(),
                                "Indústria")));

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                new BigDecimal(
                        "500000.00"));

        SimplesCompetenceTaxComponentTotal icms = result
                .findComponent(
                        TaxComponent.ICMS)
                .orElseThrow();

        assertEquals(
                new BigDecimal(
                        "341.04"),
                icms.amountForDisplay());

        assertTrue(
                result
                        .findComponent(
                                TaxComponent.IPI)
                        .isPresent());
    }

    @Test
    void shouldReturnZeroFinalAmountForEmptyCompetence() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of());

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                BigDecimal.ZERO);

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.EMPTY,
                result.status());

        assertTrue(
                result.isFinal());

        assertEquals(
                new BigDecimal(
                        "0.00"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertEquals(
                0,
                result.processedCount());

        assertEquals(
                0,
                result.pendingCount());
    }

    @Test
    void shouldRejectPositiveCompetenceWithZeroRevenueBasis() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        revenue(
                                "10000.00",
                                RevenueActivityType.COMMERCE,
                                Set.of(),
                                "Comércio")));

        assertThrows(
                IllegalArgumentException.class,
                () -> processor.process(
                        competenceRevenue,
                        BigDecimal.ZERO));
    }

    @Test
    void shouldGenerateTraceableCompetenceDecision() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        revenue(
                                "10000.00",
                                RevenueActivityType.COMMERCE,
                                Set.of(),
                                "Comércio")));

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                "SIMPLES_COMPETENCE_REVENUE_PROCESSING",
                result
                        .decision()
                        .ruleCode());

        assertEquals(
                "SIMPLES-COMPETENCE-REVENUE-2018-2026",
                result
                        .decision()
                        .ruleVersion());
    }

    private RevenueEntry revenue(
            String amount,
            RevenueActivityType activityType,
            Set<RevenueTaxTreatment> treatments,
            String description) {
        return RevenueEntry.create(
                COMPETENCE,
                new BigDecimal(
                        amount),
                activityType,
                false,
                treatments,
                RevenueOrigin.MANUAL_ENTRY,
                description);
    }
}