package br.com.auditortributario.taxrule.simples;

public enum TaxBracketRevenueBasisType {

    RBT12_PROPORTIONALIZED(
            "RBT12p",
            "Receita Bruta Acumulada Proporcionalizada"),

    RBT12(
            "RBT12",
            "Receita Bruta Acumulada nos 12 meses anteriores");

    private final String code;
    private final String displayName;

    TaxBracketRevenueBasisType(
            String code,
            String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}