package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class SimplesRevenueAnnexIVRegistryTest {

    private static final YearMonth COMPETENCE = YearMonth.of(
            2026,
            8);

    private final SimplesRevenueTaxTableRegistry tableRegistry = new SimplesRevenueTaxTableRegistry();

    private final SimplesRevenueTaxDistributionRegistry distributionRegistry = new SimplesRevenueTaxDistributionRegistry();

    private final SimplesRevenueTaxBracketSelector bracketSelector = new SimplesRevenueTaxBracketSelector();

    @Test
    void shouldExposeOfficialAnnexIVTaxTable() {
        SimplesRevenueTaxTable table = tableRegistry.find(
                SimplesRevenueTaxRoute.ANNEX_IV,
                COMPETENCE);

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_IV,
                table.route());

        assertEquals(
                6,
                table.brackets().size());

        assertBracket(
                table,
                1,
                "180000.00",
                "0.0450",
                "0.00");

        assertBracket(
                table,
                2,
                "360000.00",
                "0.0900",
                "8100.00");

        assertBracket(
                table,
                3,
                "720000.00",
                "0.1020",
                "12420.00");

        assertBracket(
                table,
                4,
                "1800000.00",
                "0.1400",
                "39780.00");

        assertBracket(
                table,
                5,
                "3600000.00",
                "0.2200",
                "183780.00");

        assertBracket(
                table,
                6,
                "4800000.00",
                "0.3300",
                "828000.00");
    }

    @Test
    void shouldSelectSecondAnnexIVBracketForThreeHundredThousand() {
        SimplesRevenueTaxBracket bracket = bracketSelector.select(
                SimplesRevenueTaxRoute.ANNEX_IV,
                COMPETENCE,
                new BigDecimal(
                        "300000.00"));

        assertEquals(
                2,
                bracket.number());

        assertEquals(
                0,
                bracket.nominalRate()
                        .compareTo(
                                new BigDecimal(
                                        "0.0900")));

        assertEquals(
                0,
                bracket.deduction()
                        .compareTo(
                                new BigDecimal(
                                        "8100.00")));
    }

    @Test
    void shouldExposeAnnexIVFirstBracketDistributionWithoutCpp() {
        SimplesRevenueTaxDistributionRule rule = distributionRegistry.find(
                SimplesRevenueTaxRoute.ANNEX_IV,
                1,
                COMPETENCE);

        assertShare(
                rule,
                TaxComponent.IRPJ,
                "0.1880");

        assertShare(
                rule,
                TaxComponent.CSLL,
                "0.1520");

        assertShare(
                rule,
                TaxComponent.COFINS,
                "0.1767");

        assertShare(
                rule,
                TaxComponent.PIS_PASEP,
                "0.0383");

        assertShare(
                rule,
                TaxComponent.ISS,
                "0.4450");

        assertFalse(
                hasComponent(
                        rule,
                        TaxComponent.CPP));
    }

    @Test
    void shouldKeepAnnexIVFifthBracketBaseDistribution() {
        SimplesRevenueTaxDistributionRule rule = distributionRegistry.find(
                SimplesRevenueTaxRoute.ANNEX_IV,
                5,
                COMPETENCE);

        assertShare(
                rule,
                TaxComponent.IRPJ,
                "0.1880");

        assertShare(
                rule,
                TaxComponent.CSLL,
                "0.1920");

        assertShare(
                rule,
                TaxComponent.COFINS,
                "0.1808");

        assertShare(
                rule,
                TaxComponent.PIS_PASEP,
                "0.0392");

        assertShare(
                rule,
                TaxComponent.ISS,
                "0.4000");

        assertFalse(
                hasComponent(
                        rule,
                        TaxComponent.CPP));
    }

    @Test
    void shouldExposeAnnexIVSixthBracketWithoutIssOrCpp() {
        SimplesRevenueTaxDistributionRule rule = distributionRegistry.find(
                SimplesRevenueTaxRoute.ANNEX_IV,
                6,
                COMPETENCE);

        assertShare(
                rule,
                TaxComponent.IRPJ,
                "0.5350");

        assertShare(
                rule,
                TaxComponent.CSLL,
                "0.2150");

        assertShare(
                rule,
                TaxComponent.COFINS,
                "0.2055");

        assertShare(
                rule,
                TaxComponent.PIS_PASEP,
                "0.0445");

        assertFalse(
                hasComponent(
                        rule,
                        TaxComponent.ISS));

        assertFalse(
                hasComponent(
                        rule,
                        TaxComponent.CPP));
    }

    @Test
    void shouldAlwaysAllocateOneHundredPercentOfAnnexIVBaseDistribution() {
        for (int bracketNumber = 1; bracketNumber <= 6; bracketNumber++) {

            SimplesRevenueTaxDistributionRule rule = distributionRegistry.find(
                    SimplesRevenueTaxRoute.ANNEX_IV,
                    bracketNumber,
                    COMPETENCE);

            BigDecimal total = rule
                    .shares()
                    .stream()
                    .map(
                            SimplesRevenueTaxDistributionRule.ComponentShare::distributionRate)
                    .reduce(
                            BigDecimal.ZERO,
                            BigDecimal::add);

            assertEquals(
                    0,
                    BigDecimal.ONE.compareTo(
                            total));
        }
    }

    @Test
    void shouldNeverIncludeCppInsideAnnexIVDistribution() {
        for (int bracketNumber = 1; bracketNumber <= 6; bracketNumber++) {

            SimplesRevenueTaxDistributionRule rule = distributionRegistry.find(
                    SimplesRevenueTaxRoute.ANNEX_IV,
                    bracketNumber,
                    COMPETENCE);

            assertFalse(
                    hasComponent(
                            rule,
                            TaxComponent.CPP));
        }
    }

    private void assertBracket(
            SimplesRevenueTaxTable table,
            int bracketNumber,
            String maximumRevenue,
            String nominalRate,
            String deduction) {
        SimplesRevenueTaxBracket bracket = table
                .brackets()
                .get(
                        bracketNumber - 1);

        assertEquals(
                bracketNumber,
                bracket.number());

        assertEquals(
                SimplesRevenueTaxRoute.ANNEX_IV,
                bracket.route());

        assertEquals(
                0,
                bracket.maximumRevenue()
                        .compareTo(
                                new BigDecimal(
                                        maximumRevenue)));

        assertEquals(
                0,
                bracket.nominalRate()
                        .compareTo(
                                new BigDecimal(
                                        nominalRate)));

        assertEquals(
                0,
                bracket.deduction()
                        .compareTo(
                                new BigDecimal(
                                        deduction)));
    }

    private void assertShare(
            SimplesRevenueTaxDistributionRule rule,
            TaxComponent component,
            String expectedRate) {
        SimplesRevenueTaxDistributionRule.ComponentShare share = rule
                .shares()
                .stream()
                .filter(
                        current -> current.component() == component)
                .findFirst()
                .orElseThrow();

        assertEquals(
                0,
                share
                        .distributionRate()
                        .compareTo(
                                new BigDecimal(
                                        expectedRate)));
    }

    private boolean hasComponent(
            SimplesRevenueTaxDistributionRule rule,
            TaxComponent component) {
        return rule
                .shares()
                .stream()
                .anyMatch(
                        share -> share.component() == component);
    }
}