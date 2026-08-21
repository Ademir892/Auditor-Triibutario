package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.revenue.CompetenceRevenue;
import br.com.auditortributario.taxrule.domain.revenue.RevenueActivityType;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueOrigin;
import br.com.auditortributario.taxrule.simples.FatorRAutomaticCalculator;
import br.com.auditortributario.taxrule.simples.FatorRCalculationRequest;
import br.com.auditortributario.taxrule.simples.FatorRCalculationResult;
import br.com.auditortributario.taxrule.simples.MonthlyRevenue;
import br.com.auditortributario.taxrule.simples.SimplesAnnex;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisCalculator;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisRequest;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisResult;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesCompetenceRevenueEndToEndTest {

    private static final LocalDate OPENING_DATE = LocalDate.of(
            2026,
            8,
            10);

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final TaxBracketRevenueBasisCalculator revenueBasisCalculator = new TaxBracketRevenueBasisCalculator();

    private final FatorRAutomaticCalculator fatorRCalculator = new FatorRAutomaticCalculator();

    private final SimplesCompetenceRevenueTaxProcessor processor = new SimplesCompetenceRevenueTaxProcessor();

    @Test
    void shouldProcessFatorRServiceInAnnexIIIEndToEnd() {
        RevenueEntry service = serviceRevenue(
                "20000.00");

        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        service));

        TaxBracketRevenueBasisResult revenueBasisResult = calculateRevenueBasis(
                "20000.00");

        FatorRCalculationResult fatorRResult = calculateFatorR(
                "6000.00",
                "20000.00");

        SimplesCompetenceTaxContext context = SimplesCompetenceTaxContext.withServiceTaxData(
                revenueBasisResult,
                fatorRResult);

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                context);

        assertEquals(
                TaxBracketRevenueBasisType.RBT12_PROPORTIONALIZED,
                revenueBasisResult.basisType());

        assertEquals(
                new BigDecimal(
                        "240000.00"),
                revenueBasisResult.revenueBasis());

        assertEquals(
                SimplesAnnex.ANEXO_III,
                fatorRResult.annex());

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.COMPLETED,
                result.status());

        assertTrue(
                result.isFinal());

        assertEquals(
                new BigDecimal(
                        "1460.00"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        SimplesCompetenceRevenueItemResult item = result
                .items()
                .getFirst();

        assertEquals(
                SimplesCompetenceRevenueItemStatus.COMPLETED,
                item.status());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_III,
                item
                        .classification()
                        .route()
                        .orElseThrow());

        assertTrue(
                item
                        .taxResult()
                        .orElseThrow() instanceof SimplesServiceRevenueTaxResult);

        SimplesServiceRevenueTaxResult serviceResult = (SimplesServiceRevenueTaxResult) item
                .taxResult()
                .orElseThrow();

        assertEquals(
                2,
                serviceResult
                        .bracketSelectionResult()
                        .bracket()
                        .number());

        assertEquals(
                new BigDecimal(
                        "0.073"),
                serviceResult
                        .effectiveRateResult()
                        .effectiveRate());

        assertEquals(
                new BigDecimal(
                        "1460.00"),
                serviceResult
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertTrue(
                result
                        .findComponent(
                                TaxComponent.ISS)
                        .isPresent());
    }

    @Test
    void shouldProcessFatorRServiceInAnnexVEndToEnd() {
        RevenueEntry service = serviceRevenue(
                "20000.00");

        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        service));

        TaxBracketRevenueBasisResult revenueBasisResult = calculateRevenueBasis(
                "20000.00");

        FatorRCalculationResult fatorRResult = calculateFatorR(
                "5000.00",
                "20000.00");

        SimplesCompetenceTaxContext context = SimplesCompetenceTaxContext.withServiceTaxData(
                revenueBasisResult,
                fatorRResult);

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                context);

        assertEquals(
                SimplesAnnex.ANEXO_V,
                fatorRResult.annex());

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.COMPLETED,
                result.status());

        assertTrue(
                result.isFinal());

        assertEquals(
                new BigDecimal(
                        "3225.00"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        SimplesCompetenceRevenueItemResult item = result
                .items()
                .getFirst();

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_V,
                item
                        .classification()
                        .route()
                        .orElseThrow());

        assertTrue(
                item
                        .taxResult()
                        .orElseThrow() instanceof SimplesServiceRevenueTaxResult);

        SimplesServiceRevenueTaxResult serviceResult = (SimplesServiceRevenueTaxResult) item
                .taxResult()
                .orElseThrow();

        assertEquals(
                2,
                serviceResult
                        .bracketSelectionResult()
                        .bracket()
                        .number());

        assertEquals(
                new BigDecimal(
                        "0.16125"),
                serviceResult
                        .effectiveRateResult()
                        .effectiveRate());

        assertEquals(
                new BigDecimal(
                        "3225.00"),
                serviceResult
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertTrue(
                result
                        .findComponent(
                                TaxComponent.ISS)
                        .isPresent());
    }

    @Test
    void shouldConsolidateAnnexesIAndIIAndIIIInSameCompetence() {
        RevenueEntry commerce = standardRevenue(
                "10000.00",
                RevenueActivityType.COMMERCE,
                "Receita comercial");

        RevenueEntry industry = standardRevenue(
                "5000.00",
                RevenueActivityType.INDUSTRY,
                "Receita industrial");

        RevenueEntry service = serviceRevenue(
                "5000.00");

        CompetenceRevenue competenceRevenue = new CompetenceRevenue(
                COMPETENCE,
                List.of(
                        commerce,
                        industry,
                        service));

        /*
         * IMPORTANTE:
         *
         * A receita utilizada no cálculo do RBT12p é
         * a receita bruta TOTAL da competência:
         *
         * comércio = 10.000
         * indústria = 5.000
         * serviço = 5.000
         *
         * total = 20.000
         *
         * RBT12p = 20.000 x 12 = 240.000.
         */
        TaxBracketRevenueBasisResult revenueBasisResult = calculateRevenueBasis(
                "20000.00");

        /*
         * No primeiro mês:
         *
         * folha = 6.000
         * receita base do Fator R = 20.000
         *
         * Fator R = 30%
         *
         * Logo, o serviço é direcionado ao Anexo III.
         */
        FatorRCalculationResult fatorRResult = calculateFatorR(
                "6000.00",
                "20000.00");

        SimplesCompetenceTaxContext context = SimplesCompetenceTaxContext.withServiceTaxData(
                revenueBasisResult,
                fatorRResult);

        SimplesCompetenceRevenueTaxResult result = processor.process(
                competenceRevenue,
                context);

        assertEquals(
                new BigDecimal(
                        "20000.00"),
                competenceRevenue.totalAmount());

        assertEquals(
                new BigDecimal(
                        "240000.00"),
                revenueBasisResult.revenueBasis());

        assertEquals(
                SimplesAnnex.ANEXO_III,
                fatorRResult.annex());

        assertEquals(
                SimplesCompetenceRevenueProcessingStatus.COMPLETED,
                result.status());

        assertTrue(
                result.isFinal());

        assertEquals(
                3,
                result.processedCount());

        assertEquals(
                0,
                result.pendingCount());

        /*
         * Anexo I:
         *
         * RBT12p = 240.000
         *
         * alíquota efetiva:
         *
         * (240.000 x 7,3% - 5.940) / 240.000
         * = 4,825%
         *
         * 10.000 x 4,825%
         * = 482,50
         *
         *
         * Anexo II:
         *
         * (240.000 x 7,8% - 5.940) / 240.000
         * = 5,325%
         *
         * 5.000 x 5,325%
         * = 266,25
         *
         *
         * Anexo III:
         *
         * (240.000 x 11,2% - 9.360) / 240.000
         * = 7,30%
         *
         * 5.000 x 7,30%
         * = 365,00
         *
         *
         * TOTAL:
         *
         * 482,50 + 266,25 + 365,00
         * = 1.113,75
         */
        assertEquals(
                new BigDecimal(
                        "1113.75"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

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
                SimplesRevenueTaxRoute.ANNEX_III,
                result
                        .items()
                        .get(2)
                        .classification()
                        .route()
                        .orElseThrow());

        /*
         * ICMS comprova participação do Anexo I/II.
         */
        assertTrue(
                result
                        .findComponent(
                                TaxComponent.ICMS)
                        .isPresent());

        /*
         * IPI comprova participação do Anexo II.
         */
        assertTrue(
                result
                        .findComponent(
                                TaxComponent.IPI)
                        .isPresent());

        /*
         * ISS comprova participação do Anexo III.
         */
        assertTrue(
                result
                        .findComponent(
                                TaxComponent.ISS)
                        .isPresent());
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

    private TaxBracketRevenueBasisResult calculateRevenueBasis(
            String totalRevenue) {
        return revenueBasisCalculator.calculate(
                new TaxBracketRevenueBasisRequest(
                        OPENING_DATE,
                        COMPETENCE,
                        List.of(
                                new MonthlyRevenue(
                                        COMPETENCE,
                                        new BigDecimal(
                                                totalRevenue)))));
    }

    private FatorRCalculationResult calculateFatorR(
            String payroll,
            String revenue) {
        return fatorRCalculator.calculate(
                new FatorRCalculationRequest(
                        OPENING_DATE,
                        COMPETENCE,
                        new BigDecimal(
                                payroll),
                        new BigDecimal(
                                revenue)));
    }
}