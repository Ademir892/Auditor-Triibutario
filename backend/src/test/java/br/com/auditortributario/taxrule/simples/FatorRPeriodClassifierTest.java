package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FatorRPeriodClassifierTest {

    private final FatorRPeriodClassifier classifier = new FatorRPeriodClassifier();

    @Test
    void shouldIdentifyOpeningMonth() {
        FatorRCalculationBasis result = classifier.classify(
                LocalDate.of(2026, 1, 15),
                YearMonth.of(2026, 1));

        assertEquals(
                FatorRCalculationBasis.OPENING_MONTH,
                result);
    }

    @Test
    void shouldIdentifyCompanyUnderThirteenMonths() {
        FatorRCalculationBasis result = classifier.classify(
                LocalDate.of(2026, 1, 15),
                YearMonth.of(2026, 6));

        assertEquals(
                FatorRCalculationBasis.UNDER_13_MONTHS,
                result);
    }

    @Test
    void shouldStillUseUnderThirteenMonthsAtTwelveMonthsElapsed() {
        FatorRCalculationBasis result = classifier.classify(
                LocalDate.of(2026, 1, 15),
                YearMonth.of(2027, 1));

        assertEquals(
                FatorRCalculationBasis.UNDER_13_MONTHS,
                result);
    }

    @Test
    void shouldUseStandardRuleAtThirteenMonthsElapsed() {
        FatorRCalculationBasis result = classifier.classify(
                LocalDate.of(2026, 1, 15),
                YearMonth.of(2027, 2));

        assertEquals(
                FatorRCalculationBasis.STANDARD_12_MONTHS,
                result);
    }

    @Test
    void shouldRejectAssessmentPeriodBeforeOpeningMonth() {
        assertThrows(
                IllegalArgumentException.class,
                () -> classifier.classify(
                        LocalDate.of(2026, 1, 15),
                        YearMonth.of(2025, 12)));
    }

    @Test
    void shouldRejectNullOpeningDate() {
        assertThrows(
                NullPointerException.class,
                () -> classifier.classify(
                        null,
                        YearMonth.of(2026, 1)));
    }

    @Test
    void shouldRejectNullAssessmentPeriod() {
        assertThrows(
                NullPointerException.class,
                () -> classifier.classify(
                        LocalDate.of(2026, 1, 15),
                        null));
    }
}