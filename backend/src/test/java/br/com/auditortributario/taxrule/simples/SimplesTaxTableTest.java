package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimplesTaxTableTest {

    private final SimplesTaxTableRegistry registry = new SimplesTaxTableRegistry();

    @Test
    void shouldSelectFirstBracketAtExactlyOneHundredEightyThousand() {
        SimplesTaxTable table = getAnnexIIITable();

        SimplesTaxBracket bracket = table.findBracket(
                new BigDecimal("180000.00"));

        assertEquals(
                1,
                bracket.number());
    }

    @Test
    void shouldSelectSecondBracketAboveOneHundredEightyThousand() {
        SimplesTaxTable table = getAnnexIIITable();

        SimplesTaxBracket bracket = table.findBracket(
                new BigDecimal("180000.01"));

        assertEquals(
                2,
                bracket.number());
    }

    @Test
    void shouldSelectSecondBracketAtExactlyThreeHundredSixtyThousand() {
        SimplesTaxTable table = getAnnexIIITable();

        SimplesTaxBracket bracket = table.findBracket(
                new BigDecimal("360000.00"));

        assertEquals(
                2,
                bracket.number());
    }

    @Test
    void shouldSelectThirdBracketAboveThreeHundredSixtyThousand() {
        SimplesTaxTable table = getAnnexIIITable();

        SimplesTaxBracket bracket = table.findBracket(
                new BigDecimal("360000.01"));

        assertEquals(
                3,
                bracket.number());
    }

    @Test
    void shouldSelectSixthBracketAtMaximumRevenue() {
        SimplesTaxTable table = getAnnexIIITable();

        SimplesTaxBracket bracket = table.findBracket(
                new BigDecimal("4800000.00"));

        assertEquals(
                6,
                bracket.number());
    }

    @Test
    void shouldRejectRevenueAboveMaximumTableLimit() {
        SimplesTaxTable table = getAnnexIIITable();

        assertThrows(
                IllegalArgumentException.class,
                () -> table.findBracket(
                        new BigDecimal("4800000.01")));
    }

    @Test
    void shouldRejectNegativeRevenue() {
        SimplesTaxTable table = getAnnexIIITable();

        assertThrows(
                IllegalArgumentException.class,
                () -> table.findBracket(
                        new BigDecimal("-1.00")));
    }

    @Test
    void shouldConvertNominalRateToPercentage() {
        SimplesTaxTable table = getAnnexIIITable();

        SimplesTaxBracket bracket = table.findBracket(
                new BigDecimal("286000.00"));

        assertEquals(
                new BigDecimal("11.20"),
                bracket.nominalRateAsPercentage());
    }

    private SimplesTaxTable getAnnexIIITable() {
        return registry.find(
                SimplesAnnex.ANEXO_III,
                YearMonth.of(2026, 8));
    }
}