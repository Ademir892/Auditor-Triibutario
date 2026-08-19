package br.com.auditortributario.auditcase;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditPeriodTest {

        @Test
        void shouldRepresentSingleMonth() {
                AuditPeriod period = AuditPeriod.monthly(
                                YearMonth.of(
                                                2026,
                                                8));

                assertEquals(
                                YearMonth.of(
                                                2026,
                                                8),
                                period.start());

                assertEquals(
                                period.start(),
                                period.end());

                assertEquals(
                                1,
                                period.numberOfMonths());

                assertTrue(
                                period.isSingleMonth());
        }

        @Test
        void shouldCountMonthsInclusively() {
                AuditPeriod period = new AuditPeriod(
                                YearMonth.of(
                                                2026,
                                                2),
                                YearMonth.of(
                                                2026,
                                                8));

                assertEquals(
                                7,
                                period.numberOfMonths());
        }

        @Test
        void shouldGenerateAllMonthsInsidePeriod() {
                AuditPeriod period = new AuditPeriod(
                                YearMonth.of(
                                                2026,
                                                10),
                                YearMonth.of(
                                                2027,
                                                2));

                assertEquals(
                                List.of(
                                                YearMonth.of(
                                                                2026,
                                                                10),
                                                YearMonth.of(
                                                                2026,
                                                                11),
                                                YearMonth.of(
                                                                2026,
                                                                12),
                                                YearMonth.of(
                                                                2027,
                                                                1),
                                                YearMonth.of(
                                                                2027,
                                                                2)),
                                period.months());
        }
}