package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Optional;

public record SimplesSublimitTemporalEffectResult(
        SimplesSublimitEvaluationResult evaluationResult,
        LocalDate openingDate,
        YearMonth excessDetectedPeriod,
        SimplesSublimitEffectTiming timing,
        Optional<YearMonth> impedimentStartPeriod,
        TaxDecision decision) {

    public SimplesSublimitTemporalEffectResult {
        Objects.requireNonNull(
                evaluationResult,
                "O resultado da avaliação do sublimite não pode ser nulo.");

        Objects.requireNonNull(
                openingDate,
                "A data de abertura não pode ser nula.");

        Objects.requireNonNull(
                excessDetectedPeriod,
                "A competência de avaliação não pode ser nula.");

        Objects.requireNonNull(
                timing,
                "O momento do efeito não pode ser nulo.");

        Objects.requireNonNull(
                impedimentStartPeriod,
                "O período opcional de início do impedimento "
                        + "não pode ser nulo.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        YearMonth openingPeriod = YearMonth.from(
                openingDate);

        if (excessDetectedPeriod.isBefore(
                openingPeriod)) {
            throw new IllegalArgumentException(
                    "A competência avaliada não pode ser "
                            + "anterior ao início da atividade.");
        }

        if (!evaluationResult.isExceeded()) {

            if (timing != SimplesSublimitEffectTiming.NO_IMPEDIMENT) {

                throw new IllegalArgumentException(
                        "Uma avaliação sem excesso de sublimite "
                                + "não pode possuir impedimento.");
            }

            if (impedimentStartPeriod.isPresent()) {
                throw new IllegalArgumentException(
                        "Uma avaliação sem excesso de sublimite "
                                + "não pode possuir período de impedimento.");
            }
        }

        if (evaluationResult.isExceeded()) {

            if (timing == SimplesSublimitEffectTiming.NO_IMPEDIMENT) {

                throw new IllegalArgumentException(
                        "Uma avaliação com excesso de sublimite "
                                + "deve possuir efeito temporal.");
            }

            if (impedimentStartPeriod.isEmpty()) {
                throw new IllegalArgumentException(
                        "Uma avaliação com excesso de sublimite "
                                + "deve possuir período de início "
                                + "do impedimento.");
            }
        }
    }

    public boolean hasImpediment() {
        return timing.hasImpediment();
    }

    public boolean isRetroactive() {
        return timing.isRetroactive();
    }

    public boolean isOpeningYear() {
        return openingDate.getYear() == excessDetectedPeriod.getYear();
    }
}