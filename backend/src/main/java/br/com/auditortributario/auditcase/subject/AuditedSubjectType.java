package br.com.auditortributario.auditcase.subject;

public enum AuditedSubjectType {

    INDIVIDUAL(
            "Pessoa física"),

    BUSINESS(
            "Empresa ou entidade"),

    OTHER(
            "Outro");

    private final String displayName;

    AuditedSubjectType(
            String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}