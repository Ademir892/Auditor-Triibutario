package br.com.auditortributario.taxrule.domain;

public enum TaxComponent {

    IRPJ(
            "IRPJ",
            "Imposto sobre a Renda da Pessoa Jurídica",
            TaxJurisdiction.FEDERAL
    ),

    CSLL(
            "CSLL",
            "Contribuição Social sobre o Lucro Líquido",
            TaxJurisdiction.FEDERAL
    ),

    COFINS(
            "COFINS",
            "Contribuição para o Financiamento da Seguridade Social",
            TaxJurisdiction.FEDERAL
    ),

    PIS_PASEP(
            "PIS/Pasep",
            "Contribuição para o PIS/Pasep",
            TaxJurisdiction.FEDERAL
    ),

    CPP(
            "CPP",
            "Contribuição Patronal Previdenciária",
            TaxJurisdiction.FEDERAL
    ),

    ISS(
            "ISS",
            "Imposto sobre Serviços",
            TaxJurisdiction.MUNICIPAL
    ),

    ICMS(
            "ICMS",
            "Imposto sobre Circulação de Mercadorias e Serviços",
            TaxJurisdiction.STATE
    );

    private final String displayName;

    private final String description;

    private final TaxJurisdiction jurisdiction;

    TaxComponent(
            String displayName,
            String description,
            TaxJurisdiction jurisdiction
    ) {
        this.displayName =
                displayName;

        this.description =
                description;

        this.jurisdiction =
                jurisdiction;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public TaxJurisdiction getJurisdiction() {
        return jurisdiction;
    }
}