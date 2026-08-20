package br.com.auditortributario.taxrule.simples.composition;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.simples.SimplesAnnex;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesTaxDistributionTableRegistryTest {

    private final SimplesTaxDistributionTableRegistry registry = new SimplesTaxDistributionTableRegistry();

    @Test
    void shouldLoadAnnexIIIThirdBracket() {
        SimplesTaxDistributionRule rule = registry
                .find(
                        SimplesAnnex.ANEXO_III,
                        YearMonth.of(
                                2026,
                                1))
                .ruleFor(
                        3);

        assertEquals(
                new BigDecimal(
                        "0.0400"),
                rule.find(
                        TaxComponent.IRPJ).distributionRate());

        assertEquals(
                new BigDecimal(
                        "0.1364"),
                rule.find(
                        TaxComponent.COFINS).distributionRate());

        assertEquals(
                new BigDecimal(
                        "0.3250"),
                rule.find(
                        TaxComponent.ISS).distributionRate());
    }

    @Test
    void shouldLoadAnnexVThirdBracket() {
        SimplesTaxDistributionRule rule = registry
                .find(
                        SimplesAnnex.ANEXO_V,
                        YearMonth.of(
                                2026,
                                1))
                .ruleFor(
                        3);

        assertEquals(
                new BigDecimal(
                        "0.2400"),
                rule.find(
                        TaxComponent.IRPJ).distributionRate());

        assertEquals(
                new BigDecimal(
                        "0.1900"),
                rule.find(
                        TaxComponent.ISS).distributionRate());
    }

    @Test
    void shouldHaveIssCapRuleOnAnnexIIIFifthBracket() {
        SimplesTaxDistributionRule rule = registry
                .find(
                        SimplesAnnex.ANEXO_III,
                        YearMonth.of(
                                2026,
                                1))
                .ruleFor(
                        5);

        assertTrue(
                rule.issCapRule()
                        .isPresent());

        assertEquals(
                new BigDecimal(
                        "0.05"),
                rule
                        .issCapRule()
                        .orElseThrow()
                        .maximumEffectiveRate());
    }

    @Test
    void shouldRejectFuturePeriodWithoutValidatedTable() {
        assertThrows(
                IllegalArgumentException.class,
                () -> registry.find(
                        SimplesAnnex.ANEXO_III,
                        YearMonth.of(
                                2027,
                                1)));
    }
}