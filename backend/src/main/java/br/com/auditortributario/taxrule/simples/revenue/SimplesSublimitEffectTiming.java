package br.com.auditortributario.taxrule.simples.revenue;

public enum SimplesSublimitEffectTiming {

    NO_IMPEDIMENT(
            "Sem impedimento"),

    NEXT_MONTH(
            "A partir do mês subsequente"),

    NEXT_CALENDAR_YEAR(
            "A partir do ano-calendário subsequente"),

    RETROACTIVE_TO_OPENING(
            "Retroativo ao início da atividade");

    private final String displayName;

    SimplesSublimitEffectTiming(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean hasImpediment() {
        return this != NO_IMPEDIMENT;
    }

    public boolean isRetroactive() {
        return this == RETROACTIVE_TO_OPENING;
    }
}