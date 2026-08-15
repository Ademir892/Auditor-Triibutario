package br.com.auditortributario.taxrule.simples;

public enum SimplesAnnex {

    ANEXO_III("Anexo III"),
    ANEXO_V("Anexo V");

    private final String displayName;

    SimplesAnnex(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
