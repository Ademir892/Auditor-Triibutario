package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SimplesRevenueTaxDistributionRegistry {

        private static final YearMonth VALID_FROM = YearMonth.of(
                        2018,
                        1);

        private static final YearMonth VALID_UNTIL = YearMonth.of(
                        2026,
                        12);

        private static final String VERSION = "LC123-ANEXOS-I-II-IV-PARTILHA-2018-2026";

        private final Map<SimplesRevenueTaxRoute, Map<Integer, SimplesRevenueTaxDistributionRule>> rules;

        public SimplesRevenueTaxDistributionRegistry() {
                this.rules = Map.of(
                                SimplesRevenueTaxRoute.ANNEX_I,
                                createAnnexIRules(),

                                SimplesRevenueTaxRoute.ANNEX_II,
                                createAnnexIIRules(),

                                SimplesRevenueTaxRoute.ANNEX_IV,
                                createAnnexIVRules());
        }

        public SimplesRevenueTaxDistributionRule find(
                        SimplesRevenueTaxRoute route,
                        int bracketNumber,
                        YearMonth competence) {
                Objects.requireNonNull(
                                route,
                                "A rota tributária não pode ser nula.");

                Objects.requireNonNull(
                                competence,
                                "A competência não pode ser nula.");

                validateCompetence(
                                competence);

                Map<Integer, SimplesRevenueTaxDistributionRule> routeRules = rules.get(
                                route);

                if (routeRules == null) {
                        throw new IllegalArgumentException(
                                        "Não existe repartição implementada para "
                                                        + route.getDisplayName()
                                                        + ".");
                }

                SimplesRevenueTaxDistributionRule rule = routeRules.get(
                                bracketNumber);

                if (rule == null) {
                        throw new IllegalArgumentException(
                                        "Não existe repartição para a faixa "
                                                        + bracketNumber
                                                        + " de "
                                                        + route.getDisplayName()
                                                        + ".");
                }

                return rule;
        }

        public String version() {
                return VERSION;
        }

        private void validateCompetence(
                        YearMonth competence) {
                if (competence.isBefore(
                                VALID_FROM)
                                || competence.isAfter(
                                                VALID_UNTIL)) {

                        throw new IllegalArgumentException(
                                        "Não existe tabela de repartição validada "
                                                        + "para a competência "
                                                        + competence
                                                        + ". Vigência suportada: "
                                                        + VALID_FROM
                                                        + " até "
                                                        + VALID_UNTIL
                                                        + ".");
                }
        }

        private static Map<Integer, SimplesRevenueTaxDistributionRule> createAnnexIRules() {

                return Map.of(
                                1,
                                annexIRule(
                                                1,
                                                "0.0550",
                                                "0.0350",
                                                "0.1274",
                                                "0.0276",
                                                "0.4150",
                                                "0.3400"),
                                2,
                                annexIRule(
                                                2,
                                                "0.0550",
                                                "0.0350",
                                                "0.1274",
                                                "0.0276",
                                                "0.4150",
                                                "0.3400"),
                                3,
                                annexIRule(
                                                3,
                                                "0.0550",
                                                "0.0350",
                                                "0.1274",
                                                "0.0276",
                                                "0.4200",
                                                "0.3350"),
                                4,
                                annexIRule(
                                                4,
                                                "0.0550",
                                                "0.0350",
                                                "0.1274",
                                                "0.0276",
                                                "0.4200",
                                                "0.3350"),
                                5,
                                annexIRule(
                                                5,
                                                "0.0550",
                                                "0.0350",
                                                "0.1274",
                                                "0.0276",
                                                "0.4200",
                                                "0.3350"),
                                6,
                                annexISixthBracket());
        }

        private static Map<Integer, SimplesRevenueTaxDistributionRule> createAnnexIIRules() {

                return Map.of(
                                1,
                                annexIIStandardRule(
                                                1),
                                2,
                                annexIIStandardRule(
                                                2),
                                3,
                                annexIIStandardRule(
                                                3),
                                4,
                                annexIIStandardRule(
                                                4),
                                5,
                                annexIIStandardRule(
                                                5),
                                6,
                                annexIISixthBracket());
        }

        private static Map<Integer, SimplesRevenueTaxDistributionRule> createAnnexIVRules() {

                return Map.of(
                                1,
                                annexIVRule(
                                                1,
                                                "0.1880",
                                                "0.1520",
                                                "0.1767",
                                                "0.0383",
                                                "0.4450"),
                                2,
                                annexIVRule(
                                                2,
                                                "0.1980",
                                                "0.1520",
                                                "0.2055",
                                                "0.0445",
                                                "0.4000"),
                                3,
                                annexIVRule(
                                                3,
                                                "0.2080",
                                                "0.1520",
                                                "0.1973",
                                                "0.0427",
                                                "0.4000"),
                                4,
                                annexIVRule(
                                                4,
                                                "0.1780",
                                                "0.1920",
                                                "0.1890",
                                                "0.0410",
                                                "0.4000"),
                                5,
                                annexIVRule(
                                                5,
                                                "0.1880",
                                                "0.1920",
                                                "0.1808",
                                                "0.0392",
                                                "0.4000"),
                                6,
                                annexIVSixthBracket());
        }

        private static SimplesRevenueTaxDistributionRule annexIRule(
                        int bracketNumber,
                        String irpj,
                        String csll,
                        String cofins,
                        String pis,
                        String cpp,
                        String icms) {
                return new SimplesRevenueTaxDistributionRule(
                                SimplesRevenueTaxRoute.ANNEX_I,
                                bracketNumber,
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
                                                                cpp),
                                                share(
                                                                TaxComponent.ICMS,
                                                                icms)));
        }

        private static SimplesRevenueTaxDistributionRule annexISixthBracket() {

                return new SimplesRevenueTaxDistributionRule(
                                SimplesRevenueTaxRoute.ANNEX_I,
                                6,
                                List.of(
                                                share(
                                                                TaxComponent.IRPJ,
                                                                "0.1350"),
                                                share(
                                                                TaxComponent.CSLL,
                                                                "0.1000"),
                                                share(
                                                                TaxComponent.COFINS,
                                                                "0.2827"),
                                                share(
                                                                TaxComponent.PIS_PASEP,
                                                                "0.0613"),
                                                share(
                                                                TaxComponent.CPP,
                                                                "0.4210")));
        }

        private static SimplesRevenueTaxDistributionRule annexIIStandardRule(
                        int bracketNumber) {

                return new SimplesRevenueTaxDistributionRule(
                                SimplesRevenueTaxRoute.ANNEX_II,
                                bracketNumber,
                                List.of(
                                                share(
                                                                TaxComponent.IRPJ,
                                                                "0.0550"),
                                                share(
                                                                TaxComponent.CSLL,
                                                                "0.0350"),
                                                share(
                                                                TaxComponent.COFINS,
                                                                "0.1151"),
                                                share(
                                                                TaxComponent.PIS_PASEP,
                                                                "0.0249"),
                                                share(
                                                                TaxComponent.CPP,
                                                                "0.3750"),
                                                share(
                                                                TaxComponent.IPI,
                                                                "0.0750"),
                                                share(
                                                                TaxComponent.ICMS,
                                                                "0.3200")));
        }

        private static SimplesRevenueTaxDistributionRule annexIISixthBracket() {

                return new SimplesRevenueTaxDistributionRule(
                                SimplesRevenueTaxRoute.ANNEX_II,
                                6,
                                List.of(
                                                share(
                                                                TaxComponent.IRPJ,
                                                                "0.0850"),
                                                share(
                                                                TaxComponent.CSLL,
                                                                "0.0750"),
                                                share(
                                                                TaxComponent.COFINS,
                                                                "0.2096"),
                                                share(
                                                                TaxComponent.PIS_PASEP,
                                                                "0.0454"),
                                                share(
                                                                TaxComponent.CPP,
                                                                "0.2350"),
                                                share(
                                                                TaxComponent.IPI,
                                                                "0.3500")));
        }

        private static SimplesRevenueTaxDistributionRule annexIVRule(
                        int bracketNumber,
                        String irpj,
                        String csll,
                        String cofins,
                        String pis,
                        String iss) {
                return new SimplesRevenueTaxDistributionRule(
                                SimplesRevenueTaxRoute.ANNEX_IV,
                                bracketNumber,
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
                                                                TaxComponent.ISS,
                                                                iss)));
        }

        private static SimplesRevenueTaxDistributionRule annexIVSixthBracket() {

                return new SimplesRevenueTaxDistributionRule(
                                SimplesRevenueTaxRoute.ANNEX_IV,
                                6,
                                List.of(
                                                share(
                                                                TaxComponent.IRPJ,
                                                                "0.5350"),
                                                share(
                                                                TaxComponent.CSLL,
                                                                "0.2150"),
                                                share(
                                                                TaxComponent.COFINS,
                                                                "0.2055"),
                                                share(
                                                                TaxComponent.PIS_PASEP,
                                                                "0.0445")));
        }

        private static SimplesRevenueTaxDistributionRule.ComponentShare share(
                        TaxComponent component,
                        String distributionRate) {
                return new SimplesRevenueTaxDistributionRule.ComponentShare(
                                component,
                                new BigDecimal(
                                                distributionRate));
        }
}