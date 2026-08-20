package br.com.auditortributario.taxrule.simples.revenue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimplesRevenueTaxBracketSelectorTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesRevenueTaxBracketSelector selector = new SimplesRevenueTaxBracketSelector();

    @Test
    void shouldSelectFirstAnnexIBracket() {
        SimplesRevenueTaxBracket bracket = selector.select(
                SimplesRevenueTaxRoute.ANNEX_I,
                COMPETENCE,
                new BigDecimal(
                        "180000.00"));

        assertEquals(
                1,
                bracket.number());

        assertEquals(
                new BigDecimal(
                        "0.0400"),
                bracket.nominalRate());
    }

    @Test
    void shouldSelectSecondAnnexIBracketAboveFirstLimit() {
        SimplesRevenueTaxBracket bracket = selector.select(
                SimplesRevenueTaxRoute.ANNEX_I,
                COMPETENCE,
                new BigDecimal(
                        "180000.01"));

        assertEquals(
                2,
                bracket.number());
    }

    @Test
    void shouldSelectThirdAnnexIBracket() {
        SimplesRevenueTaxBracket bracket = selector.select(
                SimplesRevenueTaxRoute.ANNEX_I,
                COMPETENCE,
                new BigDecimal(
                        "500000.00"));

        assertEquals(
                3,
                bracket.number());

        assertEquals(
                new BigDecimal(
                        "0.0950"),
                bracket.nominalRate());

        assertEquals(
                new BigDecimal(
                        "13860.00"),
                bracket.deduction());
    }

    @Test
    void shouldSelectThirdAnnexIIBracket() {
        SimplesRevenueTaxBracket bracket = selector.select(
                SimplesRevenueTaxRoute.ANNEX_II,
                COMPETENCE,
                new BigDecimal(
                        "500000.00"));

        assertEquals(
                3,
                bracket.number());

        assertEquals(
                new BigDecimal(
                        "0.1000"),
                bracket.nominalRate());

        assertEquals(
                new BigDecimal(
                        "13860.00"),
                bracket.deduction());
    }

    @Test
    void shouldAcceptZeroRevenueBasisInFirstBracket() {
        SimplesRevenueTaxBracket bracket = selector.select(
                SimplesRevenueTaxRoute.ANNEX_I,
                COMPETENCE,
                BigDecimal.ZERO);

        assertEquals(
                1,
                bracket.number());
    }

    @Test
    void shouldRejectRevenueAboveSimplesLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> selector.select(
                        SimplesRevenueTaxRoute.ANNEX_I,
                        COMPETENCE,
                        new BigDecimal(
                                "4800000.01")));
    }

    @Test
    void shouldRejectUnsupportedAnnexInThisSelector() {
        assertThrows(
                IllegalArgumentException.class,
                () -> selector.select(
                        SimplesRevenueTaxRoute.ANNEX_III,
                        COMPETENCE,
                        new BigDecimal(
                                "500000.00")));
    }
}