
package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.TaxDecision;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesSublimitAdjustedRevenueTaxResultTest {

        @Test
        void shouldCreateFinalResultWithAdjustedComponents() {
                SimplesSublimitAdjustedRevenueTaxResult result = new SimplesSublimitAdjustedRevenueTaxResult(
                                originalResult(),
                                SimplesSublimitTaxTreatment.IN_DAS_STANDARD,
                                List.of(
                                                component(
                                                                TaxComponent.ICMS,
                                                                "0.0100",
                                                                "0.0100",
                                                                "100.00",
                                                                "100.00")),
                                Optional.of(
                                                new BigDecimal(
                                                                "900.00")),
                                true,
                                false,
                                decision());

                assertTrue(
                                result.isFinal());

                assertFalse(
                                result.hasExternalObligation());

                assertEquals(
                                new BigDecimal(
                                                "900.00"),
                                result.finalTaxAmount()
                                                .orElseThrow());

                assertEquals(
                                1,
                                result.adjustedComponents()
                                                .size());
        }

        @Test
        void shouldRejectFinalResultWithoutFinalTaxAmount() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> new SimplesSublimitAdjustedRevenueTaxResult(
                                                originalResult(),
                                                SimplesSublimitTaxTreatment.IN_DAS_STANDARD,
                                                List.of(),
                                                Optional.empty(),
                                                true,
                                                false,
                                                decision()));
        }

        @Test
        void shouldRejectOutsideDasWithoutExternalObligation() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> new SimplesSublimitAdjustedRevenueTaxResult(
                                                originalResult(),
                                                SimplesSublimitTaxTreatment.OUTSIDE_DAS,
                                                List.of(),
                                                Optional.of(
                                                                new BigDecimal(
                                                                                "800.00")),
                                                true,
                                                false,
                                                decision()));
        }

        @Test
        void shouldAllowOutsideDasWithExternalObligation() {
                SimplesSublimitAdjustedRevenueTaxResult result = new SimplesSublimitAdjustedRevenueTaxResult(
                                originalResult(),
                                SimplesSublimitTaxTreatment.OUTSIDE_DAS,
                                List.of(),
                                Optional.of(
                                                new BigDecimal(
                                                                "800.00")),
                                true,
                                true,
                                decision());

                assertTrue(
                                result.hasExternalObligation());
        }

        private SimplesRevenueTaxProcessingResult originalResult() {
                return new SimplesRevenueTaxProcessingResult() {

                        @Override
                        public SimplesRevenueClassificationResult classification() {
                                return null;
                        }

                        @Override
                        public Optional<BigDecimal> finalTaxAmount() {
                                return Optional.of(
                                                new BigDecimal(
                                                                "1000.00"));
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
                                return List.of();
                        }

                        @Override
                        public TaxDecision decision() {
                                return SimplesSublimitAdjustedRevenueTaxResultTest.this.decision();
                        }
                };
        }

        private SimplesAdjustedTaxComponent component(
                        TaxComponent taxComponent,
                        String originalEffectiveRate,
                        String adjustedEffectiveRate,
                        String originalAmount,
                        String adjustedAmount) {

                return new SimplesAdjustedTaxComponent(
                                taxComponent,
                                new BigDecimal(
                                                originalEffectiveRate),
                                new BigDecimal(
                                                adjustedEffectiveRate),
                                new BigDecimal(
                                                originalAmount),
                                new BigDecimal(
                                                adjustedAmount),
                                List.of());
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
