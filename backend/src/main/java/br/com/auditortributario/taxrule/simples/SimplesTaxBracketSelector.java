package br.com.auditortributario.taxrule.simples;

import br.com.auditortributario.taxrule.domain.TaxDecision;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class SimplesTaxBracketSelector {

    private static final String RULE_CODE = "SIMPLES_TAX_BRACKET_SELECTION";

    private final SimplesTaxTableRegistry tableRegistry;

    public SimplesTaxBracketSelector() {
        this(
                new SimplesTaxTableRegistry());
    }

    SimplesTaxBracketSelector(
            SimplesTaxTableRegistry tableRegistry) {
        this.tableRegistry = Objects.requireNonNull(
                tableRegistry,
                "O registro de tabelas tributárias não pode ser nulo.");
    }

    public SimplesTaxBracketSelectionResult select(
            SimplesTaxBracketSelectionRequest request) {
        Objects.requireNonNull(
                request,
                "A requisição de seleção da faixa não pode ser nula.");

        SimplesAnnex annex = request.fatorRResult()
                .annex();

        BigDecimal revenueBasis = request.revenueBasisResult()
                .revenueBasis();

        SimplesTaxTable taxTable = tableRegistry.find(
                annex,
                request.assessmentPeriod());

        SimplesTaxBracket bracket = taxTable.findBracket(
                revenueBasis);

        TaxDecision decision = createDecision(
                request,
                taxTable,
                bracket);

        return new SimplesTaxBracketSelectionResult(
                request.assessmentPeriod(),
                request.fatorRResult(),
                request.revenueBasisResult(),
                taxTable,
                bracket,
                decision);
    }

    private TaxDecision createDecision(
            SimplesTaxBracketSelectionRequest request,
            SimplesTaxTable taxTable,
            SimplesTaxBracket bracket) {
        FatorRCalculationResult fatorRResult = request.fatorRResult();

        TaxBracketRevenueBasisResult revenueBasisResult = request.revenueBasisResult();

        String description = "Seleção da faixa tributária do "
                + fatorRResult.annex().getDisplayName()
                + " utilizando "
                + revenueBasisResult
                        .basisType()
                        .getCode()
                + ".";

        String input = "Competência = "
                + request.assessmentPeriod()
                + "; Fator R = "
                + fatorRResult
                        .fatorR()
                        .value()
                        .toPlainString()
                + "; anexo = "
                + fatorRResult
                        .annex()
                        .getDisplayName()
                + "; "
                + revenueBasisResult
                        .basisType()
                        .getCode()
                + " = "
                + revenueBasisResult
                        .revenueBasis()
                        .toPlainString()
                + "; tabela = "
                + taxTable.version()
                + ".";

        String condition = createBracketCondition(
                taxTable,
                bracket,
                revenueBasisResult.revenueBasis());

        String result = "Faixa = "
                + bracket.number()
                + "; alíquota nominal = "
                + bracket
                        .nominalRateAsPercentage()
                        .toPlainString()
                + "%; parcela a deduzir = "
                + bracket
                        .deduction()
                        .toPlainString()
                + ".";

        return new TaxDecision(
                RULE_CODE,
                taxTable.version(),
                description,
                input,
                condition,
                result,
                taxTable.legalReference());
    }

    private String createBracketCondition(
            SimplesTaxTable taxTable,
            SimplesTaxBracket bracket,
            BigDecimal revenueBasis) {
        if (bracket.number() == 1) {
            return "Receita para enquadramento = "
                    + revenueBasis.toPlainString()
                    + "; valor menor ou igual a "
                    + bracket
                            .maximumRevenue()
                            .toPlainString()
                    + ".";
        }

        List<SimplesTaxBracket> brackets = taxTable.brackets();

        SimplesTaxBracket previousBracket = brackets.get(
                bracket.number() - 2);

        return "Receita para enquadramento = "
                + revenueBasis.toPlainString()
                + "; valor maior que "
                + previousBracket
                        .maximumRevenue()
                        .toPlainString()
                + " e menor ou igual a "
                + bracket
                        .maximumRevenue()
                        .toPlainString()
                + ".";
    }
}