package br.com.auditortributario.taxrule.simples.revenue;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public final class SimplesRevenueTaxTableRegistry {

        private static final YearMonth VALID_FROM = YearMonth.of(
                        2018,
                        1);

        private static final YearMonth VALID_UNTIL = YearMonth.of(
                        2026,
                        12);

        private static final String VERSION = "LC123-ANEXOS-I-II-IV-2018-2026";

        private final List<SimplesRevenueTaxTable> tables;

        public SimplesRevenueTaxTableRegistry() {
                this.tables = List.of(
                                createAnnexI(),
                                createAnnexII(),
                                createAnnexIV());
        }

        public SimplesRevenueTaxTable find(
                        SimplesRevenueTaxRoute route,
                        YearMonth competence) {
                Objects.requireNonNull(
                                route,
                                "A rota tributária não pode ser nula.");

                Objects.requireNonNull(
                                competence,
                                "A competência não pode ser nula.");

                return tables
                                .stream()
                                .filter(
                                                table -> table.route() == route
                                                                && table.isValidFor(
                                                                                competence))
                                .findFirst()
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "Não existe tabela tributária "
                                                                                + "validada para "
                                                                                + route.getDisplayName()
                                                                                + " na competência "
                                                                                + competence
                                                                                + "."));
        }

        private static SimplesRevenueTaxTable createAnnexI() {
                return new SimplesRevenueTaxTable(
                                SimplesRevenueTaxRoute.ANNEX_I,
                                VERSION,
                                VALID_FROM,
                                VALID_UNTIL,
                                List.of(
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_I,
                                                                1,
                                                                "180000.00",
                                                                "0.0400",
                                                                "0.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_I,
                                                                2,
                                                                "360000.00",
                                                                "0.0730",
                                                                "5940.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_I,
                                                                3,
                                                                "720000.00",
                                                                "0.0950",
                                                                "13860.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_I,
                                                                4,
                                                                "1800000.00",
                                                                "0.1070",
                                                                "22500.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_I,
                                                                5,
                                                                "3600000.00",
                                                                "0.1430",
                                                                "87300.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_I,
                                                                6,
                                                                "4800000.00",
                                                                "0.1900",
                                                                "378000.00")));
        }

        private static SimplesRevenueTaxTable createAnnexII() {
                return new SimplesRevenueTaxTable(
                                SimplesRevenueTaxRoute.ANNEX_II,
                                VERSION,
                                VALID_FROM,
                                VALID_UNTIL,
                                List.of(
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_II,
                                                                1,
                                                                "180000.00",
                                                                "0.0450",
                                                                "0.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_II,
                                                                2,
                                                                "360000.00",
                                                                "0.0780",
                                                                "5940.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_II,
                                                                3,
                                                                "720000.00",
                                                                "0.1000",
                                                                "13860.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_II,
                                                                4,
                                                                "1800000.00",
                                                                "0.1120",
                                                                "22500.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_II,
                                                                5,
                                                                "3600000.00",
                                                                "0.1470",
                                                                "85500.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_II,
                                                                6,
                                                                "4800000.00",
                                                                "0.3000",
                                                                "720000.00")));
        }

        private static SimplesRevenueTaxTable createAnnexIV() {
                return new SimplesRevenueTaxTable(
                                SimplesRevenueTaxRoute.ANNEX_IV,
                                VERSION,
                                VALID_FROM,
                                VALID_UNTIL,
                                List.of(
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_IV,
                                                                1,
                                                                "180000.00",
                                                                "0.0450",
                                                                "0.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_IV,
                                                                2,
                                                                "360000.00",
                                                                "0.0900",
                                                                "8100.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_IV,
                                                                3,
                                                                "720000.00",
                                                                "0.1020",
                                                                "12420.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_IV,
                                                                4,
                                                                "1800000.00",
                                                                "0.1400",
                                                                "39780.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_IV,
                                                                5,
                                                                "3600000.00",
                                                                "0.2200",
                                                                "183780.00"),
                                                bracket(
                                                                SimplesRevenueTaxRoute.ANNEX_IV,
                                                                6,
                                                                "4800000.00",
                                                                "0.3300",
                                                                "828000.00")));
        }

        private static SimplesRevenueTaxBracket bracket(
                        SimplesRevenueTaxRoute route,
                        int number,
                        String maximumRevenue,
                        String nominalRate,
                        String deduction) {
                return new SimplesRevenueTaxBracket(
                                route,
                                number,
                                new BigDecimal(
                                                maximumRevenue),
                                new BigDecimal(
                                                nominalRate),
                                new BigDecimal(
                                                deduction));
        }
}