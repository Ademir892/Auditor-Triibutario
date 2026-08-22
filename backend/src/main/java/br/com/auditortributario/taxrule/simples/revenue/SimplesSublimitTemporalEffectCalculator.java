package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Optional;

public final class SimplesSublimitTemporalEffectCalculator {

    private static final String RULE_CODE = "SIMPLES_SUBLIMIT_TEMPORAL_EFFECT";

    private static final String RULE_VERSION = "LC123-CGSN140-2018-2026";

    private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, art. 3º, §§ 11 e 13, "
            + "e art. 20, §§ 1º e 1º-A; "
            + "Resolução CGSN nº 140/2018, art. 12, §§ 1º a 4º.";

    public SimplesSublimitTemporalEffectResult calculate(
            SimplesSublimitEvaluationResult evaluationResult,
            LocalDate openingDate,
            YearMonth evaluationPeriod) {
        Objects.requireNonNull(
                evaluationResult,
                "O resultado da avaliação do sublimite não pode ser nulo.");

        Objects.requireNonNull(
                openingDate,
                "A data de abertura não pode ser nula.");

        Objects.requireNonNull(
                evaluationPeriod,
                "A competência de avaliação não pode ser nula.");

        YearMonth openingPeriod = YearMonth.from(
                openingDate);

        if (evaluationPeriod.isBefore(
                openingPeriod)) {
            throw new IllegalArgumentException(
                    "A competência avaliada não pode ser "
                            + "anterior ao início da atividade.");
        }

        boolean openingYear = openingDate.getYear() == evaluationPeriod.getYear();

        SimplesSublimitEffectTiming timing = determineTiming(
                evaluationResult,
                openingYear);

        Optional<YearMonth> impedimentStartPeriod = determineImpedimentStartPeriod(
                evaluationResult,
                openingPeriod,
                evaluationPeriod,
                timing);

        TaxDecision decision = createDecision(
                evaluationResult,
                openingDate,
                evaluationPeriod,
                openingYear,
                timing,
                impedimentStartPeriod);

        return new SimplesSublimitTemporalEffectResult(
                evaluationResult,
                openingDate,
                evaluationPeriod,
                timing,
                impedimentStartPeriod,
                decision);
    }

    private SimplesSublimitEffectTiming determineTiming(
            SimplesSublimitEvaluationResult evaluationResult,
            boolean openingYear) {
        if (!evaluationResult.isExceeded()) {
            return SimplesSublimitEffectTiming.NO_IMPEDIMENT;
        }

        if (evaluationResult
                .isExceededOverTwentyPercent()) {

            if (openingYear) {
                return SimplesSublimitEffectTiming.RETROACTIVE_TO_OPENING;
            }

            return SimplesSublimitEffectTiming.NEXT_MONTH;
        }

        return SimplesSublimitEffectTiming.NEXT_CALENDAR_YEAR;
    }

    private Optional<YearMonth> determineImpedimentStartPeriod(
            SimplesSublimitEvaluationResult evaluationResult,
            YearMonth openingPeriod,
            YearMonth evaluationPeriod,
            SimplesSublimitEffectTiming timing) {
        if (!evaluationResult.isExceeded()) {
            return Optional.empty();
        }

        return switch (timing) {

            case RETROACTIVE_TO_OPENING ->
                Optional.of(
                        openingPeriod);

            case NEXT_MONTH ->
                Optional.of(
                        evaluationPeriod.plusMonths(
                                1));

            case NEXT_CALENDAR_YEAR ->
                Optional.of(
                        YearMonth.of(
                                evaluationPeriod.getYear() + 1,
                                1));

            case NO_IMPEDIMENT ->
                Optional.empty();
        };
    }

    private TaxDecision createDecision(
            SimplesSublimitEvaluationResult evaluationResult,
            LocalDate openingDate,
            YearMonth evaluationPeriod,
            boolean openingYear,
            SimplesSublimitEffectTiming timing,
            Optional<YearMonth> impedimentStartPeriod) {
        String startPeriod = impedimentStartPeriod
                .map(
                        YearMonth::toString)
                .orElse(
                        "NAO_APLICAVEL");

        return new TaxDecision(
                RULE_CODE,
                RULE_VERSION,
                "Determinação do momento em que o excesso "
                        + "do sublimite produz impedimento "
                        + "ao recolhimento de ICMS e ISS "
                        + "pelo Simples Nacional.",
                "DataAbertura="
                        + openingDate
                        + "; CompetenciaAvaliacao="
                        + evaluationPeriod
                        + "; StatusSublimite="
                        + evaluationResult.status()
                        + "; Excesso="
                        + evaluationResult
                                .excessAmount()
                                .toPlainString(),
                "AnoInicioAtividade="
                        + openingYear
                        + "; ExcessoSuperiorVintePorCento="
                        + evaluationResult
                                .isExceededOverTwentyPercent(),
                "Efeito="
                        + timing
                        + "; InicioImpedimento="
                        + startPeriod,
                LEGAL_REFERENCE);
    }
}