package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.time.YearMonth;
import java.util.Objects;

public final class SimplesIcmsSublimitEffectCalculator {

    private static final String RULE_CODE = "SIMPLES_ICMS_SUBLIMIT_EFFECT";

    private static final String RULE_VERSION = "LC123-CGSN140-2018-2026";

    private static final String LEGAL_REFERENCE = "Lei Complementar nº 123/2006, arts. 19 e 20; "
            + "Resolução CGSN nº 140/2018, arts. 12 e 24.";

    public SimplesIcmsSublimitEffectResult calculate(
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

        SimplesIcmsSublimitCollectionStatus status = determineStatus(
                temporalEffect,
                assessmentPeriod);

        TaxDecision decision = createDecision(
                temporalEffect,
                assessmentPeriod,
                status);

        return new SimplesIcmsSublimitEffectResult(
                temporalEffect,
                assessmentPeriod,
                status,
                decision);
    }

    private SimplesIcmsSublimitCollectionStatus determineStatus(
            SimplesSublimitTemporalEffectResult temporalEffect,
            YearMonth assessmentPeriod) {
        SimplesSublimitEvaluationResult evaluation = temporalEffect.evaluationResult();

        if (!evaluation.isExceeded()) {
            return SimplesIcmsSublimitCollectionStatus.IN_DAS_STANDARD;
        }

        /*
         * Se o efeito é retroativo ao início da atividade,
         * todas as competências desde a abertura devem ser
         * consideradas fora do DAS para fins de ICMS.
         */
        if (temporalEffect.isRetroactive()) {
            return SimplesIcmsSublimitCollectionStatus.OUTSIDE_DAS;
        }

        /*
         * Competências anteriores ao mês em que o excesso
         * foi identificado permanecem no fluxo normal.
         */
        if (assessmentPeriod.isBefore(
                temporalEffect.excessDetectedPeriod())) {
            return SimplesIcmsSublimitCollectionStatus.IN_DAS_STANDARD;
        }

        YearMonth impedimentStartPeriod = temporalEffect
                .impedimentStartPeriod()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Uma avaliação com excesso "
                                        + "deve possuir período "
                                        + "de início do impedimento."));

        /*
         * O excesso já ocorreu, porém os efeitos do
         * impedimento ainda não começaram.
         *
         * Neste intervalo o ICMS continua dentro do
         * Simples, mas o art. 24 exige cálculo específico
         * para a parcela da receita que excedeu o sublimite.
         */
        if (assessmentPeriod.isBefore(
                impedimentStartPeriod)) {
            return SimplesIcmsSublimitCollectionStatus.IN_DAS_TRANSITIONAL;
        }

        return SimplesIcmsSublimitCollectionStatus.OUTSIDE_DAS;
    }

    private TaxDecision createDecision(
            SimplesSublimitTemporalEffectResult temporalEffect,
            YearMonth assessmentPeriod,
            SimplesIcmsSublimitCollectionStatus status) {
        String impedimentStart = temporalEffect
                .impedimentStartPeriod()
                .map(
                        YearMonth::toString)
                .orElse(
                        "NAO_APLICAVEL");

        return new TaxDecision(
                RULE_CODE,
                RULE_VERSION,
                "Determinação da forma de recolhimento do ICMS "
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
                "StatusICMS="
                        + status
                        + "; DentroDAS="
                        + status.isInsideDas()
                        + "; CalculoTransitorio="
                        + status.requiresTransitionalCalculation(),
                LEGAL_REFERENCE);
    }
}