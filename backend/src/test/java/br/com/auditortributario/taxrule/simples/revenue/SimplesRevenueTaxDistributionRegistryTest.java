package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimplesRevenueTaxDistributionRegistryTest {

    private final SimplesRevenueTaxDistributionRegistry registry = new SimplesRevenueTaxDistributionRegistry();

    @Test
    void shouldLoadAnnexISecondBracketDistribution() {
        SimplesRevenueTaxDistributionRule rule = registry.find(
                SimplesRevenueTaxRoute.ANNEX_I,
                2,
                YearMonth.of(
                        2026,
                        8));

        assertEquals(
                new BigDecimal(
                        "0.0550"),
                rule.find(
                        TaxComponent.IRPJ).distributionRate());

        assertEquals(
                new BigDecimal(
                        "0.4150"),
                rule.find(
                        TaxComponent.CPP).distributionRate());

        assertEquals(
                new BigDecimal(
                        "0.3400"),
                rule.find(
                        TaxComponent.ICMS).distributionRate());
    }

    @Test
    void shouldLoadAnnexIIThirdBracketDistribution() {
        SimplesRevenueTaxDistributionRule rule = registry.find(
                SimplesRevenueTaxRoute.ANNEX_II,
                3,
                YearMonth.of(
                        2026,
                        8));

        assertEquals(
                new BigDecimal(
                        "0.0750"),
                rule.find(
                        TaxComponent.IPI).distributionRate());

        assertEquals(
                new BigDecimal(
                        "0.3200"),
                rule.find(
                        TaxComponent.ICMS).distributionRate());
    }

    @Test
    void shouldRejectFutureCompetence() {
        assertThrows(
                IllegalArgumentException.class,
                () -> registry.find(
                        SimplesRevenueTaxRoute.ANNEX_I,
                        1,
                        YearMonth.of(
                                2027,
                                1)));
    }
}