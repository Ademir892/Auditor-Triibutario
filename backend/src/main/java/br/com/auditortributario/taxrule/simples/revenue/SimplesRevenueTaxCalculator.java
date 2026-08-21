package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponentAllocation;
import br.com.auditortributario.taxrule.domain.TaxCompositionResult;
import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class SimplesRevenueTaxCalculator {

        private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

        private static final String TAX_TABLE_VERSION = "LC123-ANEXOS-I-II-IV-2018-2026";

        private final SimplesRevenueTaxBracketSelector bracketSelector;

        private final SimplesRevenueTaxAllocationCalculator allocationCalculator;

        public SimplesRevenueTaxCalculator() {
                this.bracketSelector = new SimplesRevenueTaxBracketSelector();

                this.allocationCalculator = new SimplesRevenueTaxAllocationCalculator();
        }

        public SimplesRevenueTaxCalculationResult calculate(
                        SimplesRevenueClassificationResult classification,
                        BigDecimal revenueBasis) {
                Objects.requireNonNull(
                                classification,
                                "A classificação da receita não pode ser nula.");

                Objects.requireNonNull(
                                revenueBasis,
                                "A base de receita não pode ser nula.");

                if (!classification.isResolved()) {
                        throw new IllegalArgumentException(
                                        "A receita precisa possuir classificação tributária "
                                                        + "resolvida antes do cálculo.");
                }

                SimplesRevenueTaxRoute route = classification
                                .route()
                                .orElseThrow();

                if (!isSupportedRoute(
                                route)) {
                        throw new IllegalArgumentException(
                                        "Este calculador atualmente suporta "
                                                        + "Anexo I, Anexo II e Anexo IV.");
                }

                if (revenueBasis.compareTo(
                                BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                        "A base de receita não pode ser negativa.");
                }

                RevenueEntry revenue = classification.revenue();

                if (revenueBasis.compareTo(
                                BigDecimal.ZERO) == 0
                                && revenue
                                                .amount()
                                                .compareTo(
                                                                BigDecimal.ZERO) > 0) {

                        throw new IllegalArgumentException(
                                        "Uma receita positiva não pode ser calculada "
                                                        + "com base de enquadramento igual a zero.");
                }

                SimplesRevenueTaxBracket bracket = bracketSelector.select(
                                route,
                                revenue.competence(),
                                revenueBasis);

                /*
                 * A sexta faixa ainda fica bloqueada.
                 *
                 * Apesar de as tabelas representarem a legislação,
                 * o projeto ainda não modelou integralmente os
                 * efeitos dos sublimites estaduais e municipais
                 * sobre ICMS e ISS.
                 */
                if (bracket.number() == 6) {
                        throw new IllegalStateException(
                                        "O cálculo completo da 6ª faixa de "
                                                        + route.getDisplayName()
                                                        + " depende da modelagem de sublimites "
                                                        + "de ICMS/ISS, ainda não implementada.");
                }

                BigDecimal effectiveRate = calculateEffectiveRate(
                                revenueBasis,
                                bracket);

                BigDecimal rawTaxAmount = revenue
                                .amount()
                                .multiply(
                                                effectiveRate,
                                                MATH_CONTEXT);

                BigDecimal taxAmount = rawTaxAmount.setScale(
                                2,
                                RoundingMode.HALF_UP);

                List<TaxComponentAllocation> allocations = allocationCalculator.calculate(
                                route,
                                bracket.number(),
                                revenue.competence(),
                                revenue.amount(),
                                effectiveRate);

                TaxCompositionResult composition = new TaxCompositionResult(
                                rawTaxAmount,
                                allocations);

                if (!composition.isFullyAllocated()) {
                        throw new IllegalStateException(
                                        "A repartição tributária não totalizou "
                                                        + "o valor bruto calculado.");
                }

                TaxDecision decision = new TaxDecision(
                                "SIMPLES_REVENUE_TAX_CALCULATION",
                                TAX_TABLE_VERSION,
                                "Cálculo tributário de receita segregada "
                                                + "do Simples Nacional.",
                                "Competência="
                                                + revenue.competence()
                                                + "; Rota="
                                                + route
                                                + "; Base="
                                                + revenueBasis
                                                + "; Receita="
                                                + revenue.amount(),
                                "Faixa="
                                                + bracket.number()
                                                + "; Alíquota nominal="
                                                + bracket.nominalRate()
                                                + "; Parcela a deduzir="
                                                + bracket.deduction(),
                                "Alíquota efetiva="
                                                + effectiveRate
                                                + "; Valor bruto="
                                                + rawTaxAmount
                                                + "; Valor monetário="
                                                + taxAmount,
                                "Lei Complementar nº 123/2006, "
                                                + "Anexo "
                                                + route.getAnnexNumber()
                                                + ", vigência 2018-2026.");

                return new SimplesRevenueTaxCalculationResult(
                                revenue,
                                route,
                                revenueBasis,
                                bracket,
                                effectiveRate,
                                rawTaxAmount,
                                taxAmount,
                                composition,
                                TAX_TABLE_VERSION,
                                allocationCalculator.distributionVersion(),
                                decision);
        }

        private boolean isSupportedRoute(
                        SimplesRevenueTaxRoute route) {
                return route == SimplesRevenueTaxRoute.ANNEX_I
                                || route == SimplesRevenueTaxRoute.ANNEX_II
                                || route == SimplesRevenueTaxRoute.ANNEX_IV;
        }

        private BigDecimal calculateEffectiveRate(
                        BigDecimal revenueBasis,
                        SimplesRevenueTaxBracket bracket) {
                if (revenueBasis.compareTo(
                                BigDecimal.ZERO) == 0) {
                        return BigDecimal.ZERO;
                }

                BigDecimal numerator = revenueBasis
                                .multiply(
                                                bracket.nominalRate(),
                                                MATH_CONTEXT)
                                .subtract(
                                                bracket.deduction(),
                                                MATH_CONTEXT);

                BigDecimal effectiveRate = numerator.divide(
                                revenueBasis,
                                MATH_CONTEXT);

                if (effectiveRate.compareTo(
                                BigDecimal.ZERO) < 0) {
                        throw new IllegalStateException(
                                        "A alíquota efetiva calculada "
                                                        + "não pode ser negativa.");
                }

                return effectiveRate;
        }
}