package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.time.YearMonth;
import java.util.Objects;

public record SimplesIssSublimitEffectResult(
        SimplesSublimitTemporalEffectResult temporalEffect,
        YearMonth assessmentPeriod,
        SimplesIssSublimitCollectionStatus status,
        TaxDecision decision) {

    public SimplesIssSublimitEffectResult {
        Objects.requireNonNull(
                temporalEffect,
                "O efeito temporal do sublimite não pode ser nulo.");

        Objects.requireNonNull(
                assessmentPeriod,
                "A competência não pode ser nula.");

        Objects.requireNonNull(
                status,
                "O status de recolhimento do ISS não pode ser nulo.");

        Objects.requireNonNull(
                decision,
                "A decisão tributária não pode ser nula.");

        YearMonth openingPeriod = YearMonth.from(
                temporalEffect.openingDate());

        if (assessmentPeriod.isBefore(
                openingPeriod)) {
            throw new IllegalArgumentException(
                    "A competência não pode ser anterior "
                            + "ao início da atividade.");
        }
    }

    public boolean isInsideDas() {
        return status.isInsideDas();
    }

    public boolean requiresTransitionalCalculation() {
        return status.requiresTransitionalCalculation();
    }

    public boolean isOutsideDas() {
        return status.isOutsideDas();
    }
}