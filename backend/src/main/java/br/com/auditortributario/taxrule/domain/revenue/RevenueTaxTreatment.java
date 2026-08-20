package br.com.auditortributario.taxrule.domain.revenue;

public enum RevenueTaxTreatment {

    MONOPHASIC(
            "Tributação monofásica"),

    TAX_SUBSTITUTION(
            "Substituição tributária"),

    EXPORT(
            "Receita de exportação"),

    EXEMPTION(
            "Isenção"),

    REDUCTION(
            "Redução tributária"),

    ISS_WITHHELD(
            "ISS retido");

    private final String displayName;

    RevenueTaxTreatment(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}