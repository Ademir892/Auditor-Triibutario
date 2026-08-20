package br.com.auditortributario.taxrule.simples.revenue;

import br.com.auditortributario.taxrule.domain.TaxDecision;
import br.com.auditortributario.taxrule.domain.revenue.RevenueActivityType;
import br.com.auditortributario.taxrule.domain.revenue.RevenueEntry;
import br.com.auditortributario.taxrule.simples.FatorRCalculationResult;
import br.com.auditortributario.taxrule.simples.SimplesAnnex;

import java.time.YearMonth;
import java.util.Objects;
import java.util.Optional;

public final class SimplesRevenueClassifier {

    private static final YearMonth VALID_FROM = YearMonth.of(
            2018,
            1);

    private static final YearMonth VALID_UNTIL = YearMonth.of(
            2026,
            12);

    private static final String RULE_VERSION = "CGSN140-ART25-2018-2026";

    private static final String LEGAL_REFERENCE = "Resolução CGSN nº 140/2018, art. 25, § 1º; "
            + "Lei Complementar nº 123/2006, art. 18.";

    public SimplesRevenueClassificationResult classify(
            RevenueEntry revenue) {
        return classify(
                revenue,
                Optional.empty());
    }

    public SimplesRevenueClassificationResult classify(
            RevenueEntry revenue,
            FatorRCalculationResult fatorRResult) {
        Objects.requireNonNull(
                fatorRResult,
                "O resultado do Fator R não pode ser nulo.");

        return classify(
                revenue,
                Optional.of(
                        fatorRResult));
    }

    private SimplesRevenueClassificationResult classify(
            RevenueEntry revenue,
            Optional<FatorRCalculationResult> fatorRResult) {
        Objects.requireNonNull(
                revenue,
                "A receita não pode ser nula.");

        Objects.requireNonNull(
                fatorRResult,
                "O resultado opcional do Fator R não pode ser nulo.");

        validateAssessmentPeriod(
                revenue.competence());

        validateFatorRCompatibility(
                revenue);

        return switch (revenue.activityType()) {

            case COMMERCE ->
                resolved(
                        revenue,
                        SimplesRevenueTaxRoute.ANNEX_I,
                        "Receita de revenda de mercadorias "
                                + "direcionada ao Anexo I.",
                        "Atividade classificada como comércio.",
                        "Revenda de mercadorias.");

            case INDUSTRY ->
                resolved(
                        revenue,
                        SimplesRevenueTaxRoute.ANNEX_II,
                        "Receita decorrente de produto industrializado "
                                + "pelo contribuinte direcionada ao Anexo II.",
                        "Atividade classificada como indústria.",
                        "Venda de mercadorias industrializadas "
                                + "pelo contribuinte.");

            case SERVICE ->
                classifyService(
                        revenue,
                        fatorRResult);

            case OTHER ->
                unresolved(
                        revenue,
                        SimplesRevenueClassificationStatus.REQUIRES_MANUAL_CLASSIFICATION,
                        "A atividade informada como OTHER "
                                + "não possui regra tributária suficiente "
                                + "para determinação automática do anexo.",
                        "Atividade sem classificação tributária suficiente.",
                        "Necessária classificação adicional da atividade.");
        };
    }

    private SimplesRevenueClassificationResult classifyService(
            RevenueEntry revenue,
            Optional<FatorRCalculationResult> fatorRResult) {
        if (!revenue.subjectToFatorR()) {
            return unresolved(
                    revenue,
                    SimplesRevenueClassificationStatus.REQUIRES_SERVICE_RULE,
                    "A informação SERVICE, isoladamente, "
                            + "não permite determinar o anexo. "
                            + "O serviço precisa ser enquadrado "
                            + "pela regra específica aplicável.",
                    "Receita classificada como serviço "
                            + "não marcado como sujeito ao Fator R.",
                    "Determinar a regra específica do serviço "
                            + "antes de selecionar o anexo.");
        }

        if (fatorRResult.isEmpty()) {
            return unresolved(
                    revenue,
                    SimplesRevenueClassificationStatus.REQUIRES_FATOR_R,
                    "A receita de serviço está marcada como sujeita "
                            + "ao Fator R, mas nenhum resultado "
                            + "de Fator R foi fornecido.",
                    "Serviço sujeito ao Fator R.",
                    "Calcular o Fator R para determinar "
                            + "Anexo III ou Anexo V.");
        }

        SimplesAnnex annex = fatorRResult
                .orElseThrow()
                .annex();

        SimplesRevenueTaxRoute route = switch (annex) {

            case ANEXO_III ->
                SimplesRevenueTaxRoute.ANNEX_III;

            case ANEXO_V ->
                SimplesRevenueTaxRoute.ANNEX_V;
        };

        return resolved(
                revenue,
                route,
                "Receita de serviço sujeita ao Fator R "
                        + "direcionada ao "
                        + route.getDisplayName()
                        + ".",
                "Serviço sujeito ao Fator R "
                        + "com resultado tributário previamente calculado.",
                "Resultado do Fator R selecionou "
                        + route.getDisplayName()
                        + ".");
    }

    private SimplesRevenueClassificationResult resolved(
            RevenueEntry revenue,
            SimplesRevenueTaxRoute route,
            String explanation,
            String condition,
            String result) {
        TaxDecision decision = new TaxDecision(
                "SIMPLES_REVENUE_CLASSIFICATION",
                RULE_VERSION,
                "Classificação da receita para determinação "
                        + "da rota tributária no Simples Nacional.",
                buildInput(
                        revenue),
                condition,
                result,
                LEGAL_REFERENCE);

        return new SimplesRevenueClassificationResult(
                revenue,
                SimplesRevenueClassificationStatus.RESOLVED,
                Optional.of(
                        route),
                explanation,
                decision);
    }

    private SimplesRevenueClassificationResult unresolved(
            RevenueEntry revenue,
            SimplesRevenueClassificationStatus status,
            String explanation,
            String condition,
            String result) {
        TaxDecision decision = new TaxDecision(
                "SIMPLES_REVENUE_CLASSIFICATION",
                RULE_VERSION,
                "Classificação da receita para determinação "
                        + "da rota tributária no Simples Nacional.",
                buildInput(
                        revenue),
                condition,
                result,
                LEGAL_REFERENCE);

        return new SimplesRevenueClassificationResult(
                revenue,
                status,
                Optional.empty(),
                explanation,
                decision);
    }

    private String buildInput(
            RevenueEntry revenue) {
        return "Competência="
                + revenue.competence()
                + "; Atividade="
                + revenue.activityType()
                + "; Valor="
                + revenue.amount()
                + "; SujeitaAoFatorR="
                + revenue.subjectToFatorR()
                + "; Tratamentos="
                + revenue.treatments();
    }

    private void validateAssessmentPeriod(
            YearMonth competence) {
        if (competence.isBefore(
                VALID_FROM)
                || competence.isAfter(
                        VALID_UNTIL)) {

            throw new IllegalArgumentException(
                    "Não existe regra de classificação "
                            + "validada por este motor para a competência "
                            + competence
                            + ". Vigência suportada: "
                            + VALID_FROM
                            + " até "
                            + VALID_UNTIL
                            + ".");
        }
    }

    private void validateFatorRCompatibility(
            RevenueEntry revenue) {
        if (revenue.subjectToFatorR()
                && revenue.activityType() != RevenueActivityType.SERVICE) {

            throw new IllegalArgumentException(
                    "Somente receitas classificadas como serviço "
                            + "podem ser marcadas como sujeitas ao Fator R.");
        }
    }
}