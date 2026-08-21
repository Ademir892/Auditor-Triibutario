package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntryId;
import br.com.auditortributario.taxrule.simples.FatorRCalculationResult;
import br.com.auditortributario.taxrule.simples.TaxBracketRevenueBasisResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record SimplesCompetenceTaxContext(
                BigDecimal revenueBasis,
                Optional<FatorRCalculationResult> fatorRResult,
                Optional<TaxBracketRevenueBasisResult> revenueBasisResult,
                Map<RevenueEntryId, SimplesServiceTaxRule> serviceTaxRules) {

        public SimplesCompetenceTaxContext(
                        BigDecimal revenueBasis,
                        Optional<FatorRCalculationResult> fatorRResult,
                        Optional<TaxBracketRevenueBasisResult> revenueBasisResult) {
                this(
                                revenueBasis,
                                fatorRResult,
                                revenueBasisResult,
                                Map.of());
        }

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

                Objects.requireNonNull(
                                serviceTaxRules,
                                "As regras tributárias específicas dos serviços "
                                                + "não podem ser nulas.");

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

                serviceTaxRules = Map.copyOf(
                                serviceTaxRules);
        }

        public static SimplesCompetenceTaxContext withoutFatorR(
                        BigDecimal revenueBasis) {
                return new SimplesCompetenceTaxContext(
                                revenueBasis,
                                Optional.empty(),
                                Optional.empty(),
                                Map.of());
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
                                Optional.empty(),
                                Map.of());
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
                                                revenueBasisResult),
                                Map.of());
        }

        public SimplesCompetenceTaxContext withServiceTaxRule(
                        RevenueEntry revenue,
                        SimplesServiceTaxRule serviceTaxRule) {
                Objects.requireNonNull(
                                revenue,
                                "A receita não pode ser nula.");

                Objects.requireNonNull(
                                serviceTaxRule,
                                "A regra tributária do serviço não pode ser nula.");

                Map<RevenueEntryId, SimplesServiceTaxRule> updatedRules = new HashMap<>(
                                serviceTaxRules);

                updatedRules.put(
                                revenue.id(),
                                serviceTaxRule);

                return new SimplesCompetenceTaxContext(
                                revenueBasis,
                                fatorRResult,
                                revenueBasisResult,
                                updatedRules);
        }

        public Optional<SimplesServiceTaxRule> serviceTaxRuleFor(
                        RevenueEntry revenue) {
                Objects.requireNonNull(
                                revenue,
                                "A receita não pode ser nula.");

                return Optional.ofNullable(
                                serviceTaxRules.get(
                                                revenue.id()));
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

        public boolean hasServiceTaxRuleFor(
                        RevenueEntry revenue) {
                return serviceTaxRuleFor(
                                revenue).isPresent();
        }
}