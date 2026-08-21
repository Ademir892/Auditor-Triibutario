package br.com.auditortributario.taxrule.simples.revenue;

public enum SimplesSublimitStatus {

    WITHIN_SUBLIMIT(
            "Dentro do sublimite"),

    EXCEEDED_UP_TO_TWENTY_PERCENT(
            "Sublimite excedido em até 20%"),

    EXCEEDED_OVER_TWENTY_PERCENT(
            "Sublimite excedido em mais de 20%");

    private final String displayName;

    SimplesSublimitStatus(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isExceeded() {
        return this != WITHIN_SUBLIMIT;
    }

    public boolean isExceededOverTwentyPercent() {
        return this == EXCEEDED_OVER_TWENTY_PERCENT;
    }
}