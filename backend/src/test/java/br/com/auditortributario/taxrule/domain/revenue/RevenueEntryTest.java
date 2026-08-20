package br.com.auditortributario.taxrule.domain.revenue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RevenueEntryTest {

    @Test
    void shouldCreateStandardServiceRevenue() {
        RevenueEntry revenue = RevenueEntry.standard(
                YearMonth.of(
                        2026,
                        8),
                new BigDecimal(
                        "35000.00"),
                RevenueActivityType.SERVICE,
                true,
                RevenueOrigin.MANUAL_ENTRY,
                "Serviços sujeitos ao Fator R");

        assertNotNull(
                revenue.id());

        assertEquals(
                YearMonth.of(
                        2026,
                        8),
                revenue.competence());

        assertEquals(
                new BigDecimal(
                        "35000.00"),
                revenue.amount());

        assertEquals(
                RevenueActivityType.SERVICE,
                revenue.activityType());

        assertTrue(
                revenue.subjectToFatorR());

        assertFalse(
                revenue.hasSpecialTaxTreatment());

        assertTrue(
                revenue.treatments()
                        .isEmpty());
    }

    @Test
    void shouldCreateRevenueWithMultipleTreatments() {
        RevenueEntry revenue = RevenueEntry.create(
                YearMonth.of(
                        2026,
                        8),
                new BigDecimal(
                        "15000.00"),
                RevenueActivityType.COMMERCE,
                false,
                Set.of(
                        RevenueTaxTreatment.MONOPHASIC,
                        RevenueTaxTreatment.REDUCTION),
                RevenueOrigin.DOCUMENT_EVIDENCE,
                "Venda de mercadorias com tratamento especial");

        assertTrue(
                revenue.hasSpecialTaxTreatment());

        assertTrue(
                revenue.hasTreatment(
                        RevenueTaxTreatment.MONOPHASIC));

        assertTrue(
                revenue.hasTreatment(
                        RevenueTaxTreatment.REDUCTION));

        assertFalse(
                revenue.hasTreatment(
                        RevenueTaxTreatment.EXPORT));
    }

    @Test
    void shouldAllowZeroRevenue() {
        RevenueEntry revenue = RevenueEntry.standard(
                YearMonth.of(
                        2026,
                        8),
                BigDecimal.ZERO,
                RevenueActivityType.SERVICE,
                true,
                RevenueOrigin.MANUAL_ENTRY,
                "Receita explicitamente zerada");

        assertEquals(
                BigDecimal.ZERO,
                revenue.amount());
    }

    @Test
    void shouldRejectNegativeRevenue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RevenueEntry.standard(
                        YearMonth.of(
                                2026,
                                8),
                        new BigDecimal(
                                "-0.01"),
                        RevenueActivityType.SERVICE,
                        true,
                        RevenueOrigin.MANUAL_ENTRY,
                        "Receita inválida"));
    }

    @Test
    void shouldTrimDescription() {
        RevenueEntry revenue = RevenueEntry.standard(
                YearMonth.of(
                        2026,
                        8),
                new BigDecimal(
                        "1000.00"),
                RevenueActivityType.OTHER,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "  Receita diversa  ");

        assertEquals(
                "Receita diversa",
                revenue.description());
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RevenueEntry.standard(
                        YearMonth.of(
                                2026,
                                8),
                        new BigDecimal(
                                "1000.00"),
                        RevenueActivityType.OTHER,
                        false,
                        RevenueOrigin.MANUAL_ENTRY,
                        "   "));
    }

    @Test
    void shouldCreateAndRestoreRevenueEntryId() {
        RevenueEntryId generated = RevenueEntryId.generate();

        RevenueEntryId restored = RevenueEntryId.from(
                generated.toString());

        assertEquals(
                generated,
                restored);
    }
}