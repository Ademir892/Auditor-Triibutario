package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxCompositionResult;
import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record SimplesRevenueTaxCalculationResult(
        RevenueEntry revenue,
        SimplesRevenueTaxRoute route,
        BigDecimal revenueBasis,
        SimplesRevenueTaxBracket bracket,
        BigDecimal effectiveRate,
        BigDecimal rawTaxAmount,
        BigDecimal taxAmount,
        TaxCompositionResult composition,
        String taxTableVersion,
        String distributionTableVersion,
        TaxDecision decision) {

    public SimplesRevenueTaxCalculationResult {
        Objects.requireNonNull(
                revenue,
                "A receita não pode ser nula.");

        Objects.requireNonNull(
                route,
                "A rota tributária não pode ser nula.");

        Objects.requireNonNull(
                revenueBasis,
                "A base de receita não pode ser nula.");

        Objects.requireNonNull(
                bracket,
                "A faixa tributária não pode ser nula.");

        Objects.requireNonNull(
                effectiveRate,
                "A alíquota efetiva não pode ser nula.");

        Objects.requireNonNull(
                rawTaxAmount,
                "O valor bruto não pode ser nulo.");

        Objects.requireNonNull(
                taxAmount,
                "O valor monetário não pode ser nulo.");

        Objects.requireNonNull(
                composition,
                "A composição tributária não pode ser nula.");

        Objects.requireNonNull(
                taxTableVersion,
                "A versão da tabela tributária não pode ser nula.");

        Objects.requireNonNull(
                distributionTableVersion,
                "A versão da repartição não pode ser nula.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");
    }

    public BigDecimal effectiveRateAsPercentage() {
        return effectiveRate
                .multiply(
                        BigDecimal.valueOf(
                                100))
                .stripTrailingZeros();
    }

    public BigDecimal taxAmountForDisplay() {
        return taxAmount.setScale(
                2,
                RoundingMode.HALF_UP);
    }
}