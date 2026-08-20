package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class GuideStructureAuditor {

        private static final String RULE_CODE = "SIMPLES_GUIDE_STRUCTURE_AUDIT";

        private static final String RULE_VERSION = "2026.1";

        private static final String INTERNAL_REFERENCE = "Auditoria interna baseada na comparação entre "
                        + "o cálculo produzido pelo motor tributário "
                        + "e os dados informados na apuração analisada. "
                        + "As severidades são critérios técnicos "
                        + "do software.";

        public GuideStructureAuditResult audit(
                        GuideStructureAuditRequest request) {
                Objects.requireNonNull(
                                request,
                                "A requisição de auditoria estrutural não pode ser nula.");

                ExpectedStructure expected = extractExpectedStructure(
                                request.estimatedTaxResult());

                List<GuideStructureAuditFinding> findings = new ArrayList<>();

                int providedFields = 0;
                boolean hasMismatch = false;

                if (request.reportedFatorR().isPresent()) {
                        providedFields++;

                        if (request.reportedFatorR()
                                        .get()
                                        .compareTo(expected.fatorR()) != 0) {

                                hasMismatch = true;

                                findings.add(
                                                new GuideStructureAuditFinding(
                                                                "FATOR_R_MISMATCH",
                                                                GuideStructureAuditSeverity.HIGH,
                                                                "Fator R",
                                                                expected.fatorR().toPlainString(),
                                                                request.reportedFatorR()
                                                                                .get()
                                                                                .toPlainString(),
                                                                "O Fator R informado diverge do calculado. "
                                                                                + "Verifique folha, receita e períodos "
                                                                                + "utilizados na apuração."));
                        }
                } else {
                        findings.add(
                                        missingFinding(
                                                        "FATOR_R_NOT_REPORTED",
                                                        "Fator R",
                                                        expected.fatorR().toPlainString()));
                }

                if (request.reportedAnnex().isPresent()) {
                        providedFields++;

                        if (request.reportedAnnex().get() != expected.annex()) {
                                hasMismatch = true;

                                findings.add(
                                                new GuideStructureAuditFinding(
                                                                "ANNEX_MISMATCH",
                                                                GuideStructureAuditSeverity.HIGH,
                                                                "Anexo",
                                                                expected.annex().getDisplayName(),
                                                                request.reportedAnnex()
                                                                                .get()
                                                                                .getDisplayName(),
                                                                "O anexo informado diverge do enquadramento "
                                                                                + "obtido pelo Fator R calculado."));
                        }
                } else {
                        findings.add(
                                        missingFinding(
                                                        "ANNEX_NOT_REPORTED",
                                                        "Anexo",
                                                        expected.annex().getDisplayName()));
                }

                if (request.reportedBracketNumber().isPresent()) {
                        providedFields++;

                        if (request.reportedBracketNumber().get() != expected.bracketNumber()) {

                                hasMismatch = true;

                                findings.add(
                                                new GuideStructureAuditFinding(
                                                                "BRACKET_MISMATCH",
                                                                GuideStructureAuditSeverity.HIGH,
                                                                "Faixa tributária",
                                                                Integer.toString(
                                                                                expected.bracketNumber()),
                                                                Integer.toString(
                                                                                request.reportedBracketNumber()
                                                                                                .get()),
                                                                "A faixa informada diverge da faixa "
                                                                                + "selecionada pela base de receita. "
                                                                                + "Verifique RBT12, RBT12p e a tabela "
                                                                                + "tributária utilizada."));
                        }
                } else {
                        findings.add(
                                        missingFinding(
                                                        "BRACKET_NOT_REPORTED",
                                                        "Faixa tributária",
                                                        Integer.toString(
                                                                        expected.bracketNumber())));
                }

                if (request.reportedEffectiveRate().isPresent()) {
                        providedFields++;

                        BigDecimal reportedRate = request.reportedEffectiveRate().get();

                        BigDecimal difference = reportedRate
                                        .subtract(expected.effectiveRate())
                                        .abs();

                        if (difference.compareTo(
                                        request.effectiveRateTolerance()) > 0) {
                                hasMismatch = true;

                                findings.add(
                                                new GuideStructureAuditFinding(
                                                                "EFFECTIVE_RATE_MISMATCH",
                                                                GuideStructureAuditSeverity.MEDIUM,
                                                                "Alíquota efetiva",
                                                                expected.effectiveRate()
                                                                                .toPlainString(),
                                                                reportedRate.toPlainString(),
                                                                "A alíquota efetiva informada diverge "
                                                                                + "da calculada além da tolerância. "
                                                                                + "Verifique faixa, alíquota nominal, "
                                                                                + "parcela a deduzir e precisão "
                                                                                + "utilizada no cálculo."));
                        }
                } else {
                        findings.add(
                                        missingFinding(
                                                        "EFFECTIVE_RATE_NOT_REPORTED",
                                                        "Alíquota efetiva",
                                                        expected.effectiveRate()
                                                                        .toPlainString()));
                }

                GuideStructureAuditStatus status = determineStatus(
                                providedFields,
                                hasMismatch);

                GuideStructureAuditSeverity severity = determineSeverity(
                                findings,
                                status);

                TaxDecision decision = createDecision(
                                request,
                                expected,
                                findings,
                                status,
                                severity);

                return new GuideStructureAuditResult(
                                request.estimatedTaxResult(),
                                findings,
                                status,
                                severity,
                                decision);
        }

        private ExpectedStructure extractExpectedStructure(
                        SimplesEstimatedTaxResult estimatedTaxResult) {
                SimplesEffectiveRateResult effectiveRateResult = estimatedTaxResult.effectiveRateResult();

                SimplesTaxBracketSelectionResult bracketResult = effectiveRateResult.bracketSelectionResult();

                return new ExpectedStructure(
                                bracketResult
                                                .fatorRResult()
                                                .fatorR()
                                                .value(),
                                bracketResult
                                                .fatorRResult()
                                                .annex(),
                                bracketResult
                                                .bracket()
                                                .number(),
                                effectiveRateResult
                                                .effectiveRate());
        }

        private GuideStructureAuditFinding missingFinding(
                        String code,
                        String field,
                        String expectedValue) {
                return new GuideStructureAuditFinding(
                                code,
                                GuideStructureAuditSeverity.LOW,
                                field,
                                expectedValue,
                                "Não informado",
                                "O campo não foi informado e não pôde "
                                                + "ser comparado nesta auditoria.");
        }

        private GuideStructureAuditStatus determineStatus(
                        int providedFields,
                        boolean hasMismatch) {
                if (providedFields == 0) {
                        return GuideStructureAuditStatus.INSUFFICIENT_DATA;
                }

                if (hasMismatch) {
                        return GuideStructureAuditStatus.DIVERGENT;
                }

                if (providedFields < 4) {
                        return GuideStructureAuditStatus.PARTIALLY_COMPATIBLE;
                }

                return GuideStructureAuditStatus.COMPATIBLE;
        }

        private GuideStructureAuditSeverity determineSeverity(
                        List<GuideStructureAuditFinding> findings,
                        GuideStructureAuditStatus status) {
                if (status == GuideStructureAuditStatus.COMPATIBLE) {
                        return GuideStructureAuditSeverity.NONE;
                }

                GuideStructureAuditSeverity highest = GuideStructureAuditSeverity.NONE;

                for (GuideStructureAuditFinding finding : findings) {
                        highest = GuideStructureAuditSeverity.highest(
                                        highest,
                                        finding.severity());
                }

                return highest;
        }

        private TaxDecision createDecision(
                        GuideStructureAuditRequest request,
                        ExpectedStructure expected,
                        List<GuideStructureAuditFinding> findings,
                        GuideStructureAuditStatus status,
                        GuideStructureAuditSeverity severity) {
                String description = "Auditoria estrutural da apuração do Simples Nacional.";

                String input = "Esperado: Fator R = "
                                + expected.fatorR().toPlainString()
                                + "; anexo = "
                                + expected.annex().getDisplayName()
                                + "; faixa = "
                                + expected.bracketNumber()
                                + "; alíquota efetiva = "
                                + expected.effectiveRate().toPlainString()
                                + ". Informado: Fator R = "
                                + optionalDecimal(
                                                request.reportedFatorR())
                                + "; anexo = "
                                + optionalAnnex(
                                                request.reportedAnnex())
                                + "; faixa = "
                                + optionalInteger(
                                                request.reportedBracketNumber())
                                + "; alíquota efetiva = "
                                + optionalDecimal(
                                                request.reportedEffectiveRate())
                                + ".";

                String condition = findings.isEmpty()
                                ? "Todos os campos estruturais informados "
                                                + "correspondem ao cálculo esperado."
                                : findings.stream()
                                                .map(finding -> finding.code()
                                                                + ": "
                                                                + finding.message())
                                                .collect(
                                                                Collectors.joining(" | "));

                String result = "Status = "
                                + status.getDisplayName()
                                + "; severidade = "
                                + severity.getDisplayName()
                                + "; quantidade de achados = "
                                + findings.size()
                                + ".";

                return new TaxDecision(
                                RULE_CODE,
                                RULE_VERSION,
                                description,
                                input,
                                condition,
                                result,
                                INTERNAL_REFERENCE);
        }

        private String optionalDecimal(
                        Optional<BigDecimal> value) {
                return value
                                .map(BigDecimal::toPlainString)
                                .orElse("Não informado");
        }

        private String optionalAnnex(
                        Optional<SimplesAnnex> value) {
                return value
                                .map(SimplesAnnex::getDisplayName)
                                .orElse("Não informado");
        }

        private String optionalInteger(
                        Optional<Integer> value) {
                return value
                                .map(String::valueOf)
                                .orElse("Não informado");
        }

        private record ExpectedStructure(
                        BigDecimal fatorR,
                        SimplesAnnex annex,
                        int bracketNumber,
                        BigDecimal effectiveRate) {
        }
}