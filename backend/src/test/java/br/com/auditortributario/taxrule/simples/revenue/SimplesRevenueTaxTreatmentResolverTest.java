package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.revenue.RevenueActivityType;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueOrigin;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxComponentTreatment;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxTreatment;
import br.com.auditortributario.taxrule.domain.revenue.RevenueTaxTreatmentEffect;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplesRevenueTaxTreatmentResolverTest {

        private final SimplesRevenueTaxTreatmentResolver resolver = new SimplesRevenueTaxTreatmentResolver();

        @Test
        void shouldResolveMonophasicTreatmentForPisAndCofins() {
                RevenueEntry revenue = revenue(
                                RevenueTaxTreatment.MONOPHASIC);

                List<RevenueTaxComponentTreatment> result = resolver.resolve(
                                revenue);

                assertEquals(
                                2,
                                result.size());

                assertTrue(
                                contains(
                                                result,
                                                TaxComponent.PIS_PASEP,
                                                RevenueTaxTreatmentEffect.EXCLUDE_COMPONENT));

                assertTrue(
                                contains(
                                                result,
                                                TaxComponent.COFINS,
                                                RevenueTaxTreatmentEffect.EXCLUDE_COMPONENT));
        }

        @Test
        void shouldResolveLegacyTaxSubstitutionWithoutGuessingPosition() {
                RevenueEntry revenue = revenue(
                                RevenueTaxTreatment.TAX_SUBSTITUTION);

                List<RevenueTaxComponentTreatment> result = resolver.resolve(
                                revenue);

                assertEquals(
                                1,
                                result.size());

                assertTrue(
                                contains(
                                                result,
                                                TaxComponent.ICMS,
                                                RevenueTaxTreatmentEffect.REQUIRE_SPECIAL_CALCULATION));
        }

        @Test
        void shouldExcludeIcmsForSubstitutedTaxpayer() {
                RevenueEntry revenue = revenue(
                                RevenueTaxTreatment.ICMS_ST_SUBSTITUTED);

                List<RevenueTaxComponentTreatment> result = resolver.resolve(
                                revenue);

                assertEquals(
                                1,
                                result.size());

                assertTrue(
                                contains(
                                                result,
                                                TaxComponent.ICMS,
                                                RevenueTaxTreatmentEffect.EXCLUDE_COMPONENT));
        }

        @Test
        void shouldFlagExternalCalculationForSubstituteTaxpayer() {
                RevenueEntry revenue = revenue(
                                RevenueTaxTreatment.ICMS_ST_SUBSTITUTE);

                List<RevenueTaxComponentTreatment> result = resolver.resolve(
                                revenue);

                assertEquals(
                                1,
                                result.size());

                assertTrue(
                                contains(
                                                result,
                                                TaxComponent.ICMS,
                                                RevenueTaxTreatmentEffect.REQUIRE_EXTERNAL_CALCULATION));
        }

        @Test
        void shouldResolveWithheldIss() {
                RevenueEntry revenue = revenue(
                                RevenueTaxTreatment.ISS_WITHHELD);

                List<RevenueTaxComponentTreatment> result = resolver.resolve(
                                revenue);

                assertEquals(
                                1,
                                result.size());

                assertTrue(
                                contains(
                                                result,
                                                TaxComponent.ISS,
                                                RevenueTaxTreatmentEffect.WITHHOLD_COMPONENT));
        }

        @Test
        void shouldResolveIcmsAnticipationWithClosure() {
                RevenueEntry revenue = revenue(
                                RevenueTaxTreatment.ICMS_ANTICIPATION_WITH_CLOSURE);

                List<RevenueTaxComponentTreatment> result = resolver.resolve(
                                revenue);

                assertEquals(
                                1,
                                result.size());

                assertTrue(
                                contains(
                                                result,
                                                TaxComponent.ICMS,
                                                RevenueTaxTreatmentEffect.EXCLUDE_COMPONENT));
        }

        @Test
        void shouldResolveMultipleTreatmentsOnSameRevenue() {
                RevenueEntry revenue = RevenueEntry.create(
                                YearMonth.of(
                                                2026,
                                                8),
                                new BigDecimal(
                                                "20000.00"),
                                RevenueActivityType.COMMERCE,
                                false,
                                Set.of(
                                                RevenueTaxTreatment.MONOPHASIC,
                                                RevenueTaxTreatment.ICMS_ST_SUBSTITUTED),
                                RevenueOrigin.MANUAL_ENTRY,
                                "Receita com múltiplos tratamentos");

                List<RevenueTaxComponentTreatment> result = resolver.resolve(
                                revenue);

                assertEquals(
                                3,
                                result.size());

                assertTrue(
                                contains(
                                                result,
                                                TaxComponent.PIS_PASEP,
                                                RevenueTaxTreatmentEffect.EXCLUDE_COMPONENT));

                assertTrue(
                                contains(
                                                result,
                                                TaxComponent.COFINS,
                                                RevenueTaxTreatmentEffect.EXCLUDE_COMPONENT));

                assertTrue(
                                contains(
                                                result,
                                                TaxComponent.ICMS,
                                                RevenueTaxTreatmentEffect.EXCLUDE_COMPONENT));
        }

        @Test
        void shouldReturnEmptyListForStandardRevenue() {
                RevenueEntry revenue = RevenueEntry.standard(
                                YearMonth.of(
                                                2026,
                                                8),
                                new BigDecimal(
                                                "10000.00"),
                                RevenueActivityType.COMMERCE,
                                false,
                                RevenueOrigin.MANUAL_ENTRY,
                                "Receita normal");

                assertTrue(
                                resolver
                                                .resolve(
                                                                revenue)
                                                .isEmpty());
        }

        private RevenueEntry revenue(
                        RevenueTaxTreatment treatment) {
                return RevenueEntry.create(
                                YearMonth.of(
                                                2026,
                                                8),
                                new BigDecimal(
                                                "10000.00"),
                                RevenueActivityType.COMMERCE,
                                false,
                                Set.of(
                                                treatment),
                                RevenueOrigin.MANUAL_ENTRY,
                                "Receita para teste");
        }

        private boolean contains(
                        List<RevenueTaxComponentTreatment> treatments,
                        TaxComponent component,
                        RevenueTaxTreatmentEffect effect) {
                return treatments
                                .stream()
                                .anyMatch(
                                                treatment -> treatment.component() == component
                                                                && treatment.effect() == effect);
        }
}