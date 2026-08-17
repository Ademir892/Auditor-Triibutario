package br.com.auditortributario.taxrule.simples;

import java.time.YearMonth;
import java.util.Objects;

public record SimplesTaxBracketSelectionRequest(
                YearMonth assessmentPeriod,
                FatorRCalculationResult fatorRResult,
                TaxBracketRevenueBasisResult revenueBasisResult) {

        public SimplesTaxBracketSelectionRequest {
                Objects.requireNonNull(
                                assessmentPeriod,
                                "O período de apuração não pode ser nulo.");

                Objects.requireNonNull(
                                fatorRResult,
                                "O resultado do Fator R não pode ser nulo.");

                Objects.requireNonNull(
                                revenueBasisResult,
                                "O resultado da base de receita não pode ser nulo.");
        }
}