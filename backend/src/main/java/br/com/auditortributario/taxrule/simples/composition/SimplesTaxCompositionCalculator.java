package br.com.auditortributario.taxrule.simples.composition;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.TaxComponentAllocation;
import br.com.auditortributario.taxrule.domain.TaxCompositionResult;
import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.simples.SimplesAnnex;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxResult;
import br.com.auditortributario.taxrule.simples.SimplesTaxBracketSelectionResult;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SimplesTaxCompositionCalculator {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private final SimplesTaxDistributionTableRegistry registry;

    public SimplesTaxCompositionCalculator() {
        this.registry = new SimplesTaxDistributionTableRegistry();
    }

    public SimplesTaxCompositionResult calculate(
            SimplesEstimatedTaxResult estimatedTaxResult) {
        Objects.requireNonNull(
                estimatedTaxResult,
                "O resultado estimado não pode ser nulo.");

        SimplesTaxBracketSelectionResult selection = estimatedTaxResult
                .effectiveRateResult()
                .bracketSelectionResult();

        SimplesAnnex annex = selection
                .fatorRResult()
                .annex();

        int bracketNumber = selection
                .bracket()
                .number();

        YearMonth assessmentPeriod = estimatedTaxResult
                .taxableRevenue()
                .period();

        /*
         * A 6ª faixa possui tratamento relacionado ao sublimite
         * para ISS/ICMS que ainda não está modelado pelo projeto.
         *
         * É mais seguro rejeitar o cálculo do que devolver
         * uma repartição aparentemente completa e incorreta.
         */
        if (bracketNumber == 6) {
            throw new IllegalStateException(
                    "A composição tributária completa da 6ª faixa "
                            + "depende da modelagem de sublimites "
                            + "de ISS/ICMS, ainda não implementada.");
        }

        SimplesTaxDistributionTable table = registry.find(
                annex,
                assessmentPeriod);

        SimplesTaxDistributionRule rule = table.ruleFor(
                bracketNumber);

        BigDecimal totalEffectiveRate = estimatedTaxResult
                .effectiveRateResult()
                .effectiveRate();

        BigDecimal taxableRevenue = estimatedTaxResult
                .taxableRevenue()
                .amount();

        Map<TaxComponent, BigDecimal> componentEffectiveRates = calculateBaseEffectiveRates(
                totalEffectiveRate,
                rule);

        boolean issCapApplied = applyIssCapIfNecessary(
                rule,
                componentEffectiveRates);

        List<TaxComponentAllocation> allocations = createAllocations(
                taxableRevenue,
                totalEffectiveRate,
                rule,
                componentEffectiveRates);

        TaxCompositionResult composition = new TaxCompositionResult(
                estimatedTaxResult.rawTaxAmount(),
                allocations);

        TaxDecision decision = new TaxDecision(
                "SIMPLES_TAX_COMPOSITION",
                table.version(),
                "Repartição do valor estimado do Simples "
                        + "entre os componentes tributários.",
                "Competência="
                        + assessmentPeriod
                        + "; Anexo="
                        + annex.getDisplayName()
                        + "; Faixa="
                        + bracketNumber
                        + "; Alíquota efetiva="
                        + totalEffectiveRate,
                issCapApplied
                        ? "ISS efetivo excedeu o limite de 5% "
                                + "e o excedente foi redistribuído."
                        : "Repartição padrão da faixa aplicada.",
                "Componentes="
                        + allocations.size()
                        + "; Valor total bruto="
                        + composition.totalTaxAmount(),
                "Lei Complementar nº 123/2006, "
                        + "art. 18, § 1º-B, "
                        + "e Anexo "
                        + (annex == SimplesAnnex.ANEXO_III
                                ? "III"
                                : "V")
                        + ", vigência 2018-2026.");

        return new SimplesTaxCompositionResult(
                estimatedTaxResult,
                composition,
                issCapApplied,
                table.version(),
                decision);
    }

    private Map<TaxComponent, BigDecimal> calculateBaseEffectiveRates(
            BigDecimal totalEffectiveRate,
            SimplesTaxDistributionRule rule) {
        Map<TaxComponent, BigDecimal> result = new LinkedHashMap<>();

        for (SimplesTaxDistributionShare share : rule.shares()) {

            result.put(
                    share.component(),
                    totalEffectiveRate.multiply(
                            share.distributionRate()));
        }

        return result;
    }

    private boolean applyIssCapIfNecessary(
            SimplesTaxDistributionRule rule,
            Map<TaxComponent, BigDecimal> componentEffectiveRates) {
        if (rule.issCapRule().isEmpty()) {
            return false;
        }

        BigDecimal issEffectiveRate = componentEffectiveRates.get(
                TaxComponent.ISS);

        if (issEffectiveRate == null) {
            return false;
        }

        SimplesIssCapRule capRule = rule.issCapRule()
                .orElseThrow();

        if (issEffectiveRate.compareTo(
                capRule.maximumEffectiveRate()) <= 0) {
            return false;
        }

        BigDecimal excess = issEffectiveRate.subtract(
                capRule.maximumEffectiveRate());

        componentEffectiveRates.put(
                TaxComponent.ISS,
                capRule.maximumEffectiveRate());

        for (Map.Entry<TaxComponent, BigDecimal> entry : capRule
                .redistributionRates()
                .entrySet()) {

            TaxComponent component = entry.getKey();

            BigDecimal redistributedRate = excess.multiply(
                    entry.getValue());

            BigDecimal currentRate = componentEffectiveRates.getOrDefault(
                    component,
                    BigDecimal.ZERO);

            componentEffectiveRates.put(
                    component,
                    currentRate.add(
                            redistributedRate));
        }

        return true;
    }

    private List<TaxComponentAllocation> createAllocations(
            BigDecimal taxableRevenue,
            BigDecimal totalEffectiveRate,
            SimplesTaxDistributionRule rule,
            Map<TaxComponent, BigDecimal> componentEffectiveRates) {
        List<TaxComponentAllocation> allocations = new ArrayList<>();

        for (SimplesTaxDistributionShare share : rule.shares()) {

            BigDecimal componentEffectiveRate = componentEffectiveRates.get(
                    share.component());

            BigDecimal finalDistributionRate;

            if (totalEffectiveRate.compareTo(
                    BigDecimal.ZERO) == 0) {

                finalDistributionRate = share.distributionRate();

            } else {

                finalDistributionRate = componentEffectiveRate.divide(
                        totalEffectiveRate,
                        MATH_CONTEXT);
            }

            BigDecimal amount = taxableRevenue.multiply(
                    componentEffectiveRate);

            allocations.add(
                    new TaxComponentAllocation(
                            share.component(),
                            finalDistributionRate,
                            componentEffectiveRate,
                            amount));
        }

        return List.copyOf(
                allocations);
    }
}