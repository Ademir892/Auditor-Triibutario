package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.simples.FatorRCalculationResult;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisResult;

@FunctionalInterface
public interface SimplesServiceRevenueTaxEngine {

    SimplesRevenueTaxProcessingResult process(
            RevenueEntry revenue,
            TaxBracketRevenueBasisResult revenueBasisResult,
            FatorRCalculationResult fatorRResult,
            SimplesRevenueClassificationResult classification);
}