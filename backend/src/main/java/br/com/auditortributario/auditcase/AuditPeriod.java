package br.com.auditortributario.auditcase;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record AuditPeriod(
                YearMonth start,
                YearMonth end) {

        public AuditPeriod {
                Objects.requireNonNull(
                                start,
                                "A competência inicial não pode ser nula.");

                Objects.requireNonNull(
                                end,
                                "A competência final não pode ser nula.");

                if (end.isBefore(start)) {
                        throw new IllegalArgumentException(
                                        "A competência final não pode ser anterior "
                                                        + "à competência inicial.");
                }
        }

        public static AuditPeriod monthly(
                        YearMonth assessmentPeriod) {
                Objects.requireNonNull(
                                assessmentPeriod,
                                "A competência não pode ser nula.");

                return new AuditPeriod(
                                assessmentPeriod,
                                assessmentPeriod);
        }

        public static AuditPeriod annual(
                        int year) {
                return new AuditPeriod(
                                YearMonth.of(
                                                year,
                                                1),
                                YearMonth.of(
                                                year,
                                                12));
        }

        public boolean isSingleMonth() {
                return start.equals(
                                end);
        }

        public int numberOfMonths() {
                return Math.toIntExact(
                                start.until(
                                                end,
                                                ChronoUnit.MONTHS))
                                + 1;
        }

        public List<YearMonth> months() {
                List<YearMonth> months = new ArrayList<>();

                YearMonth current = start;

                while (!current.isAfter(end)) {
                        months.add(
                                        current);

                        current = current.plusMonths(
                                        1);
                }

                return List.copyOf(
                                months);
        }
}