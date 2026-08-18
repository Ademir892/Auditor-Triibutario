package br.com.auditortributario.taxrule.simples;

public enum GuideStructureAuditSeverity {

    NONE(
            0,
            "Sem divergência"),

    LOW(
            1,
            "Baixa"),

    MEDIUM(
            2,
            "Média"),

    HIGH(
            3,
            "Alta");

    private final int weight;
    private final String displayName;

    GuideStructureAuditSeverity(
            int weight,
            String displayName) {
        this.weight = weight;
        this.displayName = displayName;
    }

    public int getWeight() {
        return weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static GuideStructureAuditSeverity highest(
            GuideStructureAuditSeverity first,
            GuideStructureAuditSeverity second) {
        if (first.weight >= second.weight) {
            return first;
        }

        return second;
    }
}