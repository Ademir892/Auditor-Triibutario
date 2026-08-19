package br.com.auditortributario.taxrule.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaxCompositionResultTest {

    @Test
    void shouldRepresentFullyAllocatedTax() {
        TaxCompositionResult result = new TaxCompositionResult(
                new BigDecimal(
                        "1000.00"),
                List.of(
                        allocation(
                                TaxComponent.IRPJ,
                                "0.10",
                                "100.00"),
                        allocation(
                                TaxComponent.CSLL,
                                "0.10",
                                "100.00"),
                        allocation(
                                TaxComponent.COFINS,
                                "0.20",
                                "200.00"),
                        allocation(
                                TaxComponent.PIS_PASEP,
                                "0.05",
                                "50.00"),
                        allocation(
                                TaxComponent.CPP,
                                "0.35",
                                "350.00"),
                        allocation(
                                TaxComponent.ISS,
                                "0.20",
                                "200.00")));

        assertEquals(
                new BigDecimal(
                        "1000.00"),
                result.allocatedAmount());

        assertEquals(
                new BigDecimal(
                        "0.00"),
                result.unallocatedAmount());

        assertTrue(
                result.isFullyAllocated());

        assertTrue(
                result.find(
                        TaxComponent.ISS).isPresent());
    }

    @Test
    void shouldRepresentPartiallyAllocatedTax() {
        TaxCompositionResult result = new TaxCompositionResult(
                new BigDecimal(
                        "1000.00"),
                List.of(
                        allocation(
                                TaxComponent.IRPJ,
                                "0.10",
                                "100.00"),
                        allocation(
                                TaxComponent.CSLL,
                                "0.10",
                                "100.00")));

        assertEquals(
                new BigDecimal(
                        "200.00"),
                result.allocatedAmount());

        assertEquals(
                new BigDecimal(
                        "800.00"),
                result.unallocatedAmount());

        assertFalse(
                result.isFullyAllocated());
    }

    @Test
    void shouldRejectDuplicatedTaxComponent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TaxCompositionResult(
                        new BigDecimal(
                                "1000.00"),
                        List.of(
                                allocation(
                                        TaxComponent.IRPJ,
                                        "0.10",
                                        "100.00"),
                                allocation(
                                        TaxComponent.IRPJ,
                                        "0.20",
                                        "200.00"))));
    }

    @Test
    void shouldRejectAllocationAboveTotalTaxAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TaxCompositionResult(
                        new BigDecimal(
                                "100.00"),
                        List.of(
                                allocation(
                                        TaxComponent.ISS,
                                        "1.00",
                                        "101.00"))));
    }

    @Test
    void shouldReturnEmptyWhenComponentIsAbsent() {
        TaxCompositionResult result = new TaxCompositionResult(
                new BigDecimal(
                        "100.00"),
                List.of());

        assertTrue(
                result.find(
                        TaxComponent.ICMS).isEmpty());
    }

    private TaxComponentAllocation allocation(
            TaxComponent component,
            String rate,
            String amount) {
        return new TaxComponentAllocation(
                component,
                new BigDecimal(
                        rate),
                new BigDecimal(
                        amount));
    }
}