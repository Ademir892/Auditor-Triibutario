package br.com.auditortributario.taxrule.simples.revenue;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesRevenueClassifierTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesRevenueClassifier classifier = new SimplesRevenueClassifier();

    @Test
    void shouldRouteCommerceToAnnexI() {
        RevenueEntry revenue = standardRevenue(
                RevenueActivityType.COMMERCE,
                false);

        SimplesRevenueClassificationResult result = classifier.classify(
                revenue);

        assertTrue(
                result.isResolved());

        assertEquals(
                SimplesRevenueClassificationStatus.RESOLVED,
                result.status());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_I,
                result
                        .route()
                        .orElseThrow());

        assertEquals(
                1,
                result
                        .route()
                        .orElseThrow()
                        .getAnnexNumber());
    }

    @Test
    void shouldRouteIndustryToAnnexII() {
        RevenueEntry revenue = standardRevenue(
                RevenueActivityType.INDUSTRY,
                false);

        SimplesRevenueClassificationResult result = classifier.classify(
                revenue);

        assertTrue(
                result.isResolved());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_II,
                result
                        .route()
                        .orElseThrow());
    }

    @Test
    void shouldRequireFatorRForApplicableService() {
        RevenueEntry revenue = standardRevenue(
                RevenueActivityType.SERVICE,
                true);

        SimplesRevenueClassificationResult result = classifier.classify(
                revenue);

        assertFalse(
                result.isResolved());

        assertTrue(
                result.requiresAdditionalInformation());

        assertEquals(
                SimplesRevenueClassificationStatus.REQUIRES_FATOR_R,
                result.status());

        assertTrue(
                result.route()
                        .isEmpty());
    }

    @Test
    void shouldRouteFatorRServiceToAnnexIII() {
        RevenueEntry revenue = standardRevenue(
                RevenueActivityType.SERVICE,
                true);

        FatorRCalculationResult fatorRResult = calculateFatorR(
                "30000.00",
                "100000.00");

        SimplesRevenueClassificationResult result = classifier.classify(
                revenue,
                fatorRResult);

        assertTrue(
                result.isResolved());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_III,
                result
                        .route()
                        .orElseThrow());
    }

    @Test
    void shouldRouteFatorRServiceToAnnexV() {
        RevenueEntry revenue = standardRevenue(
                RevenueActivityType.SERVICE,
                true);

        FatorRCalculationResult fatorRResult = calculateFatorR(
                "27000.00",
                "100000.00");

        SimplesRevenueClassificationResult result = classifier.classify(
                revenue,
                fatorRResult);

        assertTrue(
                result.isResolved());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_V,
                result
                        .route()
                        .orElseThrow());
    }

    @Test
    void shouldNotGuessAnnexForNonFatorRService() {
        RevenueEntry revenue = standardRevenue(
                RevenueActivityType.SERVICE,
                false);

        SimplesRevenueClassificationResult result = classifier.classify(
                revenue);

        assertFalse(
                result.isResolved());

        assertEquals(
                SimplesRevenueClassificationStatus.REQUIRES_SERVICE_RULE,
                result.status());

        assertTrue(
                result.route()
                        .isEmpty());
    }

    @Test
    void shouldRequireAdditionalClassificationForOtherActivity() {
        RevenueEntry revenue = standardRevenue(
                RevenueActivityType.OTHER,
                false);

        SimplesRevenueClassificationResult result = classifier.classify(
                revenue);

        assertFalse(
                result.isResolved());

        assertEquals(
                SimplesRevenueClassificationStatus.REQUIRES_MANUAL_CLASSIFICATION,
                result.status());
    }

    @Test
    void shouldRejectFatorROnCommerceRevenue() {
        RevenueEntry revenue = standardRevenue(
                RevenueActivityType.COMMERCE,
                true);

        assertThrows(
                IllegalArgumentException.class,
                () -> classifier.classify(
                        revenue));
    }

    @Test
    void shouldRejectFutureCompetenceWithoutValidatedRule() {
        RevenueEntry revenue = RevenueEntry.standard(
                YearMonth.of(
                        2027,
                        1),
                new BigDecimal(
                        "10000.00"),
                RevenueActivityType.COMMERCE,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "Receita futura");

        assertThrows(
                IllegalArgumentException.class,
                () -> classifier.classify(
                        revenue));
    }

    @Test
    void shouldProduceTraceableTaxDecision() {
        RevenueEntry revenue = standardRevenue(
                RevenueActivityType.COMMERCE,
                false);

        SimplesRevenueClassificationResult result = classifier.classify(
                revenue);

        assertEquals(
                "SIMPLES_REVENUE_CLASSIFICATION",
                result
                        .decision()
                        .ruleCode());

        assertEquals(
                "CGSN140-ART25-2018-2026",
                result
                        .decision()
                        .ruleVersion());
    }

    private RevenueEntry standardRevenue(
            RevenueActivityType activityType,
            boolean subjectToFatorR) {
        return RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        "10000.00"),
                activityType,
                subjectToFatorR,
                RevenueOrigin.MANUAL_ENTRY,
                "Receita para teste de classificação");
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