package br.com.auditortributario.auditcase;

public enum TaxRegime {

    SIMPLES_NACIONAL(
            "Simples Nacional"),

    LUCRO_PRESUMIDO(
            "Lucro Presumido"),

    LUCRO_REAL(
            "Lucro Real"),

    MEI(
            "Microempreendedor Individual"),

    OTHER(
            "Outro");

    private final String displayName;

    TaxRegime(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}