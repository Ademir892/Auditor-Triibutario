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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesRevenueAnnexIVClassificationTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesRevenueClassifier classifier = new SimplesRevenueClassifier();

    private final SimplesRevenueTaxCalculator taxCalculator = new SimplesRevenueTaxCalculator();

    @Test
    void shouldKeepGenericServicePendingWithoutSpecificRule() {
        RevenueEntry revenue = serviceRevenue(
                "10000.00");

        SimplesRevenueClassificationResult result = classifier.classify(
                revenue);

        assertFalse(
                result.isResolved());

        assertEquals(
                SimplesRevenueClassificationStatus.REQUIRES_SERVICE_RULE,
                result.status());

        assertTrue(
                result.route().isEmpty());
    }

    @Test
    void shouldClassifyConstructionAndEngineeringAsAnnexIV() {
        assertAnnexIVClassification(
                SimplesServiceTaxRule.ANNEX_IV_CONSTRUCTION_ENGINEERING);
    }

    @Test
    void shouldClassifySecurityCleaningAndConservationAsAnnexIV() {
        assertAnnexIVClassification(
                SimplesServiceTaxRule.ANNEX_IV_SECURITY_CLEANING_CONSERVATION);
    }

    @Test
    void shouldClassifyLegalServicesAsAnnexIV() {
        assertAnnexIVClassification(
                SimplesServiceTaxRule.ANNEX_IV_LEGAL_SERVICES);
    }

    @Test
    void shouldRejectFixedServiceRuleForFatorRRevenue() {
        RevenueEntry revenue = RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        "10000.00"),
                RevenueActivityType.SERVICE,
                true,
                RevenueOrigin.MANUAL_ENTRY,
                "Serviço sujeito ao Fator R");

        assertThrows(
                IllegalArgumentException.class,
                () -> classifier.classify(
                        revenue,
                        SimplesServiceTaxRule.ANNEX_IV_LEGAL_SERVICES));
    }

    @Test
    void shouldCalculateAnnexIVSecondBracketFromExplicitServiceRule() {
        RevenueEntry revenue = serviceRevenue(
                "10000.00");

        SimplesRevenueClassificationResult classification = classifier.classify(
                revenue,
                SimplesServiceTaxRule.ANNEX_IV_LEGAL_SERVICES);

        SimplesRevenueTaxCalculationResult result = taxCalculator.calculate(
                classification,
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_IV,
                result.route());

        assertEquals(
                2,
                result.bracket().number());

        assertEquals(
                0,
                new BigDecimal(
                        "0.063").compareTo(
                                result.effectiveRate()));

        assertEquals(
                new BigDecimal(
                        "630.00"),
                result.taxAmount());

        assertTrue(
                result.composition()
                        .find(
                                TaxComponent.ISS)
                        .isPresent());

        assertFalse(
                result.composition()
                        .find(
                                TaxComponent.CPP)
                        .isPresent());

        assertEquals(
                new BigDecimal(
                        "252.00"),
                result
                        .composition()
                        .find(
                                TaxComponent.ISS)
                        .orElseThrow()
                        .amount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP));
    }

    private void assertAnnexIVClassification(
            SimplesServiceTaxRule serviceTaxRule) {
        RevenueEntry revenue = serviceRevenue(
                "10000.00");

        SimplesRevenueClassificationResult result = classifier.classify(
                revenue,
                serviceTaxRule);

        assertTrue(
                result.isResolved());

        assertEquals(
                SimplesRevenueClassificationStatus.RESOLVED,
                result.status());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_IV,
                result
                        .route()
                        .orElseThrow());
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
                "Serviço com enquadramento específico");
    }
}