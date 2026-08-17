package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.time.YearMonth;
import java.util.Objects;

public record SimplesTaxBracketSelectionResult(
        YearMonth assessmentPeriod,
        FatorRCalculationResult fatorRResult,
        TaxBracketRevenueBasisResult revenueBasisResult,
        SimplesTaxTable taxTable,
        SimplesTaxBracket bracket,
        TaxDecision decision) {

    public SimplesTaxBracketSelectionResult {
        Objects.requireNonNull(
                assessmentPeriod,
                "O período de apuração não pode ser nulo.");

        Objects.requireNonNull(
                fatorRResult,
                "O resultado do Fator R não pode ser nulo.");

        Objects.requireNonNull(
                revenueBasisResult,
                "O resultado da base de receita não pode ser nulo.");

        Objects.requireNonNull(
                taxTable,
                "A tabela tributária não pode ser nula.");

        Objects.requireNonNull(
                bracket,
                "A faixa tributária não pode ser nula.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");
    }
}