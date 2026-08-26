package br.com.auditortributario.taxrule.simples.revenue;

import java.util.Objects;
import java.util.Optional;

public record SimplesSublimitTaxContext(
        SimplesSublimitTaxTreatment treatment,
        Optional<SimplesSublimitEvaluationResult> evaluation) {

    public SimplesSublimitTaxContext {

        Objects.requireNonNull(
                treatment,
                "O tratamento do sublimite não pode ser nulo.");

        Objects.requireNonNull(
                evaluation,
                "A avaliação opcional do sublimite não pode ser nula.");

        if (treatment == SimplesSublimitTaxTreatment.IN_DAS_TRANSITIONAL
                && evaluation.isEmpty()) {

            throw new IllegalArgumentException(
                    "O tratamento transitório do sublimite "
                            + "exige uma avaliação do sublimite.");
        }
    }

    public static SimplesSublimitTaxContext standard() {
        return new SimplesSublimitTaxContext(
                SimplesSublimitTaxTreatment.IN_DAS_STANDARD,
                Optional.empty());
    }

    public static SimplesSublimitTaxContext transitional(
            SimplesSublimitEvaluationResult evaluation) {

        Objects.requireNonNull(
                evaluation,
                "A avaliação do sublimite não pode ser nula.");

        return new SimplesSublimitTaxContext(
                SimplesSublimitTaxTreatment.IN_DAS_TRANSITIONAL,
                Optional.of(
                        evaluation));
    }

    public static SimplesSublimitTaxContext outsideDas() {
        return new SimplesSublimitTaxContext(
                SimplesSublimitTaxTreatment.OUTSIDE_DAS,
                Optional.empty());
    }

    public static SimplesSublimitTaxContext outsideDas(
            SimplesSublimitEvaluationResult evaluation) {

        Objects.requireNonNull(
                evaluation,
                "A avaliação do sublimite não pode ser nula.");

        return new SimplesSublimitTaxContext(
                SimplesSublimitTaxTreatment.OUTSIDE_DAS,
                Optional.of(
                        evaluation));
    }

    public boolean hasEvaluation() {
        return evaluation.isPresent();
    }
}