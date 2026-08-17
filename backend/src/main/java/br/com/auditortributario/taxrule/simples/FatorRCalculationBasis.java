package br.com.auditortributario.taxrule.simples;

public enum FatorRCalculationBasis {

    STANDARD_12_MONTHS(
            "Período normal",
            "FS12",
            "RBT12",
            "Folha dos 12 meses anteriores ao período de apuração",
            "Receita bruta dos 12 meses anteriores ao período de apuração"),

    OPENING_MONTH(
            "Mês de abertura",
            "FSPA",
            "RPA",
            "Folha de salários do período de apuração",
            "Receita bruta do período de apuração"),

    UNDER_13_MONTHS(
            "Empresa com menos de 13 meses de atividade",
            "FS acumulada",
            "Receita acumulada",
            "Folha acumulada desde o mês de abertura até o mês anterior ao período de apuração",
            "Receita acumulada desde o mês de abertura até o mês anterior ao período de apuração");

    private final String displayName;
    private final String payrollCode;
    private final String revenueCode;
    private final String payrollDescription;
    private final String revenueDescription;

    FatorRCalculationBasis(
            String displayName,
            String payrollCode,
            String revenueCode,
            String payrollDescription,
            String revenueDescription) {
        this.displayName = displayName;
        this.payrollCode = payrollCode;
        this.revenueCode = revenueCode;
        this.payrollDescription = payrollDescription;
        this.revenueDescription = revenueDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPayrollCode() {
        return payrollCode;
    }

    public String getRevenueCode() {
        return revenueCode;
    }

    public String getPayrollDescription() {
        return payrollDescription;
    }

    public String getRevenueDescription() {
        return revenueDescription;
    }
}