package br.com.auditortributario.taxrule.simples.composition;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.TaxComponentAllocation;
import br.com.auditortributario.taxrule.simples.FatorRAutomaticCalculator;
import br.com.auditortributario.taxrule.simples.FatorRCalculationRequest;
import br.com.auditortributario.taxrule.simples.FatorRCalculationResult;
import br.com.auditortributario.taxrule.simples.MonthlyRevenue;
import br.com.auditortributario.taxrule.simples.SimplesEffectiveRateCalculator;
import br.com.auditortributario.taxrule.simples.SimplesEffectiveRateResult;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxCalculator;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxRequest;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxResult;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracketSelectionRequest;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracketSelectionResult;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracketSelector;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisCalculator;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisRequest;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisResult;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesTaxCompositionCalculatorTest {

    private final SimplesTaxCompositionCalculator calculator = new SimplesTaxCompositionCalculator();

    @Test
    void shouldReproduceOfficialAnnexIIIExample() {
        SimplesEstimatedTaxResult estimated = createEstimatedResult(
                new BigDecimal(
                        "150000.00"),
                new BigDecimal(
                        "500000.00"),
                new BigDecimal(
                        "500000.00"),
                new BigDecimal(
                        "10000.00"));

        SimplesTaxCompositionResult result = calculator.calculate(
                estimated);

        assertEquals(
                new BigDecimal(
                        "997.20"),
                estimated.estimatedTaxAmount());

        assertFalse(
                result.issCapApplied());

        assertAllocation(
                result,
                TaxComponent.IRPJ,
                "39.89");

        assertAllocation(
                result,
                TaxComponent.CSLL,
                "34.90");

        assertAllocation(
                result,
                TaxComponent.COFINS,
                "136.02");

        assertAllocation(
                result,
                TaxComponent.PIS_PASEP,
                "29.52");

        assertAllocation(
                result,
                TaxComponent.CPP,
                "432.78");

        assertAllocation(
                result,
                TaxComponent.ISS,
                "324.09");

        assertTrue(
                result
                        .composition()
                        .isFullyAllocated());
    }

    @Test
    void shouldCalculateAnnexVThirdBracketComposition() {
        SimplesEstimatedTaxResult estimated = createEstimatedResult(
                new BigDecimal(
                        "125000.00"),
                new BigDecimal(
                        "500000.00"),
                new BigDecimal(
                        "500000.00"),
                new BigDecimal(
                        "10000.00"));

        SimplesTaxCompositionResult result = calculator.calculate(
                estimated);

        assertEquals(
                new BigDecimal(
                        "1752.00"),
                estimated.estimatedTaxAmount());

        assertAllocation(
                result,
                TaxComponent.IRPJ,
                "420.48");

        assertAllocation(
                result,
                TaxComponent.CSLL,
                "262.80");

        assertAllocation(
                result,
                TaxComponent.COFINS,
                "261.40");

        assertAllocation(
                result,
                TaxComponent.PIS_PASEP,
                "56.59");

        assertAllocation(
                result,
                TaxComponent.CPP,
                "417.85");

        assertAllocation(
                result,
                TaxComponent.ISS,
                "332.88");

        assertFalse(
                result.issCapApplied());
    }

    @Test
    void shouldCapIssAndRedistributeExcessOnAnnexIIIFifthBracket() {
        SimplesEstimatedTaxResult estimated = createEstimatedResult(
                new BigDecimal(
                        "900000.00"),
                new BigDecimal(
                        "3000000.00"),
                new BigDecimal(
                        "3000000.00"),
                new BigDecimal(
                        "10000.00"));

        SimplesTaxCompositionResult result = calculator.calculate(
                estimated);

        assertTrue(
                result.issCapApplied());

        TaxComponentAllocation iss = result
                .composition()
                .find(
                        TaxComponent.ISS)
                .orElseThrow();

        assertEquals(
                new BigDecimal(
                        "0.05"),
                iss.effectiveRate());

        assertEquals(
                new BigDecimal(
                        "500.00"),
                iss.amountForDisplay());

        assertTrue(
                result
                        .composition()
                        .isFullyAllocated());
    }

    @Test
    void shouldRejectSixthBracketUntilSublimitsAreImplemented() {
        SimplesEstimatedTaxResult estimated = createEstimatedResult(
                new BigDecimal(
                        "1200000.00"),
                new BigDecimal(
                        "4000000.00"),
                new BigDecimal(
                        "4000000.00"),
                new BigDecimal(
                        "10000.00"));

        assertThrows(
                IllegalStateException.class,
                () -> calculator.calculate(
                        estimated));
    }

    private SimplesEstimatedTaxResult createEstimatedResult(
            BigDecimal fatorRPayrollBase,
            BigDecimal fatorRRevenueBase,
            BigDecimal twelveMonthRevenue,
            BigDecimal currentRevenueAmount) {
        LocalDate openingDate = LocalDate.of(
                2024,
                1,
                10);

        YearMonth assessmentPeriod = YearMonth.of(
                2026,
                1);

        FatorRCalculationResult fatorRResult = new FatorRAutomaticCalculator()
                .calculate(
                        new FatorRCalculationRequest(
                                openingDate,
                                assessmentPeriod,
                                fatorRPayrollBase,
                                fatorRRevenueBase));

        List<MonthlyRevenue> revenues = createTwelveMonthHistory(
                assessmentPeriod,
                twelveMonthRevenue);

        MonthlyRevenue currentRevenue = new MonthlyRevenue(
                assessmentPeriod,
                currentRevenueAmount);

        List<MonthlyRevenue> calculationRevenues = new ArrayList<>(
                revenues);

        calculationRevenues.add(
                currentRevenue);

        TaxBracketRevenueBasisResult revenueBasisResult = new TaxBracketRevenueBasisCalculator()
                .calculate(
                        new TaxBracketRevenueBasisRequest(
                                openingDate,
                                assessmentPeriod,
                                calculationRevenues));

        SimplesTaxBracketSelectionResult bracketSelection = new SimplesTaxBracketSelector()
                .select(
                        new SimplesTaxBracketSelectionRequest(
                                assessmentPeriod,
                                fatorRResult,
                                revenueBasisResult));

        SimplesEffectiveRateResult effectiveRate = new SimplesEffectiveRateCalculator()
                .calculate(
                        bracketSelection);

        return new SimplesEstimatedTaxCalculator()
                .calculate(
                        new SimplesEstimatedTaxRequest(
                                currentRevenue,
                                effectiveRate));
    }

    private List<MonthlyRevenue> createTwelveMonthHistory(
            YearMonth assessmentPeriod,
            BigDecimal totalRevenue) {
        BigDecimal monthlyAmount = totalRevenue.divide(
                BigDecimal.valueOf(
                        12),
                2,
                java.math.RoundingMode.DOWN);

        List<MonthlyRevenue> revenues = new ArrayList<>();

        BigDecimal accumulated = BigDecimal.ZERO;

        for (int index = 12; index >= 2; index--) {
            YearMonth period = assessmentPeriod.minusMonths(
                    index);

            revenues.add(
                    new MonthlyRevenue(
                            period,
                            monthlyAmount));

            accumulated = accumulated.add(
                    monthlyAmount);
        }

        BigDecimal lastAmount = totalRevenue.subtract(
                accumulated);

        revenues.add(
                new MonthlyRevenue(
                        assessmentPeriod.minusMonths(
                                1),
                        lastAmount));

        return List.copyOf(
                revenues);
    }

    private void assertAllocation(
            SimplesTaxCompositionResult result,
            TaxComponent component,
            String expectedAmount) {
        TaxComponentAllocation allocation = result
                .composition()
                .find(
                        component)
                .orElseThrow();

        assertEquals(
                new BigDecimal(
                        expectedAmount),
                allocation.amountForDisplay());
    }
}