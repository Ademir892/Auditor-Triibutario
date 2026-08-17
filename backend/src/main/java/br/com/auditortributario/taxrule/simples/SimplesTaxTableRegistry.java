package br.com.auditortributario.taxrule.simples;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public final class SimplesTaxTableRegistry {

    private static final String VERSION = "2018-2026.1";

    private static final YearMonth VALID_FROM = YearMonth.of(2018, 1);

    private static final YearMonth VALID_UNTIL = YearMonth.of(2026, 12);

    private static final List<SimplesTaxTable> TABLES = List.of(
            createAnnexIIITable(),
            createAnnexVTable());

    public SimplesTaxTable find(
            SimplesAnnex annex,
            YearMonth assessmentPeriod) {
        Objects.requireNonNull(
                annex,
                "O anexo não pode ser nulo.");

        Objects.requireNonNull(
                assessmentPeriod,
                "O período de apuração não pode ser nulo.");

        return TABLES.stream()
                .filter(table -> table.annex() == annex
                        && table.isValidFor(assessmentPeriod))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Não existe tabela tributária cadastrada "
                                + "para "
                                + annex.getDisplayName()
                                + " na competência "
                                + assessmentPeriod
                                + "."));
    }

    private static SimplesTaxTable createAnnexIIITable() {
        return new SimplesTaxTable(
                VERSION,
                SimplesAnnex.ANEXO_III,
                VALID_FROM,
                VALID_UNTIL,
                List.of(
                        new SimplesTaxBracket(
                                1,
                                new BigDecimal("180000.00"),
                                new BigDecimal("0.06"),
                                new BigDecimal("0.00")),
                        new SimplesTaxBracket(
                                2,
                                new BigDecimal("360000.00"),
                                new BigDecimal("0.112"),
                                new BigDecimal("9360.00")),
                        new SimplesTaxBracket(
                                3,
                                new BigDecimal("720000.00"),
                                new BigDecimal("0.135"),
                                new BigDecimal("17640.00")),
                        new SimplesTaxBracket(
                                4,
                                new BigDecimal("1800000.00"),
                                new BigDecimal("0.16"),
                                new BigDecimal("35640.00")),
                        new SimplesTaxBracket(
                                5,
                                new BigDecimal("3600000.00"),
                                new BigDecimal("0.21"),
                                new BigDecimal("125640.00")),
                        new SimplesTaxBracket(
                                6,
                                new BigDecimal("4800000.00"),
                                new BigDecimal("0.33"),
                                new BigDecimal("648000.00"))),
                "Lei Complementar nº 123/2006, "
                        + "Anexo III, redação da LC nº 155/2016.");
    }

    private static SimplesTaxTable createAnnexVTable() {
        return new SimplesTaxTable(
                VERSION,
                SimplesAnnex.ANEXO_V,
                VALID_FROM,
                VALID_UNTIL,
                List.of(
                        new SimplesTaxBracket(
                                1,
                                new BigDecimal("180000.00"),
                                new BigDecimal("0.155"),
                                new BigDecimal("0.00")),
                        new SimplesTaxBracket(
                                2,
                                new BigDecimal("360000.00"),
                                new BigDecimal("0.18"),
                                new BigDecimal("4500.00")),
                        new SimplesTaxBracket(
                                3,
                                new BigDecimal("720000.00"),
                                new BigDecimal("0.195"),
                                new BigDecimal("9900.00")),
                        new SimplesTaxBracket(
                                4,
                                new BigDecimal("1800000.00"),
                                new BigDecimal("0.205"),
                                new BigDecimal("17100.00")),
                        new SimplesTaxBracket(
                                5,
                                new BigDecimal("3600000.00"),
                                new BigDecimal("0.23"),
                                new BigDecimal("62100.00")),
                        new SimplesTaxBracket(
                                6,
                                new BigDecimal("4800000.00"),
                                new BigDecimal("0.305"),
                                new BigDecimal("540000.00"))),
                "Lei Complementar nº 123/2006, "
                        + "Anexo V, redação da LC nº 155/2016.");
    }
}