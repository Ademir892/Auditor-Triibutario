package br.com.auditortributario.api.simples.audit;

import br.com.auditortributario.api.simples.calculation.SimplesCalculationRequest;
import br.com.auditortributario.application.simples.audit.SimplesAuditCommand;
import br.com.auditortributario.taxrule.simples.SimplesAnnex;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Optional;

public record SimplesAuditRequest(
        @NotNull @Valid SimplesCalculationRequest calculation,

        @NotNull @PositiveOrZero BigDecimal guideAmount,

        @PositiveOrZero BigDecimal reportedFatorR,

        SimplesAnnex reportedAnnex,

        @Positive Integer reportedBracketNumber,

        @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal reportedEffectiveRate) {

    public SimplesAuditCommand toCommand() {
        return new SimplesAuditCommand(
                calculation.toCommand(),
                guideAmount,
                Optional.ofNullable(
                        reportedFatorR),
                Optional.ofNullable(
                        reportedAnnex),
                Optional.ofNullable(
                        reportedBracketNumber),
                Optional.ofNullable(
                        reportedEffectiveRate));
    }
}