package br.com.auditortributario.taxrule.simples.composition;

import br.com.auditortributario.taxrule.domain.TaxCompositionResult;
import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxResult;

import java.util.Objects;

public record SimplesTaxCompositionResult(
        SimplesEstimatedTaxResult estimatedTaxResult,
        TaxCompositionResult composition,
        boolean issCapApplied,
        String tableVersion,
        TaxDecision decision) {

    public SimplesTaxCompositionResult {
        Objects.requireNonNull(
                estimatedTaxResult,
                "O resultado estimado não pode ser nulo.");

        Objects.requireNonNull(
                composition,
                "A composição tributária não pode ser nula.");

        Objects.requireNonNull(
                tableVersion,
                "A versão da tabela não pode ser nula.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        tableVersion = tableVersion.trim();

        if (tableVersion.isBlank()) {
            throw new IllegalArgumentException(
                    "A versão da tabela não pode estar vazia.");
        }
    }
}