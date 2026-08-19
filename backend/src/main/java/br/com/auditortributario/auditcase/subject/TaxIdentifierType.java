package br.com.auditortributario.auditcase.subject;

public enum TaxIdentifierType {

    CPF(
            "CPF"),

    CNPJ(
            "CNPJ"),

    OTHER(
            "Outro identificador");

    private final String displayName;

    TaxIdentifierType(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}