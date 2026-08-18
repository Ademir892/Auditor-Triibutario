package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;
import java.util.Optional;

public final class GuideAmountAuditor {

    private static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL128;

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private static final String RULE_CODE = "SIMPLES_GUIDE_AMOUNT_AUDIT";

    private static final String RULE_VERSION = "2026.1";

    private static final String LEGAL_REFERENCE = "Comparação interna de auditoria baseada no valor "
            + "estimado pelo motor tributário. "
            + "A tolerância utilizada é um critério técnico "
            + "do software e não representa regra fiscal.";

    public GuideAmountAuditResult audit(
            GuideAmountAuditRequest request) {
        Objects.requireNonNull(
                request,
                "A requisição de auditoria não pode ser nula.");

        BigDecimal expectedAmount = request.estimatedTaxResult()
                .estimatedTaxAmount();

        BigDecimal guideAmount = request.guideAmount();

        BigDecimal signedDifference = guideAmount.subtract(
                expectedAmount);

        BigDecimal absoluteDifference = signedDifference.abs();

        Optional<BigDecimal> percentageDifference = calculatePercentageDifference(
                expectedAmount,
                absoluteDifference);

        GuideAmountAuditStatus status = determineStatus(
                request.estimatedTaxResult(),
                absoluteDifference,
                request.tolerance());

        TaxDecision decision = createDecision(
                request,
                expectedAmount,
                guideAmount,
                signedDifference,
                absoluteDifference,
                percentageDifference,
                status);

        return new GuideAmountAuditResult(
                expectedAmount,
                guideAmount,
                signedDifference,
                absoluteDifference,
                percentageDifference,
                request.tolerance(),
                status,
                decision);
    }

    private GuideAmountAuditStatus determineStatus(
            SimplesEstimatedTaxResult estimatedTaxResult,
            BigDecimal absoluteDifference,
            BigDecimal tolerance) {
        if (estimatedTaxResult.status() == SimplesEstimatedTaxStatus.DEFERRED_BELOW_MINIMUM) {

            return GuideAmountAuditStatus.REQUIRES_ADDITIONAL_CONTEXT;
        }

        if (absoluteDifference.compareTo(
                BigDecimal.ZERO) == 0) {
            return GuideAmountAuditStatus.EXACT_MATCH;
        }

        if (absoluteDifference.compareTo(
                tolerance) <= 0) {
            return GuideAmountAuditStatus.WITHIN_TOLERANCE;
        }

        return GuideAmountAuditStatus.DIVERGENT;
    }

    private Optional<BigDecimal> calculatePercentageDifference(
            BigDecimal expectedAmount,
            BigDecimal absoluteDifference) {
        if (expectedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return Optional.empty();
        }

        BigDecimal percentage = absoluteDifference
                .divide(
                        expectedAmount,
                        CALCULATION_CONTEXT)
                .multiply(
                        ONE_HUNDRED,
                        CALCULATION_CONTEXT);

        return Optional.of(
                percentage);
    }

    private TaxDecision createDecision(
            GuideAmountAuditRequest request,
            BigDecimal expectedAmount,
            BigDecimal guideAmount,
            BigDecimal signedDifference,
            BigDecimal absoluteDifference,
            Optional<BigDecimal> percentageDifference,
            GuideAmountAuditStatus status) {
        String description = "Auditoria do valor informado na guia em comparação "
                + "com o valor estimado pelo motor tributário.";

        String input = "Valor estimado = "
                + expectedAmount.toPlainString()
                + "; valor informado na guia = "
                + guideAmount.toPlainString()
                + "; tolerância técnica = "
                + request.tolerance().toPlainString()
                + ".";

        String condition = createCondition(
                request,
                signedDifference,
                absoluteDifference,
                percentageDifference,
                status);

        String result = "Status = "
                + status.getDisplayName()
                + "; diferença assinada = "
                + signedDifference.toPlainString()
                + "; diferença absoluta = "
                + absoluteDifference.toPlainString()
                + createPercentageResult(
                        percentageDifference)
                + ".";

        return new TaxDecision(
                RULE_CODE,
                RULE_VERSION,
                description,
                input,
                condition,
                result,
                LEGAL_REFERENCE);
    }

    private String createCondition(
            GuideAmountAuditRequest request,
            BigDecimal signedDifference,
            BigDecimal absoluteDifference,
            Optional<BigDecimal> percentageDifference,
            GuideAmountAuditStatus status) {
        if (status == GuideAmountAuditStatus.REQUIRES_ADDITIONAL_CONTEXT) {

            return "O valor estimado está abaixo do mínimo para "
                    + "emissão de DAS e pode depender de valores "
                    + "diferidos de competências anteriores.";
        }

        if (status == GuideAmountAuditStatus.EXACT_MATCH) {
            return "O valor da guia é exatamente igual "
                    + "ao valor estimado.";
        }

        if (status == GuideAmountAuditStatus.WITHIN_TOLERANCE) {
            return "A diferença absoluta de "
                    + absoluteDifference.toPlainString()
                    + " está dentro da tolerância técnica de "
                    + request.tolerance().toPlainString()
                    + ".";
        }

        String direction;

        if (signedDifference.compareTo(BigDecimal.ZERO) > 0) {
            direction = "O valor da guia é superior ao estimado.";
        } else {
            direction = "O valor da guia é inferior ao estimado.";
        }

        return direction
                + " A diferença absoluta de "
                + absoluteDifference.toPlainString()
                + " ultrapassa a tolerância técnica de "
                + request.tolerance().toPlainString()
                + createPercentageCondition(
                        percentageDifference)
                + ".";
    }

    private String createPercentageCondition(
            Optional<BigDecimal> percentageDifference) {
        return percentageDifference
                .map(value -> "; diferença percentual aproximada = "
                        + value.toPlainString()
                        + "%")
                .orElse("");
    }

    private String createPercentageResult(
            Optional<BigDecimal> percentageDifference) {
        return percentageDifference
                .map(value -> "; diferença percentual = "
                        + value.toPlainString()
                        + "%")
                .orElse(
                        "; diferença percentual indisponível "
                                + "porque o valor esperado é zero");
    }
}