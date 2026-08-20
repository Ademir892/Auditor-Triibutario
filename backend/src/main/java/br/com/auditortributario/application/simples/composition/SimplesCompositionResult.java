package br.com.auditortributario.application.simples.composition;

import br.com.auditortributario.taxrule.simples.SimplesEstimatedTaxResult;
import br.com.auditortributario.taxrule.simples.composition.SimplesTaxCompositionResult;

import java.util.Objects;

public record SimplesCompositionResult(
                SimplesEstimatedTaxResult estimatedTaxResult,
                SimplesTaxCompositionResult compositionResult) {

        public SimplesCompositionResult {
                Objects.requireNonNull(
                                estimatedTaxResult,
                                "O resultado estimado não pode ser nulo.");

                Objects.requireNonNull(
                                compositionResult,
                                "O resultado da composição não pode ser nulo.");
        }
}