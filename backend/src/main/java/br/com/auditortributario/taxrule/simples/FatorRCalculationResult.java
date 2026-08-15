package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.util.Objects;

public record FatorRCalculationResult(
        BigDecimal fs12,
        BigDecimal rbt12,
        BigDecimal rawFactor,
        FatorR fatorR,
        SimplesAnnex annex,
        TaxDecision decision) {

    public FatorRCalculationResult {
        Objects.requireNonNull(fs12, "FS12 não pode ser nulo.");
        Objects.requireNonNull(rbt12, "RBT12 não pode ser nulo.");
        Objects.requireNonNull(rawFactor, "O Fator R bruto não pode ser nulo.");
        Objects.requireNonNull(fatorR, "O Fator R não pode ser nulo.");
        Objects.requireNonNull(annex, "O anexo não pode ser nulo.");
        Objects.requireNonNull(decision, "A decisão tributária não pode ser nula.");
    }
}