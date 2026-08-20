package br.com.auditortributario.taxrule.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaxComponentAllocationTest {

        @Test
        void shouldCreateTaxComponentAllocation() {
                TaxComponentAllocation allocation = new TaxComponentAllocation(
                                TaxComponent.COFINS,
                                new BigDecimal(
                                                "0.1364"),
                                new BigDecimal(
                                                "0.013601808"),
                                new BigDecimal(
                                                "136.018080"));

                assertEquals(
                                TaxComponent.COFINS,
                                allocation.component());

                assertEquals(
                                new BigDecimal(
                                                "13.64"),
                                allocation
                                                .distributionRateAsPercentage());

                assertEquals(
                                new BigDecimal(
                                                "1.3601808"),
                                allocation
                                                .effectiveRateAsPercentage());

                assertEquals(
                                new BigDecimal(
                                                "136.02"),
                                allocation.amountForDisplay());
        }

        @Test
        void shouldRejectNegativeDistributionRate() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> new TaxComponentAllocation(
                                                TaxComponent.IRPJ,
                                                new BigDecimal(
                                                                "-0.01"),
                                                BigDecimal.ZERO,
                                                BigDecimal.ZERO));
        }

        @Test
        void shouldRejectEffectiveRateAboveOne() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> new TaxComponentAllocation(
                                                TaxComponent.IRPJ,
                                                BigDecimal.ONE,
                                                new BigDecimal(
                                                                "1.01"),
                                                BigDecimal.ZERO));
        }

        @Test
        void shouldRejectNegativeAmount() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> new TaxComponentAllocation(
                                                TaxComponent.ISS,
                                                new BigDecimal(
                                                                "0.10"),
                                                new BigDecimal(
                                                                "0.01"),
                                                new BigDecimal(
                                                                "-1.00")));
        }
}