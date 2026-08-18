package br.com.auditortributario.taxrule.simples;

public enum ConsolidatedAuditCause {

    NONE(
            "Nenhuma divergência principal identificada"),

    FACTOR_R_OR_ANNEX(
            "Possível divergência no Fator R ou no enquadramento tributário"),

    REVENUE_BASIS_OR_BRACKET(
            "Possível divergência na receita de enquadramento ou na faixa tributária"),

    EFFECTIVE_RATE(
            "Possível divergência no cálculo da alíquota efetiva"),

    AMOUNT_ONLY(
            "Diferença de valor sem divergência estrutural identificada"),

    DEFERRED_AMOUNT(
            "O valor pode depender de tributos diferidos de competências anteriores"),

    INSUFFICIENT_DATA(
            "Dados insuficientes para determinar a principal origem da diferença");

    private final String displayName;

    ConsolidatedAuditCause(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}