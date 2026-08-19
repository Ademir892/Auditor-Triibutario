package br.com.auditortributario.auditcase;

public enum AuditCaseType {

    MONTHLY(
            "Auditoria mensal"),

    ANNUAL(
            "Auditoria anual"),

    SECOND_OPINION(
            "Segunda opinião"),

    EXPLANATORY_REVIEW(
            "Revisão explicativa");

    private final String displayName;

    AuditCaseType(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}