package br.com.auditortributario.taxrule.simples;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class FatorRPeriodClassifier {

    public FatorRCalculationBasis classify(
            LocalDate openingDate,
            YearMonth assessmentPeriod) {
        Objects.requireNonNull(
                openingDate,
                "A data de abertura não pode ser nula.");

        Objects.requireNonNull(
                assessmentPeriod,
                "O período de apuração não pode ser nulo.");

        YearMonth openingMonth = YearMonth.from(openingDate);

        if (assessmentPeriod.isBefore(openingMonth)) {
            throw new IllegalArgumentException(
                    "O período de apuração não pode ser anterior "
                            + "ao mês de abertura da empresa.");
        }

        long monthsElapsed = ChronoUnit.MONTHS.between(
                openingMonth,
                assessmentPeriod);

        if (monthsElapsed == 0) {
            return FatorRCalculationBasis.OPENING_MONTH;
        }

        if (monthsElapsed < 13) {
            return FatorRCalculationBasis.UNDER_13_MONTHS;
        }

        return FatorRCalculationBasis.STANDARD_12_MONTHS;
    }
}