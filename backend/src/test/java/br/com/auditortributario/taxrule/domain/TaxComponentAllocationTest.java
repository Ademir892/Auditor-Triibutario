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
                        "0.135"),
                new BigDecimal(
                        "134.59"));

        assertEquals(
                TaxComponent.COFINS,
                allocation.component());

        assertEquals(
                new BigDecimal(
                        "13.5"),
                allocation
                        .allocationRateAsPercentage());

        assertEquals(
                new BigDecimal(
                        "134.59"),
                allocation.amountForDisplay());
    }

    @Test
    void shouldRejectNegativeAllocationRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TaxComponentAllocation(
                        TaxComponent.IRPJ,
                        new BigDecimal(
                                "-0.01"),
                        BigDecimal.ZERO));
    }

    @Test
    void shouldRejectAllocationRateAboveOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TaxComponentAllocation(
                        TaxComponent.IRPJ,
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
                                "-1.00")));
    }
}