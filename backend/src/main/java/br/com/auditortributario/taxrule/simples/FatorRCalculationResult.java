package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.util.Objects;

public record FatorRCalculationResult(
        BigDecimal payrollBase,
        BigDecimal revenueBase,
        BigDecimal rawFactor,
        FatorR fatorR,
        SimplesAnnex annex,
        FatorRCalculationBasis calculationBasis,
        TaxDecision decision) {

    public FatorRCalculationResult {
        Objects.requireNonNull(
                payrollBase,
                "A base de folha não pode ser nula.");

        Objects.requireNonNull(
                revenueBase,
                "A base de receita não pode ser nula.");

        Objects.requireNonNull(
                rawFactor,
                "O Fator R bruto não pode ser nulo.");

        Objects.requireNonNull(
                fatorR,
                "O Fator R não pode ser nulo.");

        Objects.requireNonNull(
                annex,
                "O anexo não pode ser nulo.");

        Objects.requireNonNull(
                calculationBasis,
                "A base de cálculo do Fator R não pode ser nula.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");
    }
}