package br.com.auditortributario.taxrule.simples.composition;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.simples.SimplesAnnex;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public final class SimplesTaxDistributionTableRegistry {

    private static final YearMonth VALID_FROM = YearMonth.of(
            2018,
            1);

    private static final YearMonth VALID_UNTIL = YearMonth.of(
            2026,
            12);

    private static final String VERSION = "LC123-2018-2026";

    private final List<SimplesTaxDistributionTable> tables;

    public SimplesTaxDistributionTableRegistry() {
        this.tables = List.of(
                createAnnexIII(),
                createAnnexV());
    }

    public SimplesTaxDistributionTable find(
            SimplesAnnex annex,
            YearMonth assessmentPeriod) {
        return tables
                .stream()
                .filter(
                        table -> table.annex() == annex
                                && table.isValidFor(
                                        assessmentPeriod))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Não existe tabela de repartição "
                                        + "validada para "
                                        + annex.getDisplayName()
                                        + " na competência "
                                        + assessmentPeriod
                                        + "."));
    }

    private static SimplesTaxDistributionTable createAnnexIII() {
        SimplesIssCapRule fifthBracketIssCap = new SimplesIssCapRule(
                new BigDecimal(
                        "0.05"),
                Map.of(
                        TaxComponent.IRPJ,
                        new BigDecimal(
                                "0.0602"),

                        TaxComponent.CSLL,
                        new BigDecimal(
                                "0.0526"),

                        TaxComponent.COFINS,
                        new BigDecimal(
                                "0.1928"),

                        TaxComponent.PIS_PASEP,
                        new BigDecimal(
                                "0.0418"),

                        TaxComponent.CPP,
                        new BigDecimal(
                                "0.6526")));

        return new SimplesTaxDistributionTable(
                SimplesAnnex.ANEXO_III,
                VERSION,
                VALID_FROM,
                VALID_UNTIL,
                Map.of(
                        1,
                        rule(
                                1,
                                "0.0400",
                                "0.0350",
                                "0.1282",
                                "0.0278",
                                "0.4340",
                                "0.3350"),

                        2,
                        rule(
                                2,
                                "0.0400",
                                "0.0350",
                                "0.1405",
                                "0.0305",
                                "0.4340",
                                "0.3200"),

                        3,
                        rule(
                                3,
                                "0.0400",
                                "0.0350",
                                "0.1364",
                                "0.0296",
                                "0.4340",
                                "0.3250"),

                        4,
                        rule(
                                4,
                                "0.0400",
                                "0.0350",
                                "0.1364",
                                "0.0296",
                                "0.4340",
                                "0.3250"),

                        5,
                        rule(
                                5,
                                "0.0400",
                                "0.0350",
                                "0.1282",
                                "0.0278",
                                "0.4340",
                                "0.3350",
                                fifthBracketIssCap),

                        6,
                        ruleWithoutIss(
                                6,
                                "0.3500",
                                "0.1500",
                                "0.1603",
                                "0.0347",
                                "0.3050")));
    }

    private static SimplesTaxDistributionTable createAnnexV() {
        return new SimplesTaxDistributionTable(
                SimplesAnnex.ANEXO_V,
                VERSION,
                VALID_FROM,
                VALID_UNTIL,
                Map.of(
                        1,
                        rule(
                                1,
                                "0.2500",
                                "0.1500",
                                "0.1410",
                                "0.0305",
                                "0.2885",
                                "0.1400"),

                        2,
                        rule(
                                2,
                                "0.2300",
                                "0.1500",
                                "0.1410",
                                "0.0305",
                                "0.2785",
                                "0.1700"),

                        3,
                        rule(
                                3,
                                "0.2400",
                                "0.1500",
                                "0.1492",
                                "0.0323",
                                "0.2385",
                                "0.1900"),

                        4,
                        rule(
                                4,
                                "0.2100",
                                "0.1500",
                                "0.1574",
                                "0.0341",
                                "0.2385",
                                "0.2100"),

                        5,
                        rule(
                                5,
                                "0.2300",
                                "0.1250",
                                "0.1410",
                                "0.0305",
                                "0.2385",
                                "0.2350"),

                        6,
                        ruleWithoutIss(
                                6,
                                "0.3500",
                                "0.1550",
                                "0.1644",
                                "0.0356",
                                "0.2950")));
    }

    private static SimplesTaxDistributionRule rule(
            int bracket,
            String irpj,
            String csll,
            String cofins,
            String pis,
            String cpp,
            String iss) {
        return new SimplesTaxDistributionRule(
                bracket,
                shares(
                        irpj,
                        csll,
                        cofins,
                        pis,
                        cpp,
                        iss),
                java.util.Optional.empty());
    }

    private static SimplesTaxDistributionRule rule(
            int bracket,
            String irpj,
            String csll,
            String cofins,
            String pis,
            String cpp,
            String iss,
            SimplesIssCapRule issCapRule) {
        return new SimplesTaxDistributionRule(
                bracket,
                shares(
                        irpj,
                        csll,
                        cofins,
                        pis,
                        cpp,
                        iss),
                java.util.Optional.of(
                        issCapRule));
    }

    private static SimplesTaxDistributionRule ruleWithoutIss(
            int bracket,
            String irpj,
            String csll,
            String cofins,
            String pis,
            String cpp) {
        return new SimplesTaxDistributionRule(
                bracket,
                List.of(
                        share(
                                TaxComponent.IRPJ,
                                irpj),
                        share(
                                TaxComponent.CSLL,
                                csll),
                        share(
                                TaxComponent.COFINS,
                                cofins),
                        share(
                                TaxComponent.PIS_PASEP,
                                pis),
                        share(
                                TaxComponent.CPP,
                                cpp)),
                java.util.Optional.empty());
    }

    private static List<SimplesTaxDistributionShare> shares(
            String irpj,
            String csll,
            String cofins,
            String pis,
            String cpp,
            String iss) {
        return List.of(
                share(
                        TaxComponent.IRPJ,
                        irpj),

                share(
                        TaxComponent.CSLL,
                        csll),

                share(
                        TaxComponent.COFINS,
                        cofins),

                share(
                        TaxComponent.PIS_PASEP,
                        pis),

                share(
                        TaxComponent.CPP,
                        cpp),

                share(
                        TaxComponent.ISS,
                        iss));
    }

    private static SimplesTaxDistributionShare share(
            TaxComponent component,
            String rate) {
        return new SimplesTaxDistributionShare(
                component,
                new BigDecimal(
                        rate));
    }
}