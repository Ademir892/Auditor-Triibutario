package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;

import java.util.Objects;
import java.util.Optional;

public record SimplesCompetenceRevenueItemResult(
                RevenueEntry revenue,
                SimplesRevenueClassificationResult classification,
                SimplesCompetenceRevenueItemStatus status,
                Optional<SimplesRevenueTaxProcessingResult> taxResult,
                String explanation) {

        public SimplesCompetenceRevenueItemResult {
                Objects.requireNonNull(
                                revenue,
                                "A receita não pode ser nula.");

                Objects.requireNonNull(
                                classification,
                                "A classificação não pode ser nula.");

                Objects.requireNonNull(
                                status,
                                "O status não pode ser nulo.");

                Objects.requireNonNull(
                                taxResult,
                                "O resultado tributário opcional não pode ser nulo.");

                Objects.requireNonNull(
                                explanation,
                                "A explicação não pode ser nula.");

                if (!classification
                                .revenue()
                                .equals(
                                                revenue)) {

                        throw new IllegalArgumentException(
                                        "A classificação deve pertencer à mesma receita "
                                                        + "do item processado.");
                }

                explanation = explanation.trim();

                if (explanation.isBlank()) {
                        throw new IllegalArgumentException(
                                        "A explicação não pode estar vazia.");
                }

                validateState(
                                status,
                                classification,
                                taxResult);
        }

        public boolean isFinal() {
                return status.isFinal();
        }

        public boolean hasExternalObligation() {
                return status == SimplesCompetenceRevenueItemStatus.COMPLETED_WITH_EXTERNAL_OBLIGATION;
        }

        private static void validateState(
                        SimplesCompetenceRevenueItemStatus status,
                        SimplesRevenueClassificationResult classification,
                        Optional<SimplesRevenueTaxProcessingResult> taxResult) {
                if (status == SimplesCompetenceRevenueItemStatus.REQUIRES_FATOR_R) {

                        if (classification.isResolved()) {
                                throw new IllegalArgumentException(
                                                "Um item que necessita Fator R "
                                                                + "não pode possuir classificação resolvida.");
                        }

                        if (classification.status() != SimplesRevenueClassificationStatus.REQUIRES_FATOR_R) {

                                throw new IllegalArgumentException(
                                                "O status REQUIRES_FATOR_R exige "
                                                                + "classificação pendente especificamente "
                                                                + "por ausência do Fator R.");
                        }

                        if (taxResult.isPresent()) {
                                throw new IllegalArgumentException(
                                                "Um item pendente de Fator R "
                                                                + "não pode possuir cálculo tributário.");
                        }

                        return;
                }

                if (status == SimplesCompetenceRevenueItemStatus.REQUIRES_CLASSIFICATION) {

                        if (classification.isResolved()) {
                                throw new IllegalArgumentException(
                                                "Um item que necessita classificação "
                                                                + "não pode possuir classificação resolvida.");
                        }

                        if (classification.status() == SimplesRevenueClassificationStatus.REQUIRES_FATOR_R) {

                                throw new IllegalArgumentException(
                                                "Uma classificação pendente exclusivamente "
                                                                + "por Fator R deve utilizar "
                                                                + "o status REQUIRES_FATOR_R.");
                        }

                        if (taxResult.isPresent()) {
                                throw new IllegalArgumentException(
                                                "Um item pendente de classificação "
                                                                + "não pode possuir cálculo tributário.");
                        }

                        return;
                }

                if (status == SimplesCompetenceRevenueItemStatus.REQUIRES_REVENUE_BASIS) {

                        if (!classification.isResolved()) {
                                throw new IllegalArgumentException(
                                                "Um item pendente de base de enquadramento "
                                                                + "deve possuir classificação tributária resolvida.");
                        }

                        if (taxResult.isPresent()) {
                                throw new IllegalArgumentException(
                                                "Um item pendente da memória da base "
                                                                + "não pode possuir cálculo tributário.");
                        }

                        return;
                }

                if (status == SimplesCompetenceRevenueItemStatus.UNSUPPORTED_ROUTE) {

                        if (!classification.isResolved()) {
                                throw new IllegalArgumentException(
                                                "Uma rota não suportada exige "
                                                                + "classificação tributária resolvida.");
                        }

                        if (taxResult.isPresent()) {
                                throw new IllegalArgumentException(
                                                "Uma rota ainda não suportada "
                                                                + "não pode possuir cálculo tributário.");
                        }

                        return;
                }

                SimplesRevenueTaxProcessingResult result = taxResult.orElseThrow(
                                () -> new IllegalArgumentException(
                                                "O status "
                                                                + status
                                                                + " exige resultado tributário."));

                if (!result
                                .revenue()
                                .equals(
                                                classification.revenue())) {

                        throw new IllegalArgumentException(
                                        "O resultado tributário deve pertencer "
                                                        + "à mesma receita da classificação.");
                }

                if (status == SimplesCompetenceRevenueItemStatus.REQUIRES_ADDITIONAL_RULES) {

                        if (result.isFinal()) {
                                throw new IllegalArgumentException(
                                                "Um item pendente de regras adicionais "
                                                                + "não pode possuir valor tributário final.");
                        }

                        return;
                }

                if (!result.isFinal()) {
                        throw new IllegalArgumentException(
                                        "Um item concluído deve possuir "
                                                        + "valor tributário final.");
                }

                if (status == SimplesCompetenceRevenueItemStatus.COMPLETED_WITH_EXTERNAL_OBLIGATION
                                && !result.hasExternalObligation()) {

                        throw new IllegalArgumentException(
                                        "O status informa obrigação externa, "
                                                        + "mas o resultado tributário "
                                                        + "não possui essa obrigação.");
                }

                if (status == SimplesCompetenceRevenueItemStatus.COMPLETED
                                && result.hasExternalObligation()) {

                        throw new IllegalArgumentException(
                                        "Um resultado com obrigação externa "
                                                        + "deve utilizar o status correspondente.");
                }
        }
}