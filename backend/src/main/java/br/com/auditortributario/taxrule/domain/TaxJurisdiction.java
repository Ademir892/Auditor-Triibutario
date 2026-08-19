package br.com.auditortributario.taxrule.domain;

public enum TaxJurisdiction {

    FEDERAL(
            "Federal"),

    STATE(
            "Estadual"),

    MUNICIPAL(
            "Municipal");

    private final String displayName;

    TaxJurisdiction(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}