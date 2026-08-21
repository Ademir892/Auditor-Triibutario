package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.revenue.CompetenceRevenue;
import br.com.auditortributario.taxrule.domain.revenue.RevenueActivityType;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueOrigin;
import br.com.auditortributario.taxrule.simples.FatorRAutomaticCalculator;
import br.com.auditortributario.taxrule.simples.FatorRCalculationRequest;
import br.com.auditortributario.taxrule.simples.FatorRCalculationResult;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesCompetenceFatorRContextTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesCompetenceRevenueTaxProcessor processor = new SimplesCompetenceRevenueTaxProcessor();

    @Test
    void shouldExplicitlyRequireFatorRWhenServiceNeedsIt() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        fatorRService(
                                "10000.00")));

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
                SimplesCompetenceRevenueItemStatus.REQUIRES_FATOR_R,
                result
                        .items()
                        .getFirst()
                        .status());

        assertEquals(
                SimplesRevenueClassificationStatus.REQUIRES_FATOR_R,
                result
                        .items()
                        .getFirst()
                        .classification()
                        .status());
    }

    @Test
    void shouldResolveServiceToAnnexIIIUsingCompetenceFatorR() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        fatorRService(
                                "10000.00")));

        FatorRCalculationResult fatorRResult = calculateFatorR(
                "30000.00",
                "100000.00");

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                SimplesCompetenceTaxContext.withFatorR(
                        new BigDecimal(
                                "300000.00"),
                        fatorRResult));

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.REQUIRES_ADDITIONAL_INFORMATION,
                result.status());

        assertFalse(
                result.isFinal());

        SimplesCompetenceRevenueItemResult item = result
                .items()
                .getFirst();

        assertEquals(
                SimplesCompetenceRevenueItemStatus.REQUIRES_REVENUE_BASIS,
                item.status());

        assertTrue(
                item.classification()
                        .isResolved());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_III,
                item
                        .classification()
                        .route()
                        .orElseThrow());
    }

    @Test
    void shouldResolveServiceToAnnexVUsingCompetenceFatorR() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        fatorRService(
                                "10000.00")));

        FatorRCalculationResult fatorRResult = calculateFatorR(
                "27000.00",
                "100000.00");

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                SimplesCompetenceTaxContext.withFatorR(
                        new BigDecimal(
                                "300000.00"),
                        fatorRResult));

        SimplesCompetenceRevenueItemResult item = result
                .items()
                .getFirst();

        assertEquals(
                SimplesCompetenceRevenueItemStatus.REQUIRES_REVENUE_BASIS,
                item.status());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_V,
                item
                        .classification()
                        .route()
                        .orElseThrow());
    }

    @Test
    void shouldUseSameFatorRForMultipleApplicableServices() {
        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        fatorRService(
                                "10000.00"),
                        fatorRService(
                                "20000.00")));

        FatorRCalculationResult fatorRResult = calculateFatorR(
                "30000.00",
                "100000.00");

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                SimplesCompetenceTaxContext.withFatorR(
                        new BigDecimal(
                                "300000.00"),
                        fatorRResult));

        assertEquals(
                2,
                result.items()
                        .size());

        for (SimplesCompetenceRevenueItemResult item : result.items()) {

            assertEquals(
                    SimplesRevenueTaxRoute.ANNEX_III,
                    item
                            .classification()
                            .route()
                            .orElseThrow());

            assertEquals(
                    SimplesCompetenceRevenueItemStatus.REQUIRES_REVENUE_BASIS,
                    item.status());
        }
    }

    @Test
    void shouldKeepGoodsCalculationWorkingWithFatorRContextPresent() {
        RevenueEntry commerce = RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        "10000.00"),
                RevenueActivityType.COMMERCE,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "Receita comercial");

        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        commerce));

        FatorRCalculationResult fatorRResult = calculateFatorR(
                "30000.00",
                "100000.00");

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                SimplesCompetenceTaxContext.withFatorR(
                        new BigDecimal(
                                "300000.00"),
                        fatorRResult));

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.COMPLETED,
                result.status());

        assertTrue(
                result.isFinal());

        assertEquals(
                new BigDecimal(
                        "532.00"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());
    }

    @Test
    void shouldRejectNegativeRevenueBasisInContext() {
        FatorRCalculationResult fatorRResult = calculateFatorR(
                "30000.00",
                "100000.00");

        assertThrows(
                IllegalArgumentException.class,
                () -> SimplesCompetenceTaxContext.withFatorR(
                        new BigDecimal(
                                "-0.01"),
                        fatorRResult));
    }

    private RevenueEntry fatorRService(
            String amount) {
        return RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        amount),
                RevenueActivityType.SERVICE,
                true,
                RevenueOrigin.MANUAL_ENTRY,
                "Serviço sujeito ao Fator R");
    }

    private FatorRCalculationResult calculateFatorR(
            String payroll,
            String revenue) {
        return new FatorRAutomaticCalculator()
                .calculate(
                        new FatorRCalculationRequest(
                                LocalDate.of(
                                        2024,
                                        1,
                                        10),
                                COMPETENCE,
                                new BigDecimal(
                                        payroll),
                                new BigDecimal(
                                        revenue)));
    }
}