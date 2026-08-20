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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesRevenueTaxCompositionAdjusterTest {

        private static final YearMonth COMPETENCE = YearMonth.of(
                        2026,
                        8);

        private final SimplesRevenueTaxCompositionAdjuster adjuster = new SimplesRevenueTaxCompositionAdjuster();

        @Test
        void shouldKeepStandardRevenueUnchanged() {
                RevenueEntry revenue = RevenueEntry.standard(
                                COMPETENCE,
                                new BigDecimal(
                                                "10000.00"),
                                RevenueActivityType.SERVICE,
                                false,
                                RevenueOrigin.MANUAL_ENTRY,
                                "Receita normal");

                TaxCompositionResult original = serviceComposition();

                SimplesRevenueTaxAdjustmentResult result = adjuster.adjust(
                                revenue,
                                original);

                assertEquals(
                                SimplesRevenueTaxAdjustmentStatus.NO_ADJUSTMENT,
                                result.status());

                assertEquals(
                                new BigDecimal(
                                                "1000.00"),
                                result
                                                .adjustedSimplesAmount()
                                                .orElseThrow());
        }

        @Test
        void shouldExcludePisAndCofinsFromMonophasicRevenue() {
                RevenueEntry revenue = RevenueEntry.create(
                                COMPETENCE,
                                new BigDecimal(
                                                "10000.00"),
                                RevenueActivityType.COMMERCE,
                                false,
                                Set.of(
                                                RevenueTaxTreatment.MONOPHASIC),
                                RevenueOrigin.MANUAL_ENTRY,
                                "Receita monofásica");

                TaxCompositionResult original = commerceComposition();

                SimplesRevenueTaxAdjustmentResult result = adjuster.adjust(
                                revenue,
                                original);

                assertEquals(
                                SimplesRevenueTaxAdjustmentStatus.APPLIED,
                                result.status());

                assertEquals(
                                new BigDecimal(
                                                "850.00"),
                                result
                                                .adjustedSimplesAmount()
                                                .orElseThrow());

                assertEquals(
                                new BigDecimal(
                                                "150.00"),
                                result
                                                .reductionAmount()
                                                .orElseThrow());

                assertComponentAmount(
                                result,
                                TaxComponent.PIS_PASEP,
                                "0");

                assertComponentAmount(
                                result,
                                TaxComponent.COFINS,
                                "0");

                assertComponentAmount(
                                result,
                                TaxComponent.ICMS,
                                "300.00");
        }

        @Test
        void shouldExcludeIcmsForSubstitutedTaxpayer() {
                RevenueEntry revenue = RevenueEntry.create(
                                COMPETENCE,
                                new BigDecimal(
                                                "10000.00"),
                                RevenueActivityType.COMMERCE,
                                false,
                                Set.of(
                                                RevenueTaxTreatment.ICMS_ST_SUBSTITUTED),
                                RevenueOrigin.MANUAL_ENTRY,
                                "Receita com ICMS-ST como substituído");

                SimplesRevenueTaxAdjustmentResult result = adjuster.adjust(
                                revenue,
                                commerceComposition());

                assertEquals(
                                SimplesRevenueTaxAdjustmentStatus.APPLIED,
                                result.status());

                assertEquals(
                                new BigDecimal(
                                                "700.00"),
                                result
                                                .adjustedSimplesAmount()
                                                .orElseThrow());

                assertComponentAmount(
                                result,
                                TaxComponent.ICMS,
                                "0");
        }

        @Test
        void shouldKeepOwnIcmsForSubstituteTaxpayerAndFlagExternalObligation() {
                RevenueEntry revenue = RevenueEntry.create(
                                COMPETENCE,
                                new BigDecimal(
                                                "10000.00"),
                                RevenueActivityType.COMMERCE,
                                false,
                                Set.of(
                                                RevenueTaxTreatment.ICMS_ST_SUBSTITUTE),
                                RevenueOrigin.MANUAL_ENTRY,
                                "Receita com ICMS-ST como substituto");

                SimplesRevenueTaxAdjustmentResult result = adjuster.adjust(
                                revenue,
                                commerceComposition());

                assertEquals(
                                SimplesRevenueTaxAdjustmentStatus.APPLIED_WITH_EXTERNAL_OBLIGATION,
                                result.status());

                assertEquals(
                                new BigDecimal(
                                                "1000.00"),
                                result
                                                .adjustedSimplesAmount()
                                                .orElseThrow());

                assertComponentAmount(
                                result,
                                TaxComponent.ICMS,
                                "300.00");
        }

        @Test
        void shouldExcludeWithheldIssFromSimplesAmount() {
                RevenueEntry revenue = RevenueEntry.create(
                                COMPETENCE,
                                new BigDecimal(
                                                "10000.00"),
                                RevenueActivityType.SERVICE,
                                false,
                                Set.of(
                                                RevenueTaxTreatment.ISS_WITHHELD),
                                RevenueOrigin.MANUAL_ENTRY,
                                "Serviço com ISS retido");

                SimplesRevenueTaxAdjustmentResult result = adjuster.adjust(
                                revenue,
                                serviceComposition());

                assertEquals(
                                new BigDecimal(
                                                "800.00"),
                                result
                                                .adjustedSimplesAmount()
                                                .orElseThrow());

                assertComponentAmount(
                                result,
                                TaxComponent.ISS,
                                "0");
        }

        @Test
        void shouldNotInventResultForGenericTaxSubstitution() {
                RevenueEntry revenue = RevenueEntry.create(
                                COMPETENCE,
                                new BigDecimal(
                                                "10000.00"),
                                RevenueActivityType.COMMERCE,
                                false,
                                Set.of(
                                                RevenueTaxTreatment.TAX_SUBSTITUTION),
                                RevenueOrigin.MANUAL_ENTRY,
                                "ST sem posição definida");

                SimplesRevenueTaxAdjustmentResult result = adjuster.adjust(
                                revenue,
                                commerceComposition());

                assertEquals(
                                SimplesRevenueTaxAdjustmentStatus.REQUIRES_ADDITIONAL_RULES,
                                result.status());

                assertTrue(
                                result
                                                .adjustedSimplesAmount()
                                                .isEmpty());

                assertFalse(
                                result
                                                .pendingTreatments()
                                                .isEmpty());
        }

        @Test
        void shouldNotInventReductionWithoutSpecificRule() {
                RevenueEntry revenue = RevenueEntry.create(
                                COMPETENCE,
                                new BigDecimal(
                                                "10000.00"),
                                RevenueActivityType.SERVICE,
                                false,
                                Set.of(
                                                RevenueTaxTreatment.REDUCTION),
                                RevenueOrigin.MANUAL_ENTRY,
                                "Receita com redução não especificada");

                SimplesRevenueTaxAdjustmentResult result = adjuster.adjust(
                                revenue,
                                serviceComposition());

                assertEquals(
                                SimplesRevenueTaxAdjustmentStatus.REQUIRES_ADDITIONAL_RULES,
                                result.status());

                assertTrue(
                                result
                                                .adjustedSimplesAmount()
                                                .isEmpty());
        }

        private TaxCompositionResult commerceComposition() {
                return new TaxCompositionResult(
                                new BigDecimal(
                                                "1000.00"),
                                List.of(
                                                allocation(
                                                                TaxComponent.IRPJ,
                                                                "0.10",
                                                                "0.010",
                                                                "100.00"),

                                                allocation(
                                                                TaxComponent.CSLL,
                                                                "0.10",
                                                                "0.010",
                                                                "100.00"),

                                                allocation(
                                                                TaxComponent.COFINS,
                                                                "0.10",
                                                                "0.010",
                                                                "100.00"),

                                                allocation(
                                                                TaxComponent.PIS_PASEP,
                                                                "0.05",
                                                                "0.005",
                                                                "50.00"),

                                                allocation(
                                                                TaxComponent.CPP,
                                                                "0.35",
                                                                "0.035",
                                                                "350.00"),

                                                allocation(
                                                                TaxComponent.ICMS,
                                                                "0.30",
                                                                "0.030",
                                                                "300.00")));
        }

        private TaxCompositionResult serviceComposition() {
                return new TaxCompositionResult(
                                new BigDecimal(
                                                "1000.00"),
                                List.of(
                                                allocation(
                                                                TaxComponent.IRPJ,
                                                                "0.10",
                                                                "0.010",
                                                                "100.00"),

                                                allocation(
                                                                TaxComponent.CSLL,
                                                                "0.10",
                                                                "0.010",
                                                                "100.00"),

                                                allocation(
                                                                TaxComponent.COFINS,
                                                                "0.15",
                                                                "0.015",
                                                                "150.00"),

                                                allocation(
                                                                TaxComponent.PIS_PASEP,
                                                                "0.05",
                                                                "0.005",
                                                                "50.00"),

                                                allocation(
                                                                TaxComponent.CPP,
                                                                "0.40",
                                                                "0.040",
                                                                "400.00"),

                                                allocation(
                                                                TaxComponent.ISS,
                                                                "0.20",
                                                                "0.020",
                                                                "200.00")));
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

        private void assertComponentAmount(
                        SimplesRevenueTaxAdjustmentResult result,
                        TaxComponent component,
                        String expectedAmount) {
                SimplesAdjustedTaxComponent adjustedComponent = result
                                .components()
                                .stream()
                                .filter(
                                                current -> current.component() == component)
                                .findFirst()
                                .orElseThrow();

                assertEquals(
                                new BigDecimal(
                                                expectedAmount),
                                adjustedComponent.adjustedAmount());
        }
}