package br.com.auditortributario.taxrule.simples;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesTaxBracketSelectorTest {

    private final FatorRCalculator fatorRCalculator = new FatorRCalculator();

    private final TaxBracketRevenueBasisCalculator revenueCalculator = new TaxBracketRevenueBasisCalculator();

    private final SimplesTaxBracketSelector selector = new SimplesTaxBracketSelector();

    @Test
    void shouldSelectSecondBracketFromAnnexIII() {
        FatorRCalculationResult fatorRResult = fatorRCalculator.calculateOpeningMonth(
                new BigDecimal("6000.00"),
                new BigDecimal("20000.00"));

        TaxBracketRevenueBasisResult revenueResult = calculateFirstMonthRevenueBasis(
                "20000.00");

        SimplesTaxBracketSelectionResult result = selector.select(
                new SimplesTaxBracketSelectionRequest(
                        YearMonth.of(2026, 2),
                        fatorRResult,
                        revenueResult));

        assertEquals(
                SimplesAnnex.ANEXO_III,
                result.fatorRResult().annex());

        assertEquals(
                2,
                result.bracket().number());

        assertEquals(
                new BigDecimal("0.112"),
                result.bracket().nominalRate());

        assertEquals(
                new BigDecimal("9360.00"),
                result.bracket().deduction());
    }

    @Test
    void shouldSelectSecondBracketFromAnnexV() {
        FatorRCalculationResult fatorRResult = fatorRCalculator.calculateOpeningMonth(
                new BigDecimal("5000.00"),
                new BigDecimal("20000.00"));

        TaxBracketRevenueBasisResult revenueResult = calculateFirstMonthRevenueBasis(
                "20000.00");

        SimplesTaxBracketSelectionResult result = selector.select(
                new SimplesTaxBracketSelectionRequest(
                        YearMonth.of(2026, 2),
                        fatorRResult,
                        revenueResult));

        assertEquals(
                SimplesAnnex.ANEXO_V,
                result.fatorRResult().annex());

        assertEquals(
                2,
                result.bracket().number());

        assertEquals(
                new BigDecimal("0.18"),
                result.bracket().nominalRate());

        assertEquals(
                new BigDecimal("4500.00"),
                result.bracket().deduction());
    }

    @Test
    void shouldSelectFifthBracketUsingOfficialRbt12pExample() {
        FatorRCalculationResult fatorRResult = fatorRCalculator.calculateUnderThirteenMonths(
                new BigDecimal("30000.00"),
                new BigDecimal("100000.00"));

        TaxBracketRevenueBasisResult revenueResult = revenueCalculator.calculate(
                new TaxBracketRevenueBasisRequest(
                        LocalDate.of(2026, 2, 10),
                        YearMonth.of(2026, 5),
                        List.of(
                                revenue(
                                        2026,
                                        2,
                                        "10000.00"),
                                revenue(
                                        2026,
                                        3,
                                        "0.00"),
                                revenue(
                                        2026,
                                        4,
                                        "590000.00"))));

        SimplesTaxBracketSelectionResult result = selector.select(
                new SimplesTaxBracketSelectionRequest(
                        YearMonth.of(2026, 5),
                        fatorRResult,
                        revenueResult));

        assertEquals(
                new BigDecimal("2400000.00"),
                result.revenueBasisResult()
                        .revenueBasis());

        assertEquals(
                SimplesAnnex.ANEXO_III,
                result.fatorRResult()
                        .annex());

        assertEquals(
                5,
                result.bracket()
                        .number());

        assertEquals(
                new BigDecimal("0.21"),
                result.bracket()
                        .nominalRate());

        assertEquals(
                new BigDecimal("125640.00"),
                result.bracket()
                        .deduction());
    }

    @Test
    void shouldCreateAuditableBracketDecision() {
        FatorRCalculationResult fatorRResult = fatorRCalculator.calculateUnderThirteenMonths(
                new BigDecimal("30000.00"),
                new BigDecimal("100000.00"));

        TaxBracketRevenueBasisResult revenueResult = revenueCalculator.calculate(
                new TaxBracketRevenueBasisRequest(
                        LocalDate.of(2026, 2, 10),
                        YearMonth.of(2026, 5),
                        List.of(
                                revenue(
                                        2026,
                                        2,
                                        "10000.00"),
                                revenue(
                                        2026,
                                        3,
                                        "0.00"),
                                revenue(
                                        2026,
                                        4,
                                        "590000.00"))));

        SimplesTaxBracketSelectionResult result = selector.select(
                new SimplesTaxBracketSelectionRequest(
                        YearMonth.of(2026, 5),
                        fatorRResult,
                        revenueResult));

        assertEquals(
                "SIMPLES_TAX_BRACKET_SELECTION",
                result.decision()
                        .ruleCode());

        assertTrue(
                result.decision()
                        .description()
                        .contains("Anexo III"));

        assertTrue(
                result.decision()
                        .description()
                        .contains("RBT12p"));

        assertTrue(
                result.decision()
                        .condition()
                        .contains("2400000.00"));

        assertTrue(
                result.decision()
                        .result()
                        .contains("Faixa = 5"));

        assertTrue(
                result.decision()
                        .result()
                        .contains("21.00%"));

        assertTrue(
                result.decision()
                        .result()
                        .contains("125640.00"));
    }

    @Test
    void shouldRejectRevenueAboveSimplesTableLimit() {
        FatorRCalculationResult fatorRResult = fatorRCalculator.calculateOpeningMonth(
                new BigDecimal("300000.00"),
                new BigDecimal("1000000.00"));

        TaxBracketRevenueBasisResult revenueResult = calculateFirstMonthRevenueBasis(
                "500000.00");

        SimplesTaxBracketSelectionRequest request = new SimplesTaxBracketSelectionRequest(
                YearMonth.of(2026, 2),
                fatorRResult,
                revenueResult);

        assertThrows(
                IllegalArgumentException.class,
                () -> selector.select(request));
    }

    @Test
    void shouldRejectPeriodWithoutRegisteredTaxTable() {
        FatorRCalculationResult fatorRResult = fatorRCalculator.calculate(
                new BigDecimal("30000.00"),
                new BigDecimal("100000.00"));

        TaxBracketRevenueBasisResult revenueResult = calculateFirstMonthRevenueBasis(
                "20000.00");

        SimplesTaxBracketSelectionRequest request = new SimplesTaxBracketSelectionRequest(
                YearMonth.of(2027, 1),
                fatorRResult,
                revenueResult);

        assertThrows(
                IllegalArgumentException.class,
                () -> selector.select(request));
    }

    @Test
    void shouldRejectNullRequest() {
        assertThrows(
                NullPointerException.class,
                () -> selector.select(null));
    }

    private TaxBracketRevenueBasisResult calculateFirstMonthRevenueBasis(
            String monthlyRevenue) {
        return revenueCalculator.calculate(
                new TaxBracketRevenueBasisRequest(
                        LocalDate.of(2026, 2, 10),
                        YearMonth.of(2026, 2),
                        List.of(
                                revenue(
                                        2026,
                                        2,
                                        monthlyRevenue))));
    }

    private MonthlyRevenue revenue(
            int year,
            int month,
            String amount) {
        return new MonthlyRevenue(
                YearMonth.of(
                        year,
                        month),
                new BigDecimal(amount));
    }
}