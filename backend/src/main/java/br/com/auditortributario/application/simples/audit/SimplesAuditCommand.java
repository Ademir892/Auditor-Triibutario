package br.com.auditortributario.application.simples.audit;

import br.com.auditortributario.application.simples.SimplesCalculationCommand;
import br.com.auditortributario.taxrule.simples.SimplesAnnex;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public record SimplesAuditCommand(
        SimplesCalculationCommand calculation,
        BigDecimal guideAmount,
        Optional<BigDecimal> reportedFatorR,
        Optional<SimplesAnnex> reportedAnnex,
        Optional<Integer> reportedBracketNumber,
        Optional<BigDecimal> reportedEffectiveRate) {

    public SimplesAuditCommand {
        Objects.requireNonNull(
                calculation,
                "O cálculo tributário não pode ser nulo.");

        Objects.requireNonNull(
                guideAmount,
                "O valor informado na guia não pode ser nulo.");

        Objects.requireNonNull(
                reportedFatorR,
                "O Fator R informado não pode ser nulo.");

        Objects.requireNonNull(
                reportedAnnex,
                "O anexo informado não pode ser nulo.");

        Objects.requireNonNull(
                reportedBracketNumber,
                "A faixa informada não pode ser nula.");

        Objects.requireNonNull(
                reportedEffectiveRate,
                "A alíquota efetiva informada não pode ser nula.");

        if (guideAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor informado na guia não pode ser negativo.");
        }

        reportedFatorR.ifPresent(
                value -> {
                    if (value.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                "O Fator R informado não pode ser negativo.");
                    }
                });

        reportedBracketNumber.ifPresent(
                value -> {
                    if (value <= 0) {
                        throw new IllegalArgumentException(
                                "A faixa informada deve ser maior que zero.");
                    }
                });

        reportedEffectiveRate.ifPresent(
                value -> {
                    if (value.compareTo(BigDecimal.ZERO) < 0
                            || value.compareTo(BigDecimal.ONE) > 0) {

                        throw new IllegalArgumentException(
                                "A alíquota efetiva informada deve estar "
                                        + "entre 0 e 1.");
                    }
                });
    }
}