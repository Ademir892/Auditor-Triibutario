package br.com.auditortributario.taxrule.simples.revenue;

public enum SimplesIssSublimitCollectionStatus {

    IN_DAS_STANDARD(
            "ISS recolhido normalmente no DAS"),

    IN_DAS_TRANSITIONAL(
            "ISS ainda recolhido no DAS com regra transitória de sublimite"),

    OUTSIDE_DAS(
            "ISS recolhido fora do DAS");

    private final String displayName;

    SimplesIssSublimitCollectionStatus(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isInsideDas() {
        return this != OUTSIDE_DAS;
    }

    public boolean requiresTransitionalCalculation() {
        return this == IN_DAS_TRANSITIONAL;
    }

    public boolean isOutsideDas() {
        return this == OUTSIDE_DAS;
    }
}