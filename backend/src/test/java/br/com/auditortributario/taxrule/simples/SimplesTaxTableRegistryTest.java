package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimplesTaxTableRegistryTest {

    private final SimplesTaxTableRegistry registry = new SimplesTaxTableRegistry();

    @Test
    void shouldLoadAnnexIIITableFor2026() {
        SimplesTaxTable table = registry.find(
                SimplesAnnex.ANEXO_III,
                YearMonth.of(2026, 8));

        assertEquals(
                SimplesAnnex.ANEXO_III,
                table.annex());

        assertEquals(
                "2018-2026.1",
                table.version());

        assertEquals(
                6,
                table.brackets().size());
    }

    @Test
    void shouldContainCorrectAnnexIIIBrackets() {
        SimplesTaxTable table = registry.find(
                SimplesAnnex.ANEXO_III,
                YearMonth.of(2026, 8));

        assertBracket(
                table.brackets().get(0),
                1,
                "180000.00",
                "0.06",
                "0.00");

        assertBracket(
                table.brackets().get(1),
                2,
                "360000.00",
                "0.112",
                "9360.00");

        assertBracket(
                table.brackets().get(2),
                3,
                "720000.00",
                "0.135",
                "17640.00");

        assertBracket(
                table.brackets().get(3),
                4,
                "1800000.00",
                "0.16",
                "35640.00");

        assertBracket(
                table.brackets().get(4),
                5,
                "3600000.00",
                "0.21",
                "125640.00");

        assertBracket(
                table.brackets().get(5),
                6,
                "4800000.00",
                "0.33",
                "648000.00");
    }

    @Test
    void shouldContainCorrectAnnexVBrackets() {
        SimplesTaxTable table = registry.find(
                SimplesAnnex.ANEXO_V,
                YearMonth.of(2026, 8));

        assertBracket(
                table.brackets().get(0),
                1,
                "180000.00",
                "0.155",
                "0.00");

        assertBracket(
                table.brackets().get(1),
                2,
                "360000.00",
                "0.18",
                "4500.00");

        assertBracket(
                table.brackets().get(2),
                3,
                "720000.00",
                "0.195",
                "9900.00");

        assertBracket(
                table.brackets().get(3),
                4,
                "1800000.00",
                "0.205",
                "17100.00");

        assertBracket(
                table.brackets().get(4),
                5,
                "3600000.00",
                "0.23",
                "62100.00");

        assertBracket(
                table.brackets().get(5),
                6,
                "4800000.00",
                "0.305",
                "540000.00");
    }

    @Test
    void shouldRejectPeriodWithoutRegisteredTable() {
        assertThrows(
                IllegalArgumentException.class,
                () -> registry.find(
                        SimplesAnnex.ANEXO_III,
                        YearMonth.of(2027, 1)));
    }

    @Test
    void shouldRejectNullAnnex() {
        assertThrows(
                NullPointerException.class,
                () -> registry.find(
                        null,
                        YearMonth.of(2026, 8)));
    }

    @Test
    void shouldRejectNullAssessmentPeriod() {
        assertThrows(
                NullPointerException.class,
                () -> registry.find(
                        SimplesAnnex.ANEXO_III,
                        null));
    }

    private void assertBracket(
            SimplesTaxBracket bracket,
            int number,
            String maximumRevenue,
            String nominalRate,
            String deduction) {
        assertEquals(
                number,
                bracket.number());

        assertEquals(
                new BigDecimal(maximumRevenue),
                bracket.maximumRevenue());

        assertEquals(
                new BigDecimal(nominalRate),
                bracket.nominalRate());

        assertEquals(
                new BigDecimal(deduction),
                bracket.deduction());
    }
}