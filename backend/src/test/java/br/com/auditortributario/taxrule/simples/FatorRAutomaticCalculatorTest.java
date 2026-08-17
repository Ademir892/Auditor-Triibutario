package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FatorRAutomaticCalculatorTest {

    private final FatorRAutomaticCalculator calculator = new FatorRAutomaticCalculator();

    @Test
    void shouldAutomaticallyUseOpeningMonthRule() {
        FatorRCalculationRequest request = new FatorRCalculationRequest(
                LocalDate.of(2026, 1, 15),
                YearMonth.of(2026, 1),
                new BigDecimal("5000.00"),
                new BigDecimal("20000.00"));

        FatorRCalculationResult result = calculator.calculate(request);

        assertEquals(
                FatorRCalculationBasis.OPENING_MONTH,
                result.calculationBasis());

        assertEquals(
                new BigDecimal("0.25"),
                result.fatorR().value());

        assertEquals(
                SimplesAnnex.ANEXO_V,
                result.annex());
    }

    @Test
    void shouldAutomaticallyUseUnderThirteenMonthsRule() {
        FatorRCalculationRequest request = new FatorRCalculationRequest(
                LocalDate.of(2026, 1, 15),
                YearMonth.of(2026, 6),
                new BigDecimal("30000.00"),
                new BigDecimal("100000.00"));

        FatorRCalculationResult result = calculator.calculate(request);

        assertEquals(
                FatorRCalculationBasis.UNDER_13_MONTHS,
                result.calculationBasis());

        assertEquals(
                new BigDecimal("0.30"),
                result.fatorR().value());

        assertEquals(
                SimplesAnnex.ANEXO_III,
                result.annex());
    }

    @Test
    void shouldAutomaticallyUseStandardRuleAfterThirteenMonths() {
        FatorRCalculationRequest request = new FatorRCalculationRequest(
                LocalDate.of(2025, 1, 10),
                YearMonth.of(2026, 2),
                new BigDecimal("72000.00"),
                new BigDecimal("286000.00"));

        FatorRCalculationResult result = calculator.calculate(request);

        assertEquals(
                FatorRCalculationBasis.STANDARD_12_MONTHS,
                result.calculationBasis());

        assertEquals(
                new BigDecimal("0.25"),
                result.fatorR().value());

        assertEquals(
                SimplesAnnex.ANEXO_V,
                result.annex());
    }

    @Test
    void shouldRejectAssessmentPeriodBeforeCompanyOpening() {
        FatorRCalculationRequest request = new FatorRCalculationRequest(
                LocalDate.of(2026, 5, 10),
                YearMonth.of(2026, 4),
                new BigDecimal("5000.00"),
                new BigDecimal("20000.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(request));
    }

    @Test
    void shouldRejectNullRequest() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(null));
    }
}