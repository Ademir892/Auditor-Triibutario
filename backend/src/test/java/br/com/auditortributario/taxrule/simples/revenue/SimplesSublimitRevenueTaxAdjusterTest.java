package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.RevenueActivityType;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueOrigin;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesSublimitRevenueTaxAdjusterTest {

    private static final YearMonth COMPETENCE = YearMonth.of(2026, 8);

    private final SimplesSublimitRevenueTaxAdjuster adjuster = new SimplesSublimitRevenueTaxAdjuster();

    @Test
    void shouldKeepAllComponentsInDasUnderStandardTreatment() {
        SimplesSublimitAdjustedRevenueTaxResult result = adjuster.adjust(
                originalResult(),
                SimplesSublimitTaxContext.standard());

        assertEquals(new BigDecimal("4000.00"), result.finalTaxAmount().orElseThrow());
        assertEquals(new BigDecimal("3000.00"), component(result, TaxComponent.ICMS).adjustedAmount());
        assertFalse(result.hasExternalObligation());
    }

    @Test
    void shouldReplaceOnlyExcessIcmsWithTransitionalCalculation() {
        SimplesSublimitEvaluationResult evaluation = new SimplesSublimitEvaluator().evaluate(
                new BigDecimal("3700000.00"), new BigDecimal("3600000.00"));
        SimplesSublimitMonthlyExcessResult excess = new SimplesSublimitMonthlyExcessCalculator().calculate(
                evaluation, new BigDecimal("200000.00"));
        SimplesSublimitTemporalEffectResult temporal = new SimplesSublimitTemporalEffectCalculator().calculate(
                evaluation, LocalDate.of(2020, 1, 1), COMPETENCE);
        SimplesIcmsSublimitEffectResult effect = new SimplesIcmsSublimitEffectCalculator().calculate(
                temporal, COMPETENCE);
        SimplesIcmsTransitionalCalculationResult calculation = new SimplesIcmsTransitionalCalculator().calculate(
                SimplesRevenueTaxRoute.ANNEX_I,
                new BigDecimal("3600000.00"),
                effect,
                excess,
                new BigDecimal("120000.00"));

        SimplesSublimitAdjustedRevenueTaxResult result = adjuster.adjust(
                originalResult(),
                SimplesSublimitTaxContext.transitional(evaluation),
                Optional.of(calculation),
                Optional.empty());

        SimplesAdjustedTaxComponent icms = component(result, TaxComponent.ICMS);
        assertEquals(0, new BigDecimal("3886.875").compareTo(icms.adjustedAmount()));
        assertEquals(0, new BigDecimal("4886.875").compareTo(result.finalTaxAmount().orElseThrow()));
        assertEquals(1, icms.appliedTreatments().size());
        assertFalse(result.hasExternalObligation());
    }

    @Test
    void shouldExcludeIcmsFromDasAndExposeExternalObligation() {
        SimplesSublimitAdjustedRevenueTaxResult result = adjuster.adjust(
                originalResult(),
                SimplesSublimitTaxContext.outsideDas());

        SimplesAdjustedTaxComponent icms = component(result, TaxComponent.ICMS);
        assertEquals(0, BigDecimal.ZERO.compareTo(icms.adjustedAmount()));
        assertEquals(new BigDecimal("1000.00"), result.finalTaxAmount().orElseThrow());
        assertTrue(result.hasExternalObligation());
    }

    @Test
    void shouldRejectTransitionalTreatmentWithoutTransitionalCalculation() {
        SimplesSublimitEvaluationResult evaluation = new SimplesSublimitEvaluator().evaluate(
                new BigDecimal("3700000.00"), new BigDecimal("3600000.00"));

        assertThrows(IllegalArgumentException.class, () -> adjuster.adjust(
                originalResult(),
                SimplesSublimitTaxContext.transitional(evaluation)));
    }

    private SimplesRevenueTaxProcessingResult originalResult() {
        RevenueEntry revenue = RevenueEntry.standard(
                COMPETENCE,
                new BigDecimal("120000.00"),
                RevenueActivityType.COMMERCE,
                false,
                RevenueOrigin.MANUAL_ENTRY,
                "Receita de comércio");

        SimplesRevenueClassificationResult classification = new SimplesRevenueClassificationResult(
                revenue,
                SimplesRevenueClassificationStatus.RESOLVED,
                Optional.of(SimplesRevenueTaxRoute.ANNEX_I),
                "Receita comercial classificada no Anexo I.",
                decision());

        return new SimplesRevenueTaxProcessingResult() {
            @Override
            public SimplesRevenueClassificationResult classification() {
                return classification;
            }

            @Override
            public Optional<BigDecimal> finalTaxAmount() {
                return Optional.of(new BigDecimal("4000.00"));
            }

            @Override
            public boolean isFinal() {
                return true;
            }

            @Override
            public boolean hasExternalObligation() {
                return false;
            }

            @Override
            public List<SimplesAdjustedTaxComponent> adjustedComponents() {
                return List.of(
                        component(TaxComponent.IRPJ, "1000.00"),
                        component(TaxComponent.ICMS, "3000.00"));
            }

            @Override
            public TaxDecision decision() {
                return SimplesSublimitRevenueTaxAdjusterTest.this.decision();
            }
        };
    }

    private SimplesAdjustedTaxComponent component(TaxComponent taxComponent, String amount) {
        BigDecimal value = new BigDecimal(amount);
        return new SimplesAdjustedTaxComponent(
                taxComponent,
                value.divide(new BigDecimal("120000.00"), MathContext.DECIMAL128),
                value.divide(new BigDecimal("120000.00"), MathContext.DECIMAL128),
                value,
                value,
                List.of());
    }

    private SimplesAdjustedTaxComponent component(
            SimplesSublimitAdjustedRevenueTaxResult result,
            TaxComponent taxComponent) {
        return result.adjustedComponents().stream()
                .filter(component -> component.component() == taxComponent)
                .findFirst()
                .orElseThrow();
    }

    private TaxDecision decision() {
        return new TaxDecision(
                "TEST",
                "TEST",
                "Decisão de teste.",
                "Entrada de teste.",
                "Condição de teste.",
                "Resultado de teste.",
                "Referência legal de teste.");
    }
}
