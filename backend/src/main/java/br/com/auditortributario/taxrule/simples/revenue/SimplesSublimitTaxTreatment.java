package br.com.auditortributario.taxrule.simples.revenue;

public enum SimplesSublimitTaxTreatment {

    IN_DAS_STANDARD(
            false),

    IN_DAS_TRANSITIONAL(
            false),

    OUTSIDE_DAS(
            true);

    private final boolean externalObligation;

    SimplesSublimitTaxTreatment(
            boolean externalObligation) {

        this.externalObligation = externalObligation;
    }

    public boolean hasExternalObligation() {
        return externalObligation;
    }
}