package br.com.auditortributario.taxrule.simples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

public final class AuditReportValueFormatter {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private AuditReportValueFormatter() {
    }

    public static String formatMoney(
            BigDecimal value) {
        if (value == null) {
            throw new NullPointerException(
                    "O valor monetário não pode ser nulo.");
        }

        BigDecimal normalized = value.setScale(
                2,
                RoundingMode.HALF_UP);

        boolean negative = normalized.compareTo(BigDecimal.ZERO) < 0;

        String plain = normalized
                .abs()
                .toPlainString();

        String[] parts = plain.split("\\.");

        String integerPart = groupThousands(
                parts[0]);

        String decimalPart = parts.length > 1
                ? parts[1]
                : "00";

        return (negative ? "-R$ " : "R$ ")
                + integerPart
                + ","
                + decimalPart;
    }

    public static String formatPercentage(
            BigDecimal rate) {
        if (rate == null) {
            throw new NullPointerException(
                    "A alíquota não pode ser nula.");
        }

        BigDecimal percentage = rate.multiply(
                ONE_HUNDRED)
                .setScale(
                        5,
                        RoundingMode.HALF_UP)
                .stripTrailingZeros();

        return percentage
                .toPlainString()
                .replace(
                        '.',
                        ',')
                + "%";
    }

    public static String formatPeriod(
            YearMonth period) {
        if (period == null) {
            throw new NullPointerException(
                    "A competência não pode ser nula.");
        }

        return String.format(
                "%02d/%04d",
                period.getMonthValue(),
                period.getYear());
    }

    private static String groupThousands(
            String digits) {
        StringBuilder result = new StringBuilder();

        int length = digits.length();

        for (int index = 0; index < length; index++) {
            if (index > 0
                    && (length - index) % 3 == 0) {

                result.append('.');
            }

            result.append(
                    digits.charAt(index));
        }

        return result.toString();
    }
}