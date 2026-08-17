package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record TaxBracketRevenueBasisResult(
        TaxBracketRevenueBasisType basisType,
        BigDecimal revenueBasis,
        List<MonthlyRevenue> revenuesUsed,
        TaxDecision decision) {

    public TaxBracketRevenueBasisResult {
        Objects.requireNonNull(
                basisType,
                "O tipo da base de receita não pode ser nulo.");

        Objects.requireNonNull(
                revenueBasis,
                "A base de receita não pode ser nula.");

        Objects.requireNonNull(
                revenuesUsed,
                "As receitas utilizadas não podem ser nulas.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        revenuesUsed = List.copyOf(
                revenuesUsed);
    }
}