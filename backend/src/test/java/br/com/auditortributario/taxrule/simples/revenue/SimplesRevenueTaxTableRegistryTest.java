package br.com.auditortributario.taxrule.simples.revenue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimplesRevenueTaxTableRegistryTest {

    private final SimplesRevenueTaxTableRegistry registry = new SimplesRevenueTaxTableRegistry();

    @Test
    void shouldLoadOfficialAnnexITable() {
        SimplesRevenueTaxTable table = registry.find(
                SimplesRevenueTaxRoute.ANNEX_I,
                YearMonth.of(
                        2026,
                        8));

        assertEquals(
                6,
                table.brackets()
                        .size());

        SimplesRevenueTaxBracket third = table.brackets()
                .get(
                        2);

        assertEquals(
                3,
                third.number());

        assertEquals(
                new BigDecimal(
                        "720000.00"),
                third.maximumRevenue());

        assertEquals(
                new BigDecimal(
                        "0.0950"),
                third.nominalRate());

        assertEquals(
                new BigDecimal(
                        "13860.00"),
                third.deduction());
    }

    @Test
    void shouldLoadOfficialAnnexIITable() {
        SimplesRevenueTaxTable table = registry.find(
                SimplesRevenueTaxRoute.ANNEX_II,
                YearMonth.of(
                        2026,
                        8));

        SimplesRevenueTaxBracket fifth = table.brackets()
                .get(
                        4);

        assertEquals(
                5,
                fifth.number());

        assertEquals(
                new BigDecimal(
                        "0.1470"),
                fifth.nominalRate());

        assertEquals(
                new BigDecimal(
                        "85500.00"),
                fifth.deduction());
    }

    @Test
    void shouldRejectPeriodAfterCurrentValidatedVersion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> registry.find(
                        SimplesRevenueTaxRoute.ANNEX_I,
                        YearMonth.of(
                                2027,
                                1)));
    }
}