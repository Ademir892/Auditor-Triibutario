package br.com.auditortributario.taxrule.simples.revenue;

import java.util.Objects;
import java.util.Optional;

public record SimplesSublimitTaxContext(
        SimplesSublimitTaxTreatment treatment,
        Optional<SimplesSublimitEvaluationResult> evaluation,
        Optional<SimplesSublimitMonthlyExcessResult> monthlyExcess,
        Optional<SimplesIcmsSublimitEffectResult> icmsEffect,
        Optional<SimplesIssSublimitEffectResult> issEffect) {

    public SimplesSublimitTaxContext {

        Objects.requireNonNull(
                treatment,
                "O tratamento do sublimite não pode ser nulo.");

        Objects.requireNonNull(
                evaluation,
                "A avaliação opcional do sublimite não pode ser nula.");

        Objects.requireNonNull(monthlyExcess, "O excesso mensal opcional não pode ser nulo.");
        Objects.requireNonNull(icmsEffect, "O efeito opcional do ICMS não pode ser nulo.");
        Objects.requireNonNull(issEffect, "O efeito opcional do ISS não pode ser nulo.");

        if (treatment == SimplesSublimitTaxTreatment.IN_DAS_TRANSITIONAL
                && evaluation.isEmpty()) {

            throw new IllegalArgumentException(
                    "O tratamento transitório do sublimite "
                            + "exige uma avaliação do sublimite.");
        }

        if (monthlyExcess.isPresent() && evaluation.isPresent()
                && !monthlyExcess.orElseThrow().evaluationResult().equals(evaluation.orElseThrow())) {
            throw new IllegalArgumentException("O excesso mensal deve pertencer à avaliação do sublimite informada.");
        }
    }

    public SimplesSublimitTaxContext(
            SimplesSublimitTaxTreatment treatment,
            Optional<SimplesSublimitEvaluationResult> evaluation) {
        this(treatment, evaluation, Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static SimplesSublimitTaxContext standard() {
        return new SimplesSublimitTaxContext(
                SimplesSublimitTaxTreatment.IN_DAS_STANDARD,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static SimplesSublimitTaxContext transitional(
            SimplesSublimitEvaluationResult evaluation) {

        Objects.requireNonNull(
                evaluation,
                "A avaliação do sublimite não pode ser nula.");

        return new SimplesSublimitTaxContext(
                SimplesSublimitTaxTreatment.IN_DAS_TRANSITIONAL,
                Optional.of(evaluation), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static SimplesSublimitTaxContext transitional(
            SimplesSublimitEvaluationResult evaluation,
            SimplesSublimitMonthlyExcessResult monthlyExcess,
            SimplesIcmsSublimitEffectResult icmsEffect,
            SimplesIssSublimitEffectResult issEffect) {
        return new SimplesSublimitTaxContext(
                SimplesSublimitTaxTreatment.IN_DAS_TRANSITIONAL,
                Optional.of(evaluation), Optional.of(monthlyExcess), Optional.of(icmsEffect), Optional.of(issEffect));
    }

    public static SimplesSublimitTaxContext outsideDas() {
        return new SimplesSublimitTaxContext(
                SimplesSublimitTaxTreatment.OUTSIDE_DAS,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static SimplesSublimitTaxContext outsideDas(
            SimplesSublimitEvaluationResult evaluation) {

        Objects.requireNonNull(
                evaluation,
                "A avaliação do sublimite não pode ser nula.");

        return new SimplesSublimitTaxContext(
                SimplesSublimitTaxTreatment.OUTSIDE_DAS,
                Optional.of(evaluation), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public boolean hasEvaluation() {
        return evaluation.isPresent();
    }

    public boolean hasCompleteTransitionalData() {
        return monthlyExcess.isPresent() && icmsEffect.isPresent() && issEffect.isPresent();
    }
}
