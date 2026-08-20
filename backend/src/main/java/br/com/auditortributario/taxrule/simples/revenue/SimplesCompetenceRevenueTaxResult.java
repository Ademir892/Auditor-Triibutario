package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.CompetenceRevenue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SimplesCompetenceRevenueTaxResult(
        CompetenceRevenue competenceRevenue,
        BigDecimal revenueBasis,
        SimplesCompetenceRevenueProcessingStatus status,
        List<SimplesCompetenceRevenueItemResult> items,
        BigDecimal processedTaxAmount,
        Optional<BigDecimal> finalTaxAmount,
        List<SimplesCompetenceTaxComponentTotal> componentTotals,
        boolean hasExternalObligations,
        TaxDecision decision) {

    public SimplesCompetenceRevenueTaxResult {
        Objects.requireNonNull(
                competenceRevenue,
                "A receita da competência não pode ser nula.");

        Objects.requireNonNull(
                revenueBasis,
                "A base de receita não pode ser nula.");

        Objects.requireNonNull(
                status,
                "O status não pode ser nulo.");

        Objects.requireNonNull(
                items,
                "Os itens processados não podem ser nulos.");

        Objects.requireNonNull(
                processedTaxAmount,
                "O valor tributário processado não pode ser nulo.");

        Objects.requireNonNull(
                finalTaxAmount,
                "O valor final opcional não pode ser nulo.");

        Objects.requireNonNull(
                componentTotals,
                "Os totais por componente não podem ser nulos.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        if (revenueBasis.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A base de receita não pode ser negativa.");
        }

        if (processedTaxAmount.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor tributário processado não pode ser negativo.");
        }

        items = List.copyOf(
                items);

        componentTotals = List.copyOf(
                componentTotals);

        if (items.size() != competenceRevenue.entries().size()) {

            throw new IllegalArgumentException(
                    "Cada receita da competência deve possuir "
                            + "exatamente um resultado de processamento.");
        }

        validateItems(
                competenceRevenue,
                items);

        validateComponents(
                componentTotals);

        validateFinalState(
                status,
                items,
                finalTaxAmount);
    }

    public boolean isFinal() {
        return finalTaxAmount.isPresent();
    }

    public int processedCount() {
        return (int) items
                .stream()
                .filter(
                        SimplesCompetenceRevenueItemResult::isFinal)
                .count();
    }

    public int pendingCount() {
        return items.size()
                - processedCount();
    }

    public BigDecimal processedTaxAmountForDisplay() {
        return processedTaxAmount.setScale(
                2,
                RoundingMode.HALF_UP);
    }

    public Optional<BigDecimal> finalTaxAmountForDisplay() {
        return finalTaxAmount
                .map(
                        amount -> amount.setScale(
                                2,
                                RoundingMode.HALF_UP));
    }

    public Optional<SimplesCompetenceTaxComponentTotal> findComponent(
            TaxComponent component) {
        Objects.requireNonNull(
                component,
                "O componente pesquisado não pode ser nulo.");

        return componentTotals
                .stream()
                .filter(
                        total -> total.component() == component)
                .findFirst();
    }

    private static void validateItems(
            CompetenceRevenue competenceRevenue,
            List<SimplesCompetenceRevenueItemResult> items) {
        for (SimplesCompetenceRevenueItemResult item : items) {

            if (!item
                    .revenue()
                    .competence()
                    .equals(
                            competenceRevenue.competence())) {

                throw new IllegalArgumentException(
                        "Todos os resultados devem pertencer "
                                + "à competência consolidada.");
            }
        }
    }

    private static void validateComponents(
            List<SimplesCompetenceTaxComponentTotal> componentTotals) {
        long distinctComponents = componentTotals
                .stream()
                .map(
                        SimplesCompetenceTaxComponentTotal::component)
                .distinct()
                .count();

        if (distinctComponents != componentTotals.size()) {

            throw new IllegalArgumentException(
                    "A consolidação não pode possuir "
                            + "componentes tributários duplicados.");
        }
    }

    private static void validateFinalState(
            SimplesCompetenceRevenueProcessingStatus status,
            List<SimplesCompetenceRevenueItemResult> items,
            Optional<BigDecimal> finalTaxAmount) {
        boolean allFinal = items
                .stream()
                .allMatch(
                        SimplesCompetenceRevenueItemResult::isFinal);

        if (status == SimplesCompetenceRevenueProcessingStatus.COMPLETED
                && !allFinal) {

            throw new IllegalArgumentException(
                    "Uma competência concluída "
                            + "não pode possuir receitas pendentes.");
        }

        if (status == SimplesCompetenceRevenueProcessingStatus.EMPTY
                && !items.isEmpty()) {

            throw new IllegalArgumentException(
                    "Uma competência vazia não pode possuir itens.");
        }

        if (allFinal
                && finalTaxAmount.isEmpty()) {

            throw new IllegalArgumentException(
                    "Uma competência totalmente processada "
                            + "deve possuir valor tributário final.");
        }

        if (!allFinal
                && finalTaxAmount.isPresent()) {

            throw new IllegalArgumentException(
                    "Uma competência com receitas pendentes "
                            + "não pode declarar valor tributário final.");
        }
    }
}