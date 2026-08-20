package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.revenue.RevenueActivityType;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueOrigin;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxTreatment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Set;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesGoodsRevenueTaxProcessorTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesGoodsRevenueTaxProcessor processor = new SimplesGoodsRevenueTaxProcessor();

    @Test
    void shouldProcessStandardCommerceRevenue() {
        RevenueEntry revenue = commerce(
                Set.of());

        SimplesGoodsRevenueTaxResult result = processor.process(
                revenue,
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_I,
                result
                        .baseCalculation()
                        .route());

        assertEquals(
                SimplesRevenueTaxAdjustmentStatus.NO_ADJUSTMENT,
                result
                        .adjustment()
                        .status());

        assertEquals(
                new BigDecimal(
                        "15960.00"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertTrue(
                result.isFinal());

        assertFalse(
                result.hasExternalObligation());
    }

    @Test
    void shouldApplyMonophasicPisAndCofinsToRealAnnexIComposition() {
        RevenueEntry revenue = commerce(
                Set.of(
                        RevenueTaxTreatment.MONOPHASIC));

        SimplesGoodsRevenueTaxResult result = processor.process(
                revenue,
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                SimplesRevenueTaxAdjustmentStatus.APPLIED,
                result
                        .adjustment()
                        .status());

        assertEquals(
                new BigDecimal(
                        "13486.20"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertEquals(
                new BigDecimal(
                        "2473.80"),
                result
                        .reductionAmountForDisplay()
                        .orElseThrow());

        assertAdjustedComponent(
                result,
                TaxComponent.PIS_PASEP,
                "0.00");

        assertAdjustedComponent(
                result,
                TaxComponent.COFINS,
                "0.00");

        assertAdjustedComponent(
                result,
                TaxComponent.ICMS,
                "5426.40");
    }

    @Test
    void shouldApplySubstitutedIcmsToRealAnnexIComposition() {
        RevenueEntry revenue = commerce(
                Set.of(
                        RevenueTaxTreatment.ICMS_ST_SUBSTITUTED));

        SimplesGoodsRevenueTaxResult result = processor.process(
                revenue,
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                new BigDecimal(
                        "10533.60"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertEquals(
                new BigDecimal(
                        "5426.40"),
                result
                        .reductionAmountForDisplay()
                        .orElseThrow());

        assertAdjustedComponent(
                result,
                TaxComponent.ICMS,
                "0.00");
    }

    @Test
    void shouldApplyMonophasicAndIcmsStOnSameRevenue() {
        RevenueEntry revenue = commerce(
                Set.of(
                        RevenueTaxTreatment.MONOPHASIC,
                        RevenueTaxTreatment.ICMS_ST_SUBSTITUTED));

        SimplesGoodsRevenueTaxResult result = processor.process(
                revenue,
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                new BigDecimal(
                        "8059.80"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertAdjustedComponent(
                result,
                TaxComponent.PIS_PASEP,
                "0.00");

        assertAdjustedComponent(
                result,
                TaxComponent.COFINS,
                "0.00");

        assertAdjustedComponent(
                result,
                TaxComponent.ICMS,
                "0.00");
    }

    @Test
    void shouldKeepOwnIcmsForSubstituteAndFlagExternalObligation() {
        RevenueEntry revenue = commerce(
                Set.of(
                        RevenueTaxTreatment.ICMS_ST_SUBSTITUTE));

        SimplesGoodsRevenueTaxResult result = processor.process(
                revenue,
                new BigDecimal(
                        "300000.00"));

        assertTrue(
                result.hasExternalObligation());

        assertEquals(
                SimplesRevenueTaxAdjustmentStatus.APPLIED_WITH_EXTERNAL_OBLIGATION,
                result
                        .adjustment()
                        .status());

        assertEquals(
                new BigDecimal(
                        "15960.00"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertAdjustedComponent(
                result,
                TaxComponent.ICMS,
                "5426.40");
    }

    @Test
    void shouldNotProduceFinalAmountForLegacyGenericTaxSubstitution() {
        RevenueEntry revenue = commerce(
                Set.of(
                        RevenueTaxTreatment.TAX_SUBSTITUTION));

        SimplesGoodsRevenueTaxResult result = processor.process(
                revenue,
                new BigDecimal(
                        "300000.00"));

        assertTrue(
                result.requiresAdditionalRules());

        assertFalse(
                result.isFinal());

        assertTrue(
                result
                        .finalTaxAmount()
                        .isEmpty());
    }

    @Test
    void shouldApplyMonophasicTreatmentToRealAnnexIIComposition() {
        RevenueEntry revenue = RevenueEntry.create(
                COMPETENCE,
                new BigDecimal(
                        "10000.00"),
                RevenueActivityType.INDUSTRY,
                false,
                Set.of(
                        RevenueTaxTreatment.MONOPHASIC),
                RevenueOrigin.MANUAL_ENTRY,
                "Venda industrial com tributação monofásica");

        SimplesGoodsRevenueTaxResult result = processor.process(
                revenue,
                new BigDecimal(
                        "500000.00"));

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_II,
                result
                        .baseCalculation()
                        .route());

        assertEquals(
                new BigDecimal(
                        "621.61"),
                result
                        .finalTaxAmountForDisplay()
                        .orElseThrow());

        assertAdjustedComponent(
                result,
                TaxComponent.PIS_PASEP,
                "0.00");

        assertAdjustedComponent(
                result,
                TaxComponent.COFINS,
                "0.00");

        assertAdjustedComponent(
                result,
                TaxComponent.IPI,
                "54.21");

        assertAdjustedComponent(
                result,
                TaxComponent.ICMS,
                "231.30");
    }

    @Test
    void shouldRejectServiceBecauseThisProcessorIsForGoodsRoutes() {
        RevenueEntry revenue = RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal(
                        "10000.00"),
                RevenueActivityType.SERVICE,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "Receita de serviço");

        assertThrows(
                IllegalArgumentException.class,
                () -> processor.process(
                        revenue,
                        new BigDecimal(
                                "300000.00")));
    }

    @Test
    void shouldRejectIssWithheldOnCommerceComposition() {
        RevenueEntry revenue = commerce(
                Set.of(
                        RevenueTaxTreatment.ISS_WITHHELD));

        assertThrows(
                IllegalArgumentException.class,
                () -> processor.process(
                        revenue,
                        new BigDecimal(
                                "300000.00")));
    }

    @Test
    void shouldGenerateTraceableProcessingDecision() {
        SimplesGoodsRevenueTaxResult result = processor.process(
                commerce(
                        Set.of(
                                RevenueTaxTreatment.MONOPHASIC)),
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                "SIMPLES_GOODS_REVENUE_PROCESSING",
                result
                        .decision()
                        .ruleCode());

        assertEquals(
                "SIMPLES-GOODS-REVENUE-2018-2026",
                result
                        .decision()
                        .ruleVersion());
    }

    private RevenueEntry commerce(
            Set<RevenueTaxTreatment> treatments) {
        return RevenueEntry.create(
                COMPETENCE,
                new BigDecimal(
                        "300000.00"),
                RevenueActivityType.COMMERCE,
                false,
                treatments,
                RevenueOrigin.MANUAL_ENTRY,
                "Revenda de mercadorias");
    }

    private void assertAdjustedComponent(
            SimplesGoodsRevenueTaxResult result,
            TaxComponent component,
            String expectedAmount) {
        SimplesAdjustedTaxComponent adjustedComponent = result
                .adjustment()
                .components()
                .stream()
                .filter(
                        current -> current.component() == component)
                .findFirst()
                .orElseThrow();

        assertEquals(
                new BigDecimal(
                        expectedAmount),
                adjustedComponent
                        .adjustedAmount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP));
    }
}