package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.time.YearMonth;
import java.util.Objects;

public final class SimplesIssSublimitEffectCalculator {

    private static final String RULE_CODE = "SIMPLES_ISS_SUBLIMIT_EFFECT";

    private static final String RULE_VERSION = "LC123-CGSN140-2018-2026";

    private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, arts. 19 e 20; "
            + "Resolução CGSN nº 140/2018, arts. 12 e 24.";

    public SimplesIssSublimitEffectResult calculate(
            SimplesSublimitTemporalEffectResult temporalEffect,
            YearMonth assessmentPeriod) {
        Objects.requireNonNull(
                temporalEffect,
                "O efeito temporal do sublimite não pode ser nulo.");

        Objects.requireNonNull(
                assessmentPeriod,
                "A competência não pode ser nula.");

        YearMonth openingPeriod = YearMonth.from(
                temporalEffect.openingDate());

        if (assessmentPeriod.isBefore(
                openingPeriod)) {
            throw new IllegalArgumentException(
                    "A competência não pode ser anterior "
                            + "ao início da atividade.");
        }

        SimplesIssSublimitCollectionStatus status = determineStatus(
                temporalEffect,
                assessmentPeriod);

        TaxDecision decision = createDecision(
                temporalEffect,
                assessmentPeriod,
                status);

        return new SimplesIssSublimitEffectResult(
                temporalEffect,
                assessmentPeriod,
                status,
                decision);
    }

    private SimplesIssSublimitCollectionStatus determineStatus(
            SimplesSublimitTemporalEffectResult temporalEffect,
            YearMonth assessmentPeriod) {
        SimplesSublimitEvaluationResult evaluation = temporalEffect.evaluationResult();

        if (!evaluation.isExceeded()) {
            return SimplesIssSublimitCollectionStatus.IN_DAS_STANDARD;
        }

        if (temporalEffect.isRetroactive()) {
            return SimplesIssSublimitCollectionStatus.OUTSIDE_DAS;
        }

        if (assessmentPeriod.isBefore(
                temporalEffect.excessDetectedPeriod())) {
            return SimplesIssSublimitCollectionStatus.IN_DAS_STANDARD;
        }

        YearMonth impedimentStartPeriod = temporalEffect
                .impedimentStartPeriod()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Uma avaliação com excesso "
                                        + "deve possuir período "
                                        + "de início do impedimento."));

        if (assessmentPeriod.isBefore(
                impedimentStartPeriod)) {
            return SimplesIssSublimitCollectionStatus.IN_DAS_TRANSITIONAL;
        }

        return SimplesIssSublimitCollectionStatus.OUTSIDE_DAS;
    }

    private TaxDecision createDecision(
            SimplesSublimitTemporalEffectResult temporalEffect,
            YearMonth assessmentPeriod,
            SimplesIssSublimitCollectionStatus status) {
        String impedimentStart = temporalEffect
                .impedimentStartPeriod()
                .map(
                        YearMonth::toString)
                .orElse(
                        "NAO_APLICAVEL");

        return new TaxDecision(
                RULE_CODE,
                RULE_VERSION,
                "Determinação da forma de recolhimento do ISS "
                        + "em razão do sublimite do Simples Nacional.",
                "Competencia="
                        + assessmentPeriod
                        + "; CompetenciaExcesso="
                        + temporalEffect.excessDetectedPeriod()
                        + "; StatusSublimite="
                        + temporalEffect
                                .evaluationResult()
                                .status()
                        + "; InicioImpedimento="
                        + impedimentStart,
                "EfeitoTemporal="
                        + temporalEffect.timing(),
                "StatusISS="
                        + status
                        + "; DentroDAS="
                        + status.isInsideDas()
                        + "; CalculoTransitorio="
                        + status.requiresTransitionalCalculation(),
                LEGAL_REFERENCE);
    }
}