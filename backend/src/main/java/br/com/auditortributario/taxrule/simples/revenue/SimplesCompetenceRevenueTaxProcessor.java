package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxComponent;
import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.CompetenceRevenue;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SimplesCompetenceRevenueTaxProcessor {

    private static final String RULE_VERSION = "SIMPLES-COMPETENCE-REVENUE-2018-2026";

    private final SimplesRevenueClassifier classifier;

    private final SimplesGoodsRevenueTaxProcessor goodsProcessor;

    public SimplesCompetenceRevenueTaxProcessor() {
        this.classifier = new SimplesRevenueClassifier();

        this.goodsProcessor = new SimplesGoodsRevenueTaxProcessor();
    }

    public SimplesCompetenceRevenueTaxResult process(
            CompetenceRevenue competenceRevenue,
            BigDecimal revenueBasis) {
        Objects.requireNonNull(
                competenceRevenue,
                "A receita da competência não pode ser nula.");

        Objects.requireNonNull(
                revenueBasis,
                "A base de receita não pode ser nula.");

        validateRevenueBasis(
                competenceRevenue,
                revenueBasis);

        List<SimplesCompetenceRevenueItemResult> items = new ArrayList<>();

        for (RevenueEntry revenue : competenceRevenue.entries()) {

            items.add(
                    processRevenue(
                            revenue,
                            revenueBasis));
        }

        List<SimplesCompetenceRevenueItemResult> immutableItems = List.copyOf(
                items);

        BigDecimal processedTaxAmount = calculateProcessedTaxAmount(
                immutableItems);

        List<SimplesCompetenceTaxComponentTotal> componentTotals = consolidateComponents(
                immutableItems);

        boolean allFinal = immutableItems
                .stream()
                .allMatch(
                        SimplesCompetenceRevenueItemResult::isFinal);

        boolean anyFinal = immutableItems
                .stream()
                .anyMatch(
                        SimplesCompetenceRevenueItemResult::isFinal);

        Optional<BigDecimal> finalTaxAmount = allFinal
                ? Optional.of(
                        processedTaxAmount)
                : Optional.empty();

        boolean hasExternalObligations = immutableItems
                .stream()
                .anyMatch(
                        SimplesCompetenceRevenueItemResult::hasExternalObligation);

        SimplesCompetenceRevenueProcessingStatus status = determineStatus(
                immutableItems,
                allFinal,
                anyFinal);

        TaxDecision decision = createDecision(
                competenceRevenue,
                revenueBasis,
                immutableItems,
                processedTaxAmount,
                finalTaxAmount,
                status,
                hasExternalObligations);

        return new SimplesCompetenceRevenueTaxResult(
                competenceRevenue,
                revenueBasis,
                status,
                immutableItems,
                processedTaxAmount,
                finalTaxAmount,
                componentTotals,
                hasExternalObligations,
                decision);
    }

    private SimplesCompetenceRevenueItemResult processRevenue(
            RevenueEntry revenue,
            BigDecimal revenueBasis) {
        SimplesRevenueClassificationResult classification = classifier.classify(
                revenue);

        if (!classification.isResolved()) {
            return new SimplesCompetenceRevenueItemResult(
                    revenue,
                    classification,
                    SimplesCompetenceRevenueItemStatus.REQUIRES_CLASSIFICATION,
                    Optional.empty(),
                    classification.explanation());
        }

        SimplesRevenueTaxRoute route = classification
                .route()
                .orElseThrow();

        if (!isGoodsRoute(
                route)) {
            return new SimplesCompetenceRevenueItemResult(
                    revenue,
                    classification,
                    SimplesCompetenceRevenueItemStatus.UNSUPPORTED_ROUTE,
                    Optional.empty(),
                    "A rota "
                            + route.getDisplayName()
                            + " já foi identificada, "
                            + "mas ainda não possui processamento "
                            + "integrado neste consolidado.");
        }

        SimplesGoodsRevenueTaxResult taxResult = goodsProcessor.process(
                revenue,
                revenueBasis);

        if (!taxResult.isFinal()) {
            return new SimplesCompetenceRevenueItemResult(
                    revenue,
                    classification,
                    SimplesCompetenceRevenueItemStatus.REQUIRES_ADDITIONAL_RULES,
                    Optional.of(
                            taxResult),
                    "A receita foi classificada e calculada parcialmente, "
                            + "mas depende de regras tributárias adicionais "
                            + "antes da definição do valor final.");
        }

        if (taxResult.hasExternalObligation()) {
            return new SimplesCompetenceRevenueItemResult(
                    revenue,
                    classification,
                    SimplesCompetenceRevenueItemStatus.COMPLETED_WITH_EXTERNAL_OBLIGATION,
                    Optional.of(
                            taxResult),
                    "A receita foi processada, mas possui "
                            + "obrigação tributária externa ao Simples Nacional.");
        }

        return new SimplesCompetenceRevenueItemResult(
                revenue,
                classification,
                SimplesCompetenceRevenueItemStatus.COMPLETED,
                Optional.of(
                        taxResult),
                "A receita foi processada integralmente.");
    }

    private boolean isGoodsRoute(
            SimplesRevenueTaxRoute route) {
        return route == SimplesRevenueTaxRoute.ANNEX_I
                || route == SimplesRevenueTaxRoute.ANNEX_II;
    }

    private BigDecimal calculateProcessedTaxAmount(
            List<SimplesCompetenceRevenueItemResult> items) {
        return items
                .stream()
                .filter(
                        SimplesCompetenceRevenueItemResult::isFinal)
                .map(
                        item -> item
                                .taxResult()
                                .orElseThrow()
                                .finalTaxAmount()
                                .orElseThrow())
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);
    }

    private List<SimplesCompetenceTaxComponentTotal> consolidateComponents(
            List<SimplesCompetenceRevenueItemResult> items) {
        Map<TaxComponent, BigDecimal> totals = new EnumMap<>(
                TaxComponent.class);

        for (SimplesCompetenceRevenueItemResult item : items) {

            if (!item.isFinal()) {
                continue;
            }

            SimplesGoodsRevenueTaxResult taxResult = item
                    .taxResult()
                    .orElseThrow();

            for (SimplesAdjustedTaxComponent component : taxResult
                    .adjustment()
                    .components()) {

                totals.merge(
                        component.component(),
                        component.adjustedAmount(),
                        BigDecimal::add);
            }
        }

        return totals
                .entrySet()
                .stream()
                .map(
                        entry -> new SimplesCompetenceTaxComponentTotal(
                                entry.getKey(),
                                entry.getValue()))
                .toList();
    }

    private SimplesCompetenceRevenueProcessingStatus determineStatus(
            List<SimplesCompetenceRevenueItemResult> items,
            boolean allFinal,
            boolean anyFinal) {
        if (items.isEmpty()) {
            return SimplesCompetenceRevenueProcessingStatus.EMPTY;
        }

        if (allFinal) {
            return SimplesCompetenceRevenueProcessingStatus.COMPLETED;
        }

        if (anyFinal) {
            return SimplesCompetenceRevenueProcessingStatus.PARTIALLY_PROCESSED;
        }

        return SimplesCompetenceRevenueProcessingStatus.REQUIRES_ADDITIONAL_INFORMATION;
    }

    private void validateRevenueBasis(
            CompetenceRevenue competenceRevenue,
            BigDecimal revenueBasis) {
        if (revenueBasis.compareTo(
                BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "A base de receita não pode ser negativa.");
        }

        if (competenceRevenue.totalAmount().compareTo(
                BigDecimal.ZERO) > 0
                && revenueBasis.compareTo(
                        BigDecimal.ZERO) == 0) {

            throw new IllegalArgumentException(
                    "Uma competência com receita positiva "
                            + "não pode possuir base de enquadramento igual a zero.");
        }
    }

    private TaxDecision createDecision(
            CompetenceRevenue competenceRevenue,
            BigDecimal revenueBasis,
            List<SimplesCompetenceRevenueItemResult> items,
            BigDecimal processedTaxAmount,
            Optional<BigDecimal> finalTaxAmount,
            SimplesCompetenceRevenueProcessingStatus status,
            boolean hasExternalObligations) {
        long processedCount = items
                .stream()
                .filter(
                        SimplesCompetenceRevenueItemResult::isFinal)
                .count();

        long pendingCount = items.size()
                - processedCount;

        String finalAmount = finalTaxAmount
                .map(
                        BigDecimal::toPlainString)
                .orElse(
                        "PENDENTE");

        return new TaxDecision(
                "SIMPLES_COMPETENCE_REVENUE_PROCESSING",
                RULE_VERSION,
                "Processamento consolidado das receitas segregadas "
                        + "de uma competência do Simples Nacional.",
                "Competência="
                        + competenceRevenue.competence()
                        + "; ReceitaTotal="
                        + competenceRevenue.totalAmount()
                        + "; Base="
                        + revenueBasis
                        + "; QuantidadeReceitas="
                        + items.size(),
                "Processadas="
                        + processedCount
                        + "; Pendentes="
                        + pendingCount
                        + "; ObrigacoesExternas="
                        + hasExternalObligations,
                "Status="
                        + status
                        + "; ValorProcessado="
                        + processedTaxAmount
                        + "; ValorFinal="
                        + finalAmount,
                "Lei Complementar nº 123/2006, art. 18; "
                        + "Resolução CGSN nº 140/2018, art. 25.");
    }
}