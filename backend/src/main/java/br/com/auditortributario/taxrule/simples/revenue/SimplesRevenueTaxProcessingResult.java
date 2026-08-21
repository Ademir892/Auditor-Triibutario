package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SimplesRevenueTaxProcessingResult {

    SimplesRevenueClassificationResult classification();

    Optional<BigDecimal> finalTaxAmount();

    boolean isFinal();

    boolean hasExternalObligation();

    List<SimplesAdjustedTaxComponent> adjustedComponents();

    TaxDecision decision();

    default RevenueEntry revenue() {
        return classification().revenue();
    }
}