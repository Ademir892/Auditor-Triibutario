package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.TaxComponentAllocation;
import br.com.auditortributario.taxrule.domain.revenue.RevenueActivityType;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueOrigin;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesRevenueTaxCalculatorTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesRevenueClassifier classifier = new SimplesRevenueClassifier();

    private final SimplesRevenueTaxCalculator calculator = new SimplesRevenueTaxCalculator();

    @Test
    void shouldReproduceOfficialAnnexIExample() {
        RevenueEntry revenue = RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        "300000.00"),
                RevenueActivityType.COMMERCE,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "Revenda de mercadorias");

        SimplesRevenueClassificationResult classification = classifier.classify(
                revenue);

        SimplesRevenueTaxCalculationResult result = calculator.calculate(
                classification,
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_I,
                result.route());

        assertEquals(
                2,
                result.bracket()
                        .number());

        assertEquals(
                new BigDecimal(
                        "0.0532"),
                result.effectiveRate());

        assertEquals(
                new BigDecimal(
                        "15960.00"),
                result.taxAmount());

        assertAllocation(
                result,
                TaxComponent.IRPJ,
                "877.80");

        assertAllocation(
                result,
                TaxComponent.CSLL,
                "558.60");

        assertAllocation(
                result,
                TaxComponent.COFINS,
                "2033.30");

        assertAllocation(
                result,
                TaxComponent.PIS_PASEP,
                "440.50");

        assertAllocation(
                result,
                TaxComponent.CPP,
                "6623.40");

        assertAllocation(
                result,
                TaxComponent.ICMS,
                "5426.40");

        assertTrue(
                result
                        .composition()
                        .isFullyAllocated());
    }

    @Test
    void shouldCalculateAnnexIIThirdBracket() {
        RevenueEntry revenue = RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        "10000.00"),
                RevenueActivityType.INDUSTRY,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "Venda de produto industrializado");

        SimplesRevenueClassificationResult classification = classifier.classify(
                revenue);

        SimplesRevenueTaxCalculationResult result = calculator.calculate(
                classification,
                new BigDecimal(
                        "500000.00"));

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_II,
                result.route());

        assertEquals(
                3,
                result.bracket()
                        .number());

        assertEquals(
                new BigDecimal(
                        "0.07228"),
                result.effectiveRate());

        assertEquals(
                new BigDecimal(
                        "722.80"),
                result.taxAmount());

        assertAllocation(
                result,
                TaxComponent.IRPJ,
                "39.75");

        assertAllocation(
                result,
                TaxComponent.CSLL,
                "25.30");

        assertAllocation(
                result,
                TaxComponent.COFINS,
                "83.19");

        assertAllocation(
                result,
                TaxComponent.PIS_PASEP,
                "18.00");

        assertAllocation(
                result,
                TaxComponent.CPP,
                "271.05");

        assertAllocation(
                result,
                TaxComponent.IPI,
                "54.21");

        assertAllocation(
                result,
                TaxComponent.ICMS,
                "231.30");

        assertTrue(
                result
                        .composition()
                        .isFullyAllocated());
    }

    @Test
    void shouldCalculateZeroRevenueAsZeroTax() {
        RevenueEntry revenue = RevenueEntry.standard(
                COMPETENCE,
                BigDecimal.ZERO,
                RevenueActivityType.COMMERCE,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "Receita zerada");

        SimplesRevenueTaxCalculationResult result = calculator.calculate(
                classifier.classify(
                        revenue),
                BigDecimal.ZERO);

        assertEquals(
                BigDecimal.ZERO,
                result.effectiveRate());

        assertEquals(
                new BigDecimal(
                        "0.00"),
                result.taxAmount());
    }

    @Test
    void shouldRejectPositiveRevenueWithZeroRevenueBasis() {
        RevenueEntry revenue = RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        "10000.00"),
                RevenueActivityType.COMMERCE,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "Receita inválida");

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        classifier.classify(
                                revenue),
                        BigDecimal.ZERO));
    }

    @Test
    void shouldRejectUnresolvedClassification() {
        RevenueEntry revenue = RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        "10000.00"),
                RevenueActivityType.SERVICE,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "Serviço ainda não classificado");

        SimplesRevenueClassificationResult classification = classifier.classify(
                revenue);

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        classification,
                        new BigDecimal(
                                "500000.00")));
    }

    @Test
    void shouldBlockSixthBracketUntilIcmsSublimitsAreImplemented() {
        RevenueEntry revenue = RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        "10000.00"),
                RevenueActivityType.COMMERCE,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "Comércio na sexta faixa");

        assertThrows(
                IllegalStateException.class,
                () -> calculator.calculate(
                        classifier.classify(
                                revenue),
                        new BigDecimal(
                                "4000000.00")));
    }

    private void assertAllocation(
            SimplesRevenueTaxCalculationResult result,
            TaxComponent component,
            String expectedAmount) {
        TaxComponentAllocation allocation = result
                .composition()
                .find(
                        component)
                .orElseThrow();

        assertEquals(
                new BigDecimal(
                        expectedAmount),
                allocation.amountForDisplay());
    }
}