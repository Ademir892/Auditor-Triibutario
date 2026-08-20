package br.com.auditortributario.taxrule.simples.revenue;

public enum SimplesRevenueTaxRoute {

    ANNEX_I(
            1,
            "Anexo I"),

    ANNEX_II(
            2,
            "Anexo II"),

    ANNEX_III(
            3,
            "Anexo III"),

    ANNEX_IV(
            4,
            "Anexo IV"),

    ANNEX_V(
            5,
            "Anexo V");

    private final int annexNumber;

    private final String displayName;

    SimplesRevenueTaxRoute(
            int annexNumber,
            String displayName) {
        this.annexNumber = annexNumber;

        this.displayName = displayName;
    }

    public int getAnnexNumber() {
        return annexNumber;
    }

    public String getDisplayName() {
        return displayName;
    }
}