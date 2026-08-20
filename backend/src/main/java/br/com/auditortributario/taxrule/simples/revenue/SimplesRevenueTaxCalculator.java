package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponentAllocation;
import br.com.auditortributario.taxrule.domain.TaxCompositionResult;
import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SimplesRevenueTaxCalculator {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private static final String TAX_TABLE_VERSION = "LC123-ANEXOS-I-II-2018-2026";

    private final SimplesRevenueTaxBracketSelector bracketSelector;

    private final SimplesRevenueTaxDistributionRegistry distributionRegistry;

    public SimplesRevenueTaxCalculator() {
        this.bracketSelector = new SimplesRevenueTaxBracketSelector();

        this.distributionRegistry = new SimplesRevenueTaxDistributionRegistry();
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

        if (route != SimplesRevenueTaxRoute.ANNEX_I
                && route != SimplesRevenueTaxRoute.ANNEX_II) {

            throw new IllegalArgumentException(
                    "Este calculador atualmente suporta apenas "
                            + "Anexo I e Anexo II.");
        }

        if (revenueBasis.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A base de receita não pode ser negativa.");
        }

        RevenueEntry revenue = classification.revenue();

        if (revenueBasis.compareTo(
                BigDecimal.ZERO) == 0
                && revenue.amount().compareTo(
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
         * A sexta faixa depende da correta modelagem dos sublimites
         * estaduais e dos efeitos sobre ICMS.
         *
         * A tabela existe para representar a legislação, mas o motor
         * não produzirá uma composição aparentemente definitiva
         * enquanto essa parte ainda não estiver implementada.
         */
        if (bracket.number() == 6) {
            throw new IllegalStateException(
                    "O cálculo completo da 6ª faixa de "
                            + route.getDisplayName()
                            + " depende da modelagem de sublimites "
                            + "de ICMS, ainda não implementada.");
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

        SimplesRevenueTaxDistributionRule distributionRule = distributionRegistry.find(
                route,
                bracket.number(),
                revenue.competence());

        List<TaxComponentAllocation> allocations = createAllocations(
                revenue.amount(),
                effectiveRate,
                distributionRule);

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
                distributionRegistry.version(),
                decision);
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
                    "A alíquota efetiva calculada não pode ser negativa.");
        }

        return effectiveRate;
    }

    private List<TaxComponentAllocation> createAllocations(
            BigDecimal revenueAmount,
            BigDecimal totalEffectiveRate,
            SimplesRevenueTaxDistributionRule rule) {
        List<TaxComponentAllocation> allocations = new ArrayList<>();

        for (SimplesRevenueTaxDistributionRule.ComponentShare share : rule.shares()) {

            BigDecimal componentEffectiveRate = totalEffectiveRate.multiply(
                    share.distributionRate());

            BigDecimal componentAmount = revenueAmount.multiply(
                    componentEffectiveRate);

            allocations.add(
                    new TaxComponentAllocation(
                            share.component(),
                            share.distributionRate(),
                            componentEffectiveRate,
                            componentAmount));
        }

        return List.copyOf(
                allocations);
    }
}