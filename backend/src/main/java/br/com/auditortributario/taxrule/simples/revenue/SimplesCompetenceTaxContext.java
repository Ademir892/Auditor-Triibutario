package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.simples.FatorRCalculationResult;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisResult;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public record SimplesCompetenceTaxContext(
                BigDecimal revenueBasis,
                Optional<FatorRCalculationResult> fatorRResult,
                Optional<TaxBracketRevenueBasisResult> revenueBasisResult) {

        public SimplesCompetenceTaxContext {
                Objects.requireNonNull(
                                revenueBasis,
                                "A base de receita não pode ser nula.");

                Objects.requireNonNull(
                                fatorRResult,
                                "O resultado opcional do Fator R não pode ser nulo.");

                Objects.requireNonNull(
                                revenueBasisResult,
                                "O resultado opcional da base de enquadramento "
                                                + "não pode ser nulo.");

                if (revenueBasis.compareTo(
                                BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                        "A base de receita não pode ser negativa.");
                }

                if (revenueBasisResult.isPresent()
                                && revenueBasisResult
                                                .orElseThrow()
                                                .revenueBasis()
                                                .compareTo(
                                                                revenueBasis) != 0) {

                        throw new IllegalArgumentException(
                                        "A base de receita informada no contexto "
                                                        + "deve corresponder à base presente "
                                                        + "no resultado de enquadramento.");
                }
        }

        public static SimplesCompetenceTaxContext withoutFatorR(
                        BigDecimal revenueBasis) {
                return new SimplesCompetenceTaxContext(
                                revenueBasis,
                                Optional.empty(),
                                Optional.empty());
        }

        public static SimplesCompetenceTaxContext withFatorR(
                        BigDecimal revenueBasis,
                        FatorRCalculationResult fatorRResult) {
                Objects.requireNonNull(
                                fatorRResult,
                                "O resultado do Fator R não pode ser nulo.");

                return new SimplesCompetenceTaxContext(
                                revenueBasis,
                                Optional.of(
                                                fatorRResult),
                                Optional.empty());
        }

        public static SimplesCompetenceTaxContext withServiceTaxData(
                        TaxBracketRevenueBasisResult revenueBasisResult,
                        FatorRCalculationResult fatorRResult) {
                Objects.requireNonNull(
                                revenueBasisResult,
                                "O resultado da base de enquadramento "
                                                + "não pode ser nulo.");

                Objects.requireNonNull(
                                fatorRResult,
                                "O resultado do Fator R não pode ser nulo.");

                return new SimplesCompetenceTaxContext(
                                revenueBasisResult.revenueBasis(),
                                Optional.of(
                                                fatorRResult),
                                Optional.of(
                                                revenueBasisResult));
        }

        public boolean hasFatorRResult() {
                return fatorRResult.isPresent();
        }

        public boolean hasRevenueBasisResult() {
                return revenueBasisResult.isPresent();
        }

        public boolean hasCompleteServiceTaxData() {
                return fatorRResult.isPresent()
                                && revenueBasisResult.isPresent();
        }
}